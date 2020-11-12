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
package com.alipay.sofa.registry.server.meta.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.ws.rs.NotSupportedException;

import com.alipay.sofa.registry.common.model.metaserver.*;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.SessionNodeService;
import com.alipay.sofa.registry.store.api.DBService;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.node.NodeOperator;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.server.meta.repository.VersionRepositoryService;
import com.alipay.sofa.registry.server.meta.task.Constant;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 * @author shangyu.wh
 * @version $Id: SessionStoreService.java, v 0.1 2018-01-12 14:14 shangyu.wh Exp $
 */
public class SessionStoreService implements StoreService<SessionNode> {

    private static final Logger                                   LOGGER        = LoggerFactory
                                                                                    .getLogger(SessionStoreService.class);

    private static final Logger                                   TASK_LOGGER   = LoggerFactory
                                                                                    .getLogger(
                                                                                        SessionStoreService.class,
                                                                                        "[Task]");
    private final ReentrantReadWriteLock                          readWriteLock = new ReentrantReadWriteLock();
    private final Lock                                            read          = readWriteLock
                                                                                    .readLock();
    private final Lock                                            write         = readWriteLock
                                                                                    .writeLock();
    @Autowired
    private TaskListenerManager                                   taskListenerManager;
    @Autowired
    private StoreService                                          dataStoreService;
    @Autowired
    private NodeConfig                                            nodeConfig;
    @Autowired
    private MetaServerConfig                                      metaServerConfig;

    @RaftReference(uniqueId = "sessionServer")
    private RepositoryService<String, RenewDecorate<SessionNode>> sessionRepositoryService;

    @RaftReference(uniqueId = "sessionServer")
    private VersionRepositoryService<String>                      sessionVersionRepositoryService;

    @RaftReference
    private DBService                                             dbService;

    @Override
    public NodeType getNodeType() {
        return NodeType.SESSION;
    }

