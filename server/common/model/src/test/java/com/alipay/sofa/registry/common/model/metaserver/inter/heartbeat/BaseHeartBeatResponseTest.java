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
package com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class BaseHeartBeatResponseTest {

  @Test
  public void testSimpleConstructor() {
    BaseHeartBeatResponse response = new BaseHeartBeatResponse(true, "leader1", 100L);
    Assert.assertTrue(response.isHeartbeatOnLeader());
    Assert.assertEquals("leader1", response.getMetaLeader());
    Assert.assertEquals(100L, response.getMetaLeaderEpoch());
    Assert.assertNull(response.getSlotTable());
    Assert.assertNull(response.getFlowOperationThrottlingStatus());
  }

  @Test
  public void testConstructorWithMetaNodesAndSlotTable() {
    MetaNode metaNode = new MetaNode(new URL("192.168.1.1", 9600), "dc1");
    VersionedList<MetaNode> metaNodes = new VersionedList<>(1L, Arrays.asList(metaNode));
    SlotTable slotTable = SlotTable.INIT;

    BaseHeartBeatResponse response =
        new BaseHeartBeatResponse(true, metaNodes, slotTable, "leader1", 100L);

    Assert.assertTrue(response.isHeartbeatOnLeader());
    Assert.assertEquals("leader1", response.getMetaLeader());
    Assert.assertEquals(100L, response.getMetaLeaderEpoch());
    Assert.assertNotNull(response.getSlotTable());
    Assert.assertEquals(1, response.getMetaNodes().size());
    Assert.assertNull(response.getFlowOperationThrottlingStatus());
  }

  @Test
  public void testConstructorWithRemoteSlotTableStatus() {
    MetaNode metaNode = new MetaNode(new URL("192.168.1.1", 9600), "dc1");
    VersionedList<MetaNode> metaNodes = new VersionedList<>(1L, Arrays.asList(metaNode));
    VersionedList<SessionNode> sessionNodes = new VersionedList<>(1L, Collections.emptyList());
    SlotTable slotTable = SlotTable.INIT;
    Map<String, RemoteSlotTableStatus> remoteStatus = new HashMap<>();

    BaseHeartBeatResponse response =
        new BaseHeartBeatResponse(
            true, metaNodes, slotTable, sessionNodes, "leader1", 100L, remoteStatus);

    Assert.assertTrue(response.isHeartbeatOnLeader());
    Assert.assertNotNull(response.getRemoteSlotTableStatus());
    Assert.assertNull(response.getFlowOperationThrottlingStatus());
  }

  @Test
  public void testConstructorWithFlowOperationThrottlingStatus() {
    MetaNode metaNode = new MetaNode(new URL("192.168.1.1", 9600), "dc1");
    VersionedList<MetaNode> metaNodes = new VersionedList<>(1L, Arrays.asList(metaNode));
    VersionedList<SessionNode> sessionNodes = new VersionedList<>(1L, Collections.emptyList());
    SlotTable slotTable = SlotTable.INIT;
    Map<String, RemoteSlotTableStatus> remoteStatus = new HashMap<>();
    FlowOperationThrottlingStatus throttlingStatus = FlowOperationThrottlingStatus.enabled(75.0);

    BaseHeartBeatResponse response =
        new BaseHeartBeatResponse(
            true,
            metaNodes,
            slotTable,
            sessionNodes,
            "leader1",
            100L,
            remoteStatus,
            throttlingStatus);

    Assert.assertTrue(response.isHeartbeatOnLeader());
    Assert.assertNotNull(response.getFlowOperationThrottlingStatus());
    Assert.assertTrue(response.getFlowOperationThrottlingStatus().isEnabled());
    Assert.assertEquals(
        75.0, response.getFlowOperationThrottlingStatus().getThrottlePercent(), 0.001);
  }

  @Test
  public void testConstructorWithNullFlowOperationThrottlingStatus() {
    MetaNode metaNode = new MetaNode(new URL("192.168.1.1", 9600), "dc1");
    VersionedList<MetaNode> metaNodes = new VersionedList<>(1L, Arrays.asList(metaNode));
    VersionedList<SessionNode> sessionNodes = new VersionedList<>(1L, Collections.emptyList());
    SlotTable slotTable = SlotTable.INIT;
    Map<String, RemoteSlotTableStatus> remoteStatus = new HashMap<>();

    BaseHeartBeatResponse response =
        new BaseHeartBeatResponse(
            true, metaNodes, slotTable, sessionNodes, "leader1", 100L, remoteStatus, null);

    Assert.assertNull(response.getFlowOperationThrottlingStatus());
  }

  @Test
  public void testGetFlowOperationThrottlingStatusDisabled() {
    MetaNode metaNode = new MetaNode(new URL("192.168.1.1", 9600), "dc1");
    VersionedList<MetaNode> metaNodes = new VersionedList<>(1L, Arrays.asList(metaNode));
    VersionedList<SessionNode> sessionNodes = new VersionedList<>(1L, Collections.emptyList());
    SlotTable slotTable = SlotTable.INIT;
    Map<String, RemoteSlotTableStatus> remoteStatus = new HashMap<>();
    FlowOperationThrottlingStatus throttlingStatus = FlowOperationThrottlingStatus.disabled();

    BaseHeartBeatResponse response =
        new BaseHeartBeatResponse(
            true,
            metaNodes,
            slotTable,
            sessionNodes,
            "leader1",
            100L,
            remoteStatus,
            throttlingStatus);

    Assert.assertNotNull(response.getFlowOperationThrottlingStatus());
    Assert.assertFalse(response.getFlowOperationThrottlingStatus().isEnabled());
    Assert.assertEquals(
        0.0, response.getFlowOperationThrottlingStatus().getThrottlePercent(), 0.001);
  }
}
