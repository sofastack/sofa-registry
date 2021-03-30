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
package com.alipay.sofa.registry.server.meta.slot.balance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LeaderOnlyBalancerTest extends AbstractMetaServerTestBase {

  private SimpleSlotManager slotManager;

  private List<String> currentDataServers;

  private SlotTableBuilder slotTableBuilder;

  private LeaderOnlyBalancer balancer;

  @Before
  public void beforeLeaderOnlyBalancerTest() {
    NodeConfig nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());

    currentDataServers = Lists.newArrayList("10.0.0.1", "10.0.0.2", "10.0.0.3");
    slotManager = new SimpleSlotManager();
    slotManager.setSlotNums(16);
    slotManager.setSlotReplicas(1);
    slotTableBuilder = new SlotTableBuilder(slotManager.getSlotTable(), 16, 1);
    balancer = new LeaderOnlyBalancer(slotTableBuilder, currentDataServers);
    balancer.setMaxMoveLeaderSlots(2);
  }

  @Test
  public void testBalance() {
    String singleNode = currentDataServers.get(0);
    for (int slotId = 0; slotId < 16; slotId++) {
      slotTableBuilder.replaceLeader(slotId, singleNode);
    }
    slotTableBuilder.init(currentDataServers);
    SlotTable prev = slotTableBuilder.build();
    SlotTable slotTable = balancer.balance();
    System.out.println("@@@" + slotTable);
    Assert.assertEquals(
        StringFormatter.format("servers={}, slots={}", currentDataServers, slotTable),
        1,
        slotTable.transfer(currentDataServers.get(1), false).get(0).totalSlotNum());
    Assert.assertEquals(
        StringFormatter.format("servers={}, slots={}", currentDataServers, slotTable),
        1,
        slotTable.transfer(currentDataServers.get(2), false).get(0).totalSlotNum());

    Assert.assertTrue(
        isMoreBalanced(
            prev,
            slotTable,
            Lists.newArrayList(
                new DataNode(new URL("10.0.0.1"), getDc()),
                new DataNode(new URL("10.0.0.2"), getDc()),
                new DataNode(new URL("10.0.0.3"), getDc()))));

    slotTable = balancer.balance();
    Assert.assertEquals(
        StringFormatter.format("servers={}, slots={}", currentDataServers, slotTable),
        2,
        slotTable.transfer(currentDataServers.get(1), false).get(0).totalSlotNum());
    Assert.assertEquals(
        StringFormatter.format("servers={}, slots={}", currentDataServers, slotTable),
        2,
        slotTable.transfer(currentDataServers.get(2), false).get(0).totalSlotNum());

    Assert.assertTrue(
        isMoreBalanced(
            prev,
            slotTable,
            Lists.newArrayList(
                new DataNode(new URL("10.0.0.1"), getDc()),
                new DataNode(new URL("10.0.0.2"), getDc()),
                new DataNode(new URL("10.0.0.3"), getDc()))));
  }

  @Test
  public void testNoDataNodesNeedBalance() {
    List<DataNode> dataNodes = randomDataNodes(3);
    SlotTable slotTable = randomSlotTable(dataNodes);
    currentDataServers = NodeUtils.transferNodeToIpList(dataNodes);
    slotTableBuilder.init(currentDataServers);
    slotManager.refresh(slotTable);
    balancer = new LeaderOnlyBalancer(slotTableBuilder, currentDataServers);
    Assert.assertNull(balancer.balance());
  }

  @Test
  public void testFindDataServersNeedLeaderSlots() {
    String singleNode = currentDataServers.get(0);
    for (int slotId = 0; slotId < 16; slotId++) {
      slotTableBuilder.replaceLeader(slotId, singleNode);
    }
    slotTableBuilder.init(currentDataServers);
    // TODO
    //        Assert.assertFalse(balancer.findDataServersNeedLeaderSlots(
    //            SlotConfig.SLOT_NUM / currentDataServers.size()).isEmpty());
    //        Set<String> expected = Sets.newHashSet("10.0.0.2", "10.0.0.3");
    //        Set<String> actual = balancer.findDataServersNeedLeaderSlots(SlotConfig.SLOT_NUM
    //                                                                     /
    // currentDataServers.size());
    //
    //        Assert.assertEquals(expected, actual);
  }
}
