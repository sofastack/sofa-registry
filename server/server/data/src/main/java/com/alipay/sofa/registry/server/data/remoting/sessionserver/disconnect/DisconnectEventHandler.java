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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.ClientChangeEvent;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;

/**
 * @author qian.lqlq
 * @version $Id: ClientDisconnectEventHandler.java, v 0.1 2017-12-07 15:32 qian.lqlq Exp $
 */
public class DisconnectEventHandler implements InitializingBean {

    private static final Logger                         LOGGER       = LoggerFactory
                                                                         .getLogger(DisconnectEventHandler.class);

    private static final Logger                         LOGGER_START = LoggerFactory
                                                                         .getLogger("DATA-START-LOGS");

    /**
     * a DelayQueue that contains client disconnect events
     */
    private final DelayQueue<DisconnectEvent>           EVENT_QUEUE  = new DelayQueue<>();

    @Autowired
    private SessionServerConnectionFactory              sessionServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter                       dataChangeEventCenter;

    @Autowired
    private DataServerConfig                            dataServerConfig;

    private static final BlockingQueue<DisconnectEvent> noWorkQueue  = new LinkedBlockingQueue<>();

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
        EVENT_QUEUE.add(event);
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