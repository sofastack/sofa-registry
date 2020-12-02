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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.datacenter.DataCenterAware;
import com.alipay.sofa.registry.exception.*;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.cluster.RaftInterface;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.SlotTableAware;
import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public class CrossDcSlotAllocator extends AbstractLifecycle implements SlotAllocator,
                                                           DataCenterAware, SlotTableAware {

    private final String                     dcName;

    private final ScheduledExecutorService   scheduled;

    private final CrossDcMetaServer          metaServer;

    private volatile ScheduledFuture<?>      future;

    private final AtomicReference<SlotTable> currentSlotTable = new AtomicReference<>();

    private final Exchange                   exchange;

    private final RaftExchanger              raftExchanger;

    private final AtomicInteger              index            = new AtomicInteger(0);

    private final ReadWriteLock              lock             = new ReentrantReadWriteLock();

    /** inner class for exposing less interfaces, see class desc */
    private RaftSlotTableStorage             raftStorage;

    /**
     * Constructor.
     *
     * @param dcName        the dc name
     * @param scheduled     the scheduled
     * @param exchange      the exchange
     * @param metaServer    the meta server
     * @param raftExchanger the raft exchanger
     */
    public CrossDcSlotAllocator(String dcName, ScheduledExecutorService scheduled,
                                Exchange exchange, CrossDcMetaServer metaServer,
                                RaftExchanger raftExchanger) {
        this.dcName = dcName;
        this.scheduled = scheduled;
        this.exchange = exchange;
        this.metaServer = metaServer;
        this.raftExchanger = raftExchanger;
    }

    /**
     * Gets get slot table.
     *
     * @return the get slot table
     */
    @Override
    public SlotTable getSlotTable() {
        if (!getLifecycleState().isStarted() && !getLifecycleState().isStarting()) {
            throw new IllegalStateException("[RemoteDcSlotAllocator]not available not");
        }
        return raftStorage.getSlotTable();
    }

    /**
     * Gets get dc.
     *
     * @return the get dc
     */
    @Override
    public String getDc() {
        return dcName;
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        LocalRaftSlotTableStorage localStorage = new LocalRaftSlotTableStorage();
        localStorage.registerAsRaftService();
        raftStorage = (RaftSlotTableStorage) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { RaftSlotTableStorage.class },
            new ProxyHandler(RaftSlotTableStorage.class, getServiceId(), raftExchanger
                .getRaftClient()));
    }

    @Override
    protected void doStart() throws StartException {
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (ServiceStateMachine.getInstance().isLeader()) {
                    refreshSlotTable(0);
                }
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() throws StopException {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    /**
     * Sets slot table.
     *
     * @param slotTable the slot table
     */
    @RaftInterface
    public void setSlotTable(SlotTable slotTable) {
        currentSlotTable.set(slotTable);
    }

    @VisibleForTesting
    protected long getIntervalMilli() {
        return 60 * 1000;
    }

    @VisibleForTesting
    protected void refreshSlotTable(int retryTimes) {
        if (retryTimes > 3) {
            logger.warn("[refreshSlotTable]try timeout, more than {} times", 3);
            return;
        }
        final long currentEpoch = currentSlotTable.get() == null ? -1 : currentSlotTable.get()
            .getEpoch();
        GetSlotTableRequest request = new GetSlotTableRequest(currentEpoch, null, false);
        MetaNode metaNode = getRemoteMetaServer();
        exchange.getClient(Exchange.META_SERVER_TYPE).sendCallback(metaNode.getNodeUrl(), request,
            new CallbackHandler() {
                @Override
                @SuppressWarnings("unchecked")
                public void onCallback(Channel channel, Object message) {
                    if (!(message instanceof SlotTable)) {
                        logger.error("[refreshSlotTable]wanted SlotTable, but receive: [{}]{}",
                            message.getClass(), message);
                        return;
                    }
                    SlotTable slotTable = (SlotTable) message;
                    lock.writeLock().lock();
                    try {
                        if (currentEpoch < slotTable.getEpoch()) {
                            if (logger.isWarnEnabled()) {
                                logger
                                    .warn(
                                        "[refreshSlotTable] remote slot table changed, \n prev: {} \n change to: {}",
                                        currentSlotTable.get(), slotTable);
                            }
                            raftStorage.setSlotTable(slotTable);
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
                }

                @Override
                public void onException(Channel channel, Throwable exception) {
                    if (logger.isErrorEnabled()) {
                        logger
                            .error(
                                "[refreshSlotTable][{}]Bolt Request Failure, remote: {}, will try other meta-server",
                                getDc(), channel == null ? "unknown" : channel.getRemoteAddress()
                                    .getHostName(), exception);
                    }
                    index.set(index.incrementAndGet() % metaServer.getClusterMembers().size());
                    // if failure, try again with another meta server.
                    // good luck with that. :)
                    refreshSlotTable(retryTimes + 1);
                }

                @Override
                public Executor getExecutor() {
                    return scheduled;
                }
            }, 5000);
    }

    private MetaNode getRemoteMetaServer() {
        return metaServer.getClusterMembers().get(index.get());
    }

    private final String getServiceId() {
        return String.format("%s-%s", LocalRaftSlotTableStorage.SERVICE_ID_PREFIX, dcName);
    }

    /**
     * Inner class to expose as little as interfaces to public raft service
     * Only very necessarily method is exposing through 'Processor'
     */
    public class LocalRaftSlotTableStorage implements RaftSlotTableStorage {

        public final static String SERVICE_ID_PREFIX = "CrossDcSlotAllocator.RaftSlotTableStorage";

        /**
         * Run raft protocol to set slot table.
         *
         * @param slotTable the slot table
         */
        public void setSlotTable(SlotTable slotTable) {
            CrossDcSlotAllocator.this.setSlotTable(slotTable);
        }

        private final void registerAsRaftService() {
            Processor.getInstance().addWorker(getServiceId(), RaftSlotTableStorage.class,
                LocalRaftSlotTableStorage.this);
        }

        private final void unregisterAsRaftService() {
            Processor.getInstance().removeWorker(getServiceId());
        }

        @Override
        public SlotTable getSlotTable() {
            return CrossDcSlotAllocator.this.currentSlotTable.get();
        }
    }

    public interface RaftSlotTableStorage extends SlotTableAware {
        void setSlotTable(SlotTable slotTable);
    }

    @VisibleForTesting
    CrossDcSlotAllocator setRaftStorage(RaftSlotTableStorage raftStorage) {
        this.raftStorage = raftStorage;
        return this;
    }

    @Override
    public String toString() {
        return "CrossDcSlotAllocator{" + "dcName='" + dcName + '\'' + ", metaServer=" + metaServer
               + '}';
    }
}
