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
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import java.util.*;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

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

  /**
   * Apply the given push-efficiency configuration to all workers' change-task delay settings.
   *
   * Iterates the internal dataCenterWorkers map and calls each Worker's
   * setChangeTaskWorkDelay(...) with the provided configuration. If a data center
   * entry contains a null Worker[] the method returns immediately and stops applying
   * the configuration to any remaining entries.
   *
   * @param pushEfficiencyImproveConfig configuration containing delay values to apply to workers
   */
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

  /**
   * Returns a snapshot of per-data-center, per-worker change debouncing settings.
   *
   * Each map entry key is a data-center name and the value is an array of
   * ChangeDebouncingTime instances sized to the worker array for that center.
   * If a data center has no workers registered, an empty array is returned for that key.
   *
   * @return a map from data-center name to an array of ChangeDebouncingTime representing each worker's current debouncing configuration
   */
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

  /**
   * Update debouncing intervals for every Worker across all data centers.
   *
   * This sets each Worker's change debouncing milliseconds and maximum debouncing
   * milliseconds to the provided values. If any data center entry maps to a null
   * Worker array, the method returns immediately without updating remaining entries.
   *
   * @param changeDebouncingMillis       new base debounce duration in milliseconds
   * @param changeDebouncingMaxMillis    new maximum debounce duration in milliseconds
   */
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

  /**
   * Submits a change for debounced, per-key processing and routes it to the appropriate worker.
   *
   * The change is converted into a ChangeKey (using the change context's data centers and the provided
   * dataInfoId) and handed to a Worker which applies debouncing/merge/expiration logic.
   *
   * @param dataInfoId identifier of the data item that changed
   * @param handler    callback invoked by the worker when the change is processed
   * @param changeCtx  context for the change, including the set of data centers and version/trace info
   * @return true if the change was accepted (queued or merged) by the worker; false if rejected as
   *         outdated compared to an existing pending change
   */
  boolean fireChange(String dataInfoId, ChangeHandler handler, TriggerPushContext changeCtx) {
    ChangeKey key = new ChangeKey(changeCtx.dataCenters(), dataInfoId);
    Worker worker = workerOf(key);
    return worker.commitChange(key, handler, changeCtx);
  }

  interface ChangeHandler {
    boolean onChange(String dataInfoId, TriggerPushContext changeCtx);
  }

  static final class ChangeTask {
    final TriggerPushContext changeCtx;
    final ChangeKey key;
    final ChangeHandler changeHandler;
    final long expireTimestamp;
    long expireDeadlineTimestamp;

    ChangeTask(
        ChangeKey key,
        TriggerPushContext changeCtx,
        ChangeHandler changeHandler,
        long expireTimestamp) {
      this.key = key;
      this.changeHandler = changeHandler;
      this.changeCtx = changeCtx;
      this.expireTimestamp = expireTimestamp;
    }

    void doChange() {
      changeHandler.onChange(key.dataInfoId, changeCtx);
    }

    @Override
    public String toString() {
      return StringFormatter.format(
          "ChangeTask{{},ver={},expire={},deadline={}}",
          key,
          changeCtx.getExpectDatumVersion(),
          expireTimestamp,
          expireDeadlineTimestamp);
    }
  }

  static final class Worker extends WakeUpLoopRunnable {
    // task sorted by expire probably
    final LinkedHashMap<ChangeKey, ChangeTask> tasks = Maps.newLinkedHashMap();

    /**
     * Update this worker's change debouncing and waiting intervals from the provided configuration.
     *
     * Sets the worker's changeDebouncingMillis, changeDebouncingMaxMillis, and changeTaskWaitingMillis
     * to the corresponding values in the PushEfficiencyImproveConfig.
     *
     * @param pushEfficiencyImproveConfig configuration containing changeDebouncingMillis,
     *        changeDebouncingMaxMillis, and changeTaskWaitingMillis
     */
    public void setChangeTaskWorkDelay(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
      this.changeDebouncingMillis = pushEfficiencyImproveConfig.getChangeDebouncingMillis();
      this.changeDebouncingMaxMillis = pushEfficiencyImproveConfig.getChangeDebouncingMaxMillis();
      this.changeTaskWaitingMillis = pushEfficiencyImproveConfig.getChangeTaskWaitingMillis();
    }

    /**
     * Update the worker's change debouncing intervals.
     *
     * Sets the current debounce delay used to determine how long a new change waits
     * before being eligible for execution and the maximum debounce deadline that
     * bounds how long a change may be postponed by repeated requeues.
     *
     * @param changeDebouncingMillis      base debounce delay in milliseconds
     * @param changeDebouncingMaxMillis   maximum debounce deadline in milliseconds
     */
    public void setChangeDebouncingMillis(
        int changeDebouncingMillis, int changeDebouncingMaxMillis) {
      this.changeDebouncingMillis = changeDebouncingMillis;
      this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
    }

    volatile int changeDebouncingMillis;
    volatile int changeDebouncingMaxMillis;
    int changeTaskWaitingMillis = 100;

    /**
     * Create a Worker with initial debouncing timing settings.
     *
     * @param changeDebouncingMillis initial debounce delay in milliseconds applied to newly committed change tasks
     * @param changeDebouncingMaxMillis maximum debounce window in milliseconds used as the task expire-deadline
     */
    Worker(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
      this.changeDebouncingMillis = changeDebouncingMillis;
      this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
    }

    ChangeTask get(ChangeKey key) {
      synchronized (tasks) {
        return tasks.get(key);
      }
    }

    boolean commitChange(ChangeKey key, ChangeHandler handler, TriggerPushContext changeCtx) {
      final long now = System.currentTimeMillis();
      final ChangeTask task = new ChangeTask(key, changeCtx, handler, now + changeDebouncingMillis);

      synchronized (tasks) {
        final ChangeTask exist = tasks.get(key);
        if (exist == null) {
          task.expireDeadlineTimestamp = now + changeDebouncingMaxMillis;
          tasks.put(key, task);
          return true;
        }

        if (task.changeCtx.smallerThan(exist.changeCtx)) {
          return false;
        }
        task.changeCtx.mergeVersion(exist.changeCtx);
        // compare with exist
        if (task.expireTimestamp <= exist.expireDeadlineTimestamp) {
          // not reach deadline, requeue to wait
          task.expireDeadlineTimestamp = exist.expireDeadlineTimestamp;
          // merge change, merge tracetimes
          task.changeCtx.addTraceTime(exist.changeCtx.getFirstTimes());
          // tasks is linkedMap, must remove the exist first, then enqueue in the tail
          tasks.remove(key);
          tasks.put(key, task);
        } else {
          // reach deadline, could not requeue, use exist.expire as newTask.expire
          exist.changeCtx.setExpectDatumVersion(task.changeCtx.getExpectDatumVersion());
        }

        return true;
      }
    }

    ChangeTask getExpire() {
      final long now = System.currentTimeMillis();
      synchronized (tasks) {
        if (tasks.isEmpty()) {
          return null;
        }
        Iterator<ChangeTask> it = tasks.values().iterator();
        final ChangeTask first = it.next();
        if (first.expireTimestamp <= now) {
          it.remove();
          return first;
        }
        return null;
      }
    }

    @Override
    public void runUnthrowable() {
      for (; ; ) {
        final ChangeTask task = getExpire();
        if (task == null) {
          break;
        }
        try {
          task.doChange();
        } catch (Throwable e) {
          LOGGER.error("failed to doChange, {}", task);
        }
      }
    }

    /**
     * Returns the worker's current waiting interval in milliseconds.
     *
     * This value is used by the worker's wake-up loop as the sleep/wait duration
     * when there are no expired tasks ready to process.
     *
     * @return waiting time in milliseconds
     */
    @Override
    public int getWaitingMillis() {
      return changeTaskWaitingMillis;
    }

    /**
     * Returns the current change debouncing settings for this worker.
     *
     * @return a snapshot ChangeDebouncingTime containing this worker's current
     *         changeDebouncingMillis and changeDebouncingMaxMillis
     */
    public ChangeDebouncingTime getChangeDebouncingTime() {
      return new ChangeDebouncingTime(this.changeDebouncingMillis, this.changeDebouncingMaxMillis);
    }
  }

  static final class ChangeKey {
    final String dataInfoId;
    final Set<String> dataCenters;

    ChangeKey(Set<String> dataCenters, String dataInfoId) {
      this.dataCenters = dataCenters;
      this.dataInfoId = dataInfoId;
    }

    @Override
    public String toString() {
      return dataInfoId + "@" + dataCenters;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ChangeKey changeKey = (ChangeKey) o;
      return Objects.equals(dataInfoId, changeKey.dataInfoId)
          && Objects.equals(dataCenters, changeKey.dataCenters);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dataInfoId, dataCenters);
    }
  }

  Worker workerOf(ChangeKey key) {
    String dataCenter = key.dataCenters.stream().findFirst().get();
    Worker[] workers = dataCenterWorkers.computeIfAbsent(dataCenter, k -> initWorkers());
    int n = (key.hashCode() & 0x7fffffff) % workers.length;
    return workers[n];
  }
}
