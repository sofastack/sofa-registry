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
package com.alipay.sofa.registry.server.meta.repository.service;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.jraft.processor.AbstractSnapshotProcess;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.repository.NodeRepository;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.server.meta.store.RenewDecorate;
import com.alipay.sofa.registry.store.api.annotation.RaftService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataRepositoryServcie.java, v 0.1 2018-05-08 13:11 shangyu.wh Exp $
 */
@RaftService(uniqueId = "dataServer")
public class DataRepositoryService extends AbstractSnapshotProcess
                                                                  implements
                                                                  RepositoryService<String, RenewDecorate<DataNode>> {

    private static final Logger                        LOGGER            = LoggerFactory
                                                                             .getLogger(DataRepositoryService.class);

    @Autowired
    private NodeConfig                                 nodeConfig;

    private final ReentrantReadWriteLock               readWriteLock     = new ReentrantReadWriteLock();
    private final Lock                                 read              = readWriteLock.readLock();
    private final Lock                                 write             = readWriteLock
                                                                             .writeLock();

    /**
     * data node store and version
     */
    private Map<String/*dataCenter*/, NodeRepository> registry          = new ConcurrentHashMap<>();

    private Set<String>                                snapShotFileNames = new HashSet<>();

    /**
     * constructor
     */
    public DataRepositoryService() {
    }

    /**
     * constructor
     * @param registry
     */
    public DataRepositoryService(Map<String, NodeRepository> registry) {
        this.registry = registry;
    }

    @Override
    public SnapshotProcess copy() {
        return new DataRepositoryService(new ConcurrentHashMap<>(registry));
    }

    @Override
    public RenewDecorate<DataNode> put(String ipAddress, RenewDecorate<DataNode> dataNode) {

        write.lock();
        try {
            String dataCenter = dataNode.getRenewal().getDataCenter();

            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository == null) {
                NodeRepository<DataNode> nodeRepository = new NodeRepository<>(dataCenter,
                    new ConcurrentHashMap<>(), System.currentTimeMillis());
                dataNodeRepository = registry.put(dataCenter, nodeRepository);
                if (dataNodeRepository == null) {
                    dataNodeRepository = nodeRepository;
                }
            }

            dataNodeRepository.setVersion(System.currentTimeMillis());

            Map<String/*ipAddress*/, RenewDecorate<DataNode>> dataNodes = dataNodeRepository
                .getNodeMap();
            RenewDecorate oldRenewDecorate = dataNodes.get(ipAddress);
            if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                LOGGER.info("Data node with ipAddress:" + ipAddress + " has already existed!");
            }

            dataNodes.put(ipAddress, dataNode);

        } catch (Exception e) {
            LOGGER.error("Data node add error!", e);
            throw new RuntimeException("Data node add error!", e);
        } finally {
            write.unlock();
        }

        return dataNode;
    }

    @Override
    public RenewDecorate<DataNode> remove(Object key) {

        write.lock();
        try {
            String ipAddress = (String) key;

            String dataCenter = nodeConfig.getLocalDataCenter();

            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                Map<String/*ipAddress*/, RenewDecorate<DataNode>> dataNodes = dataNodeRepository
                    .getNodeMap();
                if (dataNodes != null) {
                    RenewDecorate<DataNode> oldRenewDecorate = dataNodes.remove(ipAddress);
                    if (oldRenewDecorate == null) {
                        LOGGER.warn("Data node with ipAddress:" + ipAddress + " has not exist!");
                        return null;
                    }

                    dataNodeRepository.setVersion(System.currentTimeMillis());
                    return oldRenewDecorate;
                }
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Data node remove error!", e);
            throw new RuntimeException("Data node remove error!", e);
        } finally {
            write.unlock();
        }
    }

    @Override
    public RenewDecorate<DataNode> replace(String ipAddress, RenewDecorate<DataNode> dataNode) {

        write.lock();
        try {
            String dataCenter = dataNode.getRenewal().getDataCenter();

            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);

            if (dataNodeRepository != null) {

                Map<String/*ipAddress*/, RenewDecorate<DataNode>> dataNodes = dataNodeRepository
                    .getNodeMap();
                RenewDecorate<DataNode> oldRenewDecorate = dataNodes.get(ipAddress);
                if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                    oldRenewDecorate.setRenewal(dataNode.getRenewal());
                    oldRenewDecorate.reNew();
                } else {
                    LOGGER.error("Data node with ipAddress {} has not existed!", ipAddress);
                    throw new RuntimeException(String.format(
                        "Data node with ipAddress %s has not existed!", ipAddress));
                }
            } else {
                LOGGER.error("Data node in dataCenter: {} has not existed!", dataCenter);
                throw new RuntimeException(String.format(
                    "Data node in dataCenter: %s has not existed!", dataCenter));
            }

            return dataNode;
        } catch (Exception e) {
            LOGGER.error("Data node replace error!", e);
            throw new RuntimeException("Data node replace error!", e);
        } finally {
            write.unlock();
        }
    }

    /**
     * this function deal with dataCenter all data,according version and dataMap
     * @param dataCenter
     * @param map
     * @return
     */
    @Override
    public Map<String, RenewDecorate<DataNode>> replaceAll(String dataCenter,
                                                           Map<String, RenewDecorate<DataNode>> map,
                                                           Long version) {

        Map<String, RenewDecorate<DataNode>> oldMap;
        write.lock();
        try {
            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                oldMap = dataNodeRepository.getNodeMap();
                if (oldMap == null) {
                    LOGGER.warn("Data node in dataCenter: {} has not existed!", dataCenter);
                }
                dataNodeRepository.setNodeMap(map);
                dataNodeRepository.setVersion(version);
                return oldMap;
            } else {
                registry.put(dataCenter, new NodeRepository(dataCenter, map, version));
                return map;
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public RenewDecorate<DataNode> get(Object key) {

        read.lock();
        try {
            String ipAddress = (String) key;

            String dataCenter = nodeConfig.getLocalDataCenter();

            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                Map<String/*ipAddress*/, RenewDecorate<DataNode>> dataNodes = dataNodeRepository
                    .getNodeMap();
                if (dataNodes != null) {
                    RenewDecorate<DataNode> oldRenewDecorate = dataNodes.get(ipAddress);
                    if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                        return oldRenewDecorate;
                    } else {
                        LOGGER.warn(
                            "Data node with ipAddress {} has not existed!It not be registered!",
                            ipAddress);
                        return null;
                    }
                } else {
                    LOGGER.warn("Data node map has not existed in dataCenter:{}!", dataCenter);
                    return null;
                }
            } else {
                LOGGER.error("Data node in dataCenter: {} has not existed!", dataCenter);
                throw new RuntimeException(String.format(
                    "Data node in dataCenter: %s has not existed!", dataCenter));
            }
        } catch (Exception e) {
            LOGGER.error("Get Data node error!", e);
            throw new RuntimeException("Get Data node error!", e);
        } finally {
            read.unlock();
        }

    }

    @Override
    public Map<String, RenewDecorate<DataNode>> getAllData() {
        read.lock();
        try {
            Map<String/*ipAddress*/, RenewDecorate<DataNode>> nodes = new ConcurrentHashMap<>();
            registry.forEach((dataCenter, dataNodeRepository) -> nodes.putAll(dataNodeRepository.getNodeMap()));
            return nodes;
        } finally {
            read.unlock();
        }
    }

    @Override
    public Map<String, Map<String, RenewDecorate<DataNode>>> getAllDataMap() {
        Map<String, Map<String, RenewDecorate<DataNode>>> nodes = new ConcurrentHashMap<>();
        read.lock();
        try {
            registry.forEach(
                    (dataCenter, dataNodeRepository) -> nodes.put(dataCenter, dataNodeRepository.getNodeMap()));
            return nodes;
        } finally {
            read.unlock();
        }
    }

    @Override
    public Map<String, NodeRepository> getNodeRepositories() {
        return registry;
    }

    /**
     * Setter method for property <tt>nodeConfig</tt>.
     *
     * @param nodeConfig  value to be assigned to property nodeConfig
     */
    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    @Override
    public boolean save(String path) {
        return save(path, registry);
    }

    @Override
    public synchronized boolean load(String path) {
        try {
            Map<String, NodeRepository> map = load(path, registry.getClass());
            registry.clear();
            registry.putAll(map);
            return true;
        } catch (IOException e) {
            LOGGER.error("Load registry data error!", e);
            return false;
        }
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        if (!snapShotFileNames.isEmpty()) {
            return snapShotFileNames;
        }
        snapShotFileNames.add(this.getClass().getSimpleName());
        return snapShotFileNames;
    }

    @Override
    public boolean checkVersion(String dataCenter, Long version) {
        read.lock();
        try {
            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                Long oldValue = dataNodeRepository.getVersion();
                if (oldValue == null) {
                    return true;
                } else {
                    return version > oldValue;
                }
            } else {
                return true;
            }
        } finally {
            read.unlock();
        }
    }

    @Override
    public Long getVersion(String dataCenter) {
        read.lock();
        try {
            NodeRepository<DataNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                return dataNodeRepository.getVersion();
            }
            return null;
        } finally {
            read.unlock();
        }
    }
}