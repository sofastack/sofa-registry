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
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.LeaderAware;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotArranger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.tasks.InitReshardingTask;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotLeaderRebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotReassignTask;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SCHEDULED_EXECUTOR;

/**
 * @author chen.zhu
 * <p>
 * Nov 13, 2020
 */

public class LocalSlotManager extends AbstractLifecycle implements SlotManager, LeaderAware {

    @Autowired
    private DefaultDataServerManager   dataServerManager;

    @Autowired
    private MetaServerConfig           metaServerConfig;

    @Autowired
    private NodeConfig                 nodeConfig;

    @Autowired
    private ArrangeTaskExecutor        arrangeTaskExecutor;

    @Autowired
    private DefaultSlotManager         defaultSlotManager;

    private ScheduledExecutorService   scheduled;

    private ScheduledFuture<?>         future;

    private final ReadWriteLock              lock = new ReentrantReadWriteLock();

    private final AtomicReference<SlotTable> currentSlotTable = new AtomicReference<>(SlotTable.INIT);

    private Map<DataNode, DataNodeSlot> reverseMap = ImmutableMap.of();

    private final AtomicReference<SlotPeriodCheckType> currentCheck = new AtomicReference<>(SlotPeriodCheckType.CHECK_SLOT_ASSIGNMENT_BALANCE);

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    @PreDestroy
    public void preDestroy() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        scheduled = ThreadPoolUtil.newScheduledBuilder()
                .coreThreads(Math.min(4, OsUtils.getCpuCount()))
                .enableMetric(true)
                .poolName(getClass().getSimpleName())
                .threadFactory(new NamedThreadFactory(getClass().getSimpleName()))
                .build();
    }

    @Override
    protected void doDispose() throws DisposeException {
        if(scheduled != null) {
            scheduled.shutdownNow();
            scheduled = null;
        }
        super.doDispose();
    }

    @Override
    public SlotTable getSlotTable() {
        lock.readLock().lock();
        try {
            return currentSlotTable.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void refresh(SlotTable slotTable) {
        lock.writeLock().lock();
        try {
            if(slotTable.getEpoch() <= currentSlotTable.get().getEpoch()) {
                if(logger.isWarnEnabled()) {
                    logger.warn("[refresh]receive slot table,but epoch({}) is smaller than current({})",
                            slotTable.getEpoch(), currentSlotTable.get().getEpoch());
                }
            }
            setSlotTable(slotTable);
            refreshReverseMap();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void refreshReverseMap() {
        Map<DataNode, DataNodeSlot> newMap = Maps.newHashMap();
        List<DataNodeSlot> dataNodeSlots = getSlotTable().transfer(null, false);
        dataNodeSlots.forEach(dataNodeSlot -> newMap.put(
                new DataNode(new URL(dataNodeSlot.getDataNode()), nodeConfig.getLocalDataCenter()),
                dataNodeSlot));
        this.reverseMap = ImmutableMap.copyOf(newMap);
    }

    @Override
    public int getSlotNums() {
        return metaServerConfig.getTotalSlotNumber();
    }

    @Override
    public int getSlotReplicaNums() {
        return metaServerConfig.getSlotReplicas();
    }

    @Override
    public DataNodeSlot getDataNodeManagedSlot(DataNode dataNode, boolean ignoreFollowers) {
        lock.readLock().lock();
        try {
            DataNodeSlot target = reverseMap.get(dataNode);
            if(target == null) {
                return new DataNodeSlot(dataNode.getIp());
            }
            return target.fork(ignoreFollowers);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setSlotTable(SlotTable slotTable) {
        this.currentSlotTable.set(slotTable);
    }

    @Override
    public void isLeader() {
        initCheck();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (ServiceStateMachine.getInstance().isLeader()) {
                    currentCheck.set(currentCheck.get()
                            .action(arrangeTaskExecutor, LocalSlotManager.this, defaultSlotManager, dataServerManager)
                            .next());
                }
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void notLeader() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    private void initCheck() {
        if(currentSlotTable.get().getEpoch() != SlotTable.INIT.getEpoch()) {
            if (logger.isInfoEnabled()) {
                logger.info("[initCheck] slot table(version: {}) not empty, quit init slot table",
                        currentSlotTable.get().getEpoch());
            }
            return;
        }
        arrangeTaskExecutor.offer(new InitReshardingTask(this, defaultSlotManager, dataServerManager));
    }

    private int getIntervalMilli() {
        return 60 * 1000;
    }

    public enum SlotPeriodCheckType {
        CHECK_SLOT_ASSIGNMENT_BALANCE {
            @Override
            SlotPeriodCheckType next() {
                return CHECK_SLOT_LEADER_BALANCE;
            }

            @Override
            SlotPeriodCheckType action(ArrangeTaskExecutor arrangeTaskExecutor, LocalSlotManager localSlotManager,
                                       SlotManager raftSlotManager, DefaultDataServerManager dataServerManager) {
                arrangeTaskExecutor.offer(new SlotReassignTask(localSlotManager, raftSlotManager, dataServerManager));
                return this;
            }
        },
        CHECK_SLOT_LEADER_BALANCE {
            @Override
            SlotPeriodCheckType next() {
                return CHECK_SLOT_ASSIGNMENT_BALANCE;
            }

            @Override
            SlotPeriodCheckType action(ArrangeTaskExecutor arrangeTaskExecutor, LocalSlotManager localSlotManager,
                                       SlotManager raftSlotManager, DefaultDataServerManager dataServerManager) {
                arrangeTaskExecutor.offer(new SlotLeaderRebalanceTask(localSlotManager, raftSlotManager, dataServerManager));
                return this;
            }
        };

        abstract SlotPeriodCheckType next();
        abstract SlotPeriodCheckType action(ArrangeTaskExecutor arrangeTaskExecutor, LocalSlotManager localSlotManager,
                                            SlotManager raftSlotManager, DefaultDataServerManager dataServerManager);
    }

}
