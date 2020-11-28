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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.remoting.BoltClient;
import com.alipay.remoting.Connection;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:36 yuzhi.lyz Exp $
 */
public abstract class AbstractMetaNodeExchanger implements NodeExchanger {
    private static final Logger        LOGGER  = LoggerFactory
                                                   .getLogger(AbstractMetaNodeExchanger.class);

    @Resource(name = "metaClientHandlers")
    private Collection<ChannelHandler> metaClientHandlers;

    @Autowired
    private AbstractRaftClientManager  raftClientManager;

    @Autowired
    private Exchange                   boltExchange;

    private volatile Set<String>       metaIps = Sets.newHashSet();

    public void startRaftClient() {
        this.metaIps = Sets.newHashSet(raftClientManager.getConfigMetaIp());
        raftClientManager.startRaftClient();
    }

    @Override
    public Response request(Request request) throws RequestException {
        Client client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
        final int timeout = request.getTimeout() != null ? request.getTimeout() : getRpcTimeout();
        LOGGER.info("MetaNode Exchanger request={},timeout={},url={},callbackHandler={}", request.getRequestBody(),
                timeout, request.getRequestUrl(), request.getCallBackHandler());

        try {
            final Object result = client.sendSync(request.getRequestUrl(), request.getRequestBody(), timeout);
            return () -> result;
        } catch (Throwable e) {
            //retry
            URL url = new URL(raftClientManager.refreshLeader().getIp(), getMetaServerPort());
            LOGGER.warn("MetaNode Exchanger request send error!It will be retry once!Request url:{}", url);

            final Object result = client.sendSync(url, request.getRequestBody(), timeout);
            return () -> result;
        }
    }

    @Override
    public Client connectServer() {
        for (String node : metaIps) {
            URL url = new URL(node, getMetaServerPort());
            try {
                connect(url);
            } catch (Exception e) {
                LOGGER.error("MetaNode Exchanger connect MetaServer error!url:" + url, e);
                continue;
            }
        }
        return boltExchange.getClient(Exchange.META_SERVER_TYPE);
    }

    public Channel connect(URL url) {
        Client client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
        if (client == null) {
            synchronized (this) {
                client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
                if (client == null) {
                    client = boltExchange.connect(Exchange.META_SERVER_TYPE, url,
                        metaClientHandlers.toArray(new ChannelHandler[metaClientHandlers.size()]));
                }
            }
        }
        Channel channel = client.getChannel(url);
        if (channel == null) {
            synchronized (this) {
                channel = client.getChannel(url);
                if (channel == null) {
                    channel = client.connect(url);
                }
            }
        }
        return channel;
    }

    public PeerId getLeader() {
        return raftClientManager.getLeader();
    }

    public String getLocalDataCenter() {
        return raftClientManager.getLocalDataCenter();
    }

    public Map<String, List<Connection>> getConnections() {
        Client client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
        if (client == null) {
            return Collections.emptyMap();
        }
        return ((BoltClient) client).getAllManagedConnections();
    }

    public void updateMetaIps(Collection<String> ips) {
        this.metaIps = Sets.newHashSet(ips);
    }

    public abstract int getMetaServerPort();

    public abstract int getRpcTimeout();
}
