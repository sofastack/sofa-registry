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
package com.alipay.sofa.registry.server.meta.resource;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.server.meta.slot.status.SlotTableStatusService;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuchen
 * @date Mar 2, 2021, 11:48:41 AM
 */
public class SlotTableResourceTest extends AbstractMetaServerTestBase {

  private SlotManager slotManager;

  private DefaultDataServerManager dataServerManager;

  private SlotTableResource resource;

  private ScheduledSlotArranger slotArranger;

  private SlotTableMonitor slotTableMonitor;

  private SlotTableStatusService slotTableStatusService;

  @Before
  public void beforeSlotTableResourceTest() {
    NodeConfig nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    slotManager = new SimpleSlotManager();
    dataServerManager = mock(DefaultDataServerManager.class);
    slotTableMonitor = mock(SlotTableMonitor.class);
    slotArranger =
        spy(
            new ScheduledSlotArranger(
                dataServerManager,
                slotManager,
                slotTableMonitor,
                metaLeaderService,
                metaServerConfig));

    slotTableStatusService = new SlotTableStatusService();
    slotTableStatusService
        .setSlotArranger(slotArranger)
        .setDataServerManager(dataServerManager)
        .setSlotTableMonitor(slotTableMonitor)
        .setSlotManager(slotManager);
    resource =
        new SlotTableResource(
            slotManager,
            dataServerManager,
            slotArranger,
            metaLeaderService,
            slotTableStatusService);
  }

  @Test
  public void testForceRefreshSlotTable() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    List<DataNode> dataNodes =
        Lists.newArrayList(
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    SlotTable slotTable = new SlotTableGenerator(dataNodes).createLeaderUnBalancedSlotTable();
    slotManager.refresh(slotTable);

    when(slotArranger.tryLock()).thenReturn(true);
    GenericResponse<SlotTable> current = resource.forceRefreshSlotTable();
    printSlotTable(current.getData());
    Assert.assertTrue(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));
  }

  @Test
  public void testForceRefreshSlotTableWithoutLock() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    List<DataNode> dataNodes =
        Lists.newArrayList(
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    SlotTable slotTable = new SlotTableGenerator(dataNodes).createLeaderUnBalancedSlotTable();
    slotManager.refresh(slotTable);

    when(slotArranger.tryLock()).thenReturn(false);
    GenericResponse<SlotTable> current = resource.forceRefreshSlotTable();
    Assert.assertFalse(current.isSuccess());
    Assert.assertEquals("scheduled slot arrangement is running", current.getMessage());

    makeMetaNonLeader();
    when(slotArranger.tryLock()).thenReturn(true);
    current = resource.forceRefreshSlotTable();
    Assert.assertFalse(current.isSuccess());
    Assert.assertEquals("not the meta-server leader", current.getMessage());
  }

  @Test
  public void testStartReconcile() throws Exception {
    slotArranger.postConstruct();
    slotArranger.suspend();
    resource.startSlotTableReconcile();
    Assert.assertEquals("running", resource.getReconcileStatus().getMessage());
  }

  @Test
  public void testStopReconcile() throws Exception {
    slotArranger.postConstruct();
    resource.stopSlotTableReconcile();
    Assert.assertEquals("stopped", resource.getReconcileStatus().getMessage());
  }

  @Test
  public void testGetDataStats() {
    when(dataServerManager.getDataServersStats())
        .thenThrow(new SofaRegistryRuntimeException("expected exception"));
    GenericResponse<Object> response = resource.getDataSlotStatuses();
    Assert.assertEquals("expected exception", response.getMessage());
  }

  @Test
  public void testReconcileException() {
    doThrow(new SofaRegistryRuntimeException("expected exception")).when(slotArranger).resume();
    CommonResponse response = resource.startSlotTableReconcile();
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("expected exception", response.getMessage());

    doThrow(new SofaRegistryRuntimeException("expected exception")).when(slotArranger).suspend();
    response = resource.stopSlotTableReconcile();
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("expected exception", response.getMessage());
  }

  @Test
  public void testGetSlotStatus() {
    when(slotTableMonitor.isStableTableStable()).thenReturn(true);
    List<DataNode> dataNodes = randomDataNodes(3);
    SlotTable slotTable = randomUnBalancedSlotTable(dataNodes);
    slotManager.refresh(slotTable);
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    GenericResponse<Object> response = resource.getSlotTableStatus();
    Assert.assertTrue(response.isSuccess());
    SlotTableStatusResponse slotTableStatus = (SlotTableStatusResponse) response.getData();
    Assert.assertFalse(slotTableStatus.isSlotTableLeaderBalanced());
    Assert.assertFalse(slotTableStatus.isSlotTableFollowerBalanced());
    Assert.assertTrue(slotTableStatus.isSlotTableStable());

    dataNodes = randomDataNodes(2);
    slotTable = randomUnBalancedSlotTable(dataNodes);
    slotManager.refresh(slotTable);
    dataNodes.add(new DataNode(randomURL(randomIp()), getDc()));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    response = resource.getSlotTableStatus();
    Assert.assertTrue(response.isSuccess());
    slotTableStatus = (SlotTableStatusResponse) response.getData();
    Assert.assertFalse(slotTableStatus.isSlotTableLeaderBalanced());
    Assert.assertFalse(slotTableStatus.isSlotTableFollowerBalanced());
    Assert.assertTrue(slotTableStatus.isSlotTableStable());
  }

  @Test
  public void testGetSlotStatusWithException() {
    when(slotTableMonitor.isStableTableStable()).thenReturn(true);
    List<DataNode> dataNodes = randomDataNodes(3);
    SlotTable slotTable = randomUnBalancedSlotTable(dataNodes);
    slotManager.refresh(slotTable);
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    GenericResponse<Object> response = resource.getSlotTableStatus();
    Assert.assertTrue(response.isSuccess());
    SlotTableStatusResponse slotTableStatus = (SlotTableStatusResponse) response.getData();
    Assert.assertFalse(slotTableStatus.isSlotTableLeaderBalanced());
    Assert.assertFalse(slotTableStatus.isSlotTableFollowerBalanced());
    Assert.assertTrue(slotTableStatus.isSlotTableStable());
  }
}
