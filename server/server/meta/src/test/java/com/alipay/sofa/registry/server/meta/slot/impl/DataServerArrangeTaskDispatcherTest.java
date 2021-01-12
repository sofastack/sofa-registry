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
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.tasks.init.InitReshardingTask;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotReassignTask;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

public class DataServerArrangeTaskDispatcherTest extends AbstractTest {

    private DataServerArrangeTaskDispatcher dispatcher;

    private ArrangeTaskExecutor             arrangeTaskExecutor;

    @Mock
    private DefaultDataServerManager        dataServerManager;

    private LocalSlotManager                slotManager;

    @Mock
    private MetaServerConfig                metaServerConfig;

    @Mock
    private NodeConfig                      nodeConfig;

    @Mock
    private DefaultSlotManager              defaultSlotManager;

    @Before
    public void beforeDataServerArrangeTaskDispatcherTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        arrangeTaskExecutor = spy(new ArrangeTaskExecutor());
        dispatcher = new DataServerArrangeTaskDispatcher();
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        when(defaultSlotManager.getRaftSlotManager()).thenReturn(slotManager);
        when(metaServerConfig.getInitialSlotTableNonChangeLockTimeMilli()).thenReturn(100);
        when(metaServerConfig.getWaitForDataServerRestartTime()).thenReturn(15000);
        dispatcher.setDataServerManager(dataServerManager)
            .setDefaultSlotManager(defaultSlotManager).setSlotManager(slotManager)
            .setArrangeTaskExecutor(arrangeTaskExecutor).setMetaServerConfig(metaServerConfig);
        arrangeTaskExecutor.postConstruct();
        dispatcher.postConstruct();
    }

    @After
    public void afterDataServerArrangeTaskDispatcherTest() throws Exception {
        dispatcher.preDestory();
        arrangeTaskExecutor.preDestroy();
    }

    @Test
    public void testServerDeadButFastAlive() throws InterruptedException {
        dispatcher.setInited(new AtomicBoolean(true));
        DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
        dispatcher.serverDead(dataNode);
        Assert.assertFalse(dispatcher.getDeadServerActions().isEmpty());
        Thread.sleep(10);
        dispatcher.serverAlive(dataNode);
        Thread.sleep(10);
        Assert.assertTrue(dispatcher.getDeadServerActions().isEmpty());
    }

    @Test
    public void testServerDead() throws InterruptedException, TimeoutException {
        List<DataNode> dataNodes = Lists.newArrayList(
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc())
                );
        SlotTable slotTable = new SlotTableGenerator(dataNodes).createSlotTable();
        slotManager.refresh(slotTable);
        DataNode dataNode = dataNodes.get(0);
        DataNodeSlot dataNodeSlot = slotManager.getDataNodeManagedSlot(dataNode, false);
        when(metaServerConfig.getWaitForDataServerRestartTime()).thenReturn(10);

        List<DataNode> currentDataNodes = Lists.newArrayList(dataNodes);
        Assert.assertTrue(currentDataNodes.remove(dataNode));
        when(dataServerManager.getClusterMembers()).thenReturn(currentDataNodes);

        dispatcher.serverDead(dataNode);
        waitConditionUntilTimeOut(()->slotManager.getSlotTable().getEpoch() > slotTable.getEpoch(), 500000);

        dataNodeSlot.getLeaders().forEach(slotId->{
            Assert.assertNotEquals(dataNodeSlot.getDataNode(), slotManager.getSlotTable().getSlot(slotId).getLeader());
        });

    }

    @Test
    public void testInitSlotTable() throws InterruptedException, TimeoutException {
        when(metaServerConfig.getInitialSlotTableNonChangeLockTimeMilli()).thenReturn(200);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);

        makeRaftLeader();
        dispatcher.serverAlive(dataNodes.get(0));
        dispatcher.serverAlive(dataNodes.get(1));
        dispatcher.serverAlive(dataNodes.get(2));
        Thread.sleep(210);

        verify(arrangeTaskExecutor, atMost(1)).offer(any());
        verify(arrangeTaskExecutor, times(1)).offer(any(InitReshardingTask.class));
        Thread.sleep(100);

        Assert.assertNotEquals(SlotTable.INIT.getEpoch(), slotManager.getSlotTable().getEpoch());
    }

    @Test
    public void testServerAlive() throws InterruptedException {
        dispatcher.setInited(new AtomicBoolean(true));
        DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
        dispatcher.serverAlive(dataNode);
        verify(arrangeTaskExecutor, atLeast(1)).offer(any(SlotReassignTask.class));
    }
}