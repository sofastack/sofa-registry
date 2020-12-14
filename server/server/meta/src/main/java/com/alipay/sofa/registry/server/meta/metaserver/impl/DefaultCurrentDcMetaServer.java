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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.jraft.processor.AbstractSnapshotProcess;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public class DefaultCurrentDcMetaServer extends AbstractMetaServer implements CurrentDcMetaServer {

    @Autowired
    private SlotManager                  defaultSlotManager;

    @Autowired
    private SessionManager               sessionManager;

    @Autowired
    private DataServerManager            dataServerManager;

    @Autowired
    private RaftExchanger                raftExchanger;

    @Autowired
    private NodeConfig                   nodeConfig;

    private final AtomicLong             currentEpoch = new AtomicLong();

    private CurrentMetaServerRaftStorage raftStorage;

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
        initRaftService();
    }

    private void initMetaServers() {
        Collection<String> metaIpAddresses = nodeConfig.getMetaNodeIP().get(
            nodeConfig.getLocalDataCenter());
        List<MetaNode> metaNodes = Lists.newArrayList();
        for (String ip : metaIpAddresses) {
            metaNodes.add(new MetaNode(new URL(ip), nodeConfig.getLocalDataCenter()));
        }
        this.metaServers.set(metaNodes);
        this.currentEpoch.set(DatumVersionUtil.nextId());
    }

    private void initRaftService() {
        MetaServersRaftStorage storage = new MetaServersRaftStorage();
        storage.registerAsRaftService();
        raftStorage = (CurrentMetaServerRaftStorage) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { CurrentMetaServerRaftStorage.class },
            new ProxyHandler(CurrentMetaServerRaftStorage.class, getServiceId(), raftExchanger
                .getRaftClient()));
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    @Override
    @ReadOnLeader
    public List<SessionNode> getSessionServers() {
        return sessionManager.getClusterMembers();
    }

    @Override
    public List<DataNode> getDataServers() {
        return dataServerManager.getClusterMembers();
    }

    @Override
    public void updateClusterMembers(List<MetaNode> newMembers) {
        raftStorage.updateClusterMembers(newMembers, DatumVersionUtil.nextId());
    }

    @Override
    public SlotTable getSlotTable() {
        return defaultSlotManager.getSlotTable();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean cancel(Node renewal) {
        return getLeaseManager(renewal.getNodeType()).cancel(renewal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean renew(Node renewal, int leaseDuration) {
        return getLeaseManager(renewal.getNodeType()).renew(renewal, leaseDuration);
    }

    @Override
    public List<MetaNode> getClusterMembers() {
        return raftStorage.getClusterMembers();
    }

    @Override
    public boolean evict() {
        throw new UnsupportedOperationException(
            "CurrentDcMetaServer uses raft to keeper heart beat");
    }

    @VisibleForTesting
    @SuppressWarnings("rawUse")
    protected LeaseManager getLeaseManager(Node.NodeType nodeType) {
        if (nodeType == Node.NodeType.SESSION) {
            return sessionManager;
        } else if (nodeType == Node.NodeType.DATA) {
            return dataServerManager;
        } else if (nodeType == Node.NodeType.META) {
            return raftStorage;
        }
        throw new IllegalArgumentException(
            String.format("NodeType [%s] is not supported", nodeType));
    }

    @ReadOnLeader
    public long getEpoch() {
        return raftStorage.getEpoch();
    }

    private String getServiceId() {
        return "CurrentDcMetaServer.MetaServersRaftStorage";
    }

    @Override
    public String toString() {
        return "DefaultCurrentDcMetaServer{" + "currentEpoch=" + currentEpoch.get()
               + ", metaServers=" + metaServers + '}';
    }

    public class MetaServersRaftStorage extends AbstractSnapshotProcess implements
                                                                       CurrentMetaServerRaftStorage {

        @Override
        public long getEpoch() {
            return currentEpoch.get();
        }

        @Override
        public List<MetaNode> getClusterMembers() {
            return Lists.newArrayList(DefaultCurrentDcMetaServer.this.metaServers.get());
        }

        @Override
        public void updateClusterMembers(List<MetaNode> newMembers, long epoch) {
            if (epoch <= currentEpoch.get()) {
                logger.warn("[updateClusterMembers]Epoch[{}] is less than current[{}], ignore: {}",
                    currentEpoch.get(), epoch, newMembers);
            }
            lock.writeLock().lock();
            try {
                logger
                    .warn(
                        "[updateClusterMembers] update meta-servers, \nprevious[{}]: {} \ncurrent[{}]: {}",
                        currentEpoch.get(), getClusterMembers(), epoch, newMembers);
                currentEpoch.set(epoch);
                DefaultCurrentDcMetaServer.this.metaServers.set(Lists.newArrayList(newMembers));
            } finally {
                lock.writeLock().unlock();
            }
        }

        private final void registerAsRaftService() {
            Processor.getInstance().addWorker(getServiceId(), CurrentMetaServerRaftStorage.class,
                MetaServersRaftStorage.this);
        }

        @Override
        public boolean cancel(MetaNode renewal) {
            return metaServers.get().remove(renewal);
        }

        @Override
        public boolean renew(MetaNode renewal, int leaseDuration) {
            return false;
        }

        @Override
        public boolean evict() {
            return false;
        }

        @Override
        public boolean save(String path) {
            return save(path, metaServers.get());
        }

        @Override
        public boolean load(String path) {
            try {
                List<MetaNode> metaNodes = load(path, metaServers.get().getClass());
                metaServers.get().clear();
                metaServers.get().addAll(metaNodes);
                return true;
            } catch (IOException e) {
                logger.error("Load meta servers data error!", e);
                return false;
            }
        }

        @Override
        public SnapshotProcess copy() {
            DefaultCurrentDcMetaServer currentDcMetaServer = new DefaultCurrentDcMetaServer();
            MetaServersRaftStorage storage = currentDcMetaServer.new MetaServersRaftStorage();
            currentDcMetaServer.setRaftStorage(storage).setCurrentEpoch(currentEpoch.get());
            currentDcMetaServer.metaServers = new AtomicReference<>(Lists.newArrayList(metaServers
                .get()));
            return storage;
        }

        @Override
        public Set<String> getSnapshotFileNames() {
            Set<String> set = Sets.newHashSet();
            set.add(DefaultCurrentDcMetaServer.class.getSimpleName());
            return set;
        }
    }

    public interface CurrentMetaServerRaftStorage extends LeaseManager<MetaNode> {

        void updateClusterMembers(List<MetaNode> newMembers, long epoch);

        @ReadOnLeader
        List<MetaNode> getClusterMembers();

        @ReadOnLeader
        long getEpoch();
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setSlotManager(SlotManager slotManager) {
        this.defaultSlotManager = slotManager;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setDataServerManager(DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setRaftExchanger(RaftExchanger raftExchanger) {
        this.raftExchanger = raftExchanger;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setCurrentEpoch(long currentEpoch) {
        this.currentEpoch.set(currentEpoch);
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setRaftStorage(CurrentMetaServerRaftStorage raftStorage) {
        this.raftStorage = raftStorage;
        return this;
    }

    @VisibleForTesting
    DefaultCurrentDcMetaServer setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        return this;
    }
}
