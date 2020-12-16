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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class DefaultLocalMetaServerTest extends AbstractTest {

    private DefaultLocalMetaServer metaServer  = new DefaultLocalMetaServer();

    private SlotManager            slotManager = new LocalSlotManager();

    @Before
    public void beforeDefaultLocalMetaServerTest() throws Exception {
        slotManager = spy(slotManager);
        metaServer.setSlotManager(slotManager);
        metaServer = spy(metaServer);
        LifecycleHelper.initializeIfPossible(metaServer);
        LifecycleHelper.startIfPossible(metaServer);
    }

    @After
    public void afterDefaultLocalMetaServerTest() throws Exception {
        LifecycleHelper.stopIfPossible(metaServer);
        LifecycleHelper.disposeIfPossible(metaServer);
    }

    @Test
    public void testGetSlotTable() {
        when(slotManager.getSlotTable()).thenReturn(new SlotTable(0L, Maps.newHashMap()));
        metaServer.getSlotTable();
        verify(slotManager, times(1)).getSlotTable();
    }

    @Test
    public void testGetEpoch() {
        metaServer.updateClusterMembers(Lists.newArrayList(), 2L);
        Assert.assertEquals(2L, metaServer.getEpoch());
    }

    @Test
    public void testGetClusterMembers() {
        List<MetaNode> metaNodeList = Lists.newArrayList(new MetaNode(randomURL(), getDc()),
            new MetaNode(randomURL(), getDc()), new MetaNode(randomURL(), getDc()));
        metaServer.updateClusterMembers(metaNodeList, 2L);
        Assert.assertEquals(metaNodeList, metaServer.getClusterMembers());
    }

    @Test
    public void testUpdateClusterMembers() throws InterruptedException {
        int tasks = 1000;
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (Exception ignore) {
                    }

                    metaServer.updateClusterMembers(Lists.newArrayList(new MetaNode(randomURL(),
                        getDc()), new MetaNode(randomURL(), getDc()), new MetaNode(randomURL(),
                        getDc())), DatumVersionUtil.nextId());
                    latch.countDown();
                }
            });

        }
        latch.await(2, TimeUnit.SECONDS);
        verify(metaServer, times(tasks)).updateClusterMembers(anyList(), anyLong());

    }

    @Test
    public void testRenew() throws InterruptedException {
        int tasks = 1000;
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (Exception ignore) {
                    }
                    metaServer.renew(new MetaNode(randomURL(), getDc()));
                    latch.countDown();
                }
            });

        }
        latch.await(2, TimeUnit.SECONDS);
        verify(metaServer, times(tasks)).renew(any());
    }

    @Test
    public void testCancel() throws InterruptedException {
        int tasks = 1000;
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (Exception ignore) {
                    }
                    metaServer.renew(new MetaNode(randomURL(), getDc()));
                    latch.countDown();
                }
            });

        }
        latch.await(2, TimeUnit.SECONDS);
        verify(metaServer, times(tasks)).renew(any());

        CyclicBarrier barrier2 = new CyclicBarrier(tasks);
        CountDownLatch latch2 = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier2.await();
                    } catch (Exception ignore) {
                    }
                    metaServer.cancel(new MetaNode(randomURL(), getDc()));
                    latch2.countDown();
                }
            });

        }
        latch2.await(2, TimeUnit.SECONDS);
        verify(metaServer, times(tasks)).cancel(any());
    }

    @Test
    public void testGetSnapshotFileNames() {
        Set<String> expected = Sets.newHashSet();
        expected.add("DefaultLocalMetaServer");
        Assert.assertEquals(expected, metaServer.getSnapshotFileNames());
    }

    @Test
    public void testTestSaveAndLoad() {
        List<MetaNode> metaNodeList = Lists.newArrayList(new MetaNode(randomURL(), getDc()),
            new MetaNode(randomURL(), getDc()), new MetaNode(randomURL(), getDc()));
        metaServer.updateClusterMembers(metaNodeList, DatumVersionUtil.nextId());
        metaServer.save(metaServer.copy().getSnapshotFileNames().iterator().next());
        DefaultLocalMetaServer loadMetaServer = new DefaultLocalMetaServer();
        loadMetaServer.load("DefaultLocalMetaServer");
        Assert.assertEquals(metaNodeList, loadMetaServer.getClusterMembers());
    }
}