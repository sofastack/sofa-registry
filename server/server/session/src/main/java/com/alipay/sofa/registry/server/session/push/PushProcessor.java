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
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.*;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PushProcessor {
    private static final Logger                 LOGGER               = LoggerFactory
                                                                         .getLogger(PushProcessor.class);

    private KeyedThreadPoolExecutor             pushExecutor;
    private final Map<PendingTaskKey, PushTask> pendingTasks         = Maps.newConcurrentMap();
    private final Lock                          pendingLock          = new ReentrantLock();

    private final Map<PushingTaskKey, PushTask> pushingTasks         = Maps.newConcurrentMap();

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    @Autowired
    private PushDataGenerator                   pushDataGenerator;

    @Autowired
    private ClientNodeService                   clientNodeService;

    private final WatchDog                      watchDog             = new WatchDog();

    private final ThreadPoolExecutor            pushCallbackExecutor = MetricsableThreadPoolExecutor
                                                                         .newExecutor(
                                                                             "PushCallbackExecutor",
                                                                             2, 1000,
                                                                             new CallRunHandler());

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
            PENDING_NEW_COUNTER.inc();
            return true;
        }
        PushTask prev;
        pendingLock.lock();
        try {
            prev = pendingTasks.get(key);
            if (prev == null) {
                pendingTasks.put(key, pushTask);
                PENDING_NEW_COUNTER.inc();
            } else {
                // update the expireTimestamp as prev's, avoid the push block by the continues fire
                pushTask.expireTimestamp = prev.expireTimestamp;
                pendingTasks.put(key, pushTask);
                PENDING_REPLACE_COUNTER.inc();
            }
        } finally {
            pendingLock.unlock();
        }
        if (pushTask.noDelay) {
            watchDog.wakeup();
        }
        return true;
    }

    protected List<PushTask> createPushTask(boolean noDelay, InetSocketAddress addr,
                                            Map<String, Subscriber> subscriberMap, Datum datum) {
        PushTask pushTask = new PushTask(noDelay, addr, subscriberMap, datum);
        // wait to merge to debouncing
        pushTask.expireAfter(sessionServerConfig.getPushDataTaskDebouncingMillis());
        return Collections.singletonList(pushTask);
    }

    void firePush(boolean noDelay, InetSocketAddress addr, Map<String, Subscriber> subscriberMap,
                  Datum datum) {
        List<PushTask> fires = createPushTask(noDelay, addr, subscriberMap, datum);
        for (PushTask task : fires) {
            boolean fire = firePush(task);
            LOGGER.info("fire push={}, {}", fire, task);
        }
    }

    private boolean commitTask(PushTask task) {
        try {
            // keyed by pushingKey: client.addr && dataInfoId
            pushExecutor.execute(task.pushingTaskKey, task);
            COMMIT_COUNTER.inc();
            return true;
        } catch (Throwable e) {
            LOGGER.error("failed to exec push task {},{}", task.taskID, task.pushingTaskKey, e);
            return false;
        }
    }

    private final class WatchDog extends WakeUpLoopRunnable {

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
            return true;
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
        final String                  dataCenter;
        final long                    pushVersion;
        final Datum                   datum;
        final InetSocketAddress       addr;
        final Map<String, Subscriber> subscriberMap;
        final Subscriber              subscriber;
        final AtomicInteger           retryCount      = new AtomicInteger();

        final PushingTaskKey          pushingTaskKey;

        PushTask(boolean noDelay, InetSocketAddress addr, Map<String, Subscriber> subscriberMap,
                 Datum datum) {
            this.taskID = TraceID.newTraceID();
            this.noDelay = noDelay;
            this.dataCenter = datum.getDataCenter();
            this.pushVersion = datum.getVersion();
            this.datum = datum;
            this.addr = addr;
            this.subscriberMap = subscriberMap;
            this.subscriber = subscriberMap.values().iterator().next();
            this.pushingTaskKey = new PushingTaskKey(subscriber.getDataInfoId(), addr,
                subscriber.getScope(), subscriber.getClientVersion());
        }

        protected Object createPushData() {
            return pushDataGenerator.createPushData(datum, subscriberMap);
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
                PUSH_CLIENT_PUSHING_COUNTER.inc();
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

        PendingTaskKey pendingKeyOf() {
            return new PendingTaskKey(dataCenter, addr, subscriber.getDataInfoId(),
                subscriberMap.keySet());
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(512);
            sb.append("PushTask{").append(subscriber.getDataInfoId()).append(",ID=").append(taskID)
                .append(",createT=").append(createTimestamp).append(",expireT=")
                .append(expireTimestamp).append(",DC=").append(dataCenter).append(",ver=")
                .append(pushVersion).append(",addr=").append(addr).append(",scope=")
                .append(subscriber.getScope()).append(",subIds=").append(subscriberMap.keySet())
                .append(",sub=").append(subscriber.printPushContext()).append(",retry=")
                .append(retryCount.get());
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
            PUSH_CLIENT_SUCCESS_COUNTER.inc();
            boolean cleaned = false;
            try {
                for (Subscriber subscriber : pushTask.subscriberMap.values()) {
                    if (!subscriber
                        .checkAndUpdateVersion(pushTask.dataCenter, pushTask.pushVersion)) {
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
            PUSH_CLIENT_FAIL_COUNTER.inc();
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
        final BaseInfo.ClientVersion clientVersion;

        PushingTaskKey(String dataInfoId, InetSocketAddress addr, ScopeEnum scopeEnum,
                       BaseInfo.ClientVersion clientVersion) {
            this.dataInfoId = dataInfoId;
            this.addr = addr;
            this.scopeEnum = scopeEnum;
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
                   && scopeEnum == that.scopeEnum && clientVersion == that.clientVersion;
        }

        @Override
        public int hashCode() {
            return Objects.hash(addr, dataInfoId, scopeEnum, clientVersion);
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
