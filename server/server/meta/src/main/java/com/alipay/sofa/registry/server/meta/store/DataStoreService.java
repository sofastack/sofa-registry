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

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.repository.NodeRepository;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        throw new UnsupportedOperationException();
    }

    //TODO move this to enterprise version
    @Override
    public void updateOtherDataCenterNodes(DataCenterNodes<DataNode> dataCenterNodes) {
        throw new UnsupportedOperationException("Node type SESSION not support function");
    }

    @Override
    public DataCenterNodes getDataCenterNodes() {
        throw new UnsupportedOperationException("Node type SESSION not support function");
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

}