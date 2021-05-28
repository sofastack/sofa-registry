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
import com.alipay.sofa.registry.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected PushDataGenerator pushDataGenerator;

  @Autowired protected ClientNodeService clientNodeService;

  final WatchDog watchDog = new WatchDog();
  final Cleaner cleaner = new Cleaner();

  final AtomicLong callBackDiscardCount = new AtomicLong();
  private final ThreadPoolExecutor pushCallbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "PushCallback", OsUtils.getCpuCount() * 5, 40000, new DiscardRunHandler());

  @PostConstruct
  public void init() {
    pushExecutor =
        new KeyedThreadPoolExecutor(
            "PushExecutor",
            sessionServerConfig.getPushTaskExecutorPoolSize(),
            sessionServerConfig.getPushTaskExecutorQueueSize());
    ConcurrentUtils.createDaemonThread("PushWatchDog", watchDog).start();
    ConcurrentUtils.createDaemonThread("PushCleaner", cleaner).start();
  }

  private boolean firePush(PushTask pushTask) {
    PendingTaskKey key = pushTask.pendingKeyOf();
    if (pendingTasks.putIfAbsent(key, pushTask) == null) {
      // fast path
      if (pushTask.trace.pushCause.pushType.noDelay) {
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
      if (pushTask.trace.pushCause.pushType.noDelay) {
        watchDog.wakeup();
      }
      return true;
    } else {
      PENDING_SKIP_COUNTER.inc();
      LOGGER.info(
          "[SkipPending]key={},prev={},ver={}, now={},ver={}, retry={}",
          key,
          prev.taskID,
          prev.datum.getVersion(),
          pushTask.taskID,
          pushTask.datum.getVersion(),
          pushTask.retryCount);
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
    // most of the time, element size is 1, SingleMap to save the memory
    subscriberMap = CollectionUtils.toSingletonMap(subscriberMap);
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
          new Tuple(task.pushingTaskKey.addr, task.pushingTaskKey.hashCode() % 6), task);
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

  final class Cleaner extends LoopRunnable {
    @Override
    public void runUnthrowable() {
      int cleans = cleanPushingTaskRunTooLong();
      LOGGER.info(
          "cleans={}, callbackDiscardCounter={}, pending={}, pushing={}",
          cleans,
          callBackDiscardCount.getAndSet(0),
          pendingTasks.size(),
          pushingTasks.size());
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
    }
  }

  List<PushTask> watchCommit() {
    if (!pushSwitchService.canPush()) {
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
        if (task.trace.pushCause.pushType.noDelay || task.expireTimestamp <= now) {
          pending.add(task);
          it.remove();
        }
      }
    } finally {
      pendingLock.unlock();
    }
    return pending;
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
        task.trace.finishPush(PushTrace.PushStatus.Busy, task.taskID, task.getMaxPushedVersion());
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
    final PushingTaskKey pushingTaskKey = task.pushingTaskKey;
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

  private boolean retry(PushTask task, String reason) {
    task.retryCount++;
    final int retry = task.retryCount;
    if (retry <= sessionServerConfig.getPushTaskRetryTimes() && causeContinue(task)) {
      final int backoffMillis = getRetryBackoffTime(retry);
      task.expireAfter(backoffMillis);
      PUSH_RETRY_COUNTER.labels(reason).inc();
      return firePush(task);
    }
    return false;
  }

  // check push empty, some group maybe could not tolerate push empty
  protected boolean interruptOnPushEmpty(
      SubDatum datum, PushCause pushCause, Subscriber sub, InetSocketAddress addr) {
    return false;
  }

  boolean doPush(PushTask task) {
    if (!pushSwitchService.canPush()) {
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

      final Object data = task.createPushData();
      task.trace.startPush();

      // double check
      if (!causeContinue(task)) {
        return false;
      }
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
    task.trace.finishPush(PushTrace.PushStatus.Fail, task.taskID, task.getMaxPushedVersion());
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
    volatile long expireTimestamp;

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

    long getMaxPushedVersion() {
      return SubscriberUtils.getMaxPushedVersion(datum.getDataCenter(), subscriberMap.values());
    }

    @Override
    public void run() {
      doPush(this);
    }

    PendingTaskKey pendingKeyOf() {
      return new PendingTaskKey(
          datum.getDataCenter(), addr, subscriber.getDataInfoId(), subscriberMap.keySet());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(512);
      sb.append("PushTask{")
          .append(subscriber.getDataInfoId())
          .append(",ID=")
          .append(taskID)
          .append(",createT=")
          .append(trace.pushCreateTimestamp)
          .append(",expireT=")
          .append(expireTimestamp)
          .append(",DC=")
          .append(datum.getDataCenter())
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
        if (!subscriber.checkAndUpdateVersion(
            pushTask.datum.getDataCenter(),
            pushTask.datum.getVersion(),
            pushTask.datum.getPublishers().size())) {
          LOGGER.info(
              "PushY, but failed to updateVersion, {}, {}",
              pushTask.taskID,
              pushTask.pushingTaskKey);
        }
      }
      this.pushTask.trace.finishPush(
          PushTrace.PushStatus.OK, pushTask.taskID, subscriberPushedVersion);
      PUSH_CLIENT_SUCCESS_COUNTER.inc();
    }

    @Override
    public void onException(Channel channel, Throwable exception) {
      pushingTasks.remove(pushTask.pushingTaskKey, pushTask);
      final boolean channelConnected = channel.isConnected();
      if (channelConnected) {
        retry(pushTask, "err");
      }

      if (exception instanceof InvokeTimeoutException) {
        this.pushTask.trace.finishPush(
            PushTrace.PushStatus.Timeout, pushTask.taskID, pushTask.getMaxPushedVersion());
        LOGGER.error("[PushTimeout]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
      } else {
        if (channelConnected) {
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.Fail, pushTask.taskID, pushTask.getMaxPushedVersion());
          LOGGER.error(
              "[PushFailed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey, exception);
        } else {
          this.pushTask.trace.finishPush(
              PushTrace.PushStatus.ChanClosed, pushTask.taskID, pushTask.getMaxPushedVersion());
          // TODO no need to error?
          LOGGER.error("[PushChanClosed]taskId={}, {}", pushTask.taskID, pushTask.pushingTaskKey);
        }
      }
      PUSH_CLIENT_FAIL_COUNTER.inc();
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
      return StringFormatter.format(
          "Pending{{},{},{},subIds={}}", dataInfoId, dataCenter, addr, subscriberIds);
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

  final class DiscardRunHandler implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      callBackDiscardCount.incrementAndGet();
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
