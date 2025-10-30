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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeProcessor.class);

  @Autowired SessionServerConfig sessionServerConfig;

  Map<String, Worker[]> dataCenterWorkers = Maps.newConcurrentMap();

  @PostConstruct
  public void init() {
    Worker[] workers = initWorkers();
    dataCenterWorkers.putIfAbsent(sessionServerConfig.getSessionServerDataCenter(), workers);
  }

  private Worker[] initWorkers() {
    Worker[] workers = new Worker[sessionServerConfig.getDataChangeFetchTaskWorkerSize()];
    for (int i = 0; i < workers.length; i++) {
      workers[i] =
          new Worker(
              sessionServerConfig.getDataChangeDebouncingMillis(),
              sessionServerConfig.getDataChangeMaxDebouncingMillis());
      ConcurrentUtils.createDaemonThread("ChangeExecutor-" + i, workers[i]).start();
    }
    return workers;
  }

  public void setWorkDelayTime(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
    for (Map.Entry<String, Worker[]> entry : dataCenterWorkers.entrySet()) {
      Worker[] workers = entry.getValue();
      if (workers == null) {
        return;
      }
      for (Worker work : workers) {
        work.setChangeTaskWorkDelay(pushEfficiencyImproveConfig);
      }
    }
  }

  public void setLargeChangeAdaptiveDelayConfig(LargeChangeAdaptiveDelayConfig largeChangeAdaptiveDelayConfig) {
    for (Map.Entry<String, Worker[]> entry : dataCenterWorkers.entrySet()) {
      Worker[] workers = entry.getValue();
      if (workers == null) {
        return;
      }
      for (Worker work : workers) {
        work.setLargeChangeAdaptiveDelayConfig(largeChangeAdaptiveDelayConfig);
      }
    }
  }

  public Map<String, ChangeDebouncingTime[]> getChangeDebouncingMillis() {
    Map<String, ChangeDebouncingTime[]> dcChangeDebouncingTimes =
        new HashMap<>(dataCenterWorkers.size());
    for (Map.Entry<String, Worker[]> entry : dataCenterWorkers.entrySet()) {
      String dataCenter = entry.getKey();
      Worker[] workers = entry.getValue();
      if (workers == null) {
        dcChangeDebouncingTimes.put(dataCenter, new ChangeDebouncingTime[0]);
        continue;
      }
      ChangeDebouncingTime[] changeDebouncingTimes = new ChangeDebouncingTime[workers.length];
      for (int index = 0; index < workers.length; index++) {
        Worker worker = workers[index];
        ChangeDebouncingTime changeDebouncingTime = worker.getChangeDebouncingTime();
        changeDebouncingTimes[index] = changeDebouncingTime;
      }
      dcChangeDebouncingTimes.put(dataCenter, changeDebouncingTimes);
    }
    return dcChangeDebouncingTimes;
  }

  public void setChangeDebouncingMillis(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
    for (Map.Entry<String, Worker[]> entry : dataCenterWorkers.entrySet()) {
      Worker[] workers = entry.getValue();
      if (workers == null) {
        return;
      }
      for (Worker work : workers) {
        work.setChangeDebouncingMillis(changeDebouncingMillis, changeDebouncingMaxMillis);
      }
    }
  }

  boolean fireChange(String dataInfoId, ChangeHandler handler, TriggerPushContext changeCtx) {
    ChangeKey key = new ChangeKey(changeCtx.dataCenters(), dataInfoId);
    Worker worker = workerOf(key);
    return worker.commitChange(key, handler, changeCtx);
  }

  static final class Worker extends WakeUpLoopRunnable {

    final DefaultChangeWorker defaultChangeWorker;
    final LargeChangeAdaptiveDelayWorker largeAdaptiveDelayChangeWorker;

    volatile boolean useLargeAdapterDelayChangeWorker;

    Worker(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
      int changeTaskWaitingMillis = 100;
      this.useLargeAdapterDelayChangeWorker = LargeChangeAdaptiveDelayConfig.DEFAULT_USE_LARGE_ADAPTER_DELAY_CHANGE_WORKER;
      this.defaultChangeWorker = new DefaultChangeWorker(changeDebouncingMillis, changeDebouncingMaxMillis, changeTaskWaitingMillis);
      this.largeAdaptiveDelayChangeWorker = new LargeChangeAdaptiveDelayWorker(
              changeDebouncingMillis, changeDebouncingMaxMillis, changeTaskWaitingMillis,
              LargeChangeAdaptiveDelayConfig.DEFAULT_BASE_DELAY, LargeChangeAdaptiveDelayConfig.DEFAULT_DELAY_PER_UNIT,
              LargeChangeAdaptiveDelayConfig.DEFAULT_PUBLISHER_THRESHOLD, LargeChangeAdaptiveDelayConfig.DEFAULT_MAX_PUBLISHER_COUNT
      );
    }

    public void setChangeTaskWorkDelay(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
      this.defaultChangeWorker.setChangeTaskWorkDelay(pushEfficiencyImproveConfig);
      this.largeAdaptiveDelayChangeWorker.setChangeTaskWorkDelay(pushEfficiencyImproveConfig);
    }

    public void setChangeDebouncingMillis(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
      this.defaultChangeWorker.setChangeDebouncingMillis(changeDebouncingMillis, changeDebouncingMaxMillis);
      this.largeAdaptiveDelayChangeWorker.setChangeDebouncingMillis(changeDebouncingMillis, changeDebouncingMaxMillis);
    }

    public void setLargeChangeAdaptiveDelayConfig(LargeChangeAdaptiveDelayConfig largeChangeAdaptiveDelayConfig) {
      this.largeAdaptiveDelayChangeWorker.reset(largeChangeAdaptiveDelayConfig);
      this.useLargeAdapterDelayChangeWorker = largeChangeAdaptiveDelayConfig.isUseLargeAdapterDelayChangeWorker();
      if (this.useLargeAdapterDelayChangeWorker) {
        this.defaultChangeWorker.clear();
      } else {
        this.largeAdaptiveDelayChangeWorker.clear();
      }
    }

    ChangeTaskImpl get(ChangeKey key) {
      if (this.useLargeAdapterDelayChangeWorker) {
        return this.largeAdaptiveDelayChangeWorker.findTask(key);
      } else {
        return this.defaultChangeWorker.findTask(key);
      }
    }

    boolean commitChange(ChangeKey key, ChangeHandler handler, TriggerPushContext changeCtx) {
      if (this.useLargeAdapterDelayChangeWorker) {
        return this.largeAdaptiveDelayChangeWorker.commitChange(key, handler, changeCtx);
      } else {
        return this.defaultChangeWorker.commitChange(key, handler, changeCtx);
      }
    }

    List<ChangeTaskImpl> getExpires() {
      if (this.useLargeAdapterDelayChangeWorker) {
        return this.largeAdaptiveDelayChangeWorker.getExpireTasks();
      } else {
        return this.defaultChangeWorker.getExpireTasks();
      }
    }

    @Override
    public void runUnthrowable() {
      for (; ; ) {
        List<ChangeTaskImpl> timeoutTasks = this.getExpires();
        if (null == timeoutTasks || timeoutTasks.isEmpty()) {
          break;
        }
        for (ChangeTaskImpl task : timeoutTasks) {
          try {
            task.doChange();
          } catch (Throwable e) {
            LOGGER.error("failed to doChange, {}", task);
          }
        }
      }
    }

    @Override
    public int getWaitingMillis() {
      if (this.useLargeAdapterDelayChangeWorker) {
        return this.largeAdaptiveDelayChangeWorker.getWaitingMillis();
      } else {
        return this.defaultChangeWorker.getWaitingMillis();
      }
    }

    public ChangeDebouncingTime getChangeDebouncingTime() {
      if (this.useLargeAdapterDelayChangeWorker) {
        return this.largeAdaptiveDelayChangeWorker.getChangeDebouncingTime();
      } else {
        return this.defaultChangeWorker.getChangeDebouncingTime();
      }
    }
  }

  Worker workerOf(ChangeKey key) {
    String dataCenter = key.dataCenters.stream().findFirst().get();
    Worker[] workers = dataCenterWorkers.computeIfAbsent(dataCenter, k -> initWorkers());
    int n = (key.hashCode() & 0x7fffffff) % workers.length;
    return workers[n];
  }
}
