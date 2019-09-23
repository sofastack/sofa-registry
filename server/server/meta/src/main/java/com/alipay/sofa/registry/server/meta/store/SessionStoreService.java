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

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.node.NodeOperator;
import com.alipay.sofa.registry.server.meta.repository.NodeConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.server.meta.repository.VersionRepositoryService;
import com.alipay.sofa.registry.server.meta.task.Constant;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
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

    @RaftReference(uniqueId = "sessionServer")
    private RepositoryService<String, RenewDecorate<SessionNode>> sessionRepositoryService;

    @RaftReference(uniqueId = "sessionServer")
    private VersionRepositoryService<String>                      sessionVersionRepositoryService;

    @RaftReference(uniqueId = "sessionServer")
    private NodeConfirmStatusService<SessionNode>                 sessionConfirmStatusService;

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

            sessionConfirmStatusService.putConfirmNode(sessionNode, DataOperator.ADD);

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

            sessionConfirmStatusService.putConfirmNode(oldRenewDecorate.getRenewal(),
                DataOperator.REMOVE);
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

                    sessionConfirmStatusService.putConfirmNode(oldRenewDecorate.getRenewal(),
                        DataOperator.REMOVE);

                    //confirmNodeStatus(ipAddress, DataOperator.REMOVE);
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
    public void pushNodeListChange() {
        NodeOperator<SessionNode> fireNode;
        if ((fireNode = sessionConfirmStatusService.peekConfirmNode()) != null) {
            //if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Now:type {},node {},Push queue:{}", fireNode.getNodeOperate(), fireNode
                .getNode().getNodeUrl().getIpAddress(),
                sessionConfirmStatusService.getAllConfirmNodes());
            //}
            NodeChangeResult nodeChangeResult = getNodeChangeResult();
            Map<String, Map<String, SessionNode>> map = nodeChangeResult.getNodes();
            Map<String, SessionNode> addNodes = map.get(nodeConfig.getLocalDataCenter());
            if (addNodes != null) {
                LOGGER.info("addNodes:{}", addNodes.keySet());
                Map<String, SessionNode> previousNodes = sessionConfirmStatusService
                    .putExpectNodes(fireNode.getNode(), addNodes);

                if (!previousNodes.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("push Type:{},peek:{},list{}", fireNode.getNodeOperate(),
                            fireNode.getNode().getNodeUrl().getIpAddress(), previousNodes.keySet());
                    }
                    firePushSessionListTask(fireNode, previousNodes, nodeChangeResult);
                }
            }
        }
    }

    @Override
    public void confirmNodeStatus(String ipAddress, String confirmNodeIp) {
        NodeOperator<SessionNode> fireNode = sessionConfirmStatusService.peekConfirmNode();
        if (fireNode != null) {
            String fireNodeIp = fireNode.getNode().getNodeUrl().getIpAddress();
            if (fireNodeIp != null && !fireNodeIp.equals(confirmNodeIp)) {
                LOGGER
                    .info(
                        "Confirm node already be remove from queue!Receive ip:{},expect confirm ip:{},now peek ip:{}",
                        ipAddress, confirmNodeIp, fireNodeIp);
                return;
            }
            Map<String/*ipAddress*/, SessionNode> waitNotifyNodes = sessionConfirmStatusService
                .getExpectNodes(fireNode.getNode());
            if (waitNotifyNodes != null) {
                LOGGER.info("Peek node:{} oper:{},waitNotifyNodes:{},confirm ip:{}", fireNode
                    .getNode().getNodeUrl().getIpAddress(), fireNode.getNodeOperate(),
                    waitNotifyNodes.keySet(), ipAddress);

                Set<String> removeIp = getRemoveIp(waitNotifyNodes.keySet());
                removeIp.add(ipAddress);

                waitNotifyNodes = sessionConfirmStatusService.removeExpectConfirmNodes(
                    fireNode.getNode(), removeIp);

                if (waitNotifyNodes.isEmpty()) {
                    //all node be notified,or some disconnect node be evict
                    try {
                        if (null != sessionConfirmStatusService
                            .removeExpectNodes(sessionConfirmStatusService.pollConfirmNode()
                                .getNode())) {
                            //add init status must notify
                            LOGGER.info("Session node {} operator {} confirm!", fireNode.getNode()
                                .getNodeUrl().getIpAddress(), fireNode.getNodeOperate());
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Notify expect confirm status node " + fireNode.getNode()
                                     + " interrupted!", e);
                    }
                }
            } else {
                try {
                    //wait node not exist,
                    sessionConfirmStatusService.pollConfirmNode();
                    LOGGER
                        .info(
                            "Session node {} operator {} poll!not other node need be notify!Confirm ip {}",
                            fireNode.getNode().getNodeUrl().getIpAddress(),
                            fireNode.getNodeOperate(), ipAddress);
                } catch (InterruptedException e) {
                    LOGGER.error("Notify expect confirm status node " + fireNode.getNode()
                                 + " interrupted!", e);
                }
            }
        }

    }

    private Set<String> getRemoveIp(Set<String> waitNotifyNodes) {

        NodeChangeResult nodeChangeResult = getNodeChangeResult();
        Map<String, Map<String, SessionNode>> map = nodeChangeResult.getNodes();
        Map<String, SessionNode> addNodes = map.get(nodeConfig.getLocalDataCenter());
        if (addNodes != null && !addNodes.isEmpty()) {
            return waitNotifyNodes.stream().filter(ip -> !addNodes.keySet().contains(ip)).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private void firePushSessionListTask(NodeOperator<SessionNode> fireNode,
                                         Map<String, SessionNode> sessionNodeMap,
                                         NodeChangeResult nodeChangeResult) {

        //notify target session node registry
        TaskEvent taskEvent = new TaskEvent(TaskType.SESSION_NODE_CHANGE_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_TARGET_OPERATOR_TYPE, fireNode.getNodeOperate());
        taskEvent.setAttribute(Constant.PUSH_TARGET_SESSION_NODE, sessionNodeMap);
        taskEvent.setAttribute(Constant.PUSH_TARGET_CONFIRM_NODE, fireNode.getNode().getNodeUrl()
            .getIpAddress());
        taskEvent.setEventObj(nodeChangeResult);
        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " taskEvent:" + taskEvent);
        taskListenerManager.sendTaskEvent(taskEvent);
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
            nodeChangeResult.setVersion(sessionVersionRepositoryService.getVersion(nodeConfig.getLocalDataCenter()));
        } finally {
            read.unlock();
        }
        return nodeChangeResult;
    }

    @Override
    public void getOtherDataCenterNodeAndUpdate() {

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
        throw new NotSupportedException("Node type SESSION not support function");
    }

    /**
     * Setter method for property <tt>taskListenerManager</tt>.
     *
     * @param taskListenerManager  value to be assigned to property taskListenerManager
     */
    public void setTaskListenerManager(TaskListenerManager taskListenerManager) {
        this.taskListenerManager = taskListenerManager;
    }

    /**
     * Setter method for property <tt>dataStoreService</tt>.
     *
     * @param dataStoreService  value to be assigned to property dataStoreService
     */
    public void setDataStoreService(StoreService dataStoreService) {
        this.dataStoreService = dataStoreService;
    }

    /**
     * Setter method for property <tt>nodeConfig</tt>.
     *
     * @param nodeConfig  value to be assigned to property nodeConfig
     */
    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    /**
     * Setter method for property <tt>sessionRepositoryService</tt>.
     *
     * @param sessionRepositoryService  value to be assigned to property sessionRepositoryService
     */
    public void setSessionRepositoryService(RepositoryService<String, RenewDecorate<SessionNode>> sessionRepositoryService) {
        this.sessionRepositoryService = sessionRepositoryService;
    }

    /**
     * Setter method for property <tt>sessionVersionRepositoryService</tt>.
     *
     * @param sessionVersionRepositoryService  value to be assigned to property sessionVersionRepositoryService
     */
    public void setSessionVersionRepositoryService(VersionRepositoryService<String> sessionVersionRepositoryService) {
        this.sessionVersionRepositoryService = sessionVersionRepositoryService;
    }

    /**
     * Getter method for property <tt>sessionConfirmStatusService</tt>.
     *
     * @return property value of sessionConfirmStatusService
     */
    public NodeConfirmStatusService<SessionNode> getSessionConfirmStatusService() {
        return sessionConfirmStatusService;
    }

    /**
     * Setter method for property <tt>sessionConfirmStatusService</tt>.
     *
     * @param sessionConfirmStatusService  value to be assigned to property sessionConfirmStatusService
     */
    public void setSessionConfirmStatusService(NodeConfirmStatusService<SessionNode> sessionConfirmStatusService) {
        this.sessionConfirmStatusService = sessionConfirmStatusService;
    }
}