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
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.rpc.NodeClusterViewRequest;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.arrange.CrossDcSlotAllocator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.springframework.aop.target.dynamic.Refreshable;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020

 * Generated through Factory
 * CrossDcMetaServer is an object(could be as many as data centers we replicates data) that managing corresponding meta-server infos, as:
 * 1. meta server list (here we consider cross-dc meta-servers as one virtual meta-server, as it supports nothing but slot-table)
 * 2. slot table
 *
 * Scheduled Job is running inside meta-server object for high-cohesion perspective
 * Other objects should not and must not have the privilege to care about updating slot-table or meta-server list
 * What other objects needs to know is "Hey, I got the picture of other data-center's slot view and meta-server list"
 *
 * See @doRefresh for meta server list update
 * See @refreshSlotTable for slot table update
 * */
public class DefaultCrossDcMetaServer extends AbstractMetaServer implements CrossDcMetaServer,
                                                                Refreshable {

    private final String                   dcName;

    private final List<String>             initMetaAddresses;

    private final AtomicLong               currentVersion       = new AtomicLong();

    private final ScheduledExecutorService scheduled;

    private volatile ScheduledFuture<?>    future;

    private final AtomicLong               counter              = new AtomicLong();

    private final AtomicLong               timestamp            = new AtomicLong();

    private SlotAllocator                  allocator;

    private final Exchange                 exchange;

    private final RaftExchanger            raftExchanger;

    private final AtomicInteger            requestMetaNodeIndex = new AtomicInteger(0);

    private final MetaServerConfig         metaServerConfig;

    private MetaServerListStorage          raftStorage;

    public DefaultCrossDcMetaServer(String dcName, Collection<String> metaServerIpAddresses,
                                    ScheduledExecutorService scheduled, Exchange exchange,
                                    RaftExchanger raftExchanger, MetaServerConfig metaServerConfig) {
        this.dcName = dcName;
        this.initMetaAddresses = Lists.newArrayList(metaServerIpAddresses);
        this.scheduled = scheduled;
        this.exchange = exchange;
        this.raftExchanger = raftExchanger;
        this.metaServerConfig = metaServerConfig;
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        initMetaNodes();
        initSlotAllocator();
        initRaftStorage();
    }

    private void initMetaNodes() {
        List<MetaNode> metaNodes = Lists.newArrayList();
        for (String ip : initMetaAddresses) {
            metaNodes.add(new MetaNode(new URL(ip), dcName));
        }
        this.metaServers.set(metaNodes);
    }

    private void initSlotAllocator() throws InitializeException {
        this.allocator = new CrossDcSlotAllocator(dcName, scheduled, exchange, this, raftExchanger);
        LifecycleHelper.initializeIfPossible(allocator);
    }

    private void initRaftStorage() {
        RaftMetaServerListStorage storage = new RaftMetaServerListStorage();
        storage.registerAsRaftService();
        raftStorage = (MetaServerListStorage) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { MetaServerListStorage.class },
            new ProxyHandler(MetaServerListStorage.class, getServiceId(), raftExchanger
                .getRaftClient()));
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (ServiceStateMachine.getInstance().isLeader()) {
                    refresh();
                }
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
        // start allocator for slot purpose
        LifecycleHelper.startIfPossible(allocator);
    }

    @Override
    protected void doStop() throws StopException {
        // stop allocator for slot purpose
        try {
            LifecycleHelper.stopIfPossible(allocator);
        } catch (StopException e) {
            logger.error("[stop][stop allocator error]", e);
        }
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        LifecycleHelper.disposeIfPossible(allocator);
        super.doDispose();
    }

    @Override
    public String getDc() {
        return dcName;
    }

    @Override
    public SlotTable getSlotTable() {
        if (!getLifecycleState().isStarted()) {
            throw new IllegalStateException("[DefaultCrossDcMetaServer] not started yet");
        }
        return allocator.getSlotTable();
    }

    public long getEpoch() {
        return currentVersion.get();
    }

    @Override
    public List<MetaNode> getClusterMembers() {
        return metaServers.get();
    }

    private int getIntervalMilli() {
        return metaServerConfig.getCrossDcMetaSyncIntervalMilli();
    }

    @VisibleForTesting
    protected void doRefresh(int retryTimes) {
        if (retryTimes >= 3) {
            logger.warn("[doRefresh]retries more than {} times, stop", 3);
            return;
        }
        NodeClusterViewRequest request = new NodeClusterViewRequest(Node.NodeType.META, getDc());
        MetaNode metaServer = getRemoteMetaServer();
        exchange.getClient(Exchange.META_SERVER_TYPE).sendCallback(metaServer.getNodeUrl(),
            request, new CallbackHandler() {
                @Override
                @SuppressWarnings("unchecked")
                public void onCallback(Channel channel, Object message) {
                    if (message instanceof DataCenterNodes) {
                        raftStorage
                            .tryUpdateRemoteDcMetaServerList((DataCenterNodes<MetaNode>) message);
                    } else {
                        logger.error("[onCallback]unknown type from response: {}", message);
                    }
                }

                @Override
                public void onException(Channel channel, Throwable exception) {
                    if (logger.isErrorEnabled()) {
                        logger
                            .error(
                                "[doRefresh][{}]Bolt Request Failure, remote: {}, will try other meta-server",
                                getDc(), channel != null ? channel.getRemoteAddress().getHostName()
                                    : "unknown", exception);
                    }
                    requestMetaNodeIndex.set(requestMetaNodeIndex.incrementAndGet()
                                             % getClusterMembers().size());
                    // if failure, try again with another meta server.
                    // good luck with that. :)
                    doRefresh(retryTimes + 1);
                }

                @Override
                public Executor getExecutor() {
                    return scheduled;
                }
            }, 5000);
    }

    @Override
    public void refresh() {
        if (!(getLifecycleState().isStarting() || getLifecycleState().isStarted())) {
            if (logger.isWarnEnabled()) {
                logger.warn("[refresh][not started yet]{}", getDc());
            }
            return;
        }
        counter.incrementAndGet();
        timestamp.set(System.currentTimeMillis());
        if (logger.isInfoEnabled()) {
            logger.info("[refresh][{}][times-{}] start", getDc(), getRefreshCount());
        }
        doRefresh(0);
        if (logger.isInfoEnabled()) {
            logger.info("[refresh][{}][times-{}] end", getDc(), getRefreshCount());
        }
    }

    @Override
    public long getRefreshCount() {
        return counter.get();
    }

    @Override
    public long getLastRefreshTime() {
        return timestamp.get();
    }

    private MetaNode getRemoteMetaServer() {
        return getClusterMembers().get(requestMetaNodeIndex.get());
    }

    @VisibleForTesting
    protected DefaultCrossDcMetaServer setMetaServer(SlotAllocator allocator) {
        if (getLifecycleState().isStarted()) {
            throw new IllegalStateException("cannot reset meta-server instance while started");
        }
        this.allocator = allocator;
        return this;
    }

    private String getServiceId() {
        return String.format("%s-%s", RaftMetaServerListStorage.SERVICE_ID_PREFIX, dcName);
    }

    public class RaftMetaServerListStorage implements MetaServerListStorage {

        private static final String SERVICE_ID_PREFIX = "DefaultCrossDcMetaServer.RaftMetaServerListStorage";

        public void tryUpdateRemoteDcMetaServerList(DataCenterNodes<MetaNode> response) {
            String remoteDc = response.getDataCenterId();
            if (!getDc().equalsIgnoreCase(remoteDc)) {
                throw new IllegalArgumentException(String.format(
                    "MetaServer List Response not correct, ask [%s], received [%s]", getDc(),
                    remoteDc));
            }
            DefaultCrossDcMetaServer.this.lock.writeLock().lock();
            try {
                Long remoteVersion = response.getVersion();
                if (remoteVersion <= currentVersion.get()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "[tryUpdateRemoteDcMetaServerList]shall ignore as version is left behind, "
                                    + "remote[{}], current[{}]", remoteVersion,
                            currentVersion.get());
                    }
                    return;
                }
                if (logger.isWarnEnabled()) {
                    logger
                        .warn(
                            "[tryUpdateRemoteDcMetaServerList][{}] remote meta server changed, \nbefore: {}, \nafter: {}",
                            getDc(), DefaultCrossDcMetaServer.this.metaServers.get(),
                            response.getNodes() != null ? response.getNodes().values() : "None");
                }
                currentVersion.set(remoteVersion);
                if (response.getNodes() != null) {
                    metaServers.set(Lists.newArrayList(response.getNodes().values()));
                }
            } finally {
                DefaultCrossDcMetaServer.this.lock.writeLock().unlock();
            }

        }

        private final void registerAsRaftService() {
            Processor.getInstance().addWorker(getServiceId(), MetaServerListStorage.class,
                RaftMetaServerListStorage.this);
        }

    }

    public interface MetaServerListStorage {
        void tryUpdateRemoteDcMetaServerList(DataCenterNodes<MetaNode> response);
    }

    @VisibleForTesting
    DefaultCrossDcMetaServer setRaftStorage(MetaServerListStorage raftStorage) {
        this.raftStorage = raftStorage;
        return this;
    }

    @VisibleForTesting
    DefaultCrossDcMetaServer setAllocator(SlotAllocator allocator) {
        this.allocator = allocator;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultCrossDcMetaServer{" + "dcName='" + dcName + '\'' + ", initMetaAddresses="
               + initMetaAddresses + ", lastRefreshTime=" + timestamp + '}';
    }
}
