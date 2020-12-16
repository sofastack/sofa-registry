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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.store.api.annotation.RaftReferenceContainer;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
@RaftReferenceContainer
public class DefaultCurrentDcMetaServer extends AbstractMetaServer implements CurrentDcMetaServer {

    @Autowired
    private SessionServerManager   sessionServerManager;

    @Autowired
    private DataServerManager      dataServerManager;

    @Autowired
    private NodeConfig             nodeConfig;

    @RaftReference(uniqueId = DefaultLocalMetaServer.DEFAULT_LOCAL_META_SERVER, interfaceType = CurrentDcMetaServer.class)
    private CurrentDcMetaServer    raftMetaServer;

    @Autowired
    private DefaultLocalMetaServer localMetaServer;

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    @PreDestroy
    public void preDestory() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        initMetaServers();
    }

    private void initMetaServers() {
        Collection<String> metaIpAddresses = nodeConfig.getMetaNodeIP().get(
            nodeConfig.getLocalDataCenter());
        List<MetaNode> metaNodes = Lists.newArrayList();
        for (String ip : metaIpAddresses) {
            metaNodes.add(new MetaNode(new URL(ip), nodeConfig.getLocalDataCenter()));
        }
        localMetaServer.updateClusterMembers(metaNodes, DatumVersionUtil.nextId());
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    public DataServerManager getDataServerManager() {
        return dataServerManager;
    }

    public SessionServerManager getSessionServerManager() {
        return sessionServerManager;
    }

    @Override
    public void renew(MetaNode metaNode) {
        raftMetaServer.renew(metaNode);
        notifyObservers(new NodeAdded<>(metaNode));
    }

    @Override
    public void cancel(MetaNode metaNode) {
        raftMetaServer.cancel(metaNode);
        notifyObservers(new NodeRemoved<MetaNode>(metaNode));
    }

    @Override
    public void updateClusterMembers(List<MetaNode> newMembers, long epoch) {
        raftMetaServer.updateClusterMembers(newMembers, epoch);
    }

    @Override
    public SlotTable getSlotTable() {
        if (ServiceStateMachine.getInstance().isLeader()) {
            return localMetaServer.getSlotTable();
        }
        return raftMetaServer.getSlotTable();
    }

    @Override
    public List<MetaNode> getClusterMembers() {
        if (ServiceStateMachine.getInstance().isLeader()) {
            return localMetaServer.getClusterMembers();
        }
        return raftMetaServer.getClusterMembers();
    }

    @ReadOnLeader
    public long getEpoch() {
        if (ServiceStateMachine.getInstance().isLeader()) {
            return localMetaServer.getEpoch();
        }
        return raftMetaServer.getEpoch();
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setSessionManager(SessionServerManager sessionServerManager) {
        this.sessionServerManager = sessionServerManager;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setDataServerManager(DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setRaftMetaServer(CurrentDcMetaServer raftMetaServer) {
        this.raftMetaServer = raftMetaServer;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setLocalMetaServer(DefaultLocalMetaServer localMetaServer) {
        this.localMetaServer = localMetaServer;
        return this;
    }
}
