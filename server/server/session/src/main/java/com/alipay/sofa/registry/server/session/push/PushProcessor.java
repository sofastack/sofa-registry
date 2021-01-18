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

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AssembleType;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.trace.TraceID;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeupLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PushProcessor {
    private static final Logger                 LOGGER                   = LoggerFactory
                                                                             .getLogger(PushProcessor.class);

    private KeyedThreadPoolExecutor             pushExecutor;
    private final Map<PendingTaskKey, PushTask> pendingTasks             = Maps.newConcurrentMap();
    private final Lock                          pendingLock              = new ReentrantLock();

    private final Map<PushingTaskKey, PushTask> pushingTasks             = Maps.newConcurrentMap();

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    @Autowired
    private PushDataGenerator                   pushDataGenerator;

    @Autowired
    private ClientNodeService                   clientNodeService;

    private final WatchDog                      watchDog                 = new WatchDog();

    private final ThreadPoolExecutor            pushCallbackExecutor     = MetricsableThreadPoolExecutor
                                                                             .newExecutor(
                                                                                 "PushCallbackExecutor",
                                                                                 2,
                                                                                 1000,
                                                                                 new CallRunHandler());

    private final Counter                       pendingCounter           = Counter.build()
                                                                             .namespace("session")
                                                                             .subsystem("push")
                                                                             .name("pending_total")
                                                                             .help("pending fetch")
                                                                             .labelNames("type")
                                                                             .register();
    private final Counter.Child                 pendingReplaceCounter    = pendingCounter
                                                                             .labels("replace");
    private final Counter.Child                 pendingNewCounter        = pendingCounter
                                                                             .labels("new");
    private final Counter.Child                 pendingConflictCounter   = pendingCounter
                                                                             .labels("conflict");

    private final Counter                       commitCounter            = Counter
                                                                             .build()
                                                                             .namespace("session")
                                                                             .subsystem("push")
                                                                             .name(
                                                                                 "fire_commit_total")
                                                                             .help("commit task")
                                                                             .register();
    private final Counter                       pushClientCounter        = Counter
                                                                             .build()
                                                                             .namespace("session")
                                                                             .subsystem("push")
                                                                             .name(
                                                                                 "push_client_total")
                                                                             .help(
                                                                                 "push client task")
                                                                             .labelNames("type")
                                                                             .register();
    private final Counter.Child                 pushClientPushingCounter = pushClientCounter
                                                                             .labels("I");
    private final Counter.Child                 pushClientSuccessCounter = pushClientCounter
                                                                             .labels("S");
    private final Counter.Child                 pushClientFailCounter    = pushClientCounter
                                                                             .labels("F");

    @PostConstruct
    public void init() {
        pushExecutor = new KeyedThreadPoolExecutor("PushExecutor",
            sessionServerConfig.getPushTaskExecutorPoolSize(),
            sessionServerConfig.getPushTaskExecutorQueueSize());
        ConcurrentUtils.createDaemonThread("PushWatchDog", watchDog).start();
    }

    private boolean firePush(PushTask pushTask) {
        PendingTaskKey key = pushTask.pendingKeyOf();
        if (pendingTasks.putIfAbsent(key, pushTask) == null) {
            // fast path
            pendingNewCounter.inc();
            return true;
        }
        boolean conflict = false;
        PushTask prev = null;
        pendingLock.lock();
        try {
            prev = pendingTasks.get(key);
            if (prev == null) {
                pendingTasks.put(key, pushTask);
                pendingNewCounter.inc();
            } else if (pushTask.afterThan(prev)) {
                // update the expireTimestamp as prev's, avoid the push block by the continues fire
                pushTask.expireTimestamp = prev.expireTimestamp;
                pendingTasks.put(key, pushTask);
                pendingReplaceCounter.inc();
            } else {
                conflict = true;
            }
        } finally {
            pendingLock.unlock();
        }
        if (!conflict) {
            if (pushTask.noDelay) {
                watchDog.wakeup();
            }
            return true;
        } else {
            pendingConflictCounter.inc();
            LOGGER.info("[ConflictPending] key={}, prev={}, {}, prev {}={} > {}-{}", key,
                prev.taskID, prev.pushingTaskKey, prev.fetchSeqEnd, pushTask.taskID,
                pushTask.fetchSeqStart);
            return false;
        }
    }

    protected List<PushTask> createPushTask(boolean noDelay, long pushVersion, String dataCenter,
                                            InetSocketAddress addr,
                                            Map<String, Subscriber> subscriberMap,
                                            Map<String, Datum> datumMap, long fetchStartSeq,
                                            long fetchEndSeq) {
        PushTask pushTask = new PushTask(noDelay, pushVersion, dataCenter, addr, subscriberMap,
            datumMap, fetchStartSeq, fetchEndSeq);
        // wait to merge to debouncing
        pushTask.expireAfter(sessionServerConfig.getPushDataTaskDebouncingMillis());
        return Collections.singletonList(pushTask);
    }

    void firePush(boolean noDelay, long pushVersion, String dataCenter, InetSocketAddress addr,
                  Map<String, Subscriber> subscriberMap, Map<String, Datum> datumMap,
                  long fetchSeqStart, long fetchSeqEnd) {
        List<PushTask> fires = createPushTask(noDelay, pushVersion, dataCenter, addr,
            subscriberMap, datumMap, fetchSeqStart, fetchSeqEnd);
        for (PushTask task : fires) {
            boolean fire = firePush(task);
            LOGGER.info("fire push={}, {}", fire, task);
        }
    }

    private boolean commitTask(PushTask task) {
        try {
            // keyed by pushingKey: client.addr && dataInfoId
            pushExecutor.execute(task.pushingTaskKey, task);
            commitCounter.inc();
            return true;
        } catch (Throwable e) {
            LOGGER.error("failed to exec push task {},{}", task.taskID, task.pushingTaskKey, e);
            return false;
        }
    }

    private final class WatchDog extends WakeupLoopRunnable {

        @Override
        public void runUnthrowable() {
            List<PushTask> pending = transferAndMerge();
            if (sessionServerConfig.isStopPushSwitch()) {
                return;
            }
            if (pending.isEmpty()) {
                return;
            }
            LOGGER.info("process push tasks {}", pending.size());
            for (PushTask task : pending) {
                commitTask(task);
            }
        }

        @Override
        public int getWaitingMillis() {
            return 100;
        }
    }

    private List<PushTask> transferAndMerge() {
        List<PushTask> pending = Lists.newArrayList();
        final long now = System.currentTimeMillis();
        pendingLock.lock();
        try {
            final Iterator<Map.Entry<PendingTaskKey, PushTask>> it = pendingTasks.entrySet()
                .iterator();
            while (it.hasNext()) {
                Map.Entry<PendingTaskKey, PushTask> e = it.next();
                PushTask task = e.getValue();
                if (task.noDelay || task.expireTimestamp <= now) {
                    pending.add(task);
                    it.remove();
                }
            }
        } finally {
            pendingLock.unlock();
        }
        return pending;
    }

    private boolean checkPushing(PushTask task, PushingTaskKey pushingTaskKey) {
        // check the pushing task
        final PushTask prev = pushingTasks.get(pushingTaskKey);
        if (prev == null) {
            // check the subscriber version
            for (Subscriber subscriber : task.subscriberMap.values()) {
                // TODO need remove the conflict subscriber
                if (!subscriber.checkVersion(task.dataCenter, task.fetchSeqStart)) {
                    LOGGER.warn("conflict push {}, {}, subscriber={}", task.taskID, pushingTaskKey,
                        subscriber.printPushContext());
                    return false;
                }
            }
            return true;
        }
        if (!task.afterThan(prev)) {
            LOGGER.warn("prev push is newly, {}, prev={}, now={}", pushingTaskKey, prev.taskID,
                task.taskID);
            return false;
        }
        final long span = System.currentTimeMillis() - prev.pushTimestamp;
        if (span > sessionServerConfig.getClientNodeExchangeTimeOut() * 2) {
            // force to remove the prev task
            final boolean cleaned = pushingTasks.remove(pushingTaskKey) != null;
            LOGGER.warn("[prevRunTooLong] {}, clean={}, prev={}, now={}", pushingTaskKey, cleaned,
                prev.taskID, task.taskID);
            return true;
        }
        // task after the prev, but prev.pushclient not callback, retry
        retry(task, "waiting");
        return false;
    }

    private boolean retry(PushTask task, String reason) {
        final int retry = task.retryCount.incrementAndGet();
        if (retry <= sessionServerConfig.getPushTaskRetryTimes()) {
            final int backoffMillis = getRetryBackoffTime(retry);
            task.expireAfter(backoffMillis);
            if (firePush(task)) {
                LOGGER.info("add retry for {}, {}, {}, retry={}, backoff={}", reason, task.taskID,
                    task.pushingTaskKey, retry, backoffMillis);
                return true;
            }
        }
        LOGGER.info("skip retry for {}, {}, {}, retry={}", reason, task.taskID,
            task.pushingTaskKey, retry);
        return false;
    }

    class PushTask implements Runnable {
        final TraceID                 taskID;
        final long                    createTimestamp = System.currentTimeMillis();
        volatile long                 expireTimestamp;
        volatile long                 pushTimestamp;

        final boolean                 noDelay;
        final long                    fetchSeqStart;
        final long                    fetchSeqEnd;
        final String                  dataCenter;
        final long                    pushVersion;
        final Map<String, Datum>      datumMap;
        final InetSocketAddress       addr;
        final Map<String, Subscriber> subscriberMap;
        final Subscriber              subscriber;
        final AtomicInteger           retryCount      = new AtomicInteger();

        final PushingTaskKey          pushingTaskKey;

        PushTask(boolean noDelay, long pushVersion, String dataCenter, InetSocketAddress addr,
                 Map<String, Subscriber> subscriberMap, Map<String, Datum> datumMap,
                 long fetchSeqStart, long fetchSeqEnd) {
            this.taskID = TraceID.newTraceID();
            this.noDelay = noDelay;
            this.dataCenter = dataCenter;
            this.pushVersion = pushVersion;
            this.datumMap = datumMap;
            this.addr = addr;
            this.subscriberMap = subscriberMap;
            this.fetchSeqStart = fetchSeqStart;
            this.fetchSeqEnd = fetchSeqEnd;
            this.subscriber = subscriberMap.values().iterator().next();
            this.pushingTaskKey = new PushingTaskKey(subscriber.getDataInfoId(), addr,
                subscriber.getScope(), subscriber.getAssembleType(), subscriber.getClientVersion());
        }

        protected Object createPushData() {
            Datum merged = pushDataGenerator.mergeDatum(subscriber, dataCenter, datumMap,
                pushVersion);
            LOGGER.info("merged {}, from {}, {}, {}", merged, datumMap, taskID, pushingTaskKey);
            return pushDataGenerator.createPushData(merged, subscriberMap);
        }

        void expireAfter(long intervalMs) {
            this.expireTimestamp = System.currentTimeMillis() + intervalMs;
        }

        void updatePushTimestamp() {
            this.pushTimestamp = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if (sessionServerConfig.isStopPushSwitch()) {
                return;
            }

            try {
                if (!checkPushing(this, pushingTaskKey)) {
                    return;
                }
                Object data = createPushData();
                updatePushTimestamp();
                pushingTasks.put(pushingTaskKey, this);
                clientNodeService.pushWithCallback(data, subscriber.getSourceAddress(),
                    new PushClientCallback(this, pushingTaskKey));
                pushClientPushingCounter.inc();
                LOGGER.info("{}, pushing {}", taskID, pushingTaskKey);
            } catch (RequestChannelClosedException e) {
                // try to delete self
                boolean cleaned = pushingTasks.remove(pushingTaskKey) != null;
                LOGGER.error("{}, failed to pushing {}, cleaned={}, {}", taskID, pushingTaskKey,
                    cleaned, e.getMessage());
            } catch (Throwable e) {
                // try to delete self
                boolean cleaned = pushingTasks.remove(pushingTaskKey) != null;
                LOGGER.error("{}, failed to pushing {}, cleaned={}", taskID, pushingTaskKey,
                    cleaned, e);
            }
        }

        boolean afterThan(PushTask t) {
            return fetchSeqStart >= t.fetchSeqEnd;
        }

        PendingTaskKey pendingKeyOf() {
            return new PendingTaskKey(dataCenter, addr, subscriber.getDataInfoId(),
                subscriberMap.keySet());
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("PushTask{").append(subscriber.getDataInfoId()).append(",ID=").append(taskID)
                .append(",createT=").append(createTimestamp).append(",expireT=")
                .append(expireTimestamp).append(",seqStart=").append(fetchSeqStart)
                .append(",seqEnd=").append(fetchSeqEnd).append(",DC=").append(dataCenter)
                .append(",ver=").append(pushVersion).append(",addr=").append(addr)
                .append(",scope=").append(subscriber.getScope()).append(",subIds=")
                .append(subscriberMap.keySet()).append(",sub=")
                .append(subscriber.printPushContext()).append(",retry=").append(retryCount.get());
            return sb.toString();
        }
    }

    private final class PushClientCallback implements CallbackHandler {
        final PushTask       pushTask;
        final PushingTaskKey pushingTaskKey;
        long                 finishedTimestamp;

        PushClientCallback(PushTask pushTask, PushingTaskKey pushingTaskKey) {
            this.pushTask = pushTask;
            this.pushingTaskKey = pushingTaskKey;
        }

        @Override
        public void onCallback(Channel channel, Object message) {
            this.finishedTimestamp = System.currentTimeMillis();
            pushClientSuccessCounter.inc();
            boolean cleaned = false;
            try {
                final Map<String, Long> versions = DatumUtils.getVesions(pushTask.datumMap);
                for (Subscriber subscriber : pushTask.subscriberMap.values()) {
                    if (!subscriber.checkAndUpdateVersion(pushTask.dataCenter,
                        pushTask.pushVersion, versions, pushTask.fetchSeqStart,
                        pushTask.fetchSeqEnd)) {
                        LOGGER.warn("PushY, but failed to updateVersion, {}, {}", pushTask.taskID,
                            pushTask.pushingTaskKey);
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("error push.onCallback, {}, {}", pushTask.taskID,
                    pushTask.pushingTaskKey, e);
            } finally {
                // TODO should use remove(k, exceptV). but in some case,
                // after removed=true, the value aslo in the map
                cleaned = pushingTasks.remove(pushingTaskKey, pushTask);
            }
            LOGGER.info("PushY, clean record={}, span={}/{}, {}, {}", cleaned, pushSpanMillis(),
                totalSpanMillis(), pushTask.taskID, pushTask.pushingTaskKey);
        }

        @Override
        public void onException(Channel channel, Throwable exception) {
            this.finishedTimestamp = System.currentTimeMillis();
            pushClientFailCounter.inc();
            boolean cleaned = false;
            try {
                // TODO should use remove(k, exceptV). but in some case,
                // after removed=true, the value aslo in the map
                cleaned = pushingTasks.remove(pushingTaskKey, pushTask);
                if (channel.isConnected()) {
                    retry(pushTask, "callbackErr");
                } else {
                    LOGGER.warn("PushN, channel closed, {}, {}", pushTask.taskID, pushingTaskKey);
                }
            } catch (Throwable e) {
                LOGGER.error("error push.onException, {}, {}", pushTask.taskID, pushingTaskKey, e);
            }
            if (exception instanceof InvokeTimeoutException) {
                LOGGER.error("PushN, timeout, clean record={}, span={}/{}, {}, {}", cleaned,
                    pushSpanMillis(), totalSpanMillis(), pushTask.taskID, pushingTaskKey);
            } else {
                LOGGER
                    .error("PushN, clean record={}, span={}/{}, {}, {}", cleaned, pushSpanMillis(),
                        totalSpanMillis(), pushTask.taskID, pushingTaskKey, exception);
            }
        }

        private long pushSpanMillis() {
            return finishedTimestamp - pushTask.pushTimestamp;
        }

        private long totalSpanMillis() {
            return finishedTimestamp - pushTask.createTimestamp;
        }

        @Override
        public Executor getExecutor() {
            return pushCallbackExecutor;
        }
    }

    private static final class PendingTaskKey {
        final String            dataCenter;
        final String            dataInfoId;
        final InetSocketAddress addr;
        final Set<String>       subscriberIds;

        PendingTaskKey(String dataCenter, InetSocketAddress addr, String dataInfoId,
                       Set<String> subscriberIds) {
            this.dataCenter = dataCenter;
            this.dataInfoId = dataInfoId;
            this.addr = addr;
            this.subscriberIds = subscriberIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
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
            return "PendingTaskKey{" + dataInfoId + ", dataCenter='" + dataCenter + '\''
                   + ", addr=" + addr + ", subscriberIds=" + subscriberIds + '}';
        }
    }

    private static final class PushingTaskKey {
        final InetSocketAddress      addr;
        final String                 dataInfoId;
        final ScopeEnum              scopeEnum;
        final AssembleType           assembleType;
        final BaseInfo.ClientVersion clientVersion;

        PushingTaskKey(String dataInfoId, InetSocketAddress addr, ScopeEnum scopeEnum,
                       AssembleType assembleType, BaseInfo.ClientVersion clientVersion) {
            this.dataInfoId = dataInfoId;
            this.addr = addr;
            this.scopeEnum = scopeEnum;
            this.assembleType = assembleType;
            this.clientVersion = clientVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PushingTaskKey that = (PushingTaskKey) o;
            return Objects.equals(addr, that.addr) && Objects.equals(dataInfoId, that.dataInfoId)
                   && scopeEnum == that.scopeEnum && assembleType == that.assembleType
                   && clientVersion == that.clientVersion;
        }

        @Override
        public int hashCode() {
            return Objects.hash(addr, dataInfoId, scopeEnum, assembleType, clientVersion);
        }

        @Override
        public String toString() {
            return "PushingTaskKey{" + "addr=" + addr + ", dataInfoId='" + dataInfoId + '\''
                   + ", scopeEnum=" + scopeEnum + '}';
        }
    }

    private static final class CallRunHandler extends ThreadPoolExecutor.CallerRunsPolicy {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            super.rejectedExecution(r, e);
            LOGGER.warn("push callback busy");
        }
    }

    private int getRetryBackoffTime(int retry) {
        final int initialSleepTime = sessionServerConfig.getPushDataTaskRetryFirstDelayMillis();
        if (retry == 0) {
            return initialSleepTime;
        }
        int increment = sessionServerConfig.getPushDataTaskRetryIncrementDelayMillis();
        int result = initialSleepTime + (increment * (retry - 1));
        return result >= 0L ? result : 0;
    }

}
