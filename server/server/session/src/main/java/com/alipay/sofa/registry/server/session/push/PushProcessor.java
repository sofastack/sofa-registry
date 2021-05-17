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

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.*;

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.trace.TraceID;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public class PushProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(PushProcessor.class);

  private KeyedThreadPoolExecutor pushExecutor;
  final Map<PendingTaskKey, PushTask> pendingTasks = Maps.newConcurrentMap();
  private final Lock pendingLock = new ReentrantLock();

  final Map<PushingTaskKey, PushTask> pushingTasks = Maps.newConcurrentMap();

  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired PushDataGenerator pushDataGenerator;

  @Autowired ClientNodeService clientNodeService;

  final WatchDog watchDog = new WatchDog();

  private final ThreadPoolExecutor pushCallbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "PushCallback", OsUtils.getCpuCount() * 4, 20000, new DiscardRunHandler());

  @PostConstruct
  public void init() {
    pushExecutor =
        new KeyedThreadPoolExecutor(
            "PushExecutor",
            sessionServerConfig.getPushTaskExecutorPoolSize(),
            sessionServerConfig.getPushTaskExecutorQueueSize());
    ConcurrentUtils.createDaemonThread("PushWatchDog", watchDog).start();
  }

  private boolean firePush(PushTask pushTask) {
    PendingTaskKey key = pushTask.pendingKeyOf();
    if (pendingTasks.putIfAbsent(key, pushTask) == null) {
      // fast path
      if (pushTask.pushCause.pushType.noDelay) {
        watchDog.wakeup();
      }
      PENDING_NEW_COUNTER.inc();
      return true;
    }
    boolean skip = false;
    PushTask prev = null;
    pendingLock.lock();
    try {
      prev = pendingTasks.get(key);
      if (prev == null) {
        pendingTasks.put(key, pushTask);
        PENDING_NEW_COUNTER.inc();
      } else if (pushTask.afterThan(prev)) {
        // update the expireTimestamp as prev's, avoid the push block by the continues fire
        pushTask.expireTimestamp = prev.expireTimestamp;
        pendingTasks.put(key, pushTask);
        PENDING_REPLACE_COUNTER.inc();
      } else {
        skip = true;
      }
    } finally {
      pendingLock.unlock();
    }
    if (!skip) {
      if (pushTask.pushCause.pushType.noDelay) {
        watchDog.wakeup();
      }
      return true;
    } else {
      PENDING_SKIP_COUNTER.inc();
      LOGGER.info(
          "[SkipPending] key={}, prev={}, {}, ver={}, now={}, ver={}",
          key,
          prev.taskID,
          prev.pushingTaskKey,
          prev.datum.getVersion(),
          pushTask.taskID,
          pushTask.datum.getVersion());
      return false;
    }
  }

  protected List<PushTask> createPushTask(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      SubDatum datum) {
    PushTask pushTask = new PushTask(pushCause, addr, subscriberMap, datum);
    // set expireTimestamp, wait to merge to debouncing
    pushTask.expireAfter(sessionServerConfig.getPushDataTaskDebouncingMillis());
    return Collections.singletonList(pushTask);
  }

  void firePush(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      SubDatum datum) {
    List<PushTask> fires = createPushTask(pushCause, addr, subscriberMap, datum);
    for (PushTask task : fires) {
      boolean fire = firePush(task);
      LOGGER.info("fire push={}, {}", fire, task);
    }
  }

  private boolean commitTask(PushTask task) {
    try {
      // keyed by client.addr && (pushingKey%8)
      // avoid generating too many pushes for the same client at the same time
      pushExecutor.execute(
          new Tuple(task.pushingTaskKey.addr, task.pushingTaskKey.hashCode() % 8), task);
      COMMIT_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      LOGGER.error("failed to exec push task {},{}", task.taskID, task.pushingTaskKey, e);
      return false;
    }
  }

  final class WatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      watchCommit();
    }

    @Override
    public int getWaitingMillis() {
      return 100;
    }
  }

  List<PushTask> watchCommit() {
    if (sessionServerConfig.isStopPushSwitch()) {
      // stop push, clean the task
      pendingLock.lock();
      try {
        pendingTasks.clear();
      } finally {
        pendingLock.unlock();
      }
      return Collections.emptyList();
    }
    List<PushTask> pending = transferAndMerge();
    List<PushTask> committed = Lists.newArrayListWithCapacity(pending.size());
    LOGGER.info("process push tasks {}", pending.size());
    for (PushTask task : pending) {
      if (commitTask(task)) {
        committed.add(task);
      }
    }
    return committed;
  }

  private List<PushTask> transferAndMerge() {
    List<PushTask> pending = Lists.newArrayList();
    final long now = System.currentTimeMillis();
    pendingLock.lock();
    try {
      final Iterator<Map.Entry<PendingTaskKey, PushTask>> it = pendingTasks.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<PendingTaskKey, PushTask> e = it.next();
        PushTask task = e.getValue();
        // no delay or expire, push immediately
        if (task.pushCause.pushType.noDelay || task.expireTimestamp <= now) {
          pending.add(task);
          it.remove();
        }
      }
    } finally {
      pendingLock.unlock();
    }
    return pending;
  }

  boolean checkPushing(PushTask task) {
    final PushingTaskKey pushingTaskKey = task.pushingTaskKey;
    // check the pushing task
    final PushTask prev = pushingTasks.get(pushingTaskKey);
    if (prev == null) {
      return true;
    }
    final long now = System.currentTimeMillis();
    final long span = now - prev.pushTimestamp;
    if (span > sessionServerConfig.getClientNodeExchangeTimeoutMillis() * 3) {
      // this happens when the callbackExecutor is too busy and the callback task is discarded
      // force to remove the prev task
      final boolean cleaned = pushingTasks.remove(pushingTaskKey) != null;
      LOGGER.warn(
          "[prevPushTooLong] {}, clean={}, prev={}, now={}",
          pushingTaskKey,
          cleaned,
          prev.taskID,
          task.taskID);
      if (cleaned) {
        prev.trace.finishPush(PushTrace.PushStatus.Busy, now).print();
      }
      return true;
    }
    // task after the prev, but prev.pushClient not callback, retry
    retry(task, "waiting");
    return false;
  }

  private boolean retry(PushTask task, String reason) {
    task.retryCount++;
    final int retry = task.retryCount;
    if (retry <= sessionServerConfig.getPushTaskRetryTimes()) {
      final int backoffMillis = getRetryBackoffTime(retry);
      task.expireAfter(backoffMillis);
      if (firePush(task)) {
        LOGGER.info(
            "add retry for {}, {}, {}, retry={}, backoff={}",
            reason,
            task.taskID,
            task.pushingTaskKey,
            retry,
            backoffMillis);
        return true;
      }
    }
    LOGGER.info(
        "skip retry for {}, {}, {}, retry={}", reason, task.taskID, task.pushingTaskKey, retry);
    return false;
  }

  // check push empty, some group maybe could not tolerate push empty
  protected boolean interruptOnPushEmpty(
      SubDatum datum, PushCause pushCause, Subscriber sub, InetSocketAddress addr) {
    return false;
  }

  boolean doPush(PushTask task) {
    if (sessionServerConfig.isStopPushSwitch()) {
      return false;
    }

    try {
      if (!checkPushing(task)) {
        return false;
      }
      if (interruptOnPushEmpty(task.datum, task.pushCause, task.subscriber, task.addr)) {
        return false;
      }
      Object data = task.createPushData();
      task.updatePushTimestamp();
      task.trace.startPush(
          SubscriberUtils.getMaxPushedVersion(task.dataCenter, task.subscriberMap.values()),
          task.pushTimestamp);
      pushingTasks.put(task.pushingTaskKey, task);
      clientNodeService.pushWithCallback(
          data, task.subscriber.getSourceAddress(), new PushClientCallback(task));
      PUSH_CLIENT_PUSHING_COUNTER.inc();
      LOGGER.info("[pushing]{},{}", task.taskID, task.pushingTaskKey);
      return true;
    } catch (Throwable e) {
      handleDoPushException(task, e);
    }
    return false;
  }

  void handleDoPushException(PushTask task, Throwable e) {
    // try to delete self
    boolean cleaned = pushingTasks.remove(task.pushingTaskKey) != null;
    task.trace.finishPush(PushTrace.PushStatus.Fail, System.currentTimeMillis()).print();
    if (e instanceof RequestChannelClosedException) {
      LOGGER.error(
          "{}, failed to pushing {}, cleaned={}, {}",
          task.taskID,
          task.pushingTaskKey,
          cleaned,
          e.getMessage());
      return;
    }
    LOGGER.error(
        "{}, failed to pushing {}, cleaned={}", task.taskID, task.pushingTaskKey, cleaned, e);
  }

  class PushTask implements Runnable {
    final TraceID taskID;
    final long createTimestamp = System.currentTimeMillis();
    volatile long expireTimestamp;
    volatile long pushTimestamp;

    final PushCause pushCause;
    final String dataCenter;
    final SubDatum datum;
    final InetSocketAddress addr;
    final Map<String, Subscriber> subscriberMap;
    final Subscriber subscriber;
    int retryCount;

    final PushingTaskKey pushingTaskKey;
    final PushTrace trace;

    PushTask(
        PushCause pushCause,
        InetSocketAddress addr,
        Map<String, Subscriber> subscriberMap,
        SubDatum datum) {
      this.taskID = TraceID.newTraceID();
      this.pushCause = pushCause;
      this.dataCenter = datum.getDataCenter();
      this.datum = datum;
      this.addr = addr;
      this.subscriberMap = subscriberMap;
      this.subscriber = subscriberMap.values().iterator().next();
      this.trace =
          PushTrace.trace(
              datum,
              addr,
              subscriber.getAppName(),
              pushCause,
              subscriberMap.size(),
              SubscriberUtils.getMaxRegisterTimestamp(subscriberMap.values()));
      this.pushingTaskKey =
          new PushingTaskKey(
              subscriber.getDataInfoId(),
              addr,
              subscriber.getScope(),
              subscriber.getClientVersion());
    }

    protected Object createPushData() {
      return pushDataGenerator.createPushData(datum, subscriberMap);
    }

    void expireAfter(long intervalMs) {
      this.expireTimestamp = System.currentTimeMillis() + intervalMs;
    }

    boolean afterThan(PushTask t) {
      return datum.getVersion() > t.datum.getVersion();
    }

    void updatePushTimestamp() {
      this.pushTimestamp = System.currentTimeMillis();
    }

    @Override
    public void run() {
      doPush(this);
    }

    PendingTaskKey pendingKeyOf() {
      return new PendingTaskKey(
          dataCenter, addr, subscriber.getDataInfoId(), subscriberMap.keySet());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(512);
      sb.append("PushTask{")
          .append(subscriber.getDataInfoId())
          .append(",ID=")
          .append(taskID)
          .append(",createT=")
          .append(createTimestamp)
          .append(",expireT=")
          .append(expireTimestamp)
          .append(",DC=")
          .append(dataCenter)
          .append(",ver=")
          .append(datum.getVersion())
          .append(",addr=")
          .append(addr)
          .append(",scope=")
          .append(subscriber.getScope())
          .append(",subIds=")
          .append(subscriberMap.keySet())
          .append(",subCtx=")
          .append(subscriber.printPushContext())
          .append(",retry=")
          .append(retryCount);
      return sb.toString();
    }
  }

  final class PushClientCallback implements CallbackHandler {
    final PushTask pushTask;
    final PushingTaskKey pushingTaskKey;
    long finishedTimestamp;

    PushClientCallback(PushTask pushTask) {
      this.pushTask = pushTask;
      this.pushingTaskKey = pushTask.pushingTaskKey;
    }

    @Override
    public void onCallback(Channel channel, Object message) {
      this.finishedTimestamp = System.currentTimeMillis();
      PUSH_CLIENT_SUCCESS_COUNTER.inc();
      boolean cleaned = false;
      try {
        for (Subscriber subscriber : pushTask.subscriberMap.values()) {
          if (!subscriber.checkAndUpdateVersion(pushTask.dataCenter, pushTask.datum.getVersion())) {
            LOGGER.info(
                "PushY, but failed to updateVersion, {}, {}",
                pushTask.taskID,
                pushTask.pushingTaskKey);
          }
        }
      } catch (Throwable e) {
        LOGGER.error("error push.onCallback, {}, {}", pushTask.taskID, pushTask.pushingTaskKey, e);
      } finally {
        cleaned = pushingTasks.remove(pushingTaskKey, pushTask);
      }
      this.pushTask.trace.finishPush(PushTrace.PushStatus.OK, this.finishedTimestamp).print();
      LOGGER.info(
          "PushY, clean record={}, span={}/{}, {}, {}",
          cleaned,
          pushSpanMillis(),
          totalSpanMillis(),
          pushTask.taskID,
          pushTask.pushingTaskKey);
    }

    @Override
    public void onException(Channel channel, Throwable exception) {
      this.finishedTimestamp = System.currentTimeMillis();
      PUSH_CLIENT_FAIL_COUNTER.inc();
      boolean cleaned = false;
      try {
        cleaned = pushingTasks.remove(pushingTaskKey, pushTask);
        if (channel.isConnected()) {
          retry(pushTask, "callbackErr");
        } else {
          LOGGER.info("PushN, channel closed, {}, {}", pushTask.taskID, pushingTaskKey);
        }
      } catch (Throwable e) {
        LOGGER.error("error push.onException, {}, {}", pushTask.taskID, pushingTaskKey, e);
      }
      if (exception instanceof InvokeTimeoutException) {
        if (cleaned) {
          this.pushTask
              .trace
              .finishPush(PushTrace.PushStatus.Timeout, this.finishedTimestamp)
              .print();
        }
        LOGGER.error(
            "PushN, timeout, clean record={}, span={}/{}, {}, {}",
            cleaned,
            pushSpanMillis(),
            totalSpanMillis(),
            pushTask.taskID,
            pushingTaskKey);
      } else {
        if (cleaned) {
          this.pushTask.trace.finishPush(PushTrace.PushStatus.Fail, this.finishedTimestamp).print();
        }
        LOGGER.error(
            "PushN, clean record={}, span={}/{}, {}, {}",
            cleaned,
            pushSpanMillis(),
            totalSpanMillis(),
            pushTask.taskID,
            pushingTaskKey,
            exception);
      }
    }

    long pushSpanMillis() {
      return finishedTimestamp - pushTask.pushTimestamp;
    }

    long totalSpanMillis() {
      return finishedTimestamp - pushTask.createTimestamp;
    }

    @Override
    public Executor getExecutor() {
      return pushCallbackExecutor;
    }
  }

  static final class PendingTaskKey {
    final String dataCenter;
    final String dataInfoId;
    final InetSocketAddress addr;
    final Set<String> subscriberIds;

    PendingTaskKey(
        String dataCenter, InetSocketAddress addr, String dataInfoId, Set<String> subscriberIds) {
      this.dataCenter = dataCenter;
      this.dataInfoId = dataInfoId;
      this.addr = addr;
      this.subscriberIds = subscriberIds;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PendingTaskKey pendingTaskKey = (PendingTaskKey) o;
      return Objects.equals(addr, pendingTaskKey.addr)
          && Objects.equals(dataInfoId, pendingTaskKey.dataInfoId)
          && Objects.equals(dataCenter, pendingTaskKey.dataCenter)
          && Objects.equals(subscriberIds, pendingTaskKey.subscriberIds);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dataCenter, addr, dataInfoId, subscriberIds);
    }

    @Override
    public String toString() {
      return "PendingKey{"
          + dataInfoId
          + ','
          + dataCenter
          + ','
          + addr
          + ", subscriberIds="
          + subscriberIds
          + '}';
    }
  }

  static final class PushingTaskKey {
    final InetSocketAddress addr;
    final String dataInfoId;
    final ScopeEnum scopeEnum;
    final BaseInfo.ClientVersion clientVersion;

    PushingTaskKey(
        String dataInfoId,
        InetSocketAddress addr,
        ScopeEnum scopeEnum,
        BaseInfo.ClientVersion clientVersion) {
      this.dataInfoId = dataInfoId;
      this.addr = addr;
      this.scopeEnum = scopeEnum;
      this.clientVersion = clientVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PushingTaskKey that = (PushingTaskKey) o;
      return Objects.equals(addr, that.addr)
          && Objects.equals(dataInfoId, that.dataInfoId)
          && scopeEnum == that.scopeEnum
          && clientVersion == that.clientVersion;
    }

    @Override
    public int hashCode() {
      return Objects.hash(addr, dataInfoId, scopeEnum, clientVersion);
    }

    @Override
    public String toString() {
      return "PushingKey{" + dataInfoId + ',' + scopeEnum + ',' + addr + '}';
    }
  }

  static final class DiscardRunHandler implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      LOGGER.info("[pushCallbackBusy]");
    }
  }

  int getRetryBackoffTime(int retry) {
    final int initialSleepTime = sessionServerConfig.getPushDataTaskRetryFirstDelayMillis();
    if (retry == 0) {
      return initialSleepTime;
    }
    int increment = sessionServerConfig.getPushDataTaskRetryIncrementDelayMillis();
    int result = initialSleepTime + (increment * (retry - 1));
    return result >= 0L ? result : 0;
  }
}
