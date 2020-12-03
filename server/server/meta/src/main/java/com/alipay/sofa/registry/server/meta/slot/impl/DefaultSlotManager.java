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
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Proxy;

/**
 * @author chen.zhu
 * <p>
 * Dec 02, 2020
 */
public class DefaultSlotManager extends AbstractLifecycle implements SlotManager {

    @Autowired
    private RaftExchanger    raftExchanger;

    @Autowired
    private LocalSlotManager localSlotManager;

    private SlotManager      raftSlotManager;

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
        initRaftService();
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
    public void refresh(SlotTable slotTable) {
        raftSlotManager.refresh(slotTable);
    }

    @Override
    public int getSlotNums() {
        return raftSlotManager.getSlotNums();
    }

    @Override
    public int getSlotReplicaNums() {
        return raftSlotManager.getSlotReplicaNums();
    }

    @Override
    public DataNodeSlot getDataNodeManagedSlot(DataNode dataNode, boolean ignoreFollowers) {
        return raftSlotManager.getDataNodeManagedSlot(dataNode, ignoreFollowers);
    }

    @Override
    public SlotTable getSlotTable() {
        return raftSlotManager.getSlotTable();
    }

    private String getServiceId() {
        return "DefaultSlotManager.RaftService";
    }

    private void initRaftService() {
        Processor.getInstance().addWorker(getServiceId(), SlotManager.class, localSlotManager);
        raftSlotManager = (SlotManager) Proxy.newProxyInstance(Thread.currentThread()
            .getContextClassLoader(), new Class<?>[] { SlotManager.class }, new ProxyHandler(
            SlotManager.class, getServiceId(), raftExchanger.getRaftClient()));
    }
}
