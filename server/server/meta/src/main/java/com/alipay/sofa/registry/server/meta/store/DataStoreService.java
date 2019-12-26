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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.GetChangeListRequest;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.MetaNodeService;
import com.alipay.sofa.registry.server.meta.node.NodeOperator;
import com.alipay.sofa.registry.server.meta.repository.NodeConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.NodeRepository;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.server.meta.task.Constant;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataStoreService.java, v 0.1 2018-01-23 11:42 shangyu.wh Exp $
 */
public class DataStoreService implements StoreService<DataNode> {

    private static final Logger                                LOGGER                     = LoggerFactory
                                                                                              .getLogger(DataStoreService.class);

    private static final Logger                                TASK_LOGGER                = LoggerFactory
                                                                                              .getLogger(
                                                                                                  DataStoreService.class,
                                                                                                  "[Task]");
    private final ReentrantReadWriteLock                       readWriteLock              = new ReentrantReadWriteLock();
    private final Lock                                         read                       = readWriteLock
                                                                                              .readLock();
    private final Lock                                         write                      = readWriteLock
                                                                                              .writeLock();
    @Autowired
    private NodeConfig                                         nodeConfig;

    @Autowired
    private TaskListenerManager                                taskListenerManager;

    @RaftReference(uniqueId = "dataServer")
    private RepositoryService<String, RenewDecorate<DataNode>> dataRepositoryService;

    @RaftReference(uniqueId = "dataServer")
    private NodeConfirmStatusService<DataNode>                 dataConfirmStatusService;

    private AtomicLong                                         localDataCenterInitVersion = new AtomicLong(
                                                                                              -1L);

    @Override
    public NodeType getNodeType() {
        return NodeType.DATA;
    }

