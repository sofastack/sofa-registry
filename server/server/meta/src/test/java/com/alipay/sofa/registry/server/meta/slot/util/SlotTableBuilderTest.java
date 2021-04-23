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
package com.alipay.sofa.registry.server.meta.slot.util;

import static org.junit.Assert.assertSame;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author chen.zhu
 *     <p>Jan 15, 2021
 */
public class SlotTableBuilderTest extends AbstractMetaServerTestBase {

  @Test
  public void testGetOrCreate() throws InterruptedException {
    int slotId = random.nextInt(16);
    int tasks = 10;
    List<SlotBuilder> result = new ArrayList<>(tasks);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(null, 16, 2);
    for (int i = 0; i < tasks; i++) {
      result.add(i, slotTableBuilder.getOrCreate(slotId));
    }
    SlotBuilder sample = slotTableBuilder.getOrCreate(slotId);
    for (SlotBuilder sb : result) {
      assertSame(sb, sample);
    }
  }

  @Test
  public void testInit() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    DataNode alternative = new DataNode(randomURL(randomIp()), getDc());
    dataNodes.add(alternative);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    Assert.assertNotNull(slotTableBuilder.getDataNodeSlot(alternative.getIp()));

    for (int slotId = 0; slotId < slotTable.getSlots().size(); slotId++) {
      Assert.assertEquals(
          slotTableBuilder.getOrCreate(slotId).getLeader(), slotTable.getSlot(slotId).getLeader());
      Assert.assertEquals(
          slotTableBuilder.getOrCreate(slotId).getEpoch(),
          slotTable.getSlot(slotId).getLeaderEpoch());
      Assert.assertEquals(
          slotTableBuilder.getOrCreate(slotId).getFollowers(),
          slotTable.getSlot(slotId).getFollowers());
    }

