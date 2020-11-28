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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
@Component
public class DefaultMetaServerManager {

    @Autowired
    private CrossDcMetaServerManager crossDcMetaServerManager;

    @Autowired
    private CurrentDcMetaServer      currentDcMetaServer;

    @Autowired
    private SessionManager           sessionManager;

    @Autowired
    private DataServerManager        dataServerManager;

    @Autowired
    private NodeConfig               nodeConfig;

    public <T extends Node> NodeChangeResult<T> getSummary(Node.NodeType type) {
        switch (type) {
            case META:
                return (NodeChangeResult<T>) getMetaServerLists();
            case DATA:
                return getDataServerLists();
            case SESSION:
                return getSessionServerLists();
            default:
                break;
        }
        return null;
    }

    private NodeChangeResult getMetaServerLists() {
        NodeChangeResult<MetaNode> result = new NodeChangeResult<>(Node.NodeType.META);
        result.setLocalDataCenter(nodeConfig.getLocalDataCenter());
        Map<String, Map<String, MetaNode>> nodeMap = Maps.newHashMap();
        Map<String, Long> epochMap = Maps.newHashMap();
<<<<<<< HEAD
        for(String dcName : nodeConfig.getMetaNodeIP().keySet()) {
            List<MetaNode> metaNodeList = crossDcMetaServerManager.getOrCreate(dcName).getClusterMembers();
=======
        for (String dcName : nodeConfig.getMetaNodeIP().keySet()) {
            List<MetaNode> metaNodeList = crossDcMetaServerManager.getOrCreate(dcName)
                .getClusterMembers();
            Map<String, MetaNode> map = Maps.newHashMap();
>>>>>>> origin/feat/datastore
            nodeMap.put(dcName, transform(metaNodeList));
            epochMap.put(dcName, crossDcMetaServerManager.getOrCreate(dcName).getEpoch());
        }
        nodeMap.put(nodeConfig.getLocalDataCenter(),
            transform(currentDcMetaServer.getClusterMembers()));
        epochMap.put(nodeConfig.getLocalDataCenter(), currentDcMetaServer.getEpoch());
        result.setNodes(nodeMap);
        result.setDataCenterListVersions(epochMap);

        result.setVersion(currentDcMetaServer.getEpoch());
        return result;
    }

    private NodeChangeResult getSessionServerLists() {
        NodeChangeResult<SessionNode> result = new NodeChangeResult<>(Node.NodeType.SESSION);
        result.setLocalDataCenter(nodeConfig.getLocalDataCenter());
        Map<String, Map<String, SessionNode>> nodeMap = Maps.newHashMap();
        Map<String, Long> epochMap = Maps.newHashMap();
        nodeMap.put(nodeConfig.getLocalDataCenter(),
            transform(currentDcMetaServer.getSessionServers()));
        result.setNodes(nodeMap);
        result.setVersion(sessionManager.getEpoch());
        result.setDataCenterListVersions(epochMap);
        return result;
    }

    private NodeChangeResult getDataServerLists() {
        NodeChangeResult<DataNode> result = new NodeChangeResult<>(Node.NodeType.DATA);
        result.setLocalDataCenter(nodeConfig.getLocalDataCenter());
        Map<String, Map<String, DataNode>> nodeMap = Maps.newHashMap();
        Map<String, Long> epochMap = Maps.newHashMap();
        nodeMap.put(nodeConfig.getLocalDataCenter(),
            transform(dataServerManager.getClusterMembers()));
        result.setNodes(nodeMap);
        result.setVersion(dataServerManager.getEpoch());
        result.setDataCenterListVersions(epochMap);
        return result;
    }

    private <T extends Node> Map<String, T> transform(List<T> nodes) {
        Map<String, T> map = Maps.newHashMap();
        for (T node : nodes) {
            map.put(node.getNodeUrl().getIpAddress(), node);
        }
        return map;
    }

    @VisibleForTesting
    DefaultMetaServerManager setCrossDcMetaServerManager(CrossDcMetaServerManager crossDcMetaServerManager) {
        this.crossDcMetaServerManager = crossDcMetaServerManager;
        return this;
    }

    @VisibleForTesting
    DefaultMetaServerManager setCurrentDcMetaServer(CurrentDcMetaServer currentDcMetaServer) {
        this.currentDcMetaServer = currentDcMetaServer;
        return this;
    }

    @VisibleForTesting
    DefaultMetaServerManager setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    @VisibleForTesting
    DefaultMetaServerManager setDataServerManager(DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }

    @VisibleForTesting
    DefaultMetaServerManager setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        return this;
    }
}
