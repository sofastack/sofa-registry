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
package com.alipay.sofa.registry.server.meta.slot.assigner;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.slot.balance.DefaultSlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.alipay.sofa.registry.test.TestUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class DefaultSlotAssignerTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSlotAssignerTest.class);
  private static final Random RANDOM = new Random();

  @Test
  public void testAssign() {
    for (int slotNum = 2; slotNum <= 256; slotNum++) {
      for (int slotReplicas = 2; slotReplicas <= 2; slotReplicas++) {
        for (int dataNum = 2; dataNum < 20 && dataNum < slotNum; dataNum++) {
          for (int downNum = 1; downNum < dataNum / 2; downNum++) {
            testAssign(downNum, dataNum, slotNum, slotReplicas, false);
            testAssign(downNum, dataNum, slotNum, slotReplicas, true);
          }
        }
      }
    }
  }

  //    @Test
  //    public void testAssign1() {
  //      for (int dataNum = 2; dataNum < 50 && dataNum < 21; dataNum++) {
  //        for (int downNum = 1; downNum < dataNum; downNum++) {
  //          testAssign(downNum, dataNum, 4, 2);
  //        }
  //      }
  //    }

  public void testAssign(int downNum, int dataNum, int slotNum, int slotReplicas, boolean random) {
    LOGGER.info("#################################################################");
    String tag =
        StringFormatter.format(
            "#####testCase: down={}, data={}, slot={}, replicas={}, rand={}",
            downNum,
            dataNum,
            slotNum,
            slotReplicas,
            random);
    LOGGER.info(tag);
    List<DataNode> dataNodes = TestUtils.createDataNodes(dataNum);
    SlotTable prev = AbstractMetaServerTestBase.randomSlotTable(dataNodes, slotNum, slotReplicas);
    //              LOGGER.info("randSlotTable: maxGap={}, leaderGap={}, table={}",
    //                      AbstractMetaServerTest.maxSlotGap(prev, dataNodes),
    //                      AbstractMetaServerTest.maxLeaderGap(prev, dataNodes),prev);
    //            LOGGER.info("randDataNode:{}", prev.transfer(null,false));
    LinkedList<String> currentDataNodeIps =
        Lists.newLinkedList(NodeUtils.transferNodeToIpList(dataNodes));
    TestUtils.assertBalance(prev, currentDataNodeIps, slotNum, slotReplicas, false, tag);

    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(prev, slotNum, slotReplicas);
    List<String> downs = Lists.newArrayList();
    if (!random) {
      for (int i = 0; i < downNum; i++) {
        downs.add(currentDataNodeIps.removeFirst());
      }
    } else {
      for (int i = 0; i < downNum; i++) {
        int randIdx = RANDOM.nextInt(currentDataNodeIps.size());
        downs.add(currentDataNodeIps.remove(randIdx));
      }
    }
    //    LOGGER.info("downs={}", downs);
    // one dataServer down
    slotTableBuilder.init(currentDataNodeIps);
    for (String down : downs) {
      slotTableBuilder.removeDataServerSlots(down);
    }
    DefaultSlotAssigner assigner = new DefaultSlotAssigner(slotTableBuilder, currentDataNodeIps);
    SlotTable assign = assigner.assign();
    //    LOGGER.info("assignSlotTable: maxGap={}, leaderGap={}, table={}",
    //            AbstractMetaServerTest.maxSlotGap(assign, dataNodes),
    //            AbstractMetaServerTest.maxLeaderGap(assign, dataNodes),
    //            assign);
    //    LOGGER.info("assignDataNode:{}", assign.transfer(null,false));
    TestUtils.assertBalance(assign, currentDataNodeIps, slotNum, slotReplicas, false, tag);

    // balance the slot
    slotTableBuilder = new SlotTableBuilder(assign, slotNum, slotReplicas);
    slotTableBuilder.init(currentDataNodeIps);

    DefaultSlotBalancer balancer = new DefaultSlotBalancer(slotTableBuilder, currentDataNodeIps);
    for (int i = 0; i < slotNum; i++) {
      if (balancer.balance() == null) {
        break;
      }
    }
    SlotTable balance = slotTableBuilder.build();
    TestUtils.assertBalance(balance, currentDataNodeIps, slotNum, slotReplicas, true, tag);
  }
}
