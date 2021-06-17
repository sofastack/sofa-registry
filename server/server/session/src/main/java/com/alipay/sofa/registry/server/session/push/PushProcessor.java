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
import com.alipay.sofa.registry.common.model.store.PushData;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelOverflowException;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.task.RejectedDiscardHandler;
import com.alipay.sofa.registry.util.CollectionUtils;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.collect.Lists;
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

  final Map<PushTask.PushingTaskKey, PushTask> pushingTasks = new ConcurrentHashMap<>(1024 * 16);

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected PushDataGenerator pushDataGenerator;

  @Autowired protected ClientNodeService clientNodeService;

  final Cleaner cleaner = new Cleaner();

  final RejectedDiscardHandler discardHandler = new RejectedDiscardHandler();
  private final ThreadPoolExecutor pushCallbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "PushCallback", OsUtils.getCpuCount() * 5, 40000, discardHandler);

  @PostConstruct
  public void init() {
    pushExecutor =
        new KeyedThreadPoolExecutor(
            "PushExecutor",
            sessionServerConfig.getPushTaskExecutorPoolSize(),
            sessionServerConfig.getPushTaskExecutorQueueSize());
    intTaskBuffer();
    ConcurrentUtils.createDaemonThread("PushCleaner", cleaner).start();
    this.taskBuffer.start();
  }

  void intTaskBuffer() {
    if (this.taskBuffer == null) {
      this.taskBuffer =
          new PushTaskBuffer(sessionServerConfig.getPushTaskBufferBucketSize(), pushSwitchService);
    }
  }

  protected List<PushTask> createPushTask(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      SubDatum datum) {
    PushTask pushTask = new PushTaskImpl(pushCause, addr, subscriberMap, datum);
    // set expireTimestamp, wait to merge to debouncing
    pushTask.expireAfter(sessionServerConfig.getPushDataTaskDebouncingMillis());
    return Collections.singletonList(pushTask);
  }

  void firePush(
      PushCause pushCause,
      InetSocketAddress addr,
      Map<String, Subscriber> subscriberMap,
      SubDatum datum) {
    // most of the time, element size is 1, SingleMap to save the memory
    subscriberMap = CollectionUtils.toSingletonMap(subscriberMap);
    List<PushTask> fires = createPushTask(pushCause, addr, subscriberMap, datum);
    for (PushTask task : fires) {
      taskBuffer.buffer(task);
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
          pushingTasks.size());
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
    List<PushTask> pushes = Lists.newArrayList(pushingTasks.values());
    final long now = System.currentTimeMillis();
    final int maxSpanMillis = getPushingMaxSpanMillis();
    int count = 0;
    for (PushTask push : pushes) {
      long span = cleanPushingTaskIfRunTooLong(now, push, maxSpanMillis);
      if (span > 0) {
        count++;
        LOGGER.warn("[pushTooLong]{},span={},{}", push.taskID, span, push.pushingTaskKey);
      }
    }
    return count;
  }

  long cleanPushingTaskIfRunTooLong(long now, PushTask task, int maxSpanMillis) {
    final long span = now - task.trace.getPushStartTimestamp();
    if (span > maxSpanMillis) {
      // this happens when the callbackExecutor is too busy and the callback task is discarded
      // force to remove the prev task
      final boolean cleaned = pushingTasks.remove(task.pushingTaskKey, task);
      if (cleaned) {
        task.trace.finishPush(
            PushTrace.PushStatus.Busy,
            task.taskID,
            task.getMaxPushedVersion(),
            task.getPushDataCount());
      }
      return span;
    }
    return 0;
  }

  boolean interestOfDatum(PushTask task) {
    if (task.subscriberMap.size() == 1) {
      return task.subscriber.checkVersion(task.datum.getDataCenter(), task.datum.getVersion());
    }
    for (Subscriber subscriber : task.subscriberMap.values()) {
      if (subscriber.checkVersion(task.datum.getDataCenter(), task.datum.getVersion())) {
        return true;
      }
    }
    return false;
  }

  boolean checkPushRunning(PushTask task) {
    final PushTask.PushingTaskKey pushingTaskKey = task.pushingTaskKey;
    // check the pushing task
    final PushTask prev = pushingTasks.get(pushingTaskKey);
    if (prev == null) {
      return true;
    }
    final long now = System.currentTimeMillis();
    final int maxSpanMillis = getPushingMaxSpanMillis();
    final long span = cleanPushingTaskIfRunTooLong(now, prev, maxSpanMillis);
    if (span > 0) {
      LOGGER.warn(
          "[pushTooLong]{},span={},{},now={}", prev.taskID, span, task.pushingTaskKey, task.taskID);
      return true;
    }
    // task after the prev, but prev.pushClient not callback, retry
    retry(task, "waiting");
    return false;
  }

  boolean causeContinue(PushTask task) {
    switch (task.trace.pushCause.pushType) {
      case Reg:
        return !task.subscriber.hasPushed();
      case Empty:
        return task.subscriber.needPushEmpty(task.datum.getDataCenter());
      default:
        return interestOfDatum(task);
    }
  }

  boolean checkSkipPushEmptyAndUpdateVersion(PushTask task) {
    int skipCount = 0;
    long now = System.currentTimeMillis();
    Collection<Subscriber> subs = task.subscriberMap.values();
    for (Subscriber subscriber : subs) {
      if (subscriber.getRegisterTimestamp() < now - sessionServerConfig.getSkipPushEmptySilentMs()
          && subscriber.checkSkipPushEmpty(
              task.datum.getDataCenter(), task.datum.getVersion(), task.getPushDataCount())) {
        skipCount++;
      }
    }
    if (subs.size() > skipCount) {
      return false;
    }
    for (Subscriber subscriber : subs) {
      subscriber.checkAndUpdateCtx(
          task.datum.getDataCenter(), task.datum.getVersion(), task.getPushDataCount());
    }
    PUSH_EMPTY_SKIP_COUNTER.inc();
    LOGGER.info(
        "[pushEmptySkip]{},{},{}", task.taskID, task.pushingTaskKey, task.datum.getVersion());
    return true;
  }

  private boolean retry(PushTask task, String reason) {
    task.retryCount++;
    final int retry = task.retryCount;
    if (retry <= sessionServerConfig.getPushTaskRetryTimes() && causeContinue(task)) {
      final int backoffMillis = getRetryBackoffTime(retry);
      task.expireAfter(backoffMillis);
      PUSH_RETRY_COUNTER.labels(reason).inc();
      return taskBuffer.buffer(task);
    }
    return false;
  }

  // check push empty, some group maybe could not tolerate push empty
  protected boolean interruptOnPushEmpty(
      SubDatum datum, PushCause pushCause, Subscriber sub, InetSocketAddress addr) {
    return false;
  }

  boolean doPush(PushTask task) {
    if (!pushSwitchService.canIpPush(task.addr.getAddress().getHostAddress())) {
      return false;
    }

    try {
      if (!checkPushRunning(task)) {
        return false;
      }
      if (interruptOnPushEmpty(task.datum, task.trace.pushCause, task.subscriber, task.addr)) {
        return false;
      }

      if (!causeContinue(task)) {
        return false;
      }

      final PushData pushData = task.createPushData();
      task.setPushDataCount(pushData.getDataCount());

      // double check
      if (!causeContinue(task)) {
        return false;
      }
      // check push empty can skip (last push is also empty)
      if (checkSkipPushEmptyAndUpdateVersion(task)) {
        return false;
      }

      task.trace.startPush();
      pushingTasks.put(task.pushingTaskKey, task);
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
    boolean cleaned = pushingTasks.remove(task.pushingTaskKey) != null;
    if (e instanceof RequestChannelClosedException) {
      task.trace.finishPush(
          PushTrace.PushStatus.ChanClosed,
          task.taskID,
          task.getMaxPushedVersion(),
          task.getPushDataCount());
      LOGGER.error(
          "{}, channel closed, {}, cleaned={}, {}",
          task.taskID,
          task.pushingTaskKey,
          cleaned,
          e.getMessage());
      return;
    }
    if (e instanceof ChannelOverflowException) {
      task.trace.finishPush(
          PushTrace.PushStatus.ChanOverflow,
          task.taskID,
          task.getMaxPushedVersion(),
          task.getPushDataCount());
      LOGGER.error(
          "{}, channel overflow, {}, cleaned={}, {}",
          task.taskID,
          task.pushingTaskKey,
          cleaned,
          e.getMessage());
      return;
    }
    task.trace.finishPush(
        PushTrace.PushStatus.Fail,
        task.taskID,
        task.getMaxPushedVersion(),
        task.getPushDataCount());
    LOGGER.error(
        "{}, failed to pushing {}, cleaned={}", task.taskID, task.pushingTaskKey, cleaned, e);
  }

  class PushTaskImpl extends PushTask implements Runnable {
    PushTaskImpl(
        PushCause pushCause,
        InetSocketAddress addr,
        Map<String, Subscriber> subscriberMap,
        SubDatum datum) {
      super(pushCause, addr, subscriberMap, datum);
    }

    protected PushData createPushData() {
      return pushDataGenerator.createPushData(datum, subscriberMap);
    }

    @Override
    protected boolean commit() {
      try {
        // keyed by client.addr && (pushingKey%8)
        // avoid generating too many pushes for the same client at the same time
        pushExecutor.execute(new Tuple(pushingTaskKey.addr, pushingTaskKey.hashCode() % 6), this);
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
      pushingTasks.remove(pushTask.pushingTaskKey, pushTask);
      // get max pushedVersion before checkAndUpdate
      final long subscriberPushedVersion =
          SubscriberUtils.getMaxPushedVersion(
              pushTask.datum.getDataCenter(), pushTask.subscriberMap.values());
      for (Subscriber subscriber : pushTask.subscriberMap.values()) {
        if (!subscriber.checkAndUpdateCtx(
            pushTask.datum.getDataCenter(),
            pushTask.datum.getVersion(),
            pushTask.getPushDataCount())) {
          LOGGER.info(
              "PushY, but failed to updateVersion, {}, {}",
              pushTask.taskID,
              pushTask.pushingTaskKey);
        }
      }
      this.pushTask.trace.finishPush(
          PushTrace.PushStatus.OK,
          pushTask.taskID,
          subscriberPushedVersion,
          this.pushTask.getPushDataCount());
    }

    @Override
    public void onException(Channel channel, Throwable exception) {
      pushingTasks.remove(pushTask.pushingTaskKey, pushTask);

      boolean needRecord = true;
      final boolean channelConnected = channel.isConnected();
      if (channelConnected) {
        retry(pushTask, "err");
      }

      if (exception instanceof InvokeTimeoutException) {
        this.pushTask.trace.finishPush(
            PushTrace.PushStatus.Timeout,
            pushTask.taskID,
            pushTask.getMaxPushedVersion(),
            pushTask.getPushDataCount());
        LOGGER.error("[PushTimeout]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
      } else {
        if (channelConnected) {
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.Fail,
              pushTask.taskID,
              pushTask.getMaxPushedVersion(),
              pushTask.getPushDataCount());
          LOGGER.error(
              "[PushFailed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey, exception);
        } else {
          needRecord = false;
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.ChanClosed,
              pushTask.taskID,
              pushTask.getMaxPushedVersion(),
              pushTask.getPushDataCount());
          // TODO no need to error?
          LOGGER.error("[PushChanClosed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
        }
      }

      if (needRecord) {
        // record push fail
        for (Subscriber subscriber : pushTask.subscriberMap.values()) {
          if (!subscriber.onPushFail(pushTask.datum.getDataCenter(), pushTask.datum.getVersion())) {
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
    final int initialSleepTime = sessionServerConfig.getPushDataTaskRetryFirstDelayMillis();
    if (retry == 0) {
      return initialSleepTime;
    }
    int increment = sessionServerConfig.getPushDataTaskRetryIncrementDelayMillis();
    int result = initialSleepTime + (increment * (retry - 1));
    return result >= 0L ? result : 0;
  }
}
