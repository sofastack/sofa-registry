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
package com.alipay.sofa.registry.server.meta.lease.data;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.monitor.data.DataServerStats;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultDataServerManagerTest extends AbstractMetaServerTestBase {

  private DefaultDataServerManager dataServerManager;

  @Mock private MetaServerConfig metaServerConfig;

  @Before
  public void beforeDefaultdataServerManagerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    makeMetaLeader();
    dataServerManager = new DefaultDataServerManager(metaServerConfig, metaLeaderService);
    dataServerManager.setMetaServerConfig(metaServerConfig).setSlotManager(new SimpleSlotManager());
    when(metaServerConfig.getExpireCheckIntervalMillis()).thenReturn(60);
  }

  @After
  public void afterDefaultdataServerManagerTest() throws Exception {
    dataServerManager.preDestory();
  }

  @Test
  public void testGetEpoch() {
    Assert.assertEquals(0, dataServerManager.getEpoch());
    dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
    dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
    Assert.assertNotEquals(0, dataServerManager.getEpoch());
  }

  @Test
  public void testIsLeaderGetClusterMembers() throws Exception {
    dataServerManager.postConstruct();
    DataNode node = new DataNode(randomURL(randomIp()), getDc());
    dataServerManager.renew(node, 1);
    Assert.assertTrue(dataServerManager.getDataServerMetaInfo().getEpoch() > 0);
    Assert.assertFalse(dataServerManager.getDataServerMetaInfo().getClusterMembers().isEmpty());
    Assert.assertEquals(1, dataServerManager.getDataServerMetaInfo().getClusterMembers().size());
    Assert.assertEquals(node, dataServerManager.getDataServerMetaInfo().getClusterMembers().get(0));
  }

  @Test
  public void testEvitTime() {
    Assert.assertEquals(60, dataServerManager.getEvictBetweenMilli());
  }

  @Test
  public void testGetDataServersStats() {
    DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
    List<DataNode> dataNodes = randomDataNodes(2);
    dataNodes.add(dataNode);
    SlotTable slotTable = randomSlotTable(dataNodes);
    List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
    HeartbeatRequest<DataNode> heartbeat =
        new HeartbeatRequest<>(
                dataNode,
                slotTable.getEpoch(),
                getDc(),
                System.currentTimeMillis(),
                SlotConfig.slotBasicInfo(),
                slotStatuses,
                Collections.EMPTY_MAP)
            .setSlotTable(slotTable);
    dataServerManager.onHeartbeat(heartbeat);
    List<DataServerStats> dataServerStats = dataServerManager.getDataServersStats();
    Assert.assertFalse(dataServerStats.isEmpty());
  }

  @Test
  public void testCancel() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    List<DataNode> dataNodes = randomDataNodes(10);
    for (DataNode dataNode : dataNodes) {
      dataServerManager.renew(dataNode, 1000);
    }
    NotifyObserversCounter counter = new NotifyObserversCounter();
    dataServerManager.addObserver(counter);
    dataServerManager.cancel(dataServerManager.getLease(dataNodes.get(0)));
    Assert.assertEquals(1, counter.getCounter());
  }

  @Test
  public void testRegister() throws TimeoutException, InterruptedException {
    NotifyObserversCounter counter = new NotifyObserversCounter();
    dataServerManager.addObserver(counter);
    makeMetaLeader();
    List<DataNode> dataNodes = randomDataNodes(10);
    for (DataNode dataNode : dataNodes) {
      dataServerManager.renew(dataNode, 1000);
    }
    Assert.assertEquals(10, counter.getCounter());
  }
}
