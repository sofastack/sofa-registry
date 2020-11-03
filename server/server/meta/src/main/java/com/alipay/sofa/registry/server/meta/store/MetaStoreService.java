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
import com.alipay.sofa.registry.common.model.metaserver.GetChangeListRequest;
import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.MetaNodeService;
import com.alipay.sofa.registry.server.meta.repository.NodeRepository;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaStoreService.java, v 0.1 2018-03-02 16:41 shangyu.wh Exp $
 */
public class MetaStoreService implements StoreService<MetaNode> {

    private static final Logger                                LOGGER                     = LoggerFactory
                                                                                              .getLogger(MetaStoreService.class);

    private final ReentrantReadWriteLock                       readWriteLock              = new ReentrantReadWriteLock();
    private final Lock                                         read                       = readWriteLock
                                                                                              .readLock();
    private final Lock                                         write                      = readWriteLock
                                                                                              .writeLock();

    @Autowired
    private NodeConfig                                         nodeConfig;

    private AtomicLong                                         localDataCenterInitVersion = new AtomicLong(
                                                                                              -1L);

    @Autowired
    private TaskListenerManager                                taskListenerManager;

    @RaftReference(uniqueId = "metaServer")
    private RepositoryService<String, RenewDecorate<MetaNode>> metaRepositoryService;

    @Override
    public NodeType getNodeType() {
        return NodeType.META;
    }

    @Override
    public NodeChangeResult setNodes(List<MetaNode> metaNodes) {
        NodeChangeResult nodeChangeResult;

        write.lock();
        try {

            //存放到repository（自动通过jraft同步给集群）
            String dataCenter = nodeConfig.getLocalDataCenter();
            Map<String/*ipAddress*/, RenewDecorate<MetaNode>> dataCenterNodesMap = new ConcurrentHashMap<>();
            for (MetaNode metaNode : metaNodes) {
                dataCenterNodesMap.put(metaNode.getIp(), new RenewDecorate(metaNode,
                    RenewDecorate.DEFAULT_DURATION_SECS));
            }
            metaRepositoryService.replaceAll(dataCenter, dataCenterNodesMap,
                System.currentTimeMillis());

            //触发通知(需要通知data/session)
            nodeChangeResult = getNodeChangeResult();
            LOGGER.info("Set meta node list {} success!", metaNodes);

        } finally {
            write.unlock();
        }

        return nodeChangeResult;
    }

    @Override
    public NodeChangeResult addNode(MetaNode metaNode) {
        NodeChangeResult nodeChangeResult;

        String ipAddress = metaNode.getNodeUrl().getIpAddress();

        write.lock();
        try {
            //存放到repository（自动通过jraft同步给集群）
            metaRepositoryService.put(ipAddress, new RenewDecorate(metaNode,
                RenewDecorate.DEFAULT_DURATION_SECS));

            //触发通知(需要通知data/session)
            nodeChangeResult = getNodeChangeResult();
            LOGGER.info("Add single meta node {} success!", metaNode);

        } finally {
            write.unlock();
        }

        return nodeChangeResult;
    }

    @Override
    public boolean removeNode(String ipAddress) {
        write.lock();
        try {

            //存放到repository（自动通过jraft同步给集群）
            RenewDecorate<MetaNode> dataNode = metaRepositoryService.remove(ipAddress);

            //触发通知(需要通知data/session)
            if (dataNode != null) {
                LOGGER.info("Remove single meta node {} success!", dataNode.getRenewal());
                return true;
            }
            return false;
        } finally {
            write.unlock();
        }
    }

    @Override
    public void removeNodes(Collection<MetaNode> nodes) {

    }

    @Override
    public void renew(MetaNode node, int duration) {
    }

    @Override
    public Collection<MetaNode> getExpired() {
        return null;
    }

    @Override
    public Map<String, MetaNode> getNodes() {
        return null;
    }

    @Override
    public NodeChangeResult getNodeChangeResult() {

        NodeChangeResult nodeChangeResult = new NodeChangeResult(NodeType.META);

        String localDataCenter = nodeConfig.getLocalDataCenter();

        Map<String/*dataCenter*/, NodeRepository> metaRepositoryMap = metaRepositoryService.getNodeRepositories();

        ConcurrentHashMap<String/*dataCenter*/, Map<String/*ipAddress*/, MetaNode>> pushNodes = new ConcurrentHashMap<>();

        Map<String/*dataCenter*/, Long> versionMap = new ConcurrentHashMap<>();

        metaRepositoryMap.forEach((dataCenter, metaNodeRepository) -> {

            if (localDataCenter.equalsIgnoreCase(dataCenter)) {

                nodeChangeResult.setVersion(metaNodeRepository.getVersion());
            }
            versionMap.put(dataCenter, metaNodeRepository.getVersion());

            Map<String, RenewDecorate<MetaNode>> dataMap = metaNodeRepository.getNodeMap();
            Map<String, MetaNode> newMap = new ConcurrentHashMap<>();
            dataMap.forEach((ip, dataNode) -> newMap.put(ip, dataNode.getRenewal()));
            pushNodes.put(dataCenter, newMap);
        });

        nodeChangeResult.setLocalDataCenter(localDataCenter);

        nodeChangeResult.setNodes(pushNodes);

        nodeChangeResult.setDataCenterListVersions(versionMap);

        return nodeChangeResult;
    }

