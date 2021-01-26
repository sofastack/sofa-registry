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
package com.alipay.sofa.registry.server.meta.slot.arrange;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.*;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.data.DataManagerObserver;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.assigner.DefaultSlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.balance.DefaultSlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.DataNodeComparator;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chen.zhu
 * <p>
 * Jan 14, 2021
 */
public class ScheduledSlotArranger extends AbstractLifecycleObservable implements DataManagerObserver {

    @Autowired
    private DefaultDataServerManager dataServerManager;

    @Autowired
    private LocalSlotManager         slotManager;

    @Autowired
    private DefaultSlotManager       defaultSlotManager;

    private final ScheduledSlotArrangeTask task = new ScheduledSlotArrangeTask();

    private final AtomicBoolean lock = new AtomicBoolean(false);

    public ScheduledSlotArranger() {
    }

    public ScheduledSlotArranger(DefaultDataServerManager dataServerManager,
                                 LocalSlotManager slotManager, DefaultSlotManager defaultSlotManager) {
        this.dataServerManager = dataServerManager;
        this.slotManager = slotManager;
        this.defaultSlotManager = defaultSlotManager;
    }

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
        dataServerManager.addObserver(this);
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        Thread executor = ConcurrentUtils.createDaemonThread(getClass().getSimpleName(), task);
        executor.start();
    }

    @Override
    protected void doStop() throws StopException {
        task.close();
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        dataServerManager.removeObserver(this);
        super.doDispose();
    }

    @Override
    public void update(Observable source, Object message) {
        logger.warn("[update] receive from [{}], message: {}", source, message);
        if (message instanceof NodeRemoved) {
            task.wakeup();
        }
        if (message instanceof NodeAdded) {
            task.wakeup();
        }
    }

    public boolean tryLock() {
        return lock.compareAndSet(false, true);
    }

    public void unlock() {
        lock.set(false);
    }

    private boolean isRaftLeader() {
        return ServiceStateMachine.getInstance().isLeader();
    }

    private boolean hasAnySlotsToAssign() {
        SlotTable prevSlotTable = slotManager.getSlotTable();
        List<String> currentDataNodes = NodeUtils.transferNodeToIpList(dataServerManager.getClusterMembers());
        DataNodeComparator comparator = new DataNodeComparator(prevSlotTable.getDataServers(), currentDataNodes);

        SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotManager.getSlotNums(), slotManager.getSlotReplicaNums());
        slotTableBuilder.init(prevSlotTable, currentDataNodes);

        comparator.getRemoved().forEach(slotTableBuilder::removeDataServerSlots);

        return slotTableBuilder.hasNoAssignedSlots();
    }

    private void assignSlots() {
        SlotTable slotTable = new DefaultSlotAssigner(slotManager, dataServerManager).assign();
        if (slotTable != null && !SlotTableUtils.isValidSlotTable(slotTable)) {
            throw new SofaRegistrySlotTableException("slot table is not valid: \n"
                    + JsonUtils.writeValueAsString(slotTable));
        }
        if (slotTable != null && slotTable.getEpoch() > slotManager.getSlotTable().getEpoch()) {
            defaultSlotManager.refresh(slotTable);
        }
    }

    private void balanceSlots() {
        SlotTable slotTable = new DefaultSlotBalancer(slotManager, dataServerManager).balance();
        if (!SlotTableUtils.isValidSlotTable(slotTable)) {
            throw new SofaRegistrySlotTableException("slot table is not valid: \n"
                    + JsonUtils.writeValueAsString(slotTable));
        }
        if (slotTable != null && slotTable.getEpoch() > slotManager.getSlotTable().getEpoch()) {
            defaultSlotManager.refresh(slotTable);
        }
    }

    public class ScheduledSlotArrangeTask extends WakeUpLoopRunnable {

        private final int waitingMillis = Integer.getInteger("slot.arrange.interval.milli", 3000);

        @Override
        public int getWaitingMillis() {
            return waitingMillis;
        }

        @Override
        public void runUnthrowable() {
            if (isRaftLeader()) {
                if (dataServerManager.getClusterMembers().isEmpty()) {
                    logger.warn("[ScheduledSlotArrangeTask] empty data server list, continue");
                } else {
                    tryBalanceSlots();
                }
            }
        }

        private void tryBalanceSlots() {
            if(!tryLock()) {
                return;
            }
            try {
                if (hasAnySlotsToAssign()) {
                    logger.info("[re-assign][begin] assign slots to data-server");
                    assignSlots();
                    logger.info("[re-assign][end]");
                } else {
                    logger.info("[balance][begin] balance slots to data-server");
                    balanceSlots();
                    logger.info("[balance][end]");
                }
            } finally {
                unlock();
            }
        }
    }

    public ScheduledSlotArrangeTask getTask() {
        return task;
    }
}
