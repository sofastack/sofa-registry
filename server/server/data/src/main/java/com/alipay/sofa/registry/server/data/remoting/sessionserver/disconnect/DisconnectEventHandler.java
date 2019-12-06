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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.ClientChangeEvent;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.event.AfterWorkingProcess;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.util.LocalServerStatusEnum;

/**
 * @author qian.lqlq
 * @version $Id: ClientDisconnectEventHandler.java, v 0.1 2017-12-07 15:32 qian.lqlq Exp $
 */
public class DisconnectEventHandler implements InitializingBean, AfterWorkingProcess {

    private static final Logger                         LOGGER             = LoggerFactory
                                                                               .getLogger(DisconnectEventHandler.class);

    private static final Logger                         LOGGER_START       = LoggerFactory
                                                                               .getLogger("DATA-START-LOGS");

    /**
     * a DelayQueue that contains client disconnect events
     */
    private final DelayQueue<DisconnectEvent>           EVENT_QUEUE        = new DelayQueue<>();

    @Autowired
    private SessionServerConnectionFactory              sessionServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter                       dataChangeEventCenter;

    @Autowired
    private DataServerConfig                            dataServerConfig;

    @Autowired
    private DataNodeStatus                              dataNodeStatus;

    private static final int                            BLOCK_FOR_ALL_SYNC = 5000;

    private static final BlockingQueue<DisconnectEvent> noWorkQueue        = new LinkedBlockingQueue<>();

    /**
     * receive disconnect event of client
     *
     * @param event
     */
    public void receive(DisconnectEvent event) {
        if (event.getType() == DisconnectTypeEnum.SESSION_SERVER) {
            SessionServerDisconnectEvent sessionServerDisconnectEvent = (SessionServerDisconnectEvent) event;
            LOGGER.info("receive session off event: sessionServerHost={}, processId={}",
                sessionServerDisconnectEvent.getSessionServerHost(),
                sessionServerDisconnectEvent.getProcessId());
        } else if (event.getType() == DisconnectTypeEnum.CLIENT) {
            ClientDisconnectEvent clientDisconnectEvent = (ClientDisconnectEvent) event;
            LOGGER.info("receive client off event: connectId={}",
                clientDisconnectEvent.getConnectId());
        }

        if (dataNodeStatus.getStatus() != LocalServerStatusEnum.WORKING) {
            LOGGER.info("receive disconnect event,but data server not working!");
            noWorkQueue.add(event);
            return;
        }
        EVENT_QUEUE.add(event);
    }

    public void afterWorkingProcess() {
        try {
            /*
             * After the snapshot data is synchronized during startup, it is queued and then placed asynchronously into
             * DatumCache. When the notification becomes WORKING, there may be data in the queue that is not executed
             * to DatumCache. So it need to sleep for a while.
             */
            TimeUnit.MILLISECONDS.sleep(BLOCK_FOR_ALL_SYNC);

            while (!noWorkQueue.isEmpty()) {
                DisconnectEvent event = noWorkQueue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    receive(event);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("receive disconnect event after working interrupted!", e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void afterPropertiesSet() {
        Executor executor = ExecutorFactory
                .newSingleThreadExecutor(DisconnectEventHandler.class.getSimpleName());
        executor.execute(() -> {
            while (true) {
                try {
                    DisconnectEvent disconnectEvent = EVENT_QUEUE.take();
                    if (disconnectEvent.getType() == DisconnectTypeEnum.SESSION_SERVER) {
                        SessionServerDisconnectEvent event     = (SessionServerDisconnectEvent) disconnectEvent;
                        String                       processId = event.getProcessId();
                        //check processId confirm remove,and not be registered again when delay time
                        String sessionServerHost = event.getSessionServerHost();
                        if (sessionServerConnectionFactory
                                .removeProcessIfMatch(processId,sessionServerHost)) {
                            Set<String> connectIds = sessionServerConnectionFactory
                                    .removeConnectIds(processId);

                            LOGGER.info("session off is triggered: sessionServerHost={}, connectId={}, processId={}",
                                    sessionServerHost,
                                    connectIds, processId);

                            if (connectIds != null && !connectIds.isEmpty()) {
                                for (String connectId : connectIds) {
                                    unPub(connectId, event.getRegisterTimestamp());
                                }
                            }
                        } else {
                            LOGGER.info("session off is canceled: sessionServerHost={}, processId={}",
                                    sessionServerHost, processId);
                        }
                    } else {
                        ClientDisconnectEvent event = (ClientDisconnectEvent) disconnectEvent;
                        unPub(event.getConnectId(), event.getRegisterTimestamp());
                    }
                } catch (Throwable e) {
                    LOGGER.error("handle client disconnect event failed", e);
                }
            }
        });
        LOGGER_START.info("start DisconnectEventHandler success");
    }

    /**
     *
     * @param connectId
     * @param registerTimestamp
     */
    private void unPub(String connectId, long registerTimestamp) {
        dataChangeEventCenter.onChange(new ClientChangeEvent(connectId, dataServerConfig
            .getLocalDataCenter(), registerTimestamp));
    }
}