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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.EpochAware;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SCHEDULED_EXECUTOR;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */

public abstract class AbstractRaftEnabledLeaseManager<T extends Node> extends
                                                                      AbstractLifecycleObservable
                                                                                                 implements
                                                                                                 LeaseManager<T> {

    protected RaftLeaseManager<String, T> raftLeaseManager;

    @Autowired
    private RaftExchanger                 raftExchanger;

    @Resource(name = SCHEDULED_EXECUTOR)
    private ScheduledExecutorService      scheduled;

    private ScheduledFuture<?>            future;

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        initRaftLeaseManager();
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (ServiceStateMachine.getInstance().isLeader()) {
                    raftLeaseManager.evict();
                }
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
    }

    private int getIntervalMilli() {
        return 60 * 1000;
    }

    @Override
    protected void doStop() throws StopException {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    @VisibleForTesting
    protected void register(T renewal, int leaseDuration) {
        updateRepoVersion();
        this.raftLeaseManager.renew(renewal, leaseDuration);
        notifyObservers(new NodeAdded<T>(renewal));
    }

    @Override
    public boolean cancel(T renewal) {
        updateRepoVersion();
        boolean result = this.raftLeaseManager.cancel(renewal);
        if (result) {
            notifyObservers(new NodeRemoved<T>(renewal));
        }
        return result;
    }

    @Override
    public boolean renew(T renewal, int leaseDuration) {
        updateRepoVersion();
        return this.raftLeaseManager.renew(renewal, leaseDuration);
    }

    /**
     * Inner mechanism guarantees the unique execute at a batch of time, even from multi calls
     * */
    @Override
    public boolean evict() {
        boolean nodeEvicted = this.raftLeaseManager.evict();
        if (nodeEvicted) {
            updateRepoVersion();
        }
        return nodeEvicted;
    }

    private void updateRepoVersion() {
        this.raftLeaseManager.refreshEpoch(DatumVersionUtil.nextId());
    }

    protected abstract String getServiceId();

    @SuppressWarnings("unchecked")
    private void initRaftLeaseManager() {
        String serviceId = getServiceId();
        DefaultLeaseManager<SessionNode> localRepo = new DefaultLeaseManager<>();
        localRepo.setLogger(logger);
        // register as raft service
        Processor.getInstance().addWorker(serviceId, DefaultLeaseManager.class, localRepo);
        // make field "raftLeaseManager" raft-enabled, by wrap DefaultLeaseManager being a local-repository
        // and expose wrapper as a raft service
        this.raftLeaseManager = (RaftLeaseManager) Proxy.newProxyInstance(Thread.currentThread()
            .getContextClassLoader(), new Class<?>[] { RaftLeaseManager.class }, new ProxyHandler(
            RaftLeaseManager.class, serviceId, raftExchanger.getRaftClient()));
    }

    @VisibleForTesting
    AbstractRaftEnabledLeaseManager<T> setRaftLeaseManager(RaftLeaseManager<String, T> raftLeaseManager) {
        this.raftLeaseManager = raftLeaseManager;
        return this;
    }

    @VisibleForTesting
    AbstractRaftEnabledLeaseManager<T> setRaftExchanger(RaftExchanger raftExchanger) {
        this.raftExchanger = raftExchanger;
        return this;
    }

    @VisibleForTesting
    AbstractRaftEnabledLeaseManager<T> setScheduled(ScheduledExecutorService scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    Map<String, Lease<T>> getLeaseStore() {
        return Maps.newHashMap(raftLeaseManager.getLeaseStore());
    }

    public class DefaultRaftLeaseManager<T extends Node> extends DefaultLeaseManager<T>
                                                                                       implements
                                                                                       RaftLeaseManager<String, T> {

        @Override
        public Map<String, Lease<T>> getLeaseStore() {
            lock.readLock().lock();
            try {
                return Maps.newHashMap(repo);
            } finally {
                lock.readLock().unlock();
            }
        }

        public void replace(T renewal) {
            repo.get(renewal.getNodeUrl().getIpAddress()).setRenewal(renewal);
        }
    }

    public interface RaftLeaseManager<K, T extends Node> extends LeaseManager<T>, EpochAware {

        @ReadOnLeader
        Map<K, Lease<T>> getLeaseStore();
    }
}