    @Override
    public NodeChangeResult setNodes(List<SessionNode> nodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeChangeResult addNode(SessionNode sessionNode) {

        write.lock();
        try {
            String ipAddress = sessionNode.getNodeUrl().getIpAddress();

            sessionRepositoryService.put(ipAddress, new RenewDecorate(sessionNode,
                RenewDecorate.DEFAULT_DURATION_SECS));

            sessionVersionRepositoryService.checkAndUpdateVersions(nodeConfig.getLocalDataCenter(),
                System.currentTimeMillis());

            renew(sessionNode, 30);
        } finally {
            write.unlock();
        }

        return dataStoreService.getNodeChangeResult();
    }

    @Override
    public boolean removeNode(String ipAddress) {

        write.lock();
        try {
            RenewDecorate<SessionNode> oldRenewDecorate = sessionRepositoryService
                .remove(ipAddress);
            if (oldRenewDecorate == null) {
                LOGGER
                    .info("Remove Session node with ipAddress:" + ipAddress + " has not existed!");
                return false;
            }

            sessionVersionRepositoryService.checkAndUpdateVersions(nodeConfig.getLocalDataCenter(),
                System.currentTimeMillis());
        } finally {
            write.unlock();
        }

        return true;
    }

    @Override
    public void removeNodes(Collection<SessionNode> nodes) {

        if (nodes != null && !nodes.isEmpty()) {
            write.lock();
            try {
                for (Node node : nodes) {
                    String ipAddress = node.getNodeUrl().getIpAddress();
                    RenewDecorate<SessionNode> oldRenewDecorate = sessionRepositoryService
                        .remove(ipAddress);
                    if (oldRenewDecorate == null) {
                        LOGGER.warn("Remove session nodes with ipAddress:" + ipAddress
                                    + " has not existed!");
                        continue;
                    }

                    sessionVersionRepositoryService.checkAndUpdateVersions(
                        nodeConfig.getLocalDataCenter(), System.currentTimeMillis());
                }
            } finally {
                write.unlock();
            }
        }
    }

    @Override
    public void renew(SessionNode sessionNode, int duration) {

        write.lock();
        try {
            String ipAddress = sessionNode.getNodeUrl().getIpAddress();
            RenewDecorate renewer = sessionRepositoryService.get(ipAddress);

            if (renewer == null) {
                LOGGER.warn("Renew session node with ipAddress:" + ipAddress
                            + " has not existed!It will be registered again!");
                addNode(sessionNode);
            } else {
                if (duration > 0) {
                    sessionRepositoryService.replace(ipAddress, new RenewDecorate(sessionNode,
                        duration));
                } else {
                    sessionRepositoryService.replace(ipAddress, new RenewDecorate(sessionNode,
                        RenewDecorate.DEFAULT_DURATION_SECS));
                }
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public Collection<SessionNode> getExpired() {
        Collection<SessionNode> renewerList = new ArrayList<>();
        read.lock();
        try {
            Map<String, RenewDecorate<SessionNode>> map = sessionRepositoryService.getAllData();
            map.forEach((key, value) -> {
                if (value.isExpired()) {
                    renewerList.add(value.getRenewal());
                }
            });
        } finally {
            read.unlock();
        }
        return renewerList;
    }

    @Override
    public Map<String, SessionNode> getNodes() {
        Map<String, SessionNode> tmpMap = new HashMap<>();
        read.lock();
        try {
            Map<String, RenewDecorate<SessionNode>> map = sessionRepositoryService.getAllData();
            map.forEach((key, value) -> tmpMap.put(key, value.getRenewal()));
        } finally {
            read.unlock();
        }

        return tmpMap;
    }

    @Override
    public NodeChangeResult getNodeChangeResult() {

        NodeChangeResult nodeChangeResult = new NodeChangeResult(NodeType.SESSION);

        //one session node cluster
        String localDataCenter = nodeConfig.getLocalDataCenter();
        nodeChangeResult.setLocalDataCenter(localDataCenter);

        Map<String/*dataCenter*/, Map<String /*ipAddress*/, Node>> nodes = new HashMap<>();
        read.lock();
        try {
            Map<String, Node> tmpMap = new HashMap<>();
            Map<String, RenewDecorate<SessionNode>> map = sessionRepositoryService.getAllData();
            map.forEach((key, value) -> tmpMap.put(key, value.getRenewal()));
            nodes.put(localDataCenter, tmpMap);

            nodeChangeResult.setNodes(nodes);
            nodeChangeResult.setVersion(
                    sessionVersionRepositoryService.getVersion(nodeConfig.getLocalDataCenter()));
        } finally {
            read.unlock();
        }
        return nodeChangeResult;
    }

    @Override
    public void getOtherDataCenterNodeAndUpdate() {
        throw new UnsupportedOperationException("Node type SESSION not support function");
    }

    @Override
    public DataCenterNodes getDataCenterNodes() {
        Long version = sessionVersionRepositoryService.getVersion(nodeConfig.getLocalDataCenter());
        DataCenterNodes dataCenterNodes = new DataCenterNodes(NodeType.SESSION, version,
            nodeConfig.getLocalDataCenter());
        dataCenterNodes.setNodes(getNodes());
        return dataCenterNodes;
    }

    @Override
    public void updateOtherDataCenterNodes(DataCenterNodes dataCenterNodes) {
        throw new UnsupportedOperationException("Node type SESSION not support function");
    }

    private Map<String, Integer> zoneMaxConnections(Map<String, LoadbalanceMetrics> nodes, int maxDisconnect) {
        double thresholdRatio = metaServerConfig.getSessionLoadbalanceThresholdRatio();
        Map<String, Integer> maxConnectionsMap = new HashMap<>();
        if (nodes.size() == 0) {
            return maxConnectionsMap;
        }
        double avg = nodes.values().stream().mapToInt(LoadbalanceMetrics::getConnectionCount)
                .average().getAsDouble();
        for (String ipAddress : nodes.keySet()) {
            LoadbalanceMetrics m = nodes.get(ipAddress);
            int maxConnections = (int) Math
                    .ceil(Math.max(avg * thresholdRatio, m.getConnectionCount() - maxDisconnect));
            maxConnectionsMap.put(ipAddress, maxConnections);
        }
        return maxConnectionsMap;
    }

    public Map<String /*category*/, Map<String /*zone*/, Map<String /*address*/, Integer /*connections*/>>> sessionLoadbalance(int maxDisconnect) {
        Map<String, Map<String, Map<String, Integer>>> result = new HashMap<>();
        Map<String, Map<String, Integer>> sourceConnections = new HashMap<>();

        SessionNodeService sessionNodeService = (SessionNodeService) ServiceFactory
                .getNodeService(NodeType.SESSION);
        Map<String /* zone */, Map<String /*ipAddress*/, LoadbalanceMetrics>> zones = sessionNodeService
                .fetchLoadbalanceMetrics();
        Map<String, Map<String, Integer>> zonesMaxConnectionsMap = new HashMap<>();
        for (String zoneName : zones.keySet()) {
            Map<String, LoadbalanceMetrics> nodes = zones.get(zoneName);

            Map<String, Integer> maxConnectionsMap = zoneMaxConnections(nodes, maxDisconnect);
            zonesMaxConnectionsMap.put(zoneName, maxConnectionsMap);

            sourceConnections.put(zoneName, nodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getConnectionCount())));
        }
        sessionNodeService.configureLoadbalance(zonesMaxConnectionsMap);
        result.put("source", sourceConnections);
        result.put("target", zonesMaxConnectionsMap);
        return result;
    }

    /**
     * Setter method for property <tt>taskListenerManager</tt>.
     *
     * @param taskListenerManager value to be assigned to property taskListenerManager
     */
    public void setTaskListenerManager(TaskListenerManager taskListenerManager) {
        this.taskListenerManager = taskListenerManager;
    }

    /**
     * Setter method for property <tt>dataStoreService</tt>.
     *
     * @param dataStoreService value to be assigned to property dataStoreService
     */
    public void setDataStoreService(StoreService dataStoreService) {
        this.dataStoreService = dataStoreService;
    }

    /**
     * Setter method for property <tt>nodeConfig</tt>.
     *
     * @param nodeConfig value to be assigned to property nodeConfig
     */
    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    /**
     * Setter method for property <tt>sessionRepositoryService</tt>.
     *
     * @param sessionRepositoryService value to be assigned to property sessionRepositoryService
     */
    public void setSessionRepositoryService(RepositoryService<String, RenewDecorate<SessionNode>> sessionRepositoryService) {
        this.sessionRepositoryService = sessionRepositoryService;
    }

    /**
     * Setter method for property <tt>sessionVersionRepositoryService</tt>.
     *
     * @param sessionVersionRepositoryService value to be assigned to property sessionVersionRepositoryService
     */
    public void setSessionVersionRepositoryService(VersionRepositoryService<String> sessionVersionRepositoryService) {
        this.sessionVersionRepositoryService = sessionVersionRepositoryService;
    }
}