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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import java.util.*;

public class DefaultCrossDcMetaServerTest extends AbstractMetaServerTestBase {

  //  private DefaultCrossDcMetaServer server;
  //
  //  @Mock private Exchange<MetaNode> exchange;
  //
  //  @Mock private MetaServerConfig metaServerConfig;
  //
  //  @Mock private MetaLeaderService metaLeaderService;
  //
  //  @Before
  //  public void beforeDefaultCrossDcMetaServerTest() {
  //    MockitoAnnotations.initMocks(this);
  //    when(metaServerConfig.getCrossDcMetaSyncIntervalMillis()).thenReturn(60 * 1000);
  //    Collection<String> collection = Lists.newArrayList("10.0.0.1", "10.0.0.2");
  //    server =
  //        spy(
  //            new DefaultCrossDcMetaServer(
  //                getDc(),
  //                collection,
  //                scheduled,
  //                exchange,
  //                this.metaLeaderService,
  //                metaServerConfig));
  //  }
  //
  //  @After
  //  public void afterDefaultCrossDcMetaServerTest() throws Exception {
  //    LifecycleHelper.startIfPossible(server);
  //    LifecycleHelper.disposeIfPossible(server);
  //  }
  //
  //  @Test
  //  public void testDoInitialize() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    Assert.assertTrue(server.getLifecycleState().canStart());
  //  }
  //
  //  @Test
  //  public void testDoStart() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    Assert.assertTrue(server.getLifecycleState().canStop());
  //  }
  //
  //  @Test
  //  public void testDoStop() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    LifecycleHelper.stopIfPossible(server);
  //    Assert.assertTrue(server.getLifecycleState().canStart());
  //  }
  //
  //  @Test
  //  public void testDoDispose() throws Exception {
  //    LifecycleHelper.disposeIfPossible(server);
  //    Assert.assertTrue(server.getLifecycleState().canInitialize());
  //  }
  //
  //  @Test
  //  public void testTestGetDc() {
  //    Assert.assertEquals(getDc(), server.getDc());
  //  }
  //
  //  @Test(expected = IllegalStateException.class)
  //  public void testGetSlotTable() {
  //    server.getSlotTable();
  //  }
  //
  //  @Test
  //  public void testRefreshSlotTableWithResult() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    Assert.assertEquals(2, server.getClusterMembers().size());
  //    DataCenterNodes<MetaNode> message =
  //        new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
  //    Map<String, MetaNode> nodes =
  //        ImmutableMap.of(
  //            "10.0.0.1",
  //            new MetaNode(randomURL("10.0.0.1"), getDc()),
  //            "10.0.0.2",
  //            new MetaNode(randomURL("10.0.0.2"), getDc()),
  //            "10.0.0.3",
  //            new MetaNode(randomURL("10.0.0.3"), getDc()));
  //    message.setNodes(nodes);
  //    when(exchange.getClient(Exchange.META_SERVER_TYPE))
  //        .thenReturn(getRpcClient(scheduled, 1, message));
  //    server.doRefresh(0);
  //    waitConditionUntilTimeOut(() -> server.getClusterMembers().size() > 2, 1000);
  //    List<MetaNode> expected = Lists.newArrayList(message.getNodes().values());
  //    expected.sort(new NodeComparator());
  //    List<MetaNode> actual = server.getClusterMembers();
  //    actual.sort(new NodeComparator());
  //    Assert.assertEquals(expected, actual);
  //  }
  //
  //  @Test
  //  public void testDoRefreshWithFirstTimeout() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    Assert.assertEquals(2, server.getClusterMembers().size());
  //    DataCenterNodes<MetaNode> message =
  //        new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
  //    Map<String, MetaNode> nodes =
  //        ImmutableMap.of(
  //            "10.0.0.1",
  //            new MetaNode(randomURL("10.0.0.1"), getDc()),
  //            "10.0.0.2",
  //            new MetaNode(randomURL("10.0.0.2"), getDc()),
  //            "10.0.0.3",
  //            new MetaNode(randomURL("10.0.0.3"), getDc()));
  //    message.setNodes(nodes);
  //    when(exchange.getClient(Exchange.META_SERVER_TYPE))
  //        .thenReturn(getRpcClient(scheduled, 3, new TimeoutException("expected timeout")))
  //        .thenReturn(getRpcClient(scheduled, 1, message));
  //    server.doRefresh(0);
  //    waitConditionUntilTimeOut(() -> server.getClusterMeta().getClusterMembers().size() > 2,
  // 1000);
  //    List<MetaNode> expected = Lists.newArrayList(message.getNodes().values());
  //    expected.sort(new NodeComparator());
  //    List<MetaNode> actual = server.getClusterMembers();
  //    actual.sort(new NodeComparator());
  //    Assert.assertEquals(expected, actual);
  //  }
  //
  //  @Test
  //  public void testDoRefreshWithOverRetryTimes() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    Assert.assertEquals(2, server.getClusterMembers().size());
  //
  //    when(exchange.getClient(Exchange.META_SERVER_TYPE))
  //        .thenReturn(getRpcClient(scheduled, 1, new TimeoutException()));
  //    server.doRefresh(0);
  //    Thread.sleep(100);
  //    verify(server, atLeast(3)).doRefresh(anyInt());
  //  }
  //
  //  // run manually
  //  //    @Test
  //  //    @Ignore
  //  public void testRaftMechanismWorks() throws Exception {
  //
  //    server =
  //        new DefaultCrossDcMetaServer(
  //            getDc(),
  //            Lists.newArrayList(NetUtil.getLocalAddress().getHostAddress()),
  //            scheduled,
  //            exchange,
  //            this.metaLeaderService,
  //            metaServerConfig);
  //    DataCenterNodes<MetaNode> message =
  //        new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
  //    Map<String, MetaNode> nodes =
  //        ImmutableMap.of(
  //            "10.0.0.1", new MetaNode(randomURL("10.0.0.1"), getDc()),
  //            "10.0.0.2", new MetaNode(randomURL("10.0.0.2"), getDc()),
  //            "10.0.0.3", new MetaNode(randomURL("10.0.0.3"), getDc()),
  //            "10.0.0.4", new MetaNode(randomURL("10.0.0.4"), getDc()),
  //            "10.0.0.5", new MetaNode(randomURL("10.0.0.5"), getDc()));
  //    message.setNodes(nodes);
  //    when(exchange.getClient(Exchange.META_SERVER_TYPE))
  //        //                .thenReturn(getRpcClient(scheduled, 3, new TimeoutException()))
  //        .thenReturn(getRpcClient(scheduled, 1, message));
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //
  //    server.doRefresh(0);
  //    waitConditionUntilTimeOut(() -> server.getClusterMembers().size() > 4, 30000);
  //    List<MetaNode> expected = Lists.newArrayList(message.getNodes().values());
  //    Collections.sort(
  //        expected,
  //        new Comparator<MetaNode>() {
  //          @Override
  //          public int compare(MetaNode o1, MetaNode o2) {
  //            return o1.getIp().compareTo(o2.getIp());
  //          }
  //        });
  //    List<MetaNode> real = server.getClusterMembers();
  //    Collections.sort(
  //        real,
  //        new Comparator<MetaNode>() {
  //          @Override
  //          public int compare(MetaNode o1, MetaNode o2) {
  //            return o1.getIp().compareTo(o2.getIp());
  //          }
  //        });
  //    Assert.assertEquals(expected.size(), real.size());
  //    // wait for rpc safe quit
  //    Thread.sleep(100);
  //  }
  //
  //  @Test
  //  public void testRefresh() {
  //    server.refresh();
  //    verify(server, never()).doRefresh(anyInt());
  //  }
  //
  //  @Test
  //  public void testGetRefreshCount() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    when(exchange.getClient(Exchange.META_SERVER_TYPE))
  //        .thenReturn(getRpcClient(scheduled, 1, new TimeoutException()));
  //    server.refresh();
  //    server.refresh();
  //    server.refresh();
  //    Assert.assertEquals(3, server.getRefreshCount());
  //  }
  //
  //  @Test
  //  public void testGetLastRefreshTime() {}
  //
  //  @Test
  //  public void testGetSlotTable2() throws Exception {
  //    LifecycleHelper.initializeIfPossible(server);
  //    LifecycleHelper.startIfPossible(server);
  //    SlotAllocator allocator = mock(SlotAllocator.class);
  //    server.setAllocator(allocator);
  //    server.getSlotTable();
  //    verify(allocator, atLeast(1)).getSlotTable();
  //  }
}
