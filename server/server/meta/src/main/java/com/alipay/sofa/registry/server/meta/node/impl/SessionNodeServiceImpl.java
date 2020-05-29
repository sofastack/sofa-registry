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
package com.alipay.sofa.registry.server.meta.node.impl;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alipay.sofa.registry.common.model.metaserver.*;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.SessionNodeService;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.meta.store.StoreService;

/**
 * @author shangyu.wh
 * @version $Id: SessionNodeServiceImpl.java, v 0.1 2018-01-15 17:18 shangyu.wh Exp $
 */
public class SessionNodeServiceImpl implements SessionNodeService {

    private static final Logger   LOGGER = LoggerFactory.getLogger(SessionNodeServiceImpl.class);

    @Autowired
    private NodeExchanger         sessionNodeExchanger;

    @Autowired
    private StoreService          sessionStoreService;

    @Autowired
    private MetaServerConfig      metaServerConfig;

    @Autowired
    private AbstractServerHandler sessionConnectionHandler;

    @Override
    public NodeType getNodeType() {
        return NodeType.SESSION;
    }

    @Override
    public void pushSessions(NodeChangeResult nodeChangeResult,
                             Map<String, SessionNode> sessionNodes, String confirmNodeIp) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SessionNodeServiceImpl pushSessions sessionNodes:{}", nodeChangeResult);
        }
        NodeConnectManager nodeConnectManager = getNodeConnectManager();
        Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

        if (connections.size() == 0) {
            LOGGER.warn("there are no client connected on session server port:{}",
                metaServerConfig.getSessionServerPort());
        }

        if (sessionNodes == null || sessionNodes.isEmpty()) {
            LOGGER.error("Push sessionNode list error! Input sessionNodes can't be null!");
            throw new RuntimeException(
                "Push sessionNode list error! Input sessionNodes can't be null!");
        }

        for (InetSocketAddress connection : connections) {

            if (!sessionNodes.keySet().contains(connection.getAddress().getHostAddress())) {
                continue;
            }

            try {
                Request<NodeChangeResult> nodeChangeRequest = new Request<NodeChangeResult>() {

                    @Override
                    public NodeChangeResult getRequestBody() {
                        return nodeChangeResult;
                    }

                    @Override
                    //all connect session
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };

                sessionNodeExchanger.request(nodeChangeRequest);

                //no error confirm receive
                sessionStoreService.confirmNodeStatus(connection.getAddress().getHostAddress(),
                    confirmNodeIp);

            } catch (RequestException e) {
                throw new RuntimeException("Push sessionNode list error: " + e.getMessage(), e);
            }
        }

    }

    @Override
    public void pushDataNodes(NodeChangeResult nodeChangeResult) {

        NodeConnectManager nodeConnectManager = getNodeConnectManager();
        Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

        if (connections == null || connections.isEmpty()) {
            LOGGER.error("Push sessionNode list error! No session node connected!");
            throw new RuntimeException("Push sessionNode list error! No session node connected!");
        }

        // add register confirm
        StoreService storeService = ServiceFactory.getStoreService(NodeType.SESSION);
        Map<String, SessionNode> sessionNodes = storeService.getNodes();

        if (sessionNodes == null || sessionNodes.isEmpty()) {
            LOGGER.error("Push sessionNode list error! No session node registered!");
            throw new RuntimeException("Push sessionNode list error! No session node registered!");
        }

        for (InetSocketAddress connection : connections) {

            if (!sessionNodes.keySet().contains(connection.getAddress().getHostAddress())) {
                continue;
            }

            try {
                Request<NodeChangeResult> nodeChangeRequestRequest = new Request<NodeChangeResult>() {

                    @Override
                    public NodeChangeResult getRequestBody() {
                        return nodeChangeResult;
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };

                sessionNodeExchanger.request(nodeChangeRequestRequest);

            } catch (RequestException e) {
                throw new RuntimeException("Push sessionNode list error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void notifyProvideDataChange(NotifyProvideDataChange notifyProvideDataChange) {

        NodeConnectManager nodeConnectManager = getNodeConnectManager();
        Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

        if (connections == null || connections.isEmpty()) {
            LOGGER.error("Push sessionNode list error! No session node connected!");
            throw new RuntimeException("Push sessionNode list error! No session node connected!");
        }

        // add register confirm
        StoreService storeService = ServiceFactory.getStoreService(NodeType.SESSION);
        Map<String, SessionNode> sessionNodes = storeService.getNodes();

        if (sessionNodes == null || sessionNodes.isEmpty()) {
            LOGGER.error("Push sessionNode list error! No session node registered!");
            throw new RuntimeException("Push sessionNode list error! No session node registered!");
        }

        for (InetSocketAddress connection : connections) {

            if (!sessionNodes.keySet().contains(connection.getAddress().getHostAddress())) {
                continue;
            }

            try {
                Request<NotifyProvideDataChange> request = new Request<NotifyProvideDataChange>() {

                    @Override
                    public NotifyProvideDataChange getRequestBody() {
                        return notifyProvideDataChange;
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };

                sessionNodeExchanger.request(request);

            } catch (RequestException e) {
                throw new RuntimeException("Notify provide data change error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Map<String, Map<String, LoadbalanceMetrics>> fetchLoadbalanceMetrics() {
        Map<String, SessionNode> sessionNodes = ServiceFactory.getStoreService(NodeType.SESSION)
            .getNodes();
        Map<String, InetSocketAddress> connectionsMap = getNodeConnectManager().getConnections(null)
            .stream()
            .collect(Collectors.toMap(c -> c.getAddress().getHostAddress(), Function.identity()));
        Map<String, Map<String, LoadbalanceMetrics>> zones = new HashMap<>();
        Set<String> failedZones = new HashSet<>();
        for (Map.Entry<String, SessionNode> entry : sessionNodes.entrySet()) {
            String ipAddress = entry.getKey();
            SessionNode node = entry.getValue();
            InetSocketAddress connection = connectionsMap.get(ipAddress);
            if (connection == null) {
                failedZones.add(node.getRegionId());
                LOGGER.warn("node {} has no connection, skip fetch loadbalance metrics", node);
                continue;
            }
            Map<String, LoadbalanceMetrics> zone = zones.computeIfAbsent(node.getRegionId(),
                k -> new HashMap<>());
            try {
                Request<GetLoadbalanceMetricsRequest> metricsRequest = new Request<GetLoadbalanceMetricsRequest>() {
                    @Override
                    public GetLoadbalanceMetricsRequest getRequestBody() {
                        return new GetLoadbalanceMetricsRequest();
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };
                Response response = sessionNodeExchanger.request(metricsRequest);
                LoadbalanceMetrics metrics = (LoadbalanceMetrics) response.getResult();
                zone.put(ipAddress, metrics);
            } catch (RequestException e) {
                LOGGER.warn("node {} failed to fetch load balance metrics: {}", node,
                    e.getMessage());
                failedZones.add(node.getRegionId());
            }
        }
        for (String failedZone : failedZones) {
            zones.remove(failedZone);
        }
        return zones;
    }

    @Override
    public void configureLoadbalance(Map<String, Map<String, Integer>> zonesMaxConnectionsMap) {
        Map<String, InetSocketAddress> connectionsMap = getNodeConnectManager().getConnections(null)
            .stream()
            .collect(Collectors.toMap(c -> c.getAddress().getHostAddress(), Function.identity()));

        for (String zoneName : zonesMaxConnectionsMap.keySet()) {
            Map<String, Integer> maxConnectionsMap = zonesMaxConnectionsMap.get(zoneName);
            for (String ipAddress : maxConnectionsMap.keySet()) {
                InetSocketAddress connection = connectionsMap.get(ipAddress);
                if (connection == null) {
                    continue;
                }
                Request<ConfigureLoadbalanceRequest> request = new Request<ConfigureLoadbalanceRequest>() {
                    @Override
                    public ConfigureLoadbalanceRequest getRequestBody() {
                        return new ConfigureLoadbalanceRequest(maxConnectionsMap.get(ipAddress));
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };
                try {
                    sessionNodeExchanger.request(request);
                } catch (RequestException e) {
                    LOGGER.warn("configure node {} load balance failed: {}", connection,
                        e.getMessage());
                }
            }
        }
    }

    private NodeConnectManager getNodeConnectManager() {
        if (!(sessionConnectionHandler instanceof NodeConnectManager)) {
            LOGGER.error("sessionConnectionHandler inject is not NodeConnectManager instance!");
            throw new RuntimeException(
                "sessionConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) sessionConnectionHandler;
    }

}