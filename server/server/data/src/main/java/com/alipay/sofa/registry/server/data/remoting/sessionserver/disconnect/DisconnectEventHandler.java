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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.ClientChangeEvent;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;

/**
 * @author qian.lqlq
 * @version $Id: ClientDisconnectEventHandler.java, v 0.1 2017-12-07 15:32 qian.lqlq Exp $
 */
public class DisconnectEventHandler implements InitializingBean {

    private static final Logger               LOGGER      = LoggerFactory
                                                              .getLogger(DisconnectEventHandler.class);

    /**
     * a DelayQueue that contains client disconnect events
     */
    private final DelayQueue<DisconnectEvent> EVENT_QUEUE = new DelayQueue<>();

    @Autowired
    private SessionServerConnectionFactory    sessionServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter             dataChangeEventCenter;

    @Autowired
    private DataServerConfig                  dataServerConfig;

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
        }
        EVENT_QUEUE.add(event);
    }

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("begin start DisconnectEventHandler");
        Executor executor = ExecutorFactory
                .newSingleThreadExecutor(DisconnectEventHandler.class.getSimpleName());
        executor.execute(() -> {
            while (true) {
                try {
                    DisconnectEvent disconnectEvent = EVENT_QUEUE.take();
                    if (disconnectEvent.getType() == DisconnectTypeEnum.SESSION_SERVER) {
                        SessionServerDisconnectEvent event = (SessionServerDisconnectEvent) disconnectEvent;
                        String processId = event.getProcessId();
                        //check processId confirm remove,and not be registered again when delay time
                        String sessionServerHost = event.getSessionServerHost();
                        if (sessionServerConnectionFactory
                                .removeProcessIfMatch(processId, sessionServerHost)) {
                            Set<String> clientHosts = sessionServerConnectionFactory
                                    .removeClients(processId);

                            LOGGER.info("session off is triggered: sessionServerHost={}, clientHost={}, processId={}",
                                    sessionServerHost,
                                    clientHosts, processId);

                            if (clientHosts != null && !clientHosts.isEmpty()) {
                                for (String host : clientHosts) {
                                    unPub(host, event.getRegisterTimestamp());
                                }
                            }
                        } else {
                            LOGGER.info("session off is canceled: sessionServerHost={}, processId={}",
                                    sessionServerHost, processId);
                        }
                    } else {
                        ClientDisconnectEvent event = (ClientDisconnectEvent) disconnectEvent;
                        unPub(event.getHost(), event.getRegisterTimestamp());
                    }
                } catch (Throwable e) {
                    LOGGER.error("handle client disconnect event failed", e);
                }
            }
        });
        LOGGER.info("start DisconnectEventHandler success");
    }

    /**
     *
     * @param host
     * @param registerTimestamp
     */
    private void unPub(String host, long registerTimestamp) {
        dataChangeEventCenter.onChange(new ClientChangeEvent(host, dataServerConfig
            .getLocalDataCenter(), registerTimestamp));
    }
}