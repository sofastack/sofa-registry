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
package com.alipay.sofa.registry.remoting.bolt.exchange;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltClient;
import com.alipay.sofa.registry.remoting.bolt.BoltServer;
import com.alipay.sofa.registry.remoting.exchange.Exchange;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: BoltExchange.java, v 0.1 2017-11-27 15:47 shangyu.wh Exp $
 */
public class BoltExchange implements Exchange<ChannelHandler> {

    private Map<String, Client>                clients   = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, Server> serverMap = new ConcurrentHashMap<>();

    @Override
    public Client connect(String serverType, URL serverUrl, ChannelHandler... channelHandlers) {

        if (channelHandlers == null) {
            throw new IllegalArgumentException("channelHandlers cannot be null!");
        }
        Client client = clients.get(serverType);
        if (client == null) {
            BoltClient boltClient = new BoltClient();
            boltClient.setChannelHandlers(Arrays.asList(channelHandlers));
            boltClient.connect(serverUrl);
            client = clients.putIfAbsent(serverType, boltClient);
            if (client == null) {
                client = boltClient;
            }
        } else {
            Channel channel = client.getChannel(serverUrl);
            if (channel == null) {
                BoltClient boltClient = (BoltClient) client;
                boltClient.setChannelHandlers(Arrays.asList(channelHandlers));
                boltClient.connect(serverUrl);
            }
        }
        return client;
    }

    @Override
    public Server open(URL url, ChannelHandler... channelHandlers) {

        if (channelHandlers == null) {
            throw new IllegalArgumentException("channelHandlers cannot be null!");
        }

        BoltServer server = new BoltServer(url, Arrays.asList(channelHandlers));
        setServer(server, url);
        server.startServer();
        return server;
    }

    @Override
    public Client getClient(String serverType) {
        return clients.get(serverType);
    }

    @Override
    public Server getServer(Integer port) {
        return serverMap.get(port);
    }

    /**
     * add server into serverMap
     * @param server
     * @param url
     */
    public void setServer(Server server, URL url) {
        serverMap.putIfAbsent(url.getPort(), server);
    }
}