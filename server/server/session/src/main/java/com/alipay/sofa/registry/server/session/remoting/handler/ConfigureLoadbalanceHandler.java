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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.LoadbalanceConfig;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.node.SessionNodeManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiangxu
 * @version : ConfigureLoadbalanceHandler.java, v 0.1 2020年05月29日 11:51 上午 xiangxu Exp $
 */
public class ConfigureLoadbalanceHandler extends AbstractClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalanceConfig.class);

    @Autowired
    private Exchange            boltExchange;

    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.SESSION;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return LoadbalanceConfig.class;
    }

    @Override
    public Object reply(Channel channel, Object message) {
        SessionNodeManager nodeManager = (SessionNodeManager) NodeManagerFactory
            .getNodeManager(Node.NodeType.SESSION);

        Set<String> connectionIds = new HashSet<>();
        connectionIds.addAll(sessionDataStore.getConnectPublishers().keySet());
        connectionIds.addAll(sessionInterests.getConnectSubscribers().keySet());
        int count = connectionIds.size();
        LoadbalanceConfig loadbalanceConfig = (LoadbalanceConfig) message;
        nodeManager.setWeights(loadbalanceConfig.getWeights());
        int needDropped = count - loadbalanceConfig.getMaxConnections();
        if (needDropped <= 0) {
            return null;
        }
        List<String> needDroppedConnections = calcDroppedConnections(connectionIds, needDropped);

        for (String ipAddress : needDroppedConnections) {
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

        return null;
    }

    private List<String> calcDroppedConnections(Set<String> connectionIds, int needDropped) {
        Random rand = new Random();
        List<String> dropped = new LinkedList<>();
        List<Map.Entry<String, List<String>>> appConnections = connectionIds.stream()
            .collect(Collectors.groupingBy(this::connectionIdentify, Collectors.toList()))
            .entrySet().stream().filter(e -> !"".equals(e.getKey()))
            .sorted(Comparator.comparing(e -> e.getValue().size())).collect(Collectors.toList());
        Collections.reverse(appConnections);
        while (needDropped > 0) {
            boolean finished = true;
            for (Map.Entry<String, List<String>> entry : appConnections) {
                String identify = entry.getKey();
                List<String> connections = entry.getValue();
                if (connections.size() > 0) {
                    int index = rand.nextInt(connections.size());
                    String connection = connections.remove(index);
                    LOGGER.info("drop identify {} connection {}", identify, connection);
                    dropped.add(connection);
                    needDropped--;
                    finished = false;
                }
            }
            if (finished) {
                break;
            }
        }
        return dropped;
    }

    private String connectionIdentify(String connectId) {
        Collection<Subscriber> subscribers = null;
        Collection<Publisher> publishers = null;
        if (sessionInterests.getConnectSubscribers().containsKey(connectId)) {
            subscribers = sessionInterests.getConnectSubscribers().get(connectId).values();
        }
        if (sessionDataStore.getConnectPublishers().containsKey(connectId)) {
            publishers = sessionDataStore.getConnectPublishers().get(connectId).values();
        }
        if (subscribers != null && subscribers.size() > 0) {
            // get appName from subscribers
            for (Subscriber subscriber : subscribers) {
                String appName = subscriber.getAppName();
                if (appName != null && !"".equals(appName)) {
                    return appName;
                }
            }
        }
        if (publishers != null && publishers.size() > 0) {
            // get appName from publishers
            for (Publisher publisher : publishers) {
                String appName = publisher.getAppName();
                if (appName != null && !"".equals(appName)) {
                    return appName;
                }
            }

            // get publisher dataInfoId
            List<String> publisherSlice = publishers.stream().map(BaseInfo::getDataInfoId).sorted()
                .limit(1).collect(Collectors.toList());
            if (publisherSlice.size() > 0) {
                return publisherSlice.get(0);
            }
        }

        return "";
    }
}