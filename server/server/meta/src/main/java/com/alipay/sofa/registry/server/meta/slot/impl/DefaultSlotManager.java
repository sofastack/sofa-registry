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

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.LeaderAware;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SCHEDULED_EXECUTOR;

/**
 * @author chen.zhu
 * <p>
 * Nov 13, 2020
 */
@Component
public class DefaultSlotManager extends AbstractLifecycle implements SlotManager, LeaderAware {

    //    private LocalStorageSlotManager raftSlotTableManager;

    @Autowired
    private RaftExchanger              raftExchanger;

    @Resource(name = SCHEDULED_EXECUTOR)
    private ScheduledExecutorService   scheduled;

    private ScheduledFuture<?>         future;

    private AtomicReference<SlotTable> currentSlotTable = new AtomicReference<>();

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        //        LocalStorageSlotManager localRepo = new LocalStorageSlotManager();
        //        localRepo.registerAsRaftService();
        //        raftSlotTableManager = (LocalStorageSlotManager) Proxy.newProxyInstance(
        //                Thread.currentThread().getContextClassLoader(),
        //                new Class<?>[] {LocalStorageSlotManager.class },
        //                new ProxyHandler(LocalStorageSlotManager.class, getServiceId(), raftExchanger.getRaftClient()));
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
    }

    @Override
    protected void doStop() throws StopException {
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    @Override
    public SlotTable getSlotTable() {
        //        return raftSlotTableManager.getSlotTable();
        return currentSlotTable.get();
    }

    @Override
    public void rebalance() {
        //        raftSlotTableManager.rebalance();
    }

    @Override
    public long getSlotNums() {
        return 16384;
    }

    @Override
    public int getSlotReplicaNums() {
        return 3;
    }

    @Override
    public List<DataNodeSlot> getDataNodeManagedSlots(boolean ignoreFollowers) {
        return null;
    }

    @Override
    public DataNodeSlot getDataNodeManagedSlot(DataNode dataNode, boolean ignoreFollowers) {
        return null;
    }

    public void setSlotTable(SlotTable slotTable) {
        this.currentSlotTable.set(slotTable);
    }

    private String getServiceId() {
        return "LocalStorageSlotManager";
    }

    @Override
    public void isLeader() {
        initCheck();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (ServiceStateMachine.getInstance().isLeader()) {
                    peroidCheck();
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

    }

    private void peroidCheck() {

    }

    private int getIntervalMilli() {
        return 60 * 1000;
    }

}