    //TODO move this code to enterprise version
    @Override
    public void getOtherDataCenterNodeAndUpdate() {

        MetaNodeService metaNodeService = (MetaNodeService) ServiceFactory
            .getNodeService(NodeType.META);

        Map<String, Collection<String>> metaMap = nodeConfig.getMetaNodeIP();

        if (metaMap != null && metaMap.size() > 0) {
            for (String dataCenter : metaMap.keySet()) {
                //get other dataCenter meta
                try {
                    if (!nodeConfig.getLocalDataCenter().equals(dataCenter)) {

                        GetChangeListRequest getChangeListRequest = new GetChangeListRequest(
                            NodeType.META, dataCenter);
                        //trigger fetch dataCenter meta list change
                        DataCenterNodes getDataCenterNodes = metaNodeService
                            .getDataCenterNodes(getChangeListRequest);
                        String dataCenterGet = getDataCenterNodes.getDataCenterId();

                        LOGGER.info("GetOtherDataCenterNode from DataCenter({}): {}", dataCenter,
                            getDataCenterNodes);

                        Long version = getDataCenterNodes.getVersion();
                        if (version == null) {
                            LOGGER
                                .error(
                                    "getOtherDataCenterNodeAndUpdate from DataCenter({}), meta list version is null",
                                    dataCenter);
                            continue;
                        }
                        //check for scheduler get other dataCenter meta node
                        boolean result = metaRepositoryService.checkVersion(dataCenterGet, version);
                        if (!result) {
                            LOGGER
                                .warn(
                                    "getOtherDataCenterNodeAndUpdate from DataCenter({}), meta list version {} has not updated",
                                    dataCenter, version);
                            continue;
                        }
                        updateOtherDataCenterNodes(getDataCenterNodes);
                    }
                } catch (Throwable e) {
                    LOGGER.error(String.format(
                        "getOtherDataCenterNodeAndUpdate from DataCenter(%s) error: %s",
                        dataCenter, e.getMessage()), e);
                }
            }
        }
    }

    //TODO move this to enterprise version
    @Override
    public void updateOtherDataCenterNodes(DataCenterNodes<MetaNode> dataCenterNodes) {
        write.lock();
        try {
            String dataCenter = dataCenterNodes.getDataCenterId();
            Long version = dataCenterNodes.getVersion();

            if (version == null) {
                LOGGER.error("Request message version cant not be null!");
                return;
            }
            Map<String/*ipAddress*/, MetaNode> dataCenterNodesMap = dataCenterNodes.getNodes();

            LOGGER.info("update version {} Other DataCenter {} meta Nodes {}", version, dataCenter, dataCenterNodesMap);

            Map<String/*ipAddress*/, RenewDecorate<MetaNode>> dataCenterNodesMapTemp = new ConcurrentHashMap<>();

            dataCenterNodesMap.forEach((ipAddress, metaNode) -> dataCenterNodesMapTemp
                    .put(ipAddress, new RenewDecorate(metaNode, RenewDecorate.DEFAULT_DURATION_SECS)));
            metaRepositoryService.replaceAll(dataCenter, dataCenterNodesMapTemp, version);

            if (version == localDataCenterInitVersion.get()) {
                //first dataCenter has init version,need not notify local data node
                LOGGER.info("DataCenter {} first start up,No meta node change to notify!Init version {}", dataCenter,
                        version);
                return;
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public DataCenterNodes getDataCenterNodes() {
        read.lock();
        try {
            String localDataCenter = nodeConfig.getLocalDataCenter();

            Map<String/*dataCenter*/, NodeRepository> metaNodeRepositoryMap = metaRepositoryService
                    .getNodeRepositories();

            NodeRepository<MetaNode> metaNodeRepository = metaNodeRepositoryMap.get(localDataCenter);

            if (metaNodeRepository == null) {
                DataCenterNodes dataCenterNodes = new DataCenterNodes(NodeType.META, localDataCenterInitVersion.get(),
                        localDataCenter);
                dataCenterNodes.setNodes(new ConcurrentHashMap<>());

                return dataCenterNodes;
            }

            DataCenterNodes dataCenterNodes = new DataCenterNodes(NodeType.META, metaNodeRepository.getVersion(),
                    localDataCenter);

            Map<String, RenewDecorate<MetaNode>> dataMap = metaNodeRepository.getNodeMap();
            Map<String, MetaNode> newMap = new ConcurrentHashMap<>();
            dataMap.forEach((ip, metaNode) -> newMap.put(ip, metaNode.getRenewal()));
            dataCenterNodes.setNodes(newMap);

            return dataCenterNodes;
        } finally {
            read.unlock();
        }
    }
}