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
package com.alipay.sofa.registry.server.meta.monitor;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.monitor.impl.DefaultSlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultSlotTableMonitorTest extends AbstractMetaServerTestBase {

  private DefaultSlotTableMonitor monitor = new DefaultSlotTableMonitor();

  private SimpleSlotManager slotManager;

  private List<DataNode> dataNodes;

  @Before
  public void beforeDefaultSlotTableMonitorTest() throws Exception {
    NodeConfig nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    slotManager = new SimpleSlotManager();
    dataNodes =
        Lists.newArrayList(
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()));
    slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
    slotManager = spy(slotManager);
    monitor.setSlotManager(slotManager);
    monitor.setMetaServerConfig(new MetaServerConfigBean(commonConfig));
    monitor.setMetaLeaderService(metaLeaderService);
    makeMetaLeader();
    monitor.postConstruct();
  }

  @After
  public void afterDefaultSlotTableMonitorTest() throws Exception {
    monitor.preDestroy();
  }

  @Test
  public void testRecordSlotTable() {
    monitor.recordSlotTable();
    verify(slotManager, atLeast(1)).getSlotTable();
  }

  @Test
  public void testUpdate() {
    slotManager.refresh(randomSlotTable());
    verify(slotManager, atLeast(1)).getSlotTable();
  }

  @Test
  public void testIsSlotTableStable() {
    monitor.update(slotManager, slotManager.getSlotTable());
    Assert.assertFalse(monitor.isStableTableStable());
    SlotTable slotTable = slotManager.getSlotTable();
    for (DataNode dataNode : dataNodes) {
      List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
      dataNodeSlot
          .getLeaders()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new LeaderSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        BaseSlotStatus.LeaderStatus.HEALTHY));
              });
      dataNodeSlot
          .getFollowers()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new FollowerSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis()));
              });
      monitor.onHeartbeat(
          new HeartbeatRequest<DataNode>(
              dataNode,
              slotManager.getSlotTable().getEpoch(),
              getDc(),
              System.currentTimeMillis(),
              new SlotConfig.SlotBasicInfo(
                  SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
              slotStatuses,
              Collections.EMPTY_MAP));
    }
    Assert.assertTrue(monitor.isStableTableStable());

    final boolean[] unstable = {false};
    for (DataNode dataNode : dataNodes) {
      List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
      dataNodeSlot
          .getLeaders()
          .forEach(
              slotId -> {
                if (!unstable[0]) {
                  slotStatuses.add(
                      new LeaderSlotStatus(
                          slotId,
                          slotTable.getSlot(slotId).getLeaderEpoch(),
                          dataNode.getIp(),
                          BaseSlotStatus.LeaderStatus.UNHEALTHY));
                  unstable[0] = true;
                } else {
                  slotStatuses.add(
                      new LeaderSlotStatus(
                          slotId,
                          slotTable.getSlot(slotId).getLeaderEpoch(),
                          dataNode.getIp(),
                          BaseSlotStatus.LeaderStatus.HEALTHY));
                }
              });
      dataNodeSlot
          .getFollowers()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new FollowerSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis()));
              });
      monitor.onHeartbeat(
          new HeartbeatRequest<DataNode>(
              dataNode,
              slotManager.getSlotTable().getEpoch(),
              getDc(),
              System.currentTimeMillis(),
              new SlotConfig.SlotBasicInfo(
                  SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
              slotStatuses,
              Collections.EMPTY_MAP));
    }
    Assert.assertFalse(monitor.isStableTableStable());
  }

  @Test
  public void testOnHeartbeatWithPrevEpoch() {
    monitor.update(slotManager, slotManager.getSlotTable());
    Assert.assertFalse(monitor.isStableTableStable());
    SlotTable slotTable = slotManager.getSlotTable();
    for (DataNode dataNode : dataNodes) {
      List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
      dataNodeSlot
          .getLeaders()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new LeaderSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        BaseSlotStatus.LeaderStatus.HEALTHY));
              });
      monitor.onHeartbeat(
          new HeartbeatRequest<DataNode>(
              dataNode,
              slotTable.getEpoch() - 1,
              getDc(),
              System.currentTimeMillis(),
              new SlotConfig.SlotBasicInfo(
                  SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
              slotStatuses,
              Collections.EMPTY_MAP));
    }
    Assert.assertFalse(monitor.isStableTableStable());
  }

  @Test
  public void testUpdateSlotTableThenIsStableShouldBeFalse() {
    monitor.update(slotManager, slotManager.getSlotTable());
    Assert.assertFalse(monitor.isStableTableStable());
    SlotTable slotTable = slotManager.getSlotTable();
    for (DataNode dataNode : dataNodes) {
      List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
      dataNodeSlot
          .getLeaders()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new LeaderSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        BaseSlotStatus.LeaderStatus.HEALTHY));
              });
      dataNodeSlot
          .getFollowers()
          .forEach(
              slotId -> {
                slotStatuses.add(
                    new FollowerSlotStatus(
                        slotId,
                        slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis()));
              });
      monitor.onHeartbeat(
          new HeartbeatRequest<DataNode>(
              dataNode,
              slotManager.getSlotTable().getEpoch(),
              getDc(),
              System.currentTimeMillis(),
              new SlotConfig.SlotBasicInfo(
                  SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
              slotStatuses,
              Collections.EMPTY_MAP));
    }
    Assert.assertTrue(monitor.isStableTableStable());

    slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
    monitor.update(slotManager, slotManager.getSlotTable());
    Assert.assertFalse(monitor.isStableTableStable());
  }

  @Test
  public void testReportDataServerLag() {
    String ip = randomIp();
    monitor.onHeartbeat(
        new HeartbeatRequest<DataNode>(
            new DataNode(randomURL(ip), getDc()),
            -1L,
            getDc(),
            System.currentTimeMillis(),
            new SlotConfig.SlotBasicInfo(
                SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
            Lists.newArrayList(),
            Collections.EMPTY_MAP));
    monitor.onHeartbeat(
        new HeartbeatRequest<DataNode>(
            new DataNode(randomURL(ip), getDc()),
            -1L,
            getDc(),
            System.currentTimeMillis(),
            new SlotConfig.SlotBasicInfo(
                SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
            Lists.newArrayList(),
            Collections.EMPTY_MAP));
    Assert.assertTrue(Metrics.DataSlot.getDataServerSlotLagTimes(ip) > 1);
  }

  @Test
  public void testNotStableWillNotCheckSlotStats() {
    when(metaLeaderService.amIStableAsLeader()).thenReturn(false);
    SlotTableStats slotTableStats = mock(SlotTableStats.class);
    monitor.setSlotTableStats(slotTableStats);
    String ip = randomIp();
    monitor.onHeartbeat(
        new HeartbeatRequest<DataNode>(
            new DataNode(randomURL(ip), getDc()),
            slotManager.getSlotTable().getEpoch() + 1,
            getDc(),
            System.currentTimeMillis(),
            new SlotConfig.SlotBasicInfo(
                SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS, SlotConfig.FUNC),
            Lists.newArrayList(),
            Collections.EMPTY_MAP));
    verify(slotTableStats, never()).checkSlotStatuses(any(), any());
  }

  @Test
  public void testSlotTableNullWillCauseNPE() throws Exception {
    slotManager = new SimpleSlotManager();
    monitor.setSlotManager(slotManager);
    monitor.setMetaServerConfig(new MetaServerConfigBean(commonConfig));
    monitor.setMetaLeaderService(metaLeaderService);
    makeMetaLeader();
    // init again to re-build slot-table-stats
    monitor.preDestroy();
    monitor.postConstruct();
    monitor.isStableTableStable();
  }
}
