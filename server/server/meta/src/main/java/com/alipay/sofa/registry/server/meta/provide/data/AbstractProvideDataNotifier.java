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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
public abstract class AbstractProvideDataNotifier<T extends Node> implements ProvideDataNotifier {

    protected final Logger logger    = LoggerFactory.getLogger(getClass(),
                                         String.format("[%s]", getClass().getSimpleName()));

    private Executor       executors = DefaultExecutorFactory.createCachedThreadPoolFactory(
                                         getClass().getSimpleName(),
                                         Math.min(4, OsUtils.getCpuCount()), 60 * 1000,
                                         TimeUnit.MILLISECONDS).create();

    @Override
    public void notifyProvideDataChange(ProvideDataChangeEvent event) {
        NodeConnectManager nodeConnectManager = getNodeConnectManager();
        Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

        if (connections == null || connections.isEmpty()) {
            logger.error("Push Node list error! No node connected!");
            return;
        }

        List<T> nodes = getNodes();

        if (nodes == null || nodes.isEmpty()) {
            logger.error("Push Node list error! No node registered!");
            return;
        }
        Set<String> dataIpAddresses = Sets.newHashSet();
        nodes.forEach(dataNode -> dataIpAddresses.add(dataNode.getNodeUrl().getIpAddress()));
        executors.execute(new Runnable() {
            @Override
            public void run() {
                for (InetSocketAddress connection : connections) {
                    if (!dataIpAddresses.contains(connection.getAddress().getHostAddress())) {
                        continue;
                    }

                    try {
                        getNodeExchanger().request(new ProvideDataNotification(event, connection));
                    } catch (RequestException e) {
                        throw new SofaRegistryRuntimeException("Notify provide data change to node error: "
                                + e.getMessage(), e);
                    }
                }
            }
        });

    }

    @VisibleForTesting
    AbstractProvideDataNotifier<T> setExecutors(Executor executors) {
        this.executors = executors;
        return this;
    }

    protected abstract NodeExchanger getNodeExchanger();

    protected abstract List<T> getNodes();

    protected abstract NodeConnectManager getNodeConnectManager();

    public class ProvideDataNotification implements Request<ProvideDataChangeEvent> {

        private final ProvideDataChangeEvent event;

        private final InetSocketAddress      connection;

        public ProvideDataNotification(ProvideDataChangeEvent event, InetSocketAddress connection) {
            this.event = event;
            this.connection = connection;
        }

        @Override
        public ProvideDataChangeEvent getRequestBody() {
            return event;
        }

        @Override
        public URL getRequestUrl() {
            return new URL(connection);
        }

        @Override
        public CallbackHandler getCallBackHandler() {
            return new CallbackHandler() {
                @Override
                public void onCallback(Channel channel, Object message) {
                    if (logger.isInfoEnabled()) {
                        logger
                            .info("[success] provide data notification({}): {}", channel, message);
                    }
                }

                @Override
                public void onException(Channel channel, Throwable exception) {
                    logger
                        .error("[onException] provide data notification err ({})",
                            channel != null ? channel.getRemoteAddress() : "unknown channel",
                            exception);
                }

                @Override
                public Executor getExecutor() {
                    return executors;
                }
            };
        }
    }
}
