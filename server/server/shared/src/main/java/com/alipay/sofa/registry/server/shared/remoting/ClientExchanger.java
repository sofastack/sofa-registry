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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.remoting.BoltClient;
import com.alipay.remoting.Connection;
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
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-29 12:08 yuzhi.lyz Exp $
 */
public abstract class ClientExchanger implements NodeExchanger {
    private static final Logger    LOGGER = LoggerFactory.getLogger(ClientExchanger.class);
    private final String           serverType;

    @Autowired
    protected Exchange             boltExchange;

    protected volatile Set<String> serverIps;

    protected ClientExchanger(String serverType) {
        this.serverType = serverType;
    }

    @Override
    public Response request(Request request) throws RequestException {
        Client client = boltExchange.getClient(serverType);
        final int timeout = request.getTimeout() != null ? request.getTimeout() : getRpcTimeout();
        try {
            final Object result = client.sendSync(request.getRequestUrl(), request.getRequestBody(), timeout);
            return () -> result;
        } catch (Throwable e) {
            throw new RequestException(serverType + "Exchanger request error! Request url:" + request.getRequestUrl(),
                    request, e);
        }
    }

    @Override
    public Client connectServer() {
        Set<String> ips = serverIps;
        int count = tryConnectAllServer(ips);
        if (count == 0) {
            throw new RuntimeException("failed to connect any servers, " + ips);
        }
        return getClient();
    }

    protected Client getClient() {
        return boltExchange.getClient(serverType);
    }

    protected int tryConnectAllServer(Set<String> ips) {
        int connectCount = 0;
        for (String node : ips) {
            URL url = new URL(node, getServerPort());
            try {
                connect(url);
                connectCount++;
            } catch (Throwable e) {
                LOGGER.error("Exchanger connect server error!url:" + url, e);
            }
        }
        return connectCount;
    }

    public Channel connect(URL url) {
        Client client = getClient();
        if (client == null) {
            synchronized (this) {
                client = getClient();
                if (client == null) {
                    client = boltExchange.connect(serverType, getConnNum(), url,
                        getClientHandlers().toArray(new ChannelHandler[0]));
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

    public Map<String, List<Connection>> getConnections() {
        Client client = boltExchange.getClient(serverType);
        if (client == null) {
            return Collections.emptyMap();
        }
        return ((BoltClient) client).getAllManagedConnections();
    }

    public abstract int getRpcTimeout();

    public abstract int getServerPort();

    public int getConnNum() {
        return 1;
    }

    protected abstract Collection<ChannelHandler> getClientHandlers();

    public Set<String> getServerIps() {
        return serverIps;
    }

    public void setServerIps(Collection<String> serverIps) {
        this.serverIps = Collections.unmodifiableSet(Sets.newHashSet(serverIps));
    }
}
