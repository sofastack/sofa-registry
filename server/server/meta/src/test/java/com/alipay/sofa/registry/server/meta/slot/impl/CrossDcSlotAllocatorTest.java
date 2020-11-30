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

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.lifecycle.LifecycleState;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CrossDcSlotAllocatorTest extends AbstractTest {

    private CrossDcSlotAllocator allocator;

    @Mock
    private CrossDcMetaServer    crossDcMetaServer;

    @Mock
    private Exchange             exchange;

    @Mock
    private RaftExchanger        raft;

    private ServiceStateMachine  machine;

    @Before
    public void beforeCrossDcSlotAllocatorTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        machine = spy(ServiceStateMachine.getInstance());
        when(machine.isLeader()).thenReturn(false);
        when(crossDcMetaServer.getClusterMembers()).thenReturn(
            Lists.newArrayList(new MetaNode(randomURL(), getDc())));
        when(crossDcMetaServer.getRemotes()).thenReturn(
            Lists.newArrayList(new MetaNode(randomURL(), getDc())));
        allocator = spy(new CrossDcSlotAllocator(getDc(), scheduled, exchange, crossDcMetaServer,
            raft));
    }

    @After
    public void afterCrossDcSlotAllocatorTest() throws Exception {
        LifecycleHelper.stopIfPossible(allocator);
        LifecycleHelper.disposeIfPossible(allocator);
    }

    @Test
    public void testGetSlotTable() throws TimeoutException, InterruptedException, InitializeException, StartException {
        LifecycleHelper.initializeIfPossible(allocator);
        LifecycleHelper.startIfPossible(allocator);
        allocator.setRaftStorage(allocator.new LocalRaftSlotTableStorage());

        Assert.assertNull(allocator.getSlotTable());

        when(exchange.getClient(Exchange.META_SERVER_TYPE)).thenReturn(getRpcClient(scheduled, 1,
                new SlotTable(System.currentTimeMillis(), ImmutableMap.of(
                        1, new Slot(1, "10.0.0.1", System.currentTimeMillis(), Lists.newArrayList("10.0.0.2"))
                ))));
        allocator.refreshSlotTable(0);
        waitConditionUntilTimeOut(()->allocator.getSlotTable() != null, 1000);

        Assert.assertNotNull(allocator.getSlotTable());
        Assert.assertEquals("10.0.0.1", allocator.getSlotTable().getSlot(1).getLeader());
    }

    @Test
    public void testTestGetDc() {
        Assert.assertEquals(getDc(), allocator.getDc());
    }

    @Test
    public void testRefreshSlotTableFirstFailure() throws TimeoutException, InterruptedException, StartException, InitializeException {
        LifecycleHelper.initializeIfPossible(allocator);
        LifecycleHelper.startIfPossible(allocator);
        allocator.setRaftStorage(allocator.new LocalRaftSlotTableStorage());

        Assert.assertNull(allocator.getSlotTable());

        when(exchange.getClient(Exchange.META_SERVER_TYPE))
                .thenReturn(getRpcClient(scheduled, 3, new TimeoutException("expected timeout")))
                .thenReturn(getRpcClient(scheduled, 1,
                new SlotTable(System.currentTimeMillis(), ImmutableMap.of(
                        1, new Slot(1, "10.0.0.1", System.currentTimeMillis(), Lists.newArrayList("10.0.0.2"))
                ))));
        allocator.refreshSlotTable(0);
        waitConditionUntilTimeOut(()->allocator.getSlotTable() != null, 1000);

        Assert.assertNotNull(allocator.getSlotTable());
        Assert.assertEquals("10.0.0.1", allocator.getSlotTable().getSlot(1).getLeader());
    }

    @Test
    public void testRefreshSlotTableRetryOverTimes() throws TimeoutException, InterruptedException,
                                                    StartException, InitializeException {
        LifecycleHelper.initializeIfPossible(allocator);
        LifecycleHelper.startIfPossible(allocator);
        allocator.setRaftStorage(allocator.new LocalRaftSlotTableStorage());

        Assert.assertNull(allocator.getSlotTable());

        when(exchange.getClient(Exchange.META_SERVER_TYPE)).thenReturn(
            getRpcClient(scheduled, 3, new TimeoutException("expected timeout")));

        allocator.refreshSlotTable(0);
        Thread.sleep(20);

        Assert.assertNull(allocator.getSlotTable());
        verify(allocator, atLeast(3)).refreshSlotTable(anyInt());
    }

    //run manually
    @Test
//    @Ignore
    public void testRaftMechanismWorks() throws Exception {
        RaftExchanger raftExchanger = startRaftExchanger();

        allocator = spy(new CrossDcSlotAllocator(getDc(), scheduled, exchange, crossDcMetaServer, raftExchanger));
        when(exchange.getClient(Exchange.META_SERVER_TYPE))
                .thenReturn(getRpcClient(scheduled, 3, new TimeoutException("expected timeout")))
                .thenReturn(getRpcClient(scheduled, 1,
                        new SlotTable(System.currentTimeMillis(), ImmutableMap.of(
                                1, new Slot(1, randomIp(), System.currentTimeMillis(), Lists.newArrayList(randomIp())),
                                2, new Slot(2, randomIp(), System.currentTimeMillis(), Lists.newArrayList(randomIp())),
                                3, new Slot(3, randomIp(), System.currentTimeMillis(), Lists.newArrayList(randomIp())),
                                4, new Slot(4, randomIp(), System.currentTimeMillis(), Lists.newArrayList(randomIp()))
                        ))));

        LifecycleHelper.initializeIfPossible(allocator);
        LifecycleHelper.startIfPossible(allocator);

        allocator.refreshSlotTable(0);
        waitConditionUntilTimeOut(()->allocator.getSlotTable() != null, 2000);
        // wait for rpc safe quit
        Thread.sleep(100);
        Assert.assertNotNull(allocator.getSlotTable());
        Assert.assertEquals(4, allocator.getSlotTable().getSlotIds().size());

        raftExchanger.shutdown();
    }

    @Test
    public void testDoInitialize() throws InitializeException {
        LifecycleHelper.initializeIfPossible(allocator);
        Assert.assertEquals(LifecycleState.LifecyclePhase.INITIALIZED, allocator
            .getLifecycleState().getPhase());
    }

    @Test
    public void testDoStart() throws InitializeException, StopException, StartException {
        LifecycleHelper.initializeIfPossible(allocator);
        LifecycleHelper.startIfPossible(allocator);
        Assert.assertEquals(LifecycleState.LifecyclePhase.STARTED, allocator.getLifecycleState()
            .getPhase());
        verify(allocator, never()).refreshSlotTable(anyInt());
        verify(allocator, times(1)).doStart();
    }

    @Test
    public void testDoStop() {

    }

    @Test
    public void testDoDispose() {
    }
}