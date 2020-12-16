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
package com.alipay.sofa.registry.server.meta.lease.session;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModified;
import com.alipay.sofa.registry.server.meta.lease.AbstractRaftEnabledLeaseManager;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.store.api.annotation.RaftReferenceContainer;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@RaftReferenceContainer
public class DefaultSessionServerManager extends AbstractRaftEnabledLeaseManager<SessionNode>
                                                                                             implements
                                                                                             SessionServerManager {

    @RaftReference(uniqueId = SessionLeaseManager.SESSION_LEASE_MANAGER, interfaceType = LeaseManager.class)
    private LeaseManager<SessionNode> raftSessionLeaseManager;

    @Autowired
    private SessionLeaseManager       sessionLeaseManager;

    @Autowired
    private MetaServerConfig          metaServerConfig;

    private ExecutorService           executors;

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
    protected SessionLeaseManager getLocalLeaseManager() {
        return sessionLeaseManager;
    }

    @Override
    protected LeaseManager<SessionNode> getRaftLeaseManager() {
        return raftSessionLeaseManager;
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        executors = DefaultExecutorFactory.createAllowCoreTimeout(getClass().getSimpleName(),
            Math.min(4, OsUtils.getCpuCount())).create();
        sessionLeaseManager.setExecutors(executors);
        sessionLeaseManager.setLogger(logger);
    }

    @Override
    protected void doDispose() throws DisposeException {
        if (executors != null) {
            executors.shutdownNow();
        }
        super.doDispose();
    }

    /**
     * Different from data server, session node maintains a 'ProcessId' to be as unique Id for Session Process(not server)
     *
     * Once a restart event happened on the same session-server, an notification will be sent
     * */
    @Override
    public boolean renew(SessionNode renewal, int leaseDuration) {
        if (!ServiceStateMachine.getInstance().isLeader()) {
            return raftSessionLeaseManager.renew(renewal, leaseDuration);
        }
        int validLeaseDuration = leaseDuration > 0 ? leaseDuration : Lease.DEFAULT_DURATION_SECS;
        Lease<SessionNode> lease = null;
        lock.readLock().lock();
        try {
            lease = sessionLeaseManager.getLease(renewal);
        } finally {
            lock.readLock().unlock();
        }
        /*
         * no exist lease, try register the node to all meta-servers through raft
         *
         * Or, notify session server changes, if restart event has been monitored
         * */
        lock.writeLock().lock();
        try {
            if (lease == null) {
                sessionLeaseManager.register(new Lease<>(renewal, validLeaseDuration));
                notifyObservers(new NodeAdded<>(renewal));
                return false;
            }
            if (renewal.getProcessId() != null
                && !Objects.equals(lease.getRenewal().getProcessId(), renewal.getProcessId())) {
                logger.warn("[renew] session node is restart, as process-Id change from {} to {}",
                    lease.getRenewal().getProcessId(), renewal.getProcessId());
                // replace the session node, as it has changed process-id already
                lease.setRenewal(renewal);
                sessionLeaseManager.register(new Lease<>(renewal, validLeaseDuration));
                notifyObservers(new NodeModified<>(lease.getRenewal(), renewal));
            } else {
                sessionLeaseManager.renew(renewal, validLeaseDuration);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return true;
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
    DefaultSessionServerManager setRaftSessionLeaseManager(LeaseManager<SessionNode> raftSessionLeaseManager) {
        this.raftSessionLeaseManager = raftSessionLeaseManager;
        return this;
    }

    @VisibleForTesting
    DefaultSessionServerManager setSessionLeaseManager(SessionLeaseManager sessionLeaseManager) {
        this.sessionLeaseManager = sessionLeaseManager;
        return this;
    }

    @VisibleForTesting
    DefaultSessionServerManager setMetaServerConfig(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
        return this;
    }
}
