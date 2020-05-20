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
package com.alipay.sofa.registry.server.session.remoting.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.alipay.sofa.registry.timer.RecycleAsyncHashedWheelTimer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author shangyu.wh
 * @version $Id: ServerConnectionLisener.java, v 0.1 2017-11-30 15:04 shangyu.wh Exp $
 */
public class ClientNodeConnectionHandler extends AbstractServerHandler {

    private static final Logger          LOGGER       = LoggerFactory.getLogger("SESSION-CONNECT");
    private static final Logger          RENEW_LOGGER = LoggerFactory.getLogger(
                                                          ValueConstants.LOGGER_NAME_RENEW,
                                                          "[ClientNodeConnectionHandler]");

    private RecycleAsyncHashedWheelTimer recycleAsyncHashedWheelTimer;

    @Autowired
    private Registry                     sessionRegistry;

    @Autowired
    private DataStore                    sessionDataStore;

    @Autowired
    private Interests                    sessionInterests;

    @Autowired
    private Watchers                     sessionWatchers;

    @Autowired
    private ExecutorManager              executorManager;

    @Autowired
    private SessionServerConfig          sessionServerConfig;

    @Autowired
    private Exchange                     boltExchange;

    @PostConstruct
    public void init() {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        recycleAsyncHashedWheelTimer = new RecycleAsyncHashedWheelTimer(threadFactoryBuilder
            .setNameFormat("Registry-RenewDatumTask-WheelTimer").build(),
            sessionServerConfig.getRenewDatumWheelTicksDuration(), TimeUnit.MILLISECONDS,
            sessionServerConfig.getRenewDatumWheelTicksSize(),
            sessionServerConfig.getRenewDatumWheelThreadSize(),
            sessionServerConfig.getRenewDatumWheelQueueSize(), threadFactoryBuilder.setNameFormat(
                "Registry-RenewDatumTask-WheelExecutor-%d").build(), new TaskFailedCallback() {
                @Override
                public void executionRejected(Throwable e) {
                    RENEW_LOGGER.error("executionRejected: " + e.getMessage(), e);
                }

                @Override
                public void executionFailed(Throwable e) {
                    RENEW_LOGGER.error("executionFailed: " + e.getMessage(), e);
                }
            });
    }

    @Override
    public HandlerType getType() {
        return HandlerType.LISENTER;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        super.connected(channel);
        fireRenewDatum(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        super.disconnected(channel);
        fireCancelClient(channel);
    }

    private void fireCancelClient(Channel channel) {
        //avoid block connect ConnectionEventExecutor thread pool
        executorManager.getConnectClientExecutor().execute(() -> {

            String connectId = NetUtil.toAddressString(channel.getRemoteAddress()) + ValueConstants.CONNECT_ID_SPLIT +
                    NetUtil.toAddressString(channel.getLocalAddress());
            if (checkCache(connectId)) {
                List<String> connectIds = new ArrayList<>();
                connectIds.add(connectId);
                sessionRegistry.cancel(connectIds);
            }
        });
    }

    private boolean checkCache(String connectId) {
        boolean checkSub = checkSub(connectId);
        boolean checkPub = checkPub(connectId);
        boolean checkWatcher = checkWatcher(connectId);
        LOGGER.info("Client off checkCache connectId:{} result pub:{},sub:{},wat:{}", connectId,
            checkPub, checkSub, checkWatcher);
        return checkPub || checkSub || checkWatcher;
    }

    private boolean checkPub(String connectId) {
        Map pubMap = sessionDataStore.queryByConnectId(connectId);
        return pubMap != null && !pubMap.isEmpty();
    }

    private boolean checkSub(String connectId) {
        Map subMap = sessionInterests.queryByConnectId(connectId);
        return subMap != null && !subMap.isEmpty();
    }

    private boolean checkWatcher(String connectId) {
        Map subMap = sessionWatchers.queryByConnectId(connectId);
        return subMap != null && !subMap.isEmpty();
    }

    private void fireRenewDatum(Channel channel) {
        executorManager.getConnectClientExecutor().execute(() -> {
            String connectId = NetUtil.toAddressString(channel.getRemoteAddress()) + ValueConstants.CONNECT_ID_SPLIT
                    + NetUtil.toAddressString(channel.getLocalAddress());
            RENEW_LOGGER.info("Renew task is started: {}", connectId);
            recycleAsyncHashedWheelTimer.newTimeout(timerOut -> sessionRegistry.renewDatum(connectId), randomDelay(),
                    sessionServerConfig.getRenewDatumWheelTaskDelaySec(), TimeUnit.SECONDS, () -> {
                        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
                        Channel channelClient = sessionServer.getChannel(URL.valueOf(connectId));
                        boolean shouldContinue = channelClient != null && channel.isConnected();
                        if (!shouldContinue) {
                            RENEW_LOGGER.info("Renew task is stop: {}", connectId);
                        }
                        return shouldContinue;
                    });
        });
    }

    private long randomDelay() {
        return sessionServerConfig.getRenewDatumWheelTaskRandomFirstDelaySec()
               / 2
               + RandomUtils.nextInt(sessionServerConfig
                   .getRenewDatumWheelTaskRandomFirstDelaySec() / 2);
    }

}