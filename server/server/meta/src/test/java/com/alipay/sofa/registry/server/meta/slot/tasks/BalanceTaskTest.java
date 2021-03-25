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
package com.alipay.sofa.registry.server.meta.slot.tasks;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BalanceTaskTest extends AbstractMetaServerTestBase {

  private BalanceTask task;

  private SlotManager slotManager;

  @Mock private DataServerManager dataServerManager;

  @Before
  public void beforeInitReshardingTaskTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    List<DataNode> dataNodes =
        Lists.newArrayList(
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    NodeConfig nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    slotManager = new SimpleSlotManager();
  }

  @Test
  public void testRun() throws TimeoutException, InterruptedException {
    Assert.assertEquals(SlotTable.INIT, slotManager.getSlotTable());
    task =
        new BalanceTask(slotManager, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    task.run();
    Assert.assertNotEquals(SlotTable.INIT, slotManager.getSlotTable());
    printSlotTable(slotManager.getSlotTable());
  }

  @Test
  public void testNoDupLeaderAndFollower() throws Exception {
    List<DataNode> dataNodes =
        Lists.newArrayList(
            new DataNode(new URL("100.88.142.32"), getDc()),
            new DataNode(new URL("100.88.142.36"), getDc()),
            new DataNode(new URL("100.88.142.19"), getDc()));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    task =
        new BalanceTask(slotManager, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    task.run();
    SlotTable slotTable = slotManager.getSlotTable();
    slotTable
        .getSlotMap()
        .forEach(
            (slotId, slot) -> {
              Assert.assertFalse(slot.getFollowers().contains(slot.getLeader()));
            });
  }

  @Test
  public void nonExecutionDueToEmptyDataSet() {
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), Lists.newArrayList()));
    slotManager = spy(slotManager);
    task =
        new BalanceTask(slotManager, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    task.run();
    verify(slotManager, never()).refresh(any(SlotTable.class));
  }

  @Test
  public void testSlotEpochCorrect() {
    List<DataNode> dataNodes = randomDataNodes(3);
    SlotTable prev = randomSlotTable(dataNodes);
    slotManager.refresh(prev);

    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), dataNodes));
    task =
        new BalanceTask(slotManager, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    task.run();

    SlotTable current = slotManager.getSlotTable();
    for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
      Assert.assertTrue(
          prev.getSlot(slotId).getLeaderEpoch() < current.getSlot(slotId).getLeaderEpoch());
    }
  }
}
