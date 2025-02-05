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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultSlotTableStatsTest extends AbstractMetaServerTestBase {

  private SimpleSlotManager slotManager;

  private List<DataNode> dataNodes;

  private DefaultSlotTableStats slotTableStats;

  private NodeConfig nodeConfig;

  @Before
  public void beforeDefaultSlotTableMonitorTest() throws Exception {
    nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    slotManager = new SimpleSlotManager();
    slotTableStats = new DefaultSlotTableStats(slotManager, new MetaServerConfigBean(commonConfig));
    dataNodes =
        Lists.newArrayList(
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()));
    slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
    slotTableStats.initialize();
  }

  @Test
  public void testIsSlotTableStable() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testCheckSlotStatuses() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
                        slotTable.getSlot(slotId).getLeaderEpoch() - 1,
                        dataNode.getIp(),
                        BaseSlotStatus.LeaderStatus.HEALTHY));
              });
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testUpdateSlotTable() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());

    slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
    slotTableStats.updateSlotTable(slotManager.getSlotTable());
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testDataReportHeartbeatWhenInit() throws InitializeException {
    slotManager = new SimpleSlotManager();
    slotTableStats = new DefaultSlotTableStats(slotManager, new MetaServerConfigBean(commonConfig));
    slotTableStats.initialize();
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
      slotStatuses.add(
          new LeaderSlotStatus(slotId, 0, randomIp(), BaseSlotStatus.LeaderStatus.UNHEALTHY));
    }
    slotTableStats.checkSlotStatuses(new DataNode(randomURL(randomIp()), getDc()), slotStatuses);
  }

  @Test
  public void testFollowerStatus() {
    MetaServerConfigBean configBean = new MetaServerConfigBean(commonConfig);
    Assert.assertFalse(slotTableStats.isSlotFollowersStable());
    DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
      slotStatuses.add(
          new FollowerSlotStatus(
              slotId,
              slotManager.getSlotTable().getSlot(slotId).getLeaderEpoch(),
              dataNode.getIp(),
              System.currentTimeMillis(),
              -1));
    }
    slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    Assert.assertFalse(slotTableStats.isSlotFollowersStable());

    slotStatuses = Lists.newArrayList();
    for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
      slotStatuses.add(
          new FollowerSlotStatus(
              slotId,
              slotManager.getSlotTable().getSlot(slotId).getLeaderEpoch(),
              dataNode.getIp(),
              System.currentTimeMillis(),
              System.currentTimeMillis() - configBean.getDataReplicateMaxGapMillis()));
    }
    slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    Assert.assertFalse(slotTableStats.isSlotFollowersStable());

    logger.info(remarkableMessage("[splitter]"));
    for (DataNode node : dataNodes) {
      slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotManager.getDataNodeManagedSlot(node.getIp(), false);
      for (int slotId : dataNodeSlot.getFollowers()) {
        slotStatuses.add(
            new FollowerSlotStatus(
                slotId,
                slotManager.getSlotTable().getSlot(slotId).getLeaderEpoch(),
                node.getIp(),
                System.currentTimeMillis(),
                System.currentTimeMillis() - 1000));
      }
      slotTableStats.checkSlotStatuses(node, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotFollowersStable());
  }

  @Test
  public void testUpdateSlotTableWhenFollowerChangeOnly() {
    Assert.assertFalse(slotTableStats.isSlotFollowersStable());
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    for (DataNode node : dataNodes) {
      slotStatuses = Lists.newArrayList();
      DataNodeSlot dataNodeSlot = slotManager.getDataNodeManagedSlot(node.getIp(), false);
      for (int slotId : dataNodeSlot.getFollowers()) {
        slotStatuses.add(
            new FollowerSlotStatus(
                slotId,
                slotManager.getSlotTable().getSlot(slotId).getLeaderEpoch(),
                node.getIp(),
                System.currentTimeMillis(),
                System.currentTimeMillis() - 1000));
      }
      slotTableStats.checkSlotStatuses(node, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotFollowersStable());

    SlotTable prev = slotManager.getSlotTable();
    Map<Integer, Slot> slotMap = prev.getSlotMap();
    Slot prevSlot = slotMap.get(1);
    Set<String> followers = Sets.newHashSet(prevSlot.getFollowers());
    List<String> newFollowers = NodeUtils.transferNodeToIpList(dataNodes);
    newFollowers.removeAll(followers);
    slotMap.put(1, new Slot(1, prevSlot.getLeader(), prevSlot.getLeaderEpoch(), newFollowers));
    SlotTable slotTable = new SlotTable(prev.getEpoch() + 1, slotMap.values());
    slotManager.refresh(slotTable);

    SlotStats prevSlotStats = slotTableStats.getSlotStats(1);
    slotTableStats.updateSlotTable(slotTable);
    Assert.assertFalse(slotTableStats.isSlotFollowersStable());
    SlotStats curSlotStats = slotTableStats.getSlotStats(1);
    Assert.assertNotEquals(prevSlotStats, curSlotStats);
  }

  @Test
  public void testStableResultNotImpactByStableData() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
    // now, we add some stable data, that data-server is reporting slots it does not contains
    DataNode dataNode1 = dataNodes.get(0);
    DataNode dataNode2 = dataNodes.get(1);
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode1.getIp(), false).get(0);
    dataNodeSlot
        .getLeaders()
        .forEach(
            slotId -> {
              slotStatuses.add(
                  new LeaderSlotStatus(
                      slotId,
                      slotTable.getSlot(slotId).getLeaderEpoch(),
                      dataNode2.getIp(),
                      BaseSlotStatus.LeaderStatus.UNHEALTHY));
            });
    slotTableStats.checkSlotStatuses(dataNode2, slotStatuses);
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testSlotTableUpdateFollowerStableData() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
    // now, we add some stable data, that data-server is reporting slots it does not contains
    DataNode dataNode1 = dataNodes.get(0);
    DataNode dataNode2 = dataNodes.get(1);
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode1.getIp(), false).get(0);
    dataNodeSlot
        .getLeaders()
        .forEach(
            slotId -> {
              slotStatuses.add(
                  new FollowerSlotStatus(
                      slotId,
                      slotTable.getSlot(slotId).getLeaderEpoch(),
                      dataNode2.getIp(),
                      System.currentTimeMillis(),
                      -1L));
            });
    slotTableStats.checkSlotStatuses(dataNode2, slotStatuses);
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testSlotNotEquals() {
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
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
      slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
    }
    Assert.assertTrue(slotTableStats.isSlotLeadersStable());
    slotManager.refresh(
        new SlotTableGenerator(dataNodes).setNextLeader(1).setNextFollower(2).createSlotTable());
    Assert.assertFalse(slotTableStats.isSlotLeadersStable());
  }

  @Test
  public void testIsFollowerStableNPE() {
    slotManager = new SimpleSlotManager();
    slotTableStats = new DefaultSlotTableStats(slotManager, new MetaServerConfigBean(commonConfig));
    slotTableStats.initialize();
    slotTableStats.isSlotFollowersStable();
  }
}
