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
package com.alipay.sofa.registry.server.meta.monitor;

import com.alipay.sofa.jraft.util.ThreadPoolUtil;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorder;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author chen.zhu
 * <p>
 * Dec 25, 2020
 */
public class DefaultSlotTableMonitor extends AbstractLifecycle implements SlotTableMonitor,
                                                              Observer {

    @Autowired
    private LocalSlotManager         slotManager;

    @Autowired
    private SlotGenericResource slotGenericResource;

    private List<SlotTableRecorder>  recorders;

    private ScheduledExecutorService scheduled;

    private ScheduledFuture<?>       future;

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
        recorders = Lists.newArrayList(
                new DiskSlotTableRecorder(),
                slotGenericResource);
        scheduled = ThreadPoolUtil.newScheduledBuilder()
            .coreThreads(Math.min(OsUtils.getCpuCount(), 2))
            .threadFactory(new NamedThreadFactory(DefaultSlotTableMonitor.class.getSimpleName()))
            .poolName(DefaultSlotTableMonitor.class.getSimpleName()).enableMetric(true).build();
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        slotManager.addObserver(this);
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                recordSlotTable();
            }
        }, 1, 10, TimeUnit.MINUTES);
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
        scheduled.shutdownNow();
        super.doDispose();
    }

    @Override
    public void recordSlotTable() {
        recorders.forEach(recorder -> recorder.record(slotManager.getSlotTable()));
    }

    @Override
    public void update(Observable source, Object message) {
        if (message instanceof SlotTable) {
            logger.warn("[update] slot-table changed");
            recordSlotTable();
        }
    }

    @VisibleForTesting
    DefaultSlotTableMonitor setSlotManager(LocalSlotManager slotManager) {
        this.slotManager = slotManager;
        return this;
    }
}
