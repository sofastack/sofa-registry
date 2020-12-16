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

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.lease.AbstractRaftEnabledLeaseManager;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.store.api.annotation.RaftReferenceContainer;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@RaftReferenceContainer
public class DefaultDataServerManager extends AbstractRaftEnabledLeaseManager<DataNode> implements
                                                                                       DataServerManager {

    @RaftReference(uniqueId = DataLeaseManager.DATA_LEASE_MANAGER, interfaceType = LeaseManager.class)
    private LeaseManager<DataNode> raftDataLeaseManager;

    @Autowired
    private DataLeaseManager       dataLeaseManager;

    @Autowired
    private MetaServerConfig       metaServerConfig;

    private ExecutorService        executors;

    public DefaultDataServerManager() {
    }

    public DefaultDataServerManager(LeaseManager<DataNode> raftDataLeaseManager,
                                    DataLeaseManager dataLeaseManager,
                                    MetaServerConfig metaServerConfig) {
        this.raftDataLeaseManager = raftDataLeaseManager;
        this.dataLeaseManager = dataLeaseManager;
        this.metaServerConfig = metaServerConfig;
    }

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
        executors = DefaultExecutorFactory.createAllowCoreTimeout(getClass().getSimpleName(),
            Math.min(4, OsUtils.getCpuCount())).create();
        dataLeaseManager.setExecutors(executors);
        dataLeaseManager.setLogger(logger);
    }

    @Override
    protected void doDispose() throws DisposeException {
        if (executors != null) {
            executors.shutdownNow();
        }
        super.doDispose();
    }

    @Override
    protected long getIntervalMilli() {
        return TimeUnit.SECONDS.toMillis(metaServerConfig.getSchedulerHeartbeatExpBackOffBound());
    }

    @Override
    protected long getEvictBetweenMilli() {
        return TimeUnit.SECONDS.toMillis(metaServerConfig.getSchedulerHeartbeatExpBackOffBound());
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

    @Override
    public String toString() {
        return "DefaultDataServerManager";
    }
}
