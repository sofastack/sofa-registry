/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.push;

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.PUSH_REG_COMMIT_COUNTER;
import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.PUSH_REG_SKIP_COUNTER;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class RegProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegProcessor.class);
  final BufferWorker[] workers;
  final RegHandler regHandler;
  private PushEfficiencyImproveConfig pushEfficiencyImproveConfig;

  RegProcessor(int workerSize, RegHandler regHandler) {
    this.regHandler = regHandler;
    this.workers = new BufferWorker[workerSize];
    for (int i = 0; i < workers.length; i++) {
      BufferWorker worker = new BufferWorker();
      this.workers[i] = worker;
      ConcurrentUtils.createDaemonThread("RegProcessor-" + i, worker).start();
    }
  }

  public void setWorkDelayTime(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
    this.pushEfficiencyImproveConfig = pushEfficiencyImproveConfig;
    for (BufferWorker worker : this.workers) {
      worker.setRegWorkWaitingMillis(pushEfficiencyImproveConfig.getRegWorkWaitingMillis());
    }
  }

  interface RegHandler {
    boolean onReg(String dataInfoId, List<Subscriber> subscribers);
  }

  boolean fireOnReg(Subscriber subscriber) {
    final String dataInfoId = subscriber.getDataInfoId();
    BufferWorker worker = indexOf(subscriber.getDataInfoId());
    SubBuffer buffer = worker.subMap.computeIfAbsent(dataInfoId, k -> new SubBuffer());
    boolean result = buffer.add(subscriber);
    if (null != pushEfficiencyImproveConfig
        && pushEfficiencyImproveConfig.fetchRegWorkWake(subscriber.getAppName())) {
      worker.wakeup();
    }
    return result;
  }

  private BufferWorker indexOf(String dataInfoId) {
    int n = (dataInfoId.hashCode() & 0x7fffffff) % workers.length;
    return workers[n];
  }

  private static final class SubBuffer {
    volatile Map<String, Subscriber> subs = new ConcurrentHashMap<>(256);

    boolean add(Subscriber sub) {
      // the sub maybe replaced when adding
      final Map<String, Subscriber> currentSubs = subs;
      final String registerId = sub.getRegisterId();
      // quick path
      if (currentSubs.putIfAbsent(registerId, sub) == null) {
        PUSH_REG_COMMIT_COUNTER.inc();
        return true;
      }
      for (; ; ) {
        final Subscriber existing = currentSubs.get(registerId);
        if (existing == null) {
          if (currentSubs.putIfAbsent(registerId, sub) == null) {
            PUSH_REG_COMMIT_COUNTER.inc();
            return true;
          }
        } else {
          if (!existing.registerVersion().orderThan(sub.registerVersion())) {
            PUSH_REG_SKIP_COUNTER.inc();
            LOGGER.info(
                "[SkipReg]{}, existing={}, add={}",
                sub.getDataInfoId(),
                existing.registerVersion(),
                sub.shortDesc());
            return false;
          }
          if (currentSubs.replace(registerId, existing, sub)) {
            PUSH_REG_COMMIT_COUNTER.inc();
            return true;
          }
        }
      }
    }

    Map<String, Subscriber> getAndReset() {
      // For performance, just replace the subs
      // In a concurrent scenario, small probability there are still write operations on the
      // replaced subs
      if (subs.isEmpty()) {
        return Collections.emptyMap();
      }
      Map<String, Subscriber> ret = subs;
      this.subs = new ConcurrentHashMap<>(256);
      return ret;
    }
  }

  int processBuffer(Ref ref, int hitSize) {
    List<Subscriber> subscribers = Lists.newArrayListWithCapacity(hitSize);
    for (Map.Entry<String, Subscriber> e : ref.subscriberMap.entrySet()) {
      final Subscriber sub = e.getValue();
      if (!sub.hasPushed()) {
        subscribers.add(sub);
      }
      // try to remove the sub, but subs maybe changes
      ref.subscriberMap.remove(sub.getRegisterId(), sub);
    }
    if (!subscribers.isEmpty()) {
      regHandler.onReg(ref.dataInfoId, subscribers);
    }
    return subscribers.size();
  }

  static final class Ref {
    final String dataInfoId;
    // ref of BufferWorker.subMap, do not copy it
    final Map<String, Subscriber> subscriberMap;

    Ref(String dataInfoId, Map<String, Subscriber> subscriberMap) {
      this.dataInfoId = dataInfoId;
      this.subscriberMap = subscriberMap;
    }

    int size() {
      return subscriberMap.size();
    }
  }

  final class BufferWorker extends WakeUpLoopRunnable {
    final Map<String, SubBuffer> subMap = new ConcurrentHashMap<>(1024 * 8);

    private int regWorkWaitingMillis = 200;

    public void setRegWorkWaitingMillis(int regWorkWaitingMillis) {
      this.regWorkWaitingMillis = regWorkWaitingMillis;
    }

    @Override
    public void runUnthrowable() {
      watchBuffer();
    }

    @Override
    public int getWaitingMillis() {
      return regWorkWaitingMillis;
    }

    List<Ref> getAndResetBuffer() {
      List<Ref> ret = Lists.newArrayListWithCapacity(1024);
      for (Map.Entry<String, SubBuffer> e : subMap.entrySet()) {
        Map<String, Subscriber> refSub = e.getValue().getAndReset();
        if (!refSub.isEmpty()) {
          Ref ref = new Ref(e.getKey(), refSub);
          ret.add(ref);
        }
      }
      return ret;
    }

    int watchBuffer() {
      final List<Ref> refs = getAndResetBuffer();
      if (refs.isEmpty()) {
        return 0;
      }
      int firstCount = 0;
      int secondCount = 0;
      int subSize = 0;
      for (Ref ref : refs) {
        final int size = ref.size();
        if (size != 0) {
          subSize += size;
          firstCount += processBuffer(ref, size);
        }
      }
      // double process, maybe the sub.ref has update after first process
      ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
      for (Ref ref : refs) {
        final int size = ref.size();
        if (size != 0) {
          subSize += size;
          secondCount += processBuffer(ref, size);
        }
      }
      LOGGER.info(
          "[regBuffer]dataIds={},subs={},regs={},{},{}",
          refs.size(),
          subSize,
          firstCount + secondCount,
          firstCount,
          secondCount);
      return firstCount + secondCount;
    }
  }

  @VisibleForTesting
  void suspend() {
    for (BufferWorker w : workers) {
      w.suspend();
    }
  }
}
