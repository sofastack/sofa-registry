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
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.util.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LocalSlotManagerTest extends AbstractTest {

    private LocalSlotManager slotManager;

    @Mock
    private NodeConfig       nodeConfig;

    @Before
    public void beforeLocalSlotManagerTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        slotManager.postConstruct();
    }

    @After
    public void afterLocalSlotManagerTest() throws Exception {
        slotManager.preDestroy();
    }

    @Test
    public void testRefresh() {
        slotManager = spy(slotManager);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()), new DataNode(
                randomURL(randomIp()), getDc()));
        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        verify(slotManager, times(1)).setSlotTable(any());
        Assert.assertFalse(slotManager.getDataNodeManagedSlot(dataNodes.get(0), false).getLeaders()
            .isEmpty());
        Assert.assertTrue(slotManager.getDataNodeManagedSlot(dataNodes.get(0), true).getFollowers()
            .isEmpty());
    }

    @Test
    public void testRefreshWithLowerEpoch() {
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()), new DataNode(
                randomURL(randomIp()), getDc()));
        SlotTable slotTable = new SlotTableGenerator(dataNodes).createSlotTable();
        slotManager = spy(slotManager);
        slotManager.refresh(slotTable);
        SlotTable lowerEpochTable = new SlotTable(10, slotTable.getSlotMap());
        slotManager.refresh(lowerEpochTable);
        verify(slotManager, times(1)).setSlotTable(any());
    }

    @Test
    public void testConcurrentModification() throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(2);
        CountDownLatch latch = new CountDownLatch(2);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()));

        executors.execute(()->{
            try {
                barrier.await();
                slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
            } catch (Exception e) {}
            latch.countDown();
        });
        AtomicReference<SlotTable> ref = new AtomicReference<>();
        executors.execute(()->{
            try {
                barrier.await();
            } catch (Exception e) {

            }
            ref.set(slotManager.getSlotTable());
            latch.countDown();
        });
        latch.await();
        Assert.assertNotNull(ref.get());
    }

    @Test
    public void testGetDataNodeManagedSlot() {
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()));
        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        DataNodeSlot dataNodeSlot = slotManager.getDataNodeManagedSlot(dataNodes.get(0), false);
        assertEquals(dataNodes.get(0).getIp(), dataNodeSlot.getDataNode());
        dataNodeSlot.getLeaders().forEach(slotId->
                Assert.assertEquals(slotManager.getSlotTable().getSlot(slotId).getLeader(),
                        dataNodeSlot.getDataNode()));
        dataNodeSlot.getFollowers().forEach(slotId->
                Assert.assertTrue(slotManager.getSlotTable().getSlot(slotId).getFollowers().contains(
                        dataNodeSlot.getDataNode())));
        logger.info("[leaders] {}", dataNodeSlot.getLeaders());
        logger.info("[followers] {}", dataNodeSlot.getFollowers());
        slotManager.getSlotTable().getSlotMap().forEach((slotId, slot)->{
            if(slot.getLeader().equalsIgnoreCase(dataNodeSlot.getDataNode())) {
                logger.info("[slot] {}", slotId);
                Assert.assertTrue(dataNodeSlot.getLeaders().contains(slotId));
            }
        });
    }

    @Test
    public void testSaveAndLoadSnapshot() throws IOException {
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()), new DataNode(
                randomURL(randomIp()), getDc()));
        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        slotManager.copy().save(slotManager.getSnapshotFileNames().iterator().next());
        LocalSlotManager loadSlotManager = new LocalSlotManager(nodeConfig);
        loadSlotManager.load(slotManager.getSnapshotFileNames().iterator().next());
        Assert.assertEquals(slotManager.getSlotTable(), loadSlotManager.getSlotTable());
        FileUtils.forceDelete(new File(slotManager.getSnapshotFileNames().iterator().next()));
    }

    @Test
    public void testGetSlotNums() {
        Assert.assertEquals(SlotConfig.SLOT_NUM, slotManager.getSlotNums());
    }

    @Test
    public void testGetSlotReplicaNums() {
        Assert.assertEquals(SlotConfig.SLOT_REPLICAS, slotManager.getSlotReplicaNums());
    }
}