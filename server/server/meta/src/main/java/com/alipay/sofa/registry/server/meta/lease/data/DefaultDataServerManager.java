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
package com.alipay.sofa.registry.server.meta.lease.data;

import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.lease.AbstractRaftEnabledLeaseManager;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.jraft.annotation.RaftReference;
import com.alipay.sofa.registry.jraft.annotation.RaftReferenceContainer;
import com.alipay.sofa.registry.server.meta.monitor.data.DataServerStats;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@RaftReferenceContainer
public class DefaultDataServerManager extends AbstractRaftEnabledLeaseManager<DataNode> implements
                                                                                       DataServerManager {

    @RaftReference(uniqueId = DataLeaseManager.DATA_LEASE_MANAGER, interfaceType = LeaseManager.class)
    private LeaseManager<DataNode>             raftDataLeaseManager;

    @Autowired
    private DataLeaseManager                   dataLeaseManager;

    @Autowired
    private MetaServerConfig                   metaServerConfig;

    private final Map<String, DataServerStats> dataServerStatses = Maps.newConcurrentMap();

    /**
     * Constructor.
     */
    public DefaultDataServerManager() {
    }

    /**
     * Constructor.
     *
     * @param raftDataLeaseManager the raft data lease manager
     * @param dataLeaseManager     the data lease manager
     * @param metaServerConfig     the meta server config
     */
    public DefaultDataServerManager(LeaseManager<DataNode> raftDataLeaseManager,
                                    DataLeaseManager dataLeaseManager,
                                    MetaServerConfig metaServerConfig) {
        this.raftDataLeaseManager = raftDataLeaseManager;
        this.dataLeaseManager = dataLeaseManager;
        this.metaServerConfig = metaServerConfig;
    }

    /**
     * Post construct.
     *
     * @throws Exception the exception
     */
    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    /**
     * Pre destory.
     *
     * @throws Exception the exception
     */
    @PreDestroy
    public void preDestory() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

    @Override
    protected DataLeaseManager getLocalLeaseManager() {
        return dataLeaseManager;
    }

    @Override
    protected LeaseManager<DataNode> getRaftLeaseManager() {
        return raftDataLeaseManager;
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        dataLeaseManager.setLogger(logger);
    }

    @Override
    protected long getIntervalMilli() {
        return metaServerConfig.getExpireCheckIntervalMilli();
    }

    @Override
    protected long getEvictBetweenMilli() {
        return metaServerConfig.getExpireCheckIntervalMilli();
    }

    @VisibleForTesting
    DefaultDataServerManager setRaftDataLeaseManager(LeaseManager<DataNode> raftDataLeaseManager) {
        this.raftDataLeaseManager = raftDataLeaseManager;
        return this;
    }

    @VisibleForTesting
    DefaultDataServerManager setDataLeaseManager(DataLeaseManager dataLeaseManager) {
        this.dataLeaseManager = dataLeaseManager;
        return this;
    }

    @VisibleForTesting
    DefaultDataServerManager setMetaServerConfig(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
        return this;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "DefaultDataServerManager";
    }

    /**
     * On heartbeat.
     *
     * @param heartbeat the heartbeat
     */
    @Override
    public void onHeartbeat(HeartbeatRequest<DataNode> heartbeat) {
        String dataServer = heartbeat.getNode().getIp();
        dataServerStatses.put(
            dataServer,
            new DataServerStats(dataServer, heartbeat.getSlotTableEpoch(), heartbeat
                .getSlotStatus()));
    }

    @Override
    public List<DataServerStats> getDataServersStats() {
        return Collections.unmodifiableList(Lists.newLinkedList(dataServerStatses.values()));
    }
}
