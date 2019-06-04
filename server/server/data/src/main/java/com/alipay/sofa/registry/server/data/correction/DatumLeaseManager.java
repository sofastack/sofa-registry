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
package com.alipay.sofa.registry.server.data.correction;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.ClientDisconnectEvent;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author kezhu.wukz
 * @version $Id: DatumExpiredCleaner.java, v 0.1 2019-06-03 21:08 kezhu.wukz Exp $
 */
public class DatumLeaseManager {
    private static final Logger                LOGGER                     = LoggerFactory
                                                                              .getLogger(DatumLeaseManager.class);
    private static final TimeZone              TIME_ZONE                  = TimeZone
                                                                              .getTimeZone("Asia/Shanghai");

    /** record the latest heartbeat time for each connectId, format: connectId -> lastRenewTimestamp */
    private final Map<String, Long>            connectIdReNewTimestampMap = new ConcurrentHashMap<>();

    /** lock for connectId , format: connectId -> true */
    private ConcurrentHashMap<String, Boolean> locksForConnectId          = new ConcurrentHashMap();

    private final AsyncHashedWheelTimer        datumAsyncHashedWheelTimer;
    private final ThreadPoolExecutor           datumExpiredCheckExecutor;

    @Autowired
    private DataServerConfig                   dataServerConfig;

    @Autowired
    private DisconnectEventHandler             disconnectEventHandler;

    /**
     * constructor
     */
    public DatumLeaseManager() {
        datumExpiredCheckExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory(
                "Scheduler-DatumExpiredCheckExecutor"));
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        datumAsyncHashedWheelTimer = new AsyncHashedWheelTimer(threadFactoryBuilder.setNameFormat(
            "Scheduler-WheelTimer").build(), 100, TimeUnit.MILLISECONDS, 1024,
            datumExpiredCheckExecutor, new TaskFailedCallback() {
                @Override
                public void executionRejected(Throwable e) {
                    LOGGER.error("executionRejected: " + e.getMessage(), e);
                }

                @Override
                public void executionFailed(Throwable e) {
                    LOGGER.error("executionFailed: " + e.getMessage(), e);
                }
            });
    }

    /**
     * record the reNew timestamp
     */
    public void reNew(String connectId) {
        // record the reNew timestamp
        connectIdReNewTimestampMap.put(connectId, System.currentTimeMillis());
        // try to trigger evict task
        scheduleEvictTask(connectId, 0);
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
            boolean continued    = true;
            long    nextDelaySec = 0;
            try {
                // release lock
                locksForConnectId.remove(connectId);

                // get lastReNewTime of this connectId
                long lastReNewTime = connectIdReNewTimestampMap.get(connectId);

                /*
                 * 1. lastReNewTime expires, then:
                 *   - build ClientOffEvent and hand it to DataChangeEventCenter.
                 *   - It will not be scheduled next time, so terminated.
                 * 2. lastReNewTime not expires, then:
                 *   - trigger the next schedule
                 */
                boolean isExpired =
                        System.currentTimeMillis() - lastReNewTime > dataServerConfig.getDatumTimeToLiveSec() * 1000L;
                if (isExpired) {
                    LOGGER.info("ConnectId({}) expired, lastReNewTime is {}", connectId,
                            DateFormatUtils.format(lastReNewTime, "yyyy-MM-dd HH:mm:ss", TIME_ZONE));
                    connectIdReNewTimestampMap.remove(connectId, lastReNewTime);
                    disconnectEventHandler.receive(new ClientDisconnectEvent(connectId, System.currentTimeMillis(),
                            dataServerConfig.getClientOffDelayMs() * 10));
                    continued = false;
                } else {
                    nextDelaySec =
                            dataServerConfig.getDatumTimeToLiveSec() -
                                    (System.currentTimeMillis() - lastReNewTime) / 1000L;
                    nextDelaySec = nextDelaySec <= 0 ? 1 : nextDelaySec;
                }

            } catch (Exception e) {
                LOGGER.error("Error in task of datumAsyncHashedWheelTimer", e);
            }
            if (continued) {
                scheduleEvictTask(connectId, nextDelaySec);
            }
        }, delaySec, TimeUnit.SECONDS);

    }
}