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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */

public class DefaultDataServerManager extends AbstractRaftEnabledLeaseManager<DataNode> implements
                                                                                       DataServerManager {

    private static final String DEFAULT_DATA_MANAGER_SERVICE_ID = "DefaultDataServerManager.LeaseManager";

    private ExecutorService executors;

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
        executors = DefaultExecutorFactory
                .createAllowCoreTimeout(getClass().getSimpleName(), Math.min(8, OsUtils.getCpuCount()))
                .create();
        localLeaseManager.setExecutors(executors);
        localLeaseManager.setLogger(logger);
    }

    @Override
    public void addObserver(Observer observer) {
        localLeaseManager.addObserver(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        localLeaseManager.removeObserver(observer);
    }

    @Override
    public long getEpoch() {
        return this.raftLeaseManager.getEpoch();
    }

    @Override
    public List<DataNode> getClusterMembers() {
        List<DataNode> result = Lists.newLinkedList();
        raftLeaseManager.getLeaseStore().forEach((ip,lease)->{result.add(lease.getRenewal());});
        return result;
    }

    public List<DataNode> getLocalClusterMembers() {
        List<DataNode> result = Lists.newLinkedList();
        localLeaseManager.getLeaseStore().forEach((ip,lease)->{result.add(lease.getRenewal());});
        return result;
    }

    @Override
    protected String getServiceId() {
        return DEFAULT_DATA_MANAGER_SERVICE_ID;
    }
}
