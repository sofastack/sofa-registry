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

import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
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

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionRepositoryService.java, v 0.1 2018-05-08 13:11 shangyu.wh Exp $
 */
@RaftService(uniqueId = "sessionServer")
public class SessionRepositoryService extends AbstractSnapshotProcess
                                                                     implements
                                                                     RepositoryService<String, RenewDecorate<SessionNode>> {

    private static final Logger                                                 LOGGER            = LoggerFactory
                                                                                                      .getLogger(SessionRepositoryService.class);

    @Autowired
    private NodeConfig                                                          nodeConfig;

    /**
     * session node store
     */
    private ConcurrentHashMap<String/*ipAddress*/, RenewDecorate<SessionNode>> registry          = new ConcurrentHashMap<>();

    private Set<String>                                                         snapShotFileNames = new HashSet<>();

    /**
     * constructor
     */
    public SessionRepositoryService() {
    }

    /**
     * constructor
     * @param registry
     */
    public SessionRepositoryService(ConcurrentHashMap<String, RenewDecorate<SessionNode>> registry) {
        this.registry = registry;
    }

    @Override
    public SnapshotProcess copy() {
        return new SessionRepositoryService(new ConcurrentHashMap<>(registry));
    }

    @Override
    public RenewDecorate<SessionNode> put(String ipAddress, RenewDecorate<SessionNode> sessionNode,
                                          Long currentTimeMillis) {
        try {
            RenewDecorate oldRenewDecorate = registry.get(ipAddress);
            if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                LOGGER.info("Session node with connectId:" + ipAddress + " has already existed!");
            }
            registry.put(ipAddress, sessionNode);
            LOGGER.info("Put session node {} ok", ipAddress);
        } catch (Exception e) {
            LOGGER.error("Session node add error!", e);
            throw new RuntimeException("Session node add error!", e);
        }
        return sessionNode;
    }

    @Override
    public RenewDecorate<SessionNode> remove(Object key, Long currentTimeMillis) {
        try {
            String ipAddress = (String) key;
            RenewDecorate<SessionNode> oldRenewDecorate = registry.remove(ipAddress);
            if (oldRenewDecorate == null) {
                LOGGER
                    .info("Remove Session node with ipAddress:" + ipAddress + " has not existed!");
                return null;
            }
            return oldRenewDecorate;
        } catch (Exception e) {
            LOGGER.error("Data Session remove error!", e);
            throw new RuntimeException("Session node remove error!", e);
        }
    }

    @Override
    public RenewDecorate<SessionNode> replace(String ipAddress,
                                              RenewDecorate<SessionNode> sessionNode,
                                              Long currentTimeMillis) {
        RenewDecorate<SessionNode> oldRenewDecorate = registry.get(ipAddress);
        if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
            oldRenewDecorate.setRenewal(sessionNode.getRenewal());
            oldRenewDecorate.renew();
        } else {
            LOGGER.error("Session node with ipAddress {} has not existed!", ipAddress);
            throw new RuntimeException(String.format(
                "Session node with ipAddress %s has not existed!", ipAddress));
        }
        return sessionNode;
    }

    @Override
    public RenewDecorate<SessionNode> get(Object key) {
        try {
            String ipAddress = (String) key;
            RenewDecorate<SessionNode> oldRenewDecorate = registry.get(ipAddress);
            if (oldRenewDecorate != null && oldRenewDecorate.getRenewal() != null) {
                return oldRenewDecorate;
            } else {
                LOGGER.warn("Session node with ipAddress {} has not existed!It not be registered!",
                    ipAddress);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Get Session node error!", e);
            throw new RuntimeException("Get Session node error!", e);
        }
    }

    @Override
    public Map<String, RenewDecorate<SessionNode>> getAllData() {
        return registry;
    }

    @Override
    public Map<String, Map<String, RenewDecorate<SessionNode>>> getAllDataMap() {
        Map<String, Map<String, RenewDecorate<SessionNode>>> map = new ConcurrentHashMap<>();
        map.put(nodeConfig.getLocalDataCenter(), registry);
        return map;
    }

    @Override
    public Map<String, RenewDecorate<SessionNode>> replaceAll(String dataCenter,
                                                              Map<String, RenewDecorate<SessionNode>> map,
                                                              Long version) {
        return null;
    }

    @Override
    public Map<String, NodeRepository> getNodeRepositories() {
        Map<String, NodeRepository> map = new ConcurrentHashMap<>();
        map.put(nodeConfig.getLocalDataCenter(), new NodeRepository(
            nodeConfig.getLocalDataCenter(), registry, System.currentTimeMillis()));
        return map;
    }

    @Override
    public boolean save(String path) {
        return save(path, registry);
    }

    @Override
    public synchronized boolean load(String path) {
        try {
            ConcurrentHashMap<String, RenewDecorate<SessionNode>> map = load(path,
                registry.getClass());
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
    public boolean checkVersion(String key, Long version) {
        return false;
    }

    @Override
    public Long getVersion(String key) {
        return null;
    }

    /**
     * Setter method for property <tt>nodeConfig</tt>.
     *
     * @param nodeConfig  value to be assigned to property nodeConfig
     */
    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }
}