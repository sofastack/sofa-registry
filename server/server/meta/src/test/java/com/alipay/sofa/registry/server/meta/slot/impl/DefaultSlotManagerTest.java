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
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class DefaultSlotManagerTest extends AbstractTest {

    private DefaultSlotManager       slotManager;

    private LocalSlotManager         localSlotManager;

    private SlotManager              raftSlotManager;

    @Mock
    private MetaServerConfig         metaServerConfig;

    @Mock
    private ArrangeTaskExecutor      arrangeTaskExecutor;

    @Mock
    private DefaultDataServerManager dataServerManager;

    @Mock
    private NodeConfig               nodeConfig;

    @Before
    public void beforeDefaultSlotManagerTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        when(metaServerConfig.getExpireCheckIntervalMilli()).thenReturn(60);
        localSlotManager = spy(new LocalSlotManager(nodeConfig));
        raftSlotManager = spy(new LocalSlotManager(nodeConfig));
        slotManager = new DefaultSlotManager().setDataServerManager(dataServerManager)
            .setArrangeTaskExecutor(arrangeTaskExecutor).setMetaServerConfig(metaServerConfig)
            .setLocalSlotManager(localSlotManager).setRaftSlotManager(raftSlotManager);
        slotManager = spy(slotManager);
        slotManager.postConstruct();
    }

    @After
    public void afterDefaultSlotManagerTest() throws Exception {
        slotManager.preDestroy();
    }

    @Test
    public void testNonLeaderWillNotGenerateSlotTable() {
        verify(slotManager, never()).initCheck();
    }

    @Test
    public void testLeaderWillGenerateSlotTable() throws Exception {
        localSlotManager = spy(new LocalSlotManager(nodeConfig));
        raftSlotManager = spy(new LocalSlotManager(nodeConfig));
        slotManager = new DefaultSlotManager().setDataServerManager(dataServerManager)
            .setArrangeTaskExecutor(arrangeTaskExecutor).setMetaServerConfig(metaServerConfig)
            .setLocalSlotManager(localSlotManager).setRaftSlotManager(raftSlotManager);
        slotManager = spy(slotManager);
        when(metaServerConfig.getExpireCheckIntervalMilli()).thenReturn(1);
        makeRaftLeader();
        slotManager.postConstruct();
        verify(slotManager, atLeast(1)).initCheck();
        verify(arrangeTaskExecutor, atLeast(1)).offer(any());

        Thread.sleep(1010);
        verify(arrangeTaskExecutor, atLeast(2)).offer(any());
    }

    @Test
    public void testGetSlotNums() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        Assert.assertEquals(SlotConfig.SLOT_NUM, slotManager.getSlotNums());
        verify(localSlotManager, times(1)).getSlotNums();
        verify(raftSlotManager, never()).getSlotNums();

        makeRaftNonLeader();
        Assert.assertEquals(SlotConfig.SLOT_NUM, slotManager.getSlotNums());
        verify(localSlotManager, times(1)).getSlotNums();
        verify(raftSlotManager, times(1)).getSlotNums();
    }

    @Test
    public void testGetSlotReplicaNums() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        Assert.assertEquals(SlotConfig.SLOT_REPLICAS, slotManager.getSlotReplicaNums());
        verify(localSlotManager, times(1)).getSlotReplicaNums();
        verify(raftSlotManager, never()).getSlotReplicaNums();

        makeRaftNonLeader();
        Assert.assertEquals(SlotConfig.SLOT_REPLICAS, slotManager.getSlotReplicaNums());
        verify(localSlotManager, times(1)).getSlotReplicaNums();
        verify(raftSlotManager, times(1)).getSlotReplicaNums();
    }

    @Test
    public void testGetSlotTable() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        Assert.assertEquals(SlotTable.INIT, slotManager.getSlotTable());
        verify(localSlotManager, times(1)).getSlotTable();
        verify(raftSlotManager, never()).getSlotTable();

        makeRaftNonLeader();
        Assert.assertEquals(SlotTable.INIT, slotManager.getSlotTable());
        verify(localSlotManager, times(1)).getSlotTable();
        verify(raftSlotManager, times(1)).getSlotTable();
    }

    @Test
    public void testRefresh() {
        SlotTable slotTable = new SlotTableGenerator(Lists.newArrayList(new DataNode(
            randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()), getDc())))
            .createSlotTable();
        slotManager.refresh(slotTable);
        verify(raftSlotManager, times(1)).refresh(slotTable);
    }

    @Test
    public void testGetRaftSlotManager() {
        Assert.assertEquals(raftSlotManager, slotManager.getRaftSlotManager());
    }

    @Test
    public void testGetDataNodeManagedSlot() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        Assert.assertNotNull(slotManager.getDataNodeManagedSlot(new DataNode(randomURL(), getDc()),
            false));
        verify(localSlotManager, times(1)).getDataNodeManagedSlot(any(), anyBoolean());
        verify(raftSlotManager, never()).getDataNodeManagedSlot(any(), anyBoolean());

        makeRaftNonLeader();
        Assert.assertNotNull(slotManager.getDataNodeManagedSlot(new DataNode(randomURL(), getDc()),
            false));
        verify(localSlotManager, times(1)).getDataNodeManagedSlot(any(), anyBoolean());
        verify(raftSlotManager, times(1)).getDataNodeManagedSlot(any(), anyBoolean());
    }

    @Test
    public void testInitCheck() {
        SlotTable slotTable = new SlotTableGenerator(Lists.newArrayList(new DataNode(
            randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()), getDc())))
            .createSlotTable();
        localSlotManager.refresh(slotTable);
        slotManager.initCheck();
        verify(arrangeTaskExecutor, never()).offer(any());
    }
}