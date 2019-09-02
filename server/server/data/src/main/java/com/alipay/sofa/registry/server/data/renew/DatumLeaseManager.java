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
package com.alipay.sofa.registry.server.data.renew;

import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.event.AfterWorkingProcess;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.ClientDisconnectEvent;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author kezhu.wukz
 * @version $Id: DatumExpiredCleaner.java, v 0.1 2019-06-03 21:08 kezhu.wukz Exp $
 */
public class DatumLeaseManager implements AfterWorkingProcess {
    private static final Logger   LOGGER       = LoggerFactory.getLogger(DatumLeaseManager.class);
    private static final TimeZone TIME_ZONE    = TimeZone.getTimeZone("Asia/Shanghai");
    private static final Logger   RENEW_LOGGER = LoggerFactory
            .getLogger(ValueConstants.LOGGER_NAME_RENEW, "[DatumLeaseManager]");

    /** record the latest heartbeat time for each connectId, format: connectId -> lastRenewTimestamp */
    private final Map<String, Long> connectIdRenewTimestampMap = new ConcurrentHashMap<>();

    /** lock for connectId , format: connectId -> true */
    private ConcurrentHashMap<String, Boolean> locksForConnectId = new ConcurrentHashMap();

    private volatile boolean serverWorking = false;

    private volatile boolean renewEnable = true;

    private AsyncHashedWheelTimer datumAsyncHashedWheelTimer;

    @Autowired
    private DataServerConfig dataServerConfig;

    @Autowired
    private DisconnectEventHandler disconnectEventHandler;

    @Autowired
    private DatumCache datumCache;

    @Autowired
    private DataNodeStatus dataNodeStatus;

    private ScheduledThreadPoolExecutor executorForHeartbeatLess;

    private ScheduledFuture<?> futureForHeartbeatLess;

    /**
     * constructor
     */
    @PostConstruct
    public void init() {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        datumAsyncHashedWheelTimer = new AsyncHashedWheelTimer(
                threadFactoryBuilder.setNameFormat("Registry-DatumLeaseManager-WheelTimer").build(), 100,
                TimeUnit.MILLISECONDS, 1024, dataServerConfig.getDatumLeaseManagerExecutorThreadSize(),
                dataServerConfig.getDatumLeaseManagerExecutorQueueSize(),
                threadFactoryBuilder.setNameFormat("Registry-DatumLeaseManager-WheelExecutor-%d").build(),
                new TaskFailedCallback() {
                    @Override
                    public void executionRejected(Throwable e) {
                        LOGGER.error("executionRejected: " + e.getMessage(), e);
                    }

                    @Override
                    public void executionFailed(Throwable e) {
                        LOGGER.error("executionFailed: " + e.getMessage(), e);
                    }
                });

        executorForHeartbeatLess = new ScheduledThreadPoolExecutor(1,
                threadFactoryBuilder.setNameFormat("Registry-DatumLeaseManager-ExecutorForHeartbeatLess").build());
        scheduleEvictTaskForHeartbeatLess();
    }

    /**
     * reset EvictTaskForHeartbeatLess
     */
    public synchronized void reset() {
        LOGGER.info("reset is called, EvictTaskForHeartbeatLess will delay {}s",
                dataServerConfig.getDatumTimeToLiveSec());
        if (futureForHeartbeatLess != null) {
            futureForHeartbeatLess.cancel(false);
        }
        scheduleEvictTaskForHeartbeatLess();
    }

    private void scheduleEvictTaskForHeartbeatLess() {
        futureForHeartbeatLess = executorForHeartbeatLess
                .scheduleWithFixedDelay(new EvictTaskForHeartbeatLess(), dataServerConfig.getDatumTimeToLiveSec(),
                        dataServerConfig.getDatumTimeToLiveSec(), TimeUnit.SECONDS);
    }

