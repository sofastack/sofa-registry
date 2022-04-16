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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.LifecycleState;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CrossDcSlotAllocatorTest extends AbstractMetaServerTestBase {

  private CrossDcSlotAllocator allocator;

  @Mock private CrossDcMetaServer crossDcMetaServer;

  @Mock private Exchange exchange;

  @Mock private MetaLeaderService leaderElector;

  @Before
  public void beforeCrossDcSlotAllocatorTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(crossDcMetaServer.getClusterMembers())
        .thenReturn(Lists.newArrayList(new MetaNode(randomURL(), getDc())));
    when(crossDcMetaServer.getClusterMembers())
        .thenReturn(Lists.newArrayList(new MetaNode(randomURL(), getDc())));
    allocator =
        spy(
            new CrossDcSlotAllocator(
                getDc(), scheduled, exchange, crossDcMetaServer, leaderElector));
  }

  @After
  public void afterCrossDcSlotAllocatorTest() throws Exception {
    LifecycleHelper.stopIfPossible(allocator);
    LifecycleHelper.disposeIfPossible(allocator);
  }

  @Test
  public void testGetSlotTable()
      throws TimeoutException, InterruptedException, InitializeException, StartException {
    LifecycleHelper.initializeIfPossible(allocator);
    LifecycleHelper.startIfPossible(allocator);
    Assert.assertNull(allocator.getSlotTable());

    when(exchange.getClient(Exchange.META_SERVER_TYPE))
        .thenReturn(
            getRpcClient(
                scheduled,
                1,
                new SlotTable(
                    System.currentTimeMillis(),
                    Lists.newArrayList(
                        new Slot(
                            1,
                            "10.0.0.1",
                            System.currentTimeMillis(),
                            Lists.newArrayList("10.0.0.2"))))));
    allocator.refreshSlotTable(0);
    waitConditionUntilTimeOut(() -> allocator.getSlotTable() != null, 1000);

    Assert.assertNotNull(allocator.getSlotTable());
    Assert.assertEquals("10.0.0.1", allocator.getSlotTable().getSlot(1).getLeader());
  }

  @Test
  public void testTestGetDc() {
    Assert.assertEquals(getDc(), allocator.getDc());
  }

  @Test
  public void testRefreshSlotTableFirstFailure()
      throws TimeoutException, InterruptedException, StartException, InitializeException {
    LifecycleHelper.initializeIfPossible(allocator);
    LifecycleHelper.startIfPossible(allocator);
    Assert.assertNull(allocator.getSlotTable());

    when(exchange.getClient(Exchange.META_SERVER_TYPE))
        .thenReturn(getRpcClient(scheduled, 3, new TimeoutException("expected timeout")))
        .thenReturn(
            getRpcClient(
                scheduled,
                1,
                new SlotTable(
                    System.currentTimeMillis(),
                    Lists.newArrayList(
                        new Slot(
                            1,
                            "10.0.0.1",
                            System.currentTimeMillis(),
                            Lists.newArrayList("10.0.0.2"))))));
    allocator.refreshSlotTable(0);
    waitConditionUntilTimeOut(() -> allocator.getSlotTable() != null, 1000);

    Assert.assertNotNull(allocator.getSlotTable());
    Assert.assertEquals("10.0.0.1", allocator.getSlotTable().getSlot(1).getLeader());
  }

  @Test
  public void testRefreshSlotTableRetryOverTimes()
      throws TimeoutException, InterruptedException, StartException, InitializeException {
    LifecycleHelper.initializeIfPossible(allocator);
    LifecycleHelper.startIfPossible(allocator);

    Assert.assertNull(allocator.getSlotTable());

    when(exchange.getClient(Exchange.META_SERVER_TYPE))
        .thenReturn(getRpcClient(scheduled, 3, new TimeoutException("expected timeout")));

    allocator.refreshSlotTable(0);
    Thread.sleep(100);

    Assert.assertNull(allocator.getSlotTable());
    verify(allocator, atLeast(2)).refreshSlotTable(anyInt());
  }

  @Test
  public void testDoInitialize() throws InitializeException {
    LifecycleHelper.initializeIfPossible(allocator);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.INITIALIZED, allocator.getLifecycleState().getPhase());
  }

  @Test
  public void testDoStart() throws InitializeException, StopException, StartException {
    LifecycleHelper.initializeIfPossible(allocator);
    LifecycleHelper.startIfPossible(allocator);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.STARTED, allocator.getLifecycleState().getPhase());
    verify(allocator, never()).refreshSlotTable(anyInt());
    verify(allocator, times(1)).doStart();
  }

  @Test
  public void testDoStop() {}

  @Test
  public void testDoDispose() {}
}
