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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableStats;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorder;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Dec 25, 2020
 */
public class DefaultSlotTableMonitor extends AbstractLifecycle implements SlotTableMonitor,
                                                              Observer {

    @Autowired
    private LocalSlotManager        slotManager;

    @Autowired
    private SlotGenericResource     slotGenericResource;

    private SlotTableStats          slotTableStats;

    private List<SlotTableRecorder> recorders;

    private WakeUpLoopRunnable      scheduledTask;

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
        recorders = Lists.newArrayList(new DiskSlotTableRecorder(), slotGenericResource);
        slotTableStats = new DefaultSlotTableStats(slotManager);
        slotTableStats.initialize();
        scheduledTask = new WakeUpLoopRunnable() {
            @Override
            public int getWaitingMillis() {
                return 10 * 60 * 1000;
            }

            @Override
            public void runUnthrowable() {
                recordSlotTable();
            }
        };
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        slotManager.addObserver(this);
        ConcurrentUtils.createDaemonThread(DefaultSlotTableMonitor.class.getSimpleName(),
            scheduledTask).start();
    }

    @Override
    protected void doStop() throws StopException {
        slotManager.removeObserver(this);
        if (scheduledTask != null) {
            scheduledTask.close();
        }
        super.doStop();
    }

    @Override
    public void recordSlotTable() {
        recorders.forEach(recorder -> {
            if(recorder != null) {
                recorder.record(slotManager.getSlotTable());
            }
            });
    }

    @Override
    public boolean isSlotTableStable() {
        return slotTableStats.isSlotTableStable();
    }

    @Override
    public void update(Observable source, Object message) {
        if (message instanceof SlotTable) {
            logger.warn("[update] slot-table changed, current epoch: [{}]",
                ((SlotTable) message).getEpoch());
            recordSlotTable();
            slotTableStats.updateSlotTable((SlotTable) message);
        }
    }

    @VisibleForTesting
    public DefaultSlotTableMonitor setSlotManager(LocalSlotManager slotManager) {
        this.slotManager = slotManager;
        return this;
    }

    @Override
    public void onHeartbeat(HeartbeatRequest<DataNode> heartbeat) {
        long slotTableEpoch = heartbeat.getSlotTableEpoch();
        if (slotTableEpoch < slotManager.getSlotTable().getEpoch()) {
            logger
                .warn(
                    "[onHeartbeat] data slot-table[{}] is prev version(current [{}]), won't update slot stats",
                    slotTableEpoch, slotManager.getSlotTable().getEpoch());
            return;
        }
        if (heartbeat.getSlotStatus() == null) {
            logger.warn("[onHeartbeat] empty heartbeat");
            return;
        }
        slotTableStats.checkSlotStatuses(heartbeat.getSlotStatus());
    }
}