    /**
     * record the renew timestamp
     */
    public void renew(String connectId) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("renew: connectId={}", connectId);
        }

        // record the renew timestamp
        connectIdRenewTimestampMap.put(connectId, System.currentTimeMillis());
        // try to trigger evict task
        scheduleEvictTask(connectId, 0);
    }

    /**
     * remove connectId if clientOff
     */
    public void remove(String connectId) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("remove: connectId={}", connectId);
        }

        Long removed = connectIdRenewTimestampMap.remove(connectId);
        if (removed != null) {
            LOGGER.info("remove connectId({}) because it is clientOff", connectId);
        }
    }

    /**
     * trigger evict task: if connectId expired, create ClientDisconnectEvent to cleanup datums bind to the connectId
     * PS: every connectId allows only one task to be created
     */
    private void scheduleEvictTask(String connectId, long delaySec) {
        delaySec = (delaySec <= 0) ? dataServerConfig.getDatumTimeToLiveSec() : delaySec;

        // lock for connectId: every connectId allows only one task to be created
        Boolean ifAbsent = locksForConnectId.putIfAbsent(connectId, true);
        if (ifAbsent != null) {
            return;
        }

        datumAsyncHashedWheelTimer.newTimeout(_timeout -> {
            boolean continued = true;
            long nextDelaySec = 0;
            try {
                // release lock
                locksForConnectId.remove(connectId);

                // get lastRenewTime of this connectId
                Long lastRenewTime = connectIdRenewTimestampMap.get(connectId);
                if (lastRenewTime == null) {
                    // connectId is already clientOff
                    LOGGER.info("EvictTask(connectId={}) stop because already disconnected", connectId);
                    return;
                }

                if (RENEW_LOGGER.isDebugEnabled()) {
                    RENEW_LOGGER.debug("EvictTask: connectId={}, lastRenewTime={}", connectId, format(lastRenewTime));
                }

                /*
                 * 1. lastRenewTime expires, then:
                 *   - build ClientOffEvent and hand it to DataChangeEventCenter.
                 *   - It will not be scheduled next time, so terminated.
                 * 2. lastRenewTime not expires, then:
                 *   - trigger the next schedule
                 */
                boolean isExpired =
                        System.currentTimeMillis() - lastRenewTime > dataServerConfig.getDatumTimeToLiveSec() * 1000L;
                if (!isRenewEnable()) {
                    LOGGER.info(
                            "scheduleEvictTask({}) skipped because isRenewEnable() is false, lastRenewTime is {}, DataNodeStatus is {}, will retry after {}s",
                            connectId, format(lastRenewTime), dataNodeStatus.getStatus(),
                            dataServerConfig.getDatumTimeToLiveSec());
                    nextDelaySec = dataServerConfig.getDatumTimeToLiveSec();
                } else if (isExpired) {
                    int ownPubSize = getOwnPubSize(connectId);
                    if (ownPubSize > 0) {
                        LOGGER.info("Evict connectId({}) because expired, lastRenewTime is {}, pub.size is {}",
                                connectId, format(lastRenewTime), ownPubSize);
                        evict(connectId);
                    }
                    connectIdRenewTimestampMap.remove(connectId, lastRenewTime);
                    continued = false;
                } else {
                    nextDelaySec = dataServerConfig.getDatumTimeToLiveSec()
                                   - (System.currentTimeMillis() - lastRenewTime) / 1000L;
                    nextDelaySec = nextDelaySec <= 0 ? 1 : nextDelaySec;
                }

            } catch (Exception e) {
                LOGGER.error("Error in task of datumAsyncHashedWheelTimer", e);
            }
            if (continued) {
                scheduleEvictTask(connectId, nextDelaySec);
            }
        }, delaySec, TimeUnit.SECONDS);

        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("scheduleEvictTask: connectId={}, delaySec={}", connectId, delaySec);
        }

    }

    private int getOwnPubSize(String connectId) {
        Map<String, Publisher> ownPubs = datumCache.getOwnByConnectId(connectId);
        return ownPubs != null ? ownPubs.size() : 0;
    }

    private void evict(String connectId) {
        disconnectEventHandler.receive(new ClientDisconnectEvent(connectId, System.currentTimeMillis(), 0));
    }

    private String format(long lastRenewTime) {
        return DateFormatUtils.format(lastRenewTime, "yyyy-MM-dd HH:mm:ss", TIME_ZONE);
    }

    @Override
    public void afterWorkingProcess() {
        /*
         * After the snapshot data is synchronized during startup, it is queued and then placed asynchronously into
         * DatumCache. When the notification becomes WORKING, there may be data in the queue that is not executed
         * to DatumCache. So it need to sleep for a while.
         */
        try {
            TimeUnit.MILLISECONDS.sleep(dataServerConfig.getRenewEnableDelaySec());
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        serverWorking = true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * evict own connectIds with heartbeat less
     */
    private class EvictTaskForHeartbeatLess implements Runnable {

        @Override
        public void run() {
            // If in a non-working state, cannot clean up because the renew request cannot be received at this time.
            if (!isRenewEnable()) {
                LOGGER.info(
                        "EvictTaskForHeartbeatLess skipped because isRenewEnable() is false, DataNodeStatus is {}, will retry after {}s",
                        dataNodeStatus.getStatus(), dataServerConfig.getDatumTimeToLiveSec());
                return;
            }

            Set<String> allConnectIds = datumCache.getAllConnectIds();
            for (String connectId : allConnectIds) {
                Long timestamp = connectIdRenewTimestampMap.get(connectId);
                // no heartbeat
                if (timestamp == null) {
                    int ownPubSize = getOwnPubSize(connectId);
                    if (ownPubSize > 0) {
                        LOGGER.info("Evict connectId({}) because no heartbeat, pub.size is {}", connectId,
                                ownPubSize);
                        evict(connectId);
                    }
                }
            }

            LOGGER.info("connectIdRenewTimestampMap.size is {}", connectIdRenewTimestampMap.size());
        }
    }

    public void setRenewEnable(boolean renewEnable) {
        this.renewEnable = renewEnable;
    }

    private boolean isRenewEnable() {
        return renewEnable && serverWorking;
    }

}