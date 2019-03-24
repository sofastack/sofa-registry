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

import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
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
 * @version $Id: MetaRepositoryService.java, v 0.1 2018-07-30 16:51 shangyu.wh Exp $
 */
@RaftService(uniqueId = "metaServer")
public class MetaRepositoryService extends AbstractSnapshotProcess
                                                                  implements
                                                                  RepositoryService<String, RenewDecorate<MetaNode>> {

    private static final Logger                        LOGGER            = LoggerFactory
                                                                             .getLogger(MetaRepositoryService.class);

    @Autowired
    private NodeConfig                                 nodeConfig;

    private final ReentrantReadWriteLock               readWriteLock     = new ReentrantReadWriteLock();
    private final Lock                                 read              = readWriteLock.readLock();
    private final Lock                                 write             = readWriteLock
                                                                             .writeLock();

    /**
     * meta node store and version
     */
    private Map<String/*dataCenter*/, NodeRepository> registry          = new ConcurrentHashMap<>();

    private Set<String>                                snapShotFileNames = new HashSet<>();

    /**
     * constructor
     */
    public MetaRepositoryService() {
    }

    /**
     * constructor
     * @param registry
     */
    public MetaRepositoryService(Map<String, NodeRepository> registry) {
        this.registry = registry;
    }

    @Override
    public SnapshotProcess copy() {
        return new MetaRepositoryService(new ConcurrentHashMap<>(registry));
    }

    @Override
    public RenewDecorate<MetaNode> put(String ipAddress, RenewDecorate<MetaNode> metaNode) {
        write.lock();
        try {
            String dataCenter = metaNode.getRenewal().getDataCenter();

            NodeRepository<MetaNode> metaNodeRepository = registry.get(dataCenter);
            if (metaNodeRepository == null) {
                NodeRepository<MetaNode> nodeRepository = new NodeRepository<>(dataCenter,
                    new ConcurrentHashMap<>(), System.currentTimeMillis());
                metaNodeRepository = registry.put(dataCenter, nodeRepository);
                if (metaNodeRepository == null) {
                    metaNodeRepository = nodeRepository;
                }
            }

            metaNodeRepository.setVersion(System.currentTimeMillis());

            Map<String/*ipAddress*/, RenewDecorate<MetaNode>> metaNodes = metaNodeRepository
                .getNodeMap();
            RenewDecorate oldRenewDecorate = metaNodes.get(ipAddress);
            if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                LOGGER.info("Meta node with ipAddress:" + ipAddress + " has already existed!");
            }

            metaNodes.put(ipAddress, metaNode);

        } catch (Exception e) {
            LOGGER.error("Meta node add error!", e);
            throw new RuntimeException("Meta node add error!", e);
        } finally {
            write.unlock();
        }

        return metaNode;
    }

    @Override
    public RenewDecorate<MetaNode> remove(Object key) {
        write.lock();
        try {
            String ipAddress = (String) key;

            String dataCenter = nodeConfig.getLocalDataCenter();

            NodeRepository<MetaNode> metaNodeRepository = registry.get(dataCenter);
            if (metaNodeRepository != null) {
                Map<String/*ipAddress*/, RenewDecorate<MetaNode>> metaNodes = metaNodeRepository
                    .getNodeMap();
                if (metaNodes != null) {
                    RenewDecorate<MetaNode> oldRenewDecorate = metaNodes.remove(ipAddress);
                    if (oldRenewDecorate == null) {
                        LOGGER.warn("Meta node with ipAddress:" + ipAddress + " has not exist!");
                        return null;
                    }

                    metaNodeRepository.setVersion(System.currentTimeMillis());
                    return oldRenewDecorate;
                }
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Meta node remove error!", e);
            throw new RuntimeException("Meta node remove error!", e);
        } finally {
            write.unlock();
        }
    }

    @Override
    public RenewDecorate<MetaNode> replace(String ipAddress, RenewDecorate<MetaNode> metaNode) {
        write.lock();
        try {
            String dataCenter = metaNode.getRenewal().getDataCenter();

            NodeRepository<MetaNode> metaNodeRepository = registry.get(dataCenter);

            if (metaNodeRepository != null) {
                Map<String/*ipAddress*/, RenewDecorate<MetaNode>> dataNodes = metaNodeRepository
                    .getNodeMap();

                if (dataNodes != null) {
                    RenewDecorate<MetaNode> oldRenewDecorate = dataNodes.get(ipAddress);
                    if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                        oldRenewDecorate.setRenewal(metaNode.getRenewal());
                        oldRenewDecorate.reNew();

                        metaNodeRepository.setVersion(System.currentTimeMillis());
                    } else {
                        LOGGER.error("Meta node with ipAddress {} has not existed!", ipAddress);
                        throw new RuntimeException(String.format(
                            "Meta node with ipAddress %s has not existed!", ipAddress));
                    }
                } else {
                    LOGGER.error("Meta node in dataCenter {} has not existed!", dataCenter);
                    throw new RuntimeException(String.format(
                        "Meta node in dataCenter %s has not existed!", dataCenter));
                }
            } else {
                LOGGER.error("Meta node in dataCenter: {} has not existed!", dataCenter);
                throw new RuntimeException(String.format(
                    "Meta node in dataCenter: %s has not existed!", dataCenter));
            }

            return metaNode;
        } catch (Exception e) {
            LOGGER.error("Data node replace error!", e);
            throw new RuntimeException("Data node replace error!", e);
        } finally {
            write.unlock();
        }
    }

    @Override
    public Map<String, RenewDecorate<MetaNode>> replaceAll(String dataCenter,
                                                           Map<String, RenewDecorate<MetaNode>> map,
                                                           Long version) {
        Map<String, RenewDecorate<MetaNode>> oldMap;
        write.lock();
        try {

            NodeRepository<MetaNode> metaNodeRepository = registry.get(dataCenter);
            if (metaNodeRepository != null) {
                oldMap = metaNodeRepository.getNodeMap();
                if (oldMap == null) {
                    LOGGER.warn("Meta node in dataCenter: {} has not existed!", dataCenter);
                }
                metaNodeRepository.setNodeMap(map);
                metaNodeRepository.setVersion(version);
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
    public RenewDecorate<MetaNode> get(Object key) {
        read.lock();
        try {
            String ipAddress = (String) key;

            String dataCenter = nodeConfig.getLocalDataCenter();

            NodeRepository<MetaNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                Map<String/*ipAddress*/, RenewDecorate<MetaNode>> metaNodes = dataNodeRepository
                    .getNodeMap();
                if (metaNodes == null) {
                    LOGGER.error("Meta node in dataCenter: {} has not existed!", dataCenter);
                    throw new RuntimeException(String.format(
                        "Meta node in dataCenter: %s has not existed!", dataCenter));
                }
                RenewDecorate<MetaNode> oldRenewDecorate = metaNodes.get(ipAddress);
                if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                    return oldRenewDecorate;
                } else {
                    LOGGER.warn(
                        "Meta node with ipAddress {} has not existed!It not be registered!",
                        ipAddress);
                    return null;
                }
            } else {
                LOGGER.error("Meta node in dataCenter: {} has not existed!", dataCenter);
                throw new RuntimeException(String.format(
                    "Meta node in dataCenter: %s has not existed!", dataCenter));
            }
        } catch (Exception e) {
            LOGGER.error("Get meta node error!", e);
            throw new RuntimeException("Get meta node error!", e);
        } finally {
            read.unlock();
        }
    }

    @Override
    public Map<String, RenewDecorate<MetaNode>> getAllData() {
        read.lock();
        try {
            Map<String/*ipAddress*/, RenewDecorate<MetaNode>> nodes = new ConcurrentHashMap<>();
            registry.forEach((dataCenter, metaNodeRepository) -> nodes.putAll(metaNodeRepository.getNodeMap()));
            return nodes;
        } finally {
            read.unlock();
        }
    }

    @Override
    public Map<String, Map<String, RenewDecorate<MetaNode>>> getAllDataMap() {
        Map<String, Map<String, RenewDecorate<MetaNode>>> nodes = new ConcurrentHashMap<>();
        read.lock();
        try {
            registry.forEach(
                    (dataCenter, metaNodeRepository) -> nodes.put(dataCenter, metaNodeRepository.getNodeMap()));
            return nodes;
        } finally {
            read.unlock();
        }
    }

    @Override
    public Map<String/*dataCenter*/, NodeRepository> getNodeRepositories() {
        return registry;
    }

    @Override
    public boolean checkVersion(String dataCenter, Long version) {
        read.lock();
        try {
            NodeRepository<MetaNode> metaNodeRepository = registry.get(dataCenter);
            if (metaNodeRepository != null) {
                Long oldValue = metaNodeRepository.getVersion();
                return oldValue == null || version > oldValue;
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
            NodeRepository<MetaNode> dataNodeRepository = registry.get(dataCenter);
            if (dataNodeRepository != null) {
                return dataNodeRepository.getVersion();
            }
            return null;
        } finally {
            read.unlock();
        }
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
            LOGGER.error("Load registry meta error!", e);
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
}