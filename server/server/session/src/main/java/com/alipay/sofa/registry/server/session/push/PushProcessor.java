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
import com.alipay.sofa.registry.common.model.DataCenterPushInfo;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.PushData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelOverflowException;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.task.RejectedDiscardHandler;
import com.alipay.sofa.registry.trace.TraceID;
import com.alipay.sofa.registry.util.*;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public class PushProcessor {
  private static final Logger LOGGER = PushLog.LOGGER;

  private KeyedThreadPoolExecutor pushExecutor;
  PushTaskBuffer taskBuffer;

  final Map<PushTask.PushingTaskKey, PushRecord> pushingRecords =
      new ConcurrentHashMap<>(1024 * 16);

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected PushDataGenerator pushDataGenerator;

  @Autowired protected ClientNodeService clientNodeService;

  @Autowired protected CircuitBreakerService circuitBreakerService;;

  private int pushDataTaskDebouncingMillis = 500;
  private PushEfficiencyImproveConfig pushEfficiencyImproveConfig;

  final Cleaner cleaner = new Cleaner();

  final RejectedDiscardHandler discardHandler = new RejectedDiscardHandler();
  private final ThreadPoolExecutor pushCallbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "PushCallback", OsUtils.getCpuCount() * 3, 40000, discardHandler);

  @PostConstruct
  public void init() {
    pushExecutor =
        new KeyedThreadPoolExecutor(
            "PushExecutor",
            sessionServerConfig.getPushTaskExecutorPoolSize(),
            sessionServerConfig.getPushTaskExecutorQueueSize());
    intTaskBuffer();
    ConcurrentUtils.createDaemonThread("PushCleaner", cleaner).start();
  }

  public void setPushTaskDelayTime(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
    this.taskBuffer.setPushTaskWorkWaitingMillis(
        pushEfficiencyImproveConfig.getPushTaskWaitingMillis());
    this.pushDataTaskDebouncingMillis = pushEfficiencyImproveConfig.getPushTaskDebouncingMillis();
    this.pushEfficiencyImproveConfig = pushEfficiencyImproveConfig;
  }

  void intTaskBuffer() {
    if (this.taskBuffer == null) {
      this.taskBuffer = new PushTaskBuffer(sessionServerConfig.getPushTaskBufferBucketSize());
    }
  }

  protected List<PushTask> createPushTask(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      MultiSubDatum datum) {
    PushTask pushTask = new PushTaskImpl(pushCause, addr, subscriberMap, datum);
    // set expireTimestamp, wait to merge to debouncing
    if (null != pushEfficiencyImproveConfig) {
      pushTask.expireAfter(
          pushEfficiencyImproveConfig.fetchSbfAppPushTaskDebouncingMillis(
              pushTask.subscriber.getAppName()));
    } else {
      pushTask.expireAfter(pushDataTaskDebouncingMillis);
    }
    return Collections.singletonList(pushTask);
  }

  void firePush(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      MultiSubDatum datum) {
    if (!pushSwitchService.canIpPushMulti(
        addr.getAddress().getHostAddress(), datum.dataCenters())) {
      return;
    }
    // most of the time, element size is 1, SingleMap to save the memory
    subscriberMap = CollectionUtils.toSingletonMap(subscriberMap);
    List<PushTask> fires = createPushTask(pushCause, addr, subscriberMap, datum);
    for (PushTask task : fires) {
      taskBuffer.bufferWakeUp(task);
    }
  }

  final class Cleaner extends LoopRunnable {
    @Override
    public void runUnthrowable() {
      int cleans = cleanPushingTaskRunTooLong();
      LOGGER.info(
          "cleans={}, callbackDiscardCounter={}, buffer={}, pushing={}",
          cleans,
          discardHandler.getDiscardCountThenReset(),
          taskBuffer.size(),
          pushingRecords.size());
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
    }
  }

  private int getPushingMaxSpanMillis() {
    return sessionServerConfig.getClientNodeExchangeTimeoutMillis() * 3;
  }

  int cleanPushingTaskRunTooLong() {
    final long now = System.currentTimeMillis();
    final int maxSpanMillis = getPushingMaxSpanMillis();
    int count = 0;
    for (Map.Entry<PushTask.PushingTaskKey, PushRecord> e : pushingRecords.entrySet()) {
      final PushTask.PushingTaskKey key = e.getKey();
      final PushRecord record = e.getValue();
      long span = cleanPushingTaskIfRunTooLong(now, key, record, maxSpanMillis);
      if (span > 0) {
        count++;
        LOGGER.warn("[pushTooLong]{},span={},{}", record.taskID, span, key);
      }
    }
    return count;
  }

  long cleanPushingTaskIfRunTooLong(
      long now, PushTask.PushingTaskKey pushingTaskKey, PushRecord task, int maxSpanMillis) {
    final long span = now - task.trace.getPushStartTimestamp();
    if (span > maxSpanMillis) {
      // this happens when the callbackExecutor is too busy and the callback task is discarded
      // force to remove the prev task
      final boolean cleaned = pushingRecords.remove(pushingTaskKey, task);
      if (cleaned) {
        for (DataCenterPushInfo value : task.dataCenterPushInfos.values()) {
          value.setPushVersion(0L);
        }
        task.trace.finishPush(
            PushTrace.PushStatus.Busy, task.taskID, task.dataCenterPushInfos, task.retryCount);
      }
      return span;
    }
    return 0;
  }

  boolean interestOfDatum(PushTask task) {
    Map<String, Long> versions = task.datum.getVersion();
    if (task.subscriberMap.size() == 1) {
      return task.subscriber.checkVersion(versions);
    }
    for (Subscriber subscriber : task.subscriberMap.values()) {
      if (subscriber.checkVersion(versions)) {
        return true;
      }
    }
    return false;
  }

  boolean checkPushRunning(PushTask task) {
    final PushTask.PushingTaskKey pushingTaskKey = task.pushingTaskKey;
    // check the pushing task
    final PushRecord prev = pushingRecords.get(pushingTaskKey);
    if (prev == null) {
      return true;
    }
    final long now = System.currentTimeMillis();
    final int maxSpanMillis = getPushingMaxSpanMillis();
    final long span = cleanPushingTaskIfRunTooLong(now, pushingTaskKey, prev, maxSpanMillis);
    if (span > 0) {
      LOGGER.warn(
          "[pushTooLong]{},span={},{},now={}", prev.taskID, span, task.pushingTaskKey, task.taskID);
      return true;
    }
    // task after the prev, but prev.pushClient not callback, retry
    retry(task, RetryReason.Waiting);
    return false;
  }

  boolean causeContinue(PushTask task) {
    switch (task.trace.pushCause.pushType) {
      case Reg:
        return !task.hasPushed();
      case Empty:
        return task.subscriber.needPushEmpty(task.datum.dataCenters());
      default:
        return interestOfDatum(task);
    }
  }

  boolean checkSkipPushEmptyAndUpdateVersion(PushTask task) {
    int skipCount = 0;
    long now = System.currentTimeMillis();
    Collection<Subscriber> subs = task.subscriberMap.values();
    for (Subscriber subscriber : subs) {
      boolean canSkip =
          subscriber.checkSkipPushEmpty(task.datum.getVersion(), task.getDataCenterPushCount());
      if (canSkip
          && subscriber.getRegisterTimestamp()
              < now - sessionServerConfig.getSkipPushEmptySilentMillis()) {
        skipCount++;
      }
    }
    if (subs.size() > skipCount) {
      return false;
    }
    for (Subscriber subscriber : subs) {
      subscriber.checkAndUpdateCtx(task.datum.getVersion(), task.getDataCenterPushCount());
    }
    PUSH_EMPTY_SKIP_COUNTER.inc();
    LOGGER.info(
        "[pushEmptySkip]{},{},{}", task.taskID, task.pushingTaskKey, task.datum.getVersion());
    return true;
  }

  protected enum RetryReason {
    Waiting,
    Error,
    Overflow,
  }

  // some groupId not need to retry
  protected boolean needRetry(PushTask task, RetryReason reason) {
    return true;
  }

  private boolean retry(PushTask task, RetryReason reason) {
    if (!needRetry(task, reason)) {
      return false;
    }
    task.retryCount++;
    final int retry = task.retryCount;
    if (retry <= sessionServerConfig.getPushTaskRetryTimes() && causeContinue(task)) {
      final int backoffMillis = getRetryBackoffTime(retry);
      task.expireAfter(backoffMillis);
      PUSH_RETRY_COUNTER.labels(reason.name()).inc();
      final boolean buffed = taskBuffer.bufferWakeUp(task);
      LOGGER.info(
          "[retry]{},{},{},retry={},buffed={}",
          task.taskID,
          task.pushingTaskKey,
          task.datum.getVersion(),
          task.retryCount,
          buffed);
      return buffed;
    }
    return false;
  }

  // check push empty, some group maybe could not tolerate push empty
  protected boolean interruptOnPushEmpty(
      MultiSubDatum datum,
      PushData pushData,
      PushCause pushCause,
      Subscriber sub,
      InetSocketAddress addr) {
    return false;
  }

  boolean doPush(PushTask task) {
    if (!pushSwitchService.canIpPushMulti(
        task.pushingTaskKey.addr.getAddress().getHostAddress(), task.datum.dataCenters())) {
      return false;
    }

    try {
      task.trace.startPush();
      if (!checkPushRunning(task)) {
        return false;
      }

      if (!causeContinue(task)) {
        return false;
      }

      final PushData pushData = task.createPushData();
      task.setDataCenterPushInfos(pushData.getDataCenterPushInfo());

      if (interruptOnPushEmpty(
          task.datum, pushData, task.trace.pushCause, task.subscriber, task.pushingTaskKey.addr)) {
        return false;
      }

      // double check
      if (!causeContinue(task)) {
        return false;
      }
      // check push empty can skip (last push is also empty)
      if (checkSkipPushEmptyAndUpdateVersion(task)) {
        return false;
      }

      pushingRecords.put(
          task.pushingTaskKey,
          new PushRecord(
              task.trace, task.taskID, task.retryCount, pushData.getDataCenterPushInfo()));
      clientNodeService.pushWithCallback(
          pushData.getPayload(), task.subscriber.getSourceAddress(), new PushClientCallback(task));
      PUSH_CLIENT_ING_COUNTER.inc();
      LOGGER.info(
          "[pushing]{},{},{},{}",
          task.taskID,
          task.pushingTaskKey,
          task.datum.getVersion(),
          task.getPushDataCount());
      return true;
    } catch (Throwable e) {
      handleDoPushException(task, e);
    }
    return false;
  }

  void handleDoPushException(PushTask task, Throwable e) {
    // try to delete self
    pushingRecords.remove(task.pushingTaskKey);
    if (e instanceof RequestChannelClosedException) {
      task.trace.finishPush(
          PushTrace.PushStatus.ChanClosed,
          task.taskID,
          task.getDataCenterPushInfos(),
          task.retryCount);
      // channel closed, just warn
      LOGGER.warn(
          "[PushChanClosed]taskId={}, {}, {}", task.taskID, task.pushingTaskKey, e.getMessage());
      return;
    }

    // record push exception
    if (circuitBreakerRecordWhenDoPushError(
        task.datum, task.subscriber.getSourceAddress().getIpAddress())) {
      for (Subscriber subscriber : task.subscriberMap.values()) {
        if (!circuitBreakerService.onPushFail(task.datum.getVersion(), subscriber)) {
          LOGGER.info("[handleDoPushException]taskId={}, {}", task.taskID, task.pushingTaskKey);
        }
      }
    }
    if (e instanceof ChannelOverflowException) {
      retry(task, RetryReason.Overflow);
      task.trace.finishPush(
          PushTrace.PushStatus.ChanOverflow,
          task.taskID,
          task.getDataCenterPushInfos(),
          task.retryCount);
      LOGGER.error(
          "[PushChanOverflow]taskId={}, {}, {}", task.taskID, task.pushingTaskKey, e.getMessage());
      return;
    }
    task.trace.finishPush(
        PushTrace.PushStatus.Fail, task.taskID, task.getDataCenterPushInfos(), task.retryCount);
    LOGGER.error("[PushFail]taskId={}, {}", task.taskID, task.pushingTaskKey, e);
  }

  boolean circuitBreakerRecordWhenDoPushError(MultiSubDatum datum, String ip) {
    return false;
  }

  class PushTaskImpl extends PushTask implements Runnable {
    PushTaskImpl(
        PushCause pushCause,
        InetSocketAddress addr,
        Map<String, Subscriber> subscriberMap,
        MultiSubDatum datum) {
      super(pushCause, addr, subscriberMap, datum);
    }

    protected PushData createPushData() {
      return pushDataGenerator.createPushData(
          DatumUtils.decompressMultiSubDatum(datum), subscriberMap);
    }

    @Override
    protected boolean commit() {
      try {
        if (!pushSwitchService.canIpPushMulti(
            pushingTaskKey.addr.getAddress().getHostAddress(), datum.dataCenters())) {
          return false;
        }
        // keyed by client.addr && (pushingKey%concurrencyLevel)
        // avoid generating too many pushes for the same client at the same time
        final int level = sessionServerConfig.getClientNodePushConcurrencyLevel();
        pushExecutor.execute(
            new Tuple(pushingTaskKey.addr, pushingTaskKey.hashCode() % level), this);
        COMMIT_COUNTER.inc();
        return true;
      } catch (Throwable e) {
        LOGGER.error("failed to exec push task {},{}", taskID, pushingTaskKey, e);
        return false;
      }
    }

    @Override
    public void run() {
      doPush(this);
    }
  }

  final class PushClientCallback implements CallbackHandler {
    final PushTask pushTask;

    PushClientCallback(PushTask pushTask) {
      this.pushTask = pushTask;
    }

    @Override
    public void onCallback(Channel channel, Object message) {
      pushingRecords.remove(pushTask.pushingTaskKey);
      for (Subscriber subscriber : pushTask.subscriberMap.values()) {
        if (!circuitBreakerService.onPushSuccess(
            pushTask.datum.getVersion(), pushTask.getDataCenterPushCount(), subscriber)) {
          LOGGER.info(
              "PushY, but failed to updateVersion, {}, {}",
              pushTask.taskID,
              pushTask.pushingTaskKey);
        }
      }
      this.pushTask.trace.finishPush(
          PushTrace.PushStatus.OK,
          pushTask.taskID,
          pushTask.getDataCenterPushInfos(),
          pushTask.retryCount);
    }

    @Override
    public void onException(Channel channel, Throwable exception) {
      pushingRecords.remove(pushTask.pushingTaskKey);

      boolean needRecord = true;
      final boolean channelConnected = channel.isConnected();
      if (channelConnected) {
        retry(pushTask, RetryReason.Error);
      }

      if (exception instanceof InvokeTimeoutException) {
        this.pushTask.trace.finishPush(
            PushTrace.PushStatus.Timeout,
            pushTask.taskID,
            pushTask.getDataCenterPushInfos(),
            pushTask.retryCount);
        LOGGER.error("[PushTimeout]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
      } else {
        if (channelConnected) {
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.Fail,
              pushTask.taskID,
              pushTask.getDataCenterPushInfos(),
              pushTask.retryCount);
          LOGGER.error(
              "[PushFailed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey, exception);
        } else {
          needRecord = false;
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.ChanClosed,
              pushTask.taskID,
              pushTask.getDataCenterPushInfos(),
              pushTask.retryCount);
          // channel closed, just warn
          LOGGER.warn("[PushChanClosed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
        }
      }

      if (needRecord) {
        // record push fail
        for (Subscriber subscriber : pushTask.subscriberMap.values()) {
          if (!circuitBreakerService.onPushFail(pushTask.datum.getVersion(), subscriber)) {
            LOGGER.info(
                "PushN, failed to do onPushFail, {}, {}", pushTask.taskID, pushTask.pushingTaskKey);
          }
        }
      }
    }

    @Override
    public Executor getExecutor() {
      return pushCallbackExecutor;
    }
  }

  int getRetryBackoffTime(int retry) {
    return BackOffTimes.getBackOffMillis(
        retry,
        sessionServerConfig.getPushDataTaskRetryFirstDelayMillis(),
        sessionServerConfig.getPushDataTaskRetryIncrementDelayMillis());
  }

  private static final class PushRecord {
    final PushTrace trace;
    final TraceID taskID;
    final int retryCount;

    private Map<String, DataCenterPushInfo> dataCenterPushInfos;

    PushRecord(
        PushTrace pushTrace,
        TraceID taskID,
        int retryCount,
        Map<String, DataCenterPushInfo> dataCenterPushInfos) {
      this.trace = pushTrace;
      this.taskID = taskID;
      this.retryCount = retryCount;
      this.dataCenterPushInfos = dataCenterPushInfos;
    }
  }
}
