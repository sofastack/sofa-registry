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
package com.alipay.sofa.registry.server.session.connections;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.resource.ConnectionsResource;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsResource.class);

    @Autowired
    private Exchange            boltExchange;

    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private Watchers            sessionWatchers;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    public List<String> getConnections() {
        Server server = boltExchange.getServer(sessionServerConfig.getServerPort());
        Set<String> boltConnectIds = server.getChannels().stream().map(
                channel -> channel.getRemoteAddress().getAddress().getHostAddress() + ":" + channel.getRemoteAddress().getPort()
        ).collect(Collectors.toSet());
        Set<String> connectIds = new HashSet<>();
        connectIds.addAll(sessionDataStore.getConnectPublishers().keySet().stream().map(connectId -> connectId.clientAddress()).collect(Collectors.toList()));
        connectIds.addAll(sessionInterests.getConnectSubscribers().keySet().stream().map(connectId -> connectId.clientAddress()).collect(Collectors.toList()));
        connectIds.addAll(sessionWatchers.getConnectWatchers().keySet().stream().map(connectId -> connectId.clientAddress()).collect(Collectors.toList()));
        connectIds.retainAll(boltConnectIds);
        return new ArrayList<>(connectIds);
    }

    public void setMaxConnections(int connections) {
        List<String> connectionIds = getConnections();

        int needDropped = connectionIds.size() - connections;
        if (needDropped <= 0) {
            return;
        }
        Collections.shuffle(connectionIds);
        connectionIds = connectionIds.subList(0, needDropped);
        for (String ipAddress : connectionIds) {
            Server server = boltExchange.getServer(sessionServerConfig.getServerPort());
            String[] parts = ipAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            InetSocketAddress address = new InetSocketAddress(host, port);
            Channel connection = server.getChannel(address);
            if (connection != null && connection.isConnected()) {
                LOGGER.info("close connection {}", ipAddress);
                server.close(connection);
            }
        }
    }
}