    for (DataNode dataNode : dataNodes) {
      List<DataNodeSlot> samples = slotTable.transfer(dataNode.getIp(), false);
      if (!samples.isEmpty()) {
        Assert.assertEquals(samples.get(0), slotTableBuilder.getDataNodeSlot(dataNode.getIp()));
      }
    }
  }

  @Test
  public void testSetLeader() {
    List<DataNode> dataNodes = randomDataNodes(3);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    int slotId = random.nextInt(16);
    SlotBuilder sb = slotTableBuilder.getOrCreate(slotId);
    String prevLeader = sb.getLeader();
    long prevEpoch = sb.getEpoch();
    String nextLeader = null;
    int index = 0;
    do {
      nextLeader = dataNodes.get(index++).getIp();
    } while (nextLeader.equals(prevLeader));
    logger.info("[prev leader] {}", prevLeader);
    logger.info("[next leader] {}", nextLeader);
    slotTableBuilder.replaceLeader(slotId, nextLeader);
    Assert.assertNotEquals(sb.getLeader(), prevLeader);
    logger.info("[followers] {}", sb.getFollowers());
    Assert.assertTrue(sb.getEpoch() > prevEpoch);

    Assert.assertTrue(slotTableBuilder.getDataNodeSlot(nextLeader).getLeaders().contains(slotId));
  }

  @Test
  public void testRemoveFollower() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    int slotId = random.nextInt(16);
    String follower = slotTable.getSlot(slotId).getFollowers().iterator().next();
    slotTableBuilder.removeFollower(slotId, follower);
    Assert.assertFalse(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(follower));
    Assert.assertFalse(slotTableBuilder.getDataNodeSlot(follower).getFollowers().contains(slotId));
  }

  @Test
  public void testAddFollower() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    int slotId = random.nextInt(16);
    SlotBuilder sb = slotTableBuilder.getOrCreate(slotId);
    String prevFollower = sb.getFollowers().iterator().next();

    long prevEpoch = sb.getEpoch();
    String nextFollower = prevFollower;
    int index = 0;
    do {
      nextFollower = dataNodes.get(index++).getIp();
    } while (nextFollower.equals(prevFollower) || nextFollower.equals(sb.getLeader()));

    try {
      slotTableBuilder.addFollower(slotId, nextFollower);
      Assert.assertTrue("could not reach", false);
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("to many"));
    }
    Assert.assertFalse(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(nextFollower));
    Assert.assertFalse(
        slotTableBuilder.getDataNodeSlot(nextFollower).getFollowers().contains(slotId));

    sb.removeFollower(prevFollower);
    slotTableBuilder.addFollower(slotId, nextFollower);
    Assert.assertTrue(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(nextFollower));
    Assert.assertTrue(
        slotTableBuilder.getDataNodeSlot(nextFollower).getFollowers().contains(slotId));
  }

  @Test
  public void testHasNoAssignedSlots() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());

    slotTableBuilder = new SlotTableBuilder(null, 16, 2);
    Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

    dataNodes.add(new DataNode(randomURL(randomIp()), getDc()));
    slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());
  }

  @Test
  public void testRemoveDataServerSlots() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());

    slotTableBuilder.removeDataServerSlots(dataNodes.get(0).getIp());
    Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

    Assert.assertNull(slotTableBuilder.getDataNodeSlotIfPresent(dataNodes.get(0).getIp()));
  }

  @Test
  public void testGetNoAssignedSlots() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());
    DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataNodes.get(0).getIp());
    MigrateSlotGroup expected = new MigrateSlotGroup();
    dataNodeSlot.getLeaders().forEach(expected::addLeader);
    dataNodeSlot.getFollowers().forEach(expected::addFollower);

    slotTableBuilder.removeDataServerSlots(dataNodes.get(0).getIp());
    Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

    MigrateSlotGroup actual = slotTableBuilder.getNoAssignedSlots();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testGetDataNodeSlotsLeaderBeyond() {
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(null, 6, 2);
    List<DataNode> dataNodes = randomDataNodes(3);
    String leader = dataNodes.get(0).getIp();
    for (int i = 0; i < 6; i++) {
      slotTableBuilder.getOrCreate(i);
      slotTableBuilder.replaceLeader(i, leader);
    }
    Assert.assertEquals(
        Lists.newArrayList(slotTableBuilder.getDataNodeSlot(leader)),
        slotTableBuilder.getDataNodeSlotsLeaderBeyond(6 / 3));
  }

  @Test
  public void testGetDataNodeSlotsLeaderBelow() {
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(null, 6, 2);
    List<DataNode> dataNodes = randomDataNodes(3);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    String leader = dataNodes.get(0).getIp();
    for (int i = 0; i < 6; i++) {
      slotTableBuilder.getOrCreate(i);
      slotTableBuilder.replaceLeader(i, leader);
    }
    List<DataNodeSlot> expected =
        Lists.newArrayList(
            slotTableBuilder.getDataNodeSlot(dataNodes.get(1).getIp()),
            slotTableBuilder.getDataNodeSlot(dataNodes.get(2).getIp()));
    expected.sort(
        new Comparator<DataNodeSlot>() {
          @Override
          public int compare(DataNodeSlot o1, DataNodeSlot o2) {
            return o1.getDataNode().compareTo(o2.getDataNode());
          }
        });
    List<DataNodeSlot> actual = slotTableBuilder.getDataNodeSlotsLeaderBelow(6 / 3);
    actual.sort(
        new Comparator<DataNodeSlot>() {
          @Override
          public int compare(DataNodeSlot o1, DataNodeSlot o2) {
            return o1.getDataNode().compareTo(o2.getDataNode());
          }
        });
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testIncrEpoch() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 6, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    long prevEpoch = slotTableBuilder.build().getEpoch();
    slotTableBuilder.incrEpoch();
    Assert.assertTrue(slotTableBuilder.build().getEpoch() > prevEpoch);
  }

  @Test
  public void testBuild() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 6, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    long prevEpoch = slotTableBuilder.build().getEpoch();
    slotTableBuilder.incrEpoch();
    SlotTable slotTable1 = slotTableBuilder.build();
    assertSlotTableNoDupLeaderFollower(slotTable1);
  }

  @Test
  public void testGetDataNodeSlot() {
    List<DataNode> dataNodes = randomDataNodes(6);
    SlotTable slotTable = randomSlotTable(dataNodes);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 6, 2);
    slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));
    DataNodeSlot dataNodeSlot = null;
    try {
      dataNodeSlot = slotTableBuilder.getDataNodeSlot(randomIp());
      // should not reach that
      Assert.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("no DataNodeSlot for"));
    }
    Assert.assertNull(dataNodeSlot);
    dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataNodes.get(0).getIp());
    Assert.assertNotNull(dataNodeSlot);
    logger.info("[slotTableBuilder] {}", slotTableBuilder.toString());
  }
}
