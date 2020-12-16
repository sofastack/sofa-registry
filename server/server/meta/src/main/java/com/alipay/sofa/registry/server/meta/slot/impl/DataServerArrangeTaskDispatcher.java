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

import com.alipay.sofa.jraft.util.ThreadPoolUtil;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.ArrangeTaskDispatcher;
import com.alipay.sofa.registry.server.meta.slot.tasks.InitReshardingTask;
import com.alipay.sofa.registry.server.meta.slot.tasks.ServerDeadRebalanceWork;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotReassignTask;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */

public class DataServerArrangeTaskDispatcher extends AbstractLifecycleObservable
                                                                                implements
                                                                                ArrangeTaskDispatcher<DataNode> {

    private final ConcurrentMap<DataNode, DeadServerAction> deadServerActions = Maps
                                                                                  .newConcurrentMap();

    @Autowired
    private ArrangeTaskExecutor                             arrangeTaskExecutor;

    @Autowired
    private DefaultDataServerManager                        dataServerManager;

    @Autowired
    private LocalSlotManager                                slotManager;

    @Autowired
    private DefaultSlotManager                              defaultSlotManager;

    @Autowired
    private MetaServerConfig                                metaServerConfig;

    private AtomicBoolean                                   inited            = new AtomicBoolean(
                                                                                  false);

    private AtomicBoolean                                   lock              = new AtomicBoolean(
                                                                                  false);

    private ScheduledExecutorService                        scheduled;

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
        scheduled = ThreadPoolUtil
            .newScheduledBuilder()
            .coreThreads(Math.min(OsUtils.getCpuCount(), 2))
            .poolName(DataServerArrangeTaskDispatcher.class.getSimpleName())
            .enableMetric(true)
            .threadFactory(
                new NamedThreadFactory(DataServerArrangeTaskDispatcher.class.getSimpleName()))
            .build();

    }

    @Override
    protected void doDispose() throws DisposeException {
        if (scheduled != null) {
            scheduled.shutdownNow();
            scheduled = null;
        }
        super.doDispose();
    }

    @Override
    public void serverAlive(DataNode dataNode) {
        if (logger.isInfoEnabled()) {
            logger.info("[serverAlive]{}", dataNode);
        }
        if (initSlotTableIfNeeded()) {
            lockForInitSlotTable();
        }

        if (lock.get()) {
            if (logger.isInfoEnabled()) {
                logger
                    .info("[serverAlive][{}] stop rebalance task until init slot table", dataNode);
            }
            return;
        }
        DeadServerAction deadServerAction = deadServerActions.get(new DataNode(new URL(dataNode
            .getIp()), dataNode.getDataCenter()));
        if (deadServerAction == null) {
            arrangeTaskExecutor.offer(new SlotReassignTask(slotManager, defaultSlotManager
                .getRaftSlotManager(), dataServerManager));
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[serverAlive][dead server alive]{}", dataNode);
            }
            deadServerAction.serverAlive();
        }
    }

    private boolean initSlotTableIfNeeded() {
        if (inited.compareAndSet(false, true)
            && slotManager.getSlotTable().getEpoch() == SlotTable.INIT.getEpoch()) {
            if (logger.isInfoEnabled()) {
                logger.info("[first init] lock and generate slot table after {} ms",
                    metaServerConfig.getInitialSlotTableNonChangeLockTimeMilli());
            }
            scheduled.schedule(new Runnable() {
                @Override
                public void run() {
                    arrangeTaskExecutor.offer(new InitReshardingTask(slotManager,
                        defaultSlotManager.getRaftSlotManager(), dataServerManager));
                }
            }, metaServerConfig.getInitialSlotTableNonChangeLockTimeMilli(), TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    private void lockForInitSlotTable() {
        lock.set(true);
        // release the lock after slot-table is initializing
        scheduled
            .schedule(new Runnable() {
                @Override
                public void run() {
                    lock.set(false);
                }
            }, metaServerConfig.getInitialSlotTableNonChangeLockTimeMilli() + 10,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void serverDead(DataNode dataNode) {
        if (logger.isInfoEnabled()) {
            logger.info("[serverDead]{}", dataNode.getIp());
        }
        // we only cares for data server ip
        DataNode simpleNode = new DataNode(new URL(dataNode.getIp()), dataNode.getDataCenter());
        deadServerActions.putIfAbsent(simpleNode, new DeadServerAction(simpleNode));
    }

    private int getWaitForRestartMilli() {
        return metaServerConfig.getWaitForDataServerRestartTime();
    }

    public class DeadServerAction implements Runnable {

        private DataNode           dataNode;
        private ScheduledFuture<?> future;

        public DeadServerAction(DataNode dataNode) {
            this.dataNode = dataNode;
            this.future = scheduled.schedule(this, getWaitForRestartMilli(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            cleanCache();
            arrangeTaskExecutor.offer(new ServerDeadRebalanceWork(defaultSlotManager
                .getRaftSlotManager(), slotManager, dataServerManager, dataNode));
        }

        public void serverAlive() {
            future.cancel(true);
            cleanCache();
        }

        private void cleanCache() {
            deadServerActions.remove(dataNode);
        }
    }

    /**
     *
     * For Unit Test use only
     *
     */
    @VisibleForTesting
    ConcurrentMap<DataNode, DeadServerAction> getDeadServerActions() {
        return deadServerActions;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setArrangeTaskExecutor(ArrangeTaskExecutor arrangeTaskExecutor) {
        this.arrangeTaskExecutor = arrangeTaskExecutor;
        return this;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setDataServerManager(DefaultDataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setSlotManager(LocalSlotManager slotManager) {
        this.slotManager = slotManager;
        return this;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setDefaultSlotManager(DefaultSlotManager defaultSlotManager) {
        this.defaultSlotManager = defaultSlotManager;
        return this;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setInited(AtomicBoolean inited) {
        this.inited = inited;
        return this;
    }

    @VisibleForTesting
    DataServerArrangeTaskDispatcher setMetaServerConfig(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
        return this;
    }
}