    @Override
    public NodeChangeResult setNodes(List<DataNode> nodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeChangeResult addNode(DataNode dataNode) {
        NodeChangeResult nodeChangeResult;

        String ipAddress = dataNode.getNodeUrl().getIpAddress();

        write.lock();
        try {

            dataRepositoryService.put(ipAddress, new RenewDecorate(dataNode,
                RenewDecorate.DEFAULT_DURATION_SECS));

            renew(dataNode, 30);

            nodeChangeResult = getNodeChangeResult();

            dataConfirmStatusService.putConfirmNode(dataNode, DataOperator.ADD);

        } finally {
            write.unlock();
        }
        return nodeChangeResult;
    }

    @Override
    public boolean removeNode(String ipAddress) {

        write.lock();
        try {

            RenewDecorate<DataNode> dataNode = dataRepositoryService.remove(ipAddress);
            if (dataNode != null) {

                dataConfirmStatusService.putConfirmNode(dataNode.getRenewal(), DataOperator.REMOVE);

                LOGGER.info("Remove single node {} success!", dataNode.getRenewal());
                return true;
            }
            return false;
        } finally {
            write.unlock();
        }
    }

    @Override
    public void removeNodes(Collection<DataNode> nodes) {
        write.lock();
        try {
            if (nodes != null && !nodes.isEmpty()) {
                for (DataNode dataNode : nodes) {

                    String ipAddress = dataNode.getNodeUrl().getIpAddress();
                    RenewDecorate<DataNode> dataNodeRemove = dataRepositoryService
                        .remove(ipAddress);
                    if (dataNodeRemove != null) {

                        dataConfirmStatusService.putConfirmNode(dataNode, DataOperator.REMOVE);

                        LOGGER.info("Remove node {} success!", dataNodeRemove.getRenewal());
                    }
                }
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public void renew(DataNode dataNode, int duration) {

        write.lock();
        try {
            String ipAddress = dataNode.getNodeUrl().getIpAddress();
            RenewDecorate renewer = dataRepositoryService.get(ipAddress);

            if (renewer == null) {
                LOGGER.warn("Renew Data node with ipAddress:" + ipAddress
                            + " has not existed!It will be registered again!");
                addNode(dataNode);
            } else {
                if (duration > 0) {
                    dataRepositoryService.replace(ipAddress, new RenewDecorate(dataNode, duration));
                } else {
                    dataRepositoryService.replace(ipAddress, new RenewDecorate(dataNode,
                        RenewDecorate.DEFAULT_DURATION_SECS));
                }

            }
        } finally {
            write.unlock();
        }
    }

    /**
     * only get local datacenter's dataNode, don't care other datacenter's dataNodes
     * @return
     */
    @Override
    public Collection<DataNode> getExpired() {
        Collection<DataNode> renewerList = new ArrayList<>();
        read.lock();
        try {
            Map<String, RenewDecorate<DataNode>> dataMap = dataRepositoryService.getAllData();

            dataMap.forEach((ip, dataNode) -> {

                String dataCenter = dataNode.getRenewal().getDataCenter();
                if (dataCenter.equals(nodeConfig.getLocalDataCenter())) {
                    if (dataNode.isExpired()) {
                        renewerList.add(dataNode.getRenewal());
                    }
                }
            });

        } finally {
            read.unlock();
        }
        return renewerList;
    }

    @Override
    public Map<String/*ipAddress*/, DataNode> getNodes() {
        Map<String, Map<String, DataNode>> map = getRunTime();
        Map<String, DataNode> ret = new HashMap<>();
        if (map != null && !map.isEmpty()) {
            map.forEach((dataCenter, dataNodes) -> ret.putAll(dataNodes));
        }
        return ret;
    }

    @Override
    public NodeChangeResult getNodeChangeResult() {

        NodeChangeResult nodeChangeResult = new NodeChangeResult(NodeType.DATA);
        read.lock();
        try {
            String localDataCenter = nodeConfig.getLocalDataCenter();

            Map<String/*dataCenter*/, NodeRepository> dataNodeRepositoryMap = dataRepositoryService
                    .getNodeRepositories();

            ConcurrentHashMap<String/*dataCenter*/, Map<String/*ipAddress*/, DataNode>> pushNodes = new ConcurrentHashMap<>();

            Map<String/*dataCenter*/, Long> versionMap = new ConcurrentHashMap<>();

            dataNodeRepositoryMap.forEach((dataCenter, dataNodeRepository) -> {

                if (localDataCenter.equalsIgnoreCase(dataCenter)) {

                    nodeChangeResult.setVersion(dataNodeRepository.getVersion());
                }
                versionMap.put(dataCenter, dataNodeRepository.getVersion());

                Map<String, RenewDecorate<DataNode>> dataMap = dataNodeRepository.getNodeMap();
                Map<String, DataNode> newMap = new ConcurrentHashMap<>();
                dataMap.forEach((ip, dataNode) -> newMap.put(ip, dataNode.getRenewal()));
                pushNodes.put(dataCenter, newMap);
            });

            nodeChangeResult.setNodes(pushNodes);

            nodeChangeResult.setDataCenterListVersions(versionMap);

            nodeChangeResult.setLocalDataCenter(localDataCenter);

        } finally {
            read.unlock();
        }

        return nodeChangeResult;
    }

    @Override
    public void pushNodeListChange() {
        NodeOperator<DataNode> fireNode;
        if ((fireNode = dataConfirmStatusService.peekConfirmNode()) != null) {
            LOGGER.info("Now:type {},node {},Push queue:{}", fireNode.getNodeOperate(), fireNode
                .getNode().getNodeUrl().getIpAddress(),
                dataConfirmStatusService.getAllConfirmNodes());
            NodeChangeResult nodeChangeResult = getNodeChangeResult();
            Map<String, Map<String, DataNode>> map = nodeChangeResult.getNodes();
            Map<String, DataNode> addNodes = map.get(nodeConfig.getLocalDataCenter());
            if (addNodes != null) {
                LOGGER.info("addNodes:{}", addNodes.keySet());
                Map<String, DataNode> previousNodes = dataConfirmStatusService.putExpectNodes(
                    fireNode.getNode(), addNodes);

                if (!previousNodes.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("push Type:{},peek:{},list{}", fireNode.getNodeOperate(),
                            fireNode.getNode().getNodeUrl().getIpAddress(), previousNodes.keySet());
                    }
                    firePushDataListTask(fireNode, nodeChangeResult, previousNodes, true);

                }
            }

            firePushSessionListTask(nodeChangeResult, fireNode.getNodeOperate().toString());
        }
    }

    @Override
    public void confirmNodeStatus(String ipAddress, String confirmNodeIp) {
        NodeOperator<DataNode> fireNode = dataConfirmStatusService.peekConfirmNode();
        if (fireNode != null) {
            String fireNodeIp = fireNode.getNode().getIp();
            if (fireNodeIp != null && !fireNodeIp.equals(confirmNodeIp)) {
                LOGGER
                    .info(
                        "Confirm node already be remove from queue!Receive ip:{},expect confirm ip:{},now peek ip:{}",
                        ipAddress, confirmNodeIp, fireNodeIp);
                return;
            }
            Map<String/*ipAddress*/, DataNode> waitNotifyNodes = dataConfirmStatusService
                .getExpectNodes(fireNode.getNode());

            if (waitNotifyNodes != null) {
                LOGGER.info("Peek node:{} oper:{},waitNotifyNodes:{},confirm ip:{}", fireNode
                    .getNode().getNodeUrl().getIpAddress(), fireNode.getNodeOperate(),
                    waitNotifyNodes.keySet(), ipAddress);

                Set<String> removeIp = getRemoveIp(waitNotifyNodes.keySet());
                removeIp.add(ipAddress);

                waitNotifyNodes = dataConfirmStatusService.removeExpectConfirmNodes(
                    fireNode.getNode(), removeIp);

                if (waitNotifyNodes.isEmpty()) {
                    //all node be notified,or some disconnect node be evict
                    try {
                        if (null != dataConfirmStatusService
                            .removeExpectNodes((dataConfirmStatusService.pollConfirmNode())
                                .getNode())) {
                            //add init status must notify
                            if (fireNode.getNodeOperate() == DataOperator.ADD) {
                                notifyConfirm(fireNode.getNode());
                            }
                            LOGGER.info("Data node {} operator {} be confirm,Dump queue:{}",
                                fireNode.getNode().getNodeUrl().getIpAddress(),
                                fireNode.getNodeOperate(),
                                dataConfirmStatusService.getAllConfirmNodes());
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Notify expect confirm status node {} interrupted!",
                            fireNode.getNode());
                    }
                }
            } else {
                try {
                    //wait node not exist,
                    dataConfirmStatusService.pollConfirmNode();
                    LOGGER
                        .info(
                            "Data node {} operator {} poll!not other node need be notify!Confirm ip {},Dump queue:{}",
                            fireNode.getNode().getNodeUrl().getIpAddress(),
                            fireNode.getNodeOperate(), ipAddress,
                            dataConfirmStatusService.getAllConfirmNodes());
                } catch (InterruptedException e) {
                    LOGGER.error("Notify expect confirm status node " + fireNode.getNode()
                                 + " interrupted!", e);
                }
            }
        }
    }

    private Set<String> getRemoveIp(Set<String> waitNotifyNodes) {
        Map<String, Map<String, DataNode>> map = getRunTime();
        Map<String, DataNode> addNodes = map.get(nodeConfig.getLocalDataCenter());
        if (addNodes != null && !addNodes.isEmpty()) {
            return waitNotifyNodes.stream().filter(ip -> !addNodes.keySet().contains(ip)).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private void firePushDataListTask(NodeOperator<DataNode> confirmNode,
                                      NodeChangeResult nodeChangeResult,
                                      Map<String, DataNode> targetNodes, boolean confirm) {

        //notify targetNodes change
        String ip = "";
        String nodeOperate;
        if (confirmNode != null) {
            ip = confirmNode.getNode().getIp();
            nodeOperate = confirmNode.getNodeOperate().toString();
        } else {
            nodeOperate = "OtherDataCenter update";
        }
        TaskEvent taskEvent = new TaskEvent(nodeChangeResult, TaskType.DATA_NODE_CHANGE_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_NEED_CONFIRM_KEY, confirm);
        taskEvent.setAttribute(Constant.PUSH_TARGET_DATA_NODE, targetNodes);
        taskEvent.setAttribute(Constant.PUSH_TARGET_TYPE, NodeType.DATA);
        taskEvent.setAttribute(Constant.PUSH_TARGET_OPERATOR_TYPE, nodeOperate);
        taskEvent.setAttribute(Constant.PUSH_TARGET_CONFIRM_NODE, ip);

        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " NodeType:" + NodeType.DATA
                         + " Operator:" + nodeOperate + " confirmNode:" + ip);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void firePushSessionListTask(NodeChangeResult nodeChangeResult, String nodeOperate) {

        //notify all session node
        TaskEvent taskEvent = new TaskEvent(nodeChangeResult, TaskType.DATA_NODE_CHANGE_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_TARGET_TYPE, NodeType.SESSION);
        taskEvent.setAttribute(Constant.PUSH_TARGET_OPERATOR_TYPE, nodeOperate);
        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " NodeType:" + NodeType.SESSION
                         + " Operator:" + nodeOperate);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void notifyConfirm(DataNode node) {
        TaskEvent taskEvent = new TaskEvent(node, TaskType.RECEIVE_STATUS_CONFIRM_NOTIFY_TASK);

        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " DataNode:" + node);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private Map<String, Map<String, DataNode>> getRunTime() {

        read.lock();
        try {
            ConcurrentHashMap<String/*dataCenter*/, Map<String/*ipAddress*/, DataNode>> pushNodes = new ConcurrentHashMap<>();
            Map<String, Map<String, RenewDecorate<DataNode>>> dataCenterMap = dataRepositoryService.getAllDataMap();

            dataCenterMap.forEach((dataCenter, dataMap) -> {

                Map<String, DataNode> newMap = new ConcurrentHashMap<>();
                dataMap.forEach((ip, dataNode) -> newMap.put(ip, dataNode.getRenewal()));
                pushNodes.put(dataCenter, newMap);

            });
            return pushNodes;
        } finally {
            read.unlock();
        }
    }

    //TODO move this code to enterprise version
    @Override
    public void getOtherDataCenterNodeAndUpdate() {

        MetaNodeService metaNodeService = (MetaNodeService) ServiceFactory
            .getNodeService(NodeType.META);

        Map<String, Collection<String>> metaMap = nodeConfig.getMetaNodeIP();

        if (metaMap != null && metaMap.size() > 0) {
            for (String dataCenter : metaMap.keySet()) {
                //get other dataCenter dataNodes
                if (!nodeConfig.getLocalDataCenter().equals(dataCenter)) {
                    GetChangeListRequest getChangeListRequest = new GetChangeListRequest(
                        NodeType.DATA, dataCenter);
                    //trigger fetch dataCenter data list change
                    DataCenterNodes getDataCenterNodes = metaNodeService
                        .getDataCenterNodes(getChangeListRequest);
                    if (getDataCenterNodes != null) {
                        String dataCenterGet = getDataCenterNodes.getDataCenterId();
                        Long version = getDataCenterNodes.getVersion();
                        if (version == null) {
                            LOGGER.error("Request message version cant not be null!");
                            return;
                        }
                        //check for scheduler get other dataCenter data node
                        boolean result = dataRepositoryService.checkVersion(dataCenterGet, version);
                        if (!result) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("DataCenter {} data list version {} has not updated!",
                                    dataCenter, version);
                            }
                            return;
                        }
                        updateOtherDataCenterNodes(getDataCenterNodes);
                    } else {
                        LOGGER.error("Get DataCenter data nodes change error!null");
                        throw new RuntimeException("Get null DataCenter data nodes change!");
                    }
                }
            }
        }
    }

    //TODO move this to enterprise version
    @Override
    public void updateOtherDataCenterNodes(DataCenterNodes<DataNode> dataCenterNodes) {
        write.lock();
        try {
            String dataCenter = dataCenterNodes.getDataCenterId();
            Long version = dataCenterNodes.getVersion();

            if (version == null) {
                LOGGER.error("Request message version cant not be null!");
                return;
            }

            Map<String/*ipAddress*/, DataNode> dataCenterNodesMap = dataCenterNodes.getNodes();

            LOGGER.info("update version {} Other DataCenter {} Nodes {}", version, dataCenter, dataCenterNodesMap);

            Map<String/*ipAddress*/, RenewDecorate<DataNode>> dataCenterNodesMapTemp = new ConcurrentHashMap<>();
            dataCenterNodesMap.forEach((ipAddress, dataNode) -> dataCenterNodesMapTemp
                    .put(ipAddress, new RenewDecorate(dataNode, RenewDecorate.DEFAULT_DURATION_SECS)));
            dataRepositoryService.replaceAll(dataCenter, dataCenterNodesMapTemp, version);

            if (version == localDataCenterInitVersion.get()) {
                //first dataCenter has init version,need not notify local data node
                LOGGER.info("DataCenter {} first start up,No data node change to notify!Init version {}", dataCenter,
                        version);
                return;
            }

            NodeChangeResult nodeChangeResult = getNodeChangeResult();

            firePushDataListTask(null, nodeChangeResult, null, false);

            firePushSessionListTask(nodeChangeResult, "OtherDataCenterUpdate");
        } finally {
            write.unlock();
        }
    }

    @Override
    public DataCenterNodes getDataCenterNodes() {
        read.lock();
        try {
            String localDataCenter = nodeConfig.getLocalDataCenter();

            Map<String/*dataCenter*/, NodeRepository> dataNodeRepositoryMap = dataRepositoryService
                    .getNodeRepositories();

            NodeRepository<DataNode> dataNodeRepository = dataNodeRepositoryMap.get(localDataCenter);

            if (dataNodeRepository == null) {
                //first just dataCenter exist but no data node register
                DataCenterNodes dataCenterNodes = new DataCenterNodes(NodeType.DATA, localDataCenterInitVersion.get(),
                        localDataCenter);
                dataCenterNodes.setNodes(new ConcurrentHashMap<>());
                return dataCenterNodes;
            }

            DataCenterNodes dataCenterNodes = new DataCenterNodes(NodeType.DATA, dataNodeRepository.getVersion(),
                    localDataCenter);

            Map<String, RenewDecorate<DataNode>> dataMap = dataNodeRepository.getNodeMap();
            Map<String, DataNode> newMap = new ConcurrentHashMap<>();
            dataMap.forEach((ip, dataNode) -> newMap.put(ip, dataNode.getRenewal()));

            dataCenterNodes.setNodes(newMap);

            return dataCenterNodes;
        } finally {
            read.unlock();
        }
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
     * Setter method for property <tt>taskListenerManager</tt>.
     *
     * @param taskListenerManager  value to be assigned to property taskListenerManager
     */
    public void setTaskListenerManager(TaskListenerManager taskListenerManager) {
        this.taskListenerManager = taskListenerManager;
    }

    /**
     * Setter method for property <tt>dataRepositoryService</tt>.
     *
     * @param dataRepositoryService  value to be assigned to property dataRepositoryService
     */
    public void setDataRepositoryService(RepositoryService<String, RenewDecorate<DataNode>> dataRepositoryService) {
        this.dataRepositoryService = dataRepositoryService;
    }

    /**
     * Setter method for property <tt>dataConfirmStatusService</tt>.
     *
     * @param dataConfirmStatusService  value to be assigned to property dataConfirmStatusService
     */
    public void setDataConfirmStatusService(NodeConfirmStatusService<DataNode> dataConfirmStatusService) {
        this.dataConfirmStatusService = dataConfirmStatusService;
    }

}