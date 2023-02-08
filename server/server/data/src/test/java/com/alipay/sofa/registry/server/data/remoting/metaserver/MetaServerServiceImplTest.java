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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaServerServiceImplTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaServerServiceImplTest.class);
  private MetaServerServiceImpl impl;
  private DataServerConfig dataServerConfig;
  private SlotManager slotManager;
  private MultiClusterSlotManager multiClusterSlotManager;

  @Before
  public void beforeMetaServerServiceImplTest() {
    init();
  }

  @Test
  public void testCreateRequest() {
    LeaderSlotStatus leaderSlotStatus =
        new LeaderSlotStatus(10, 20, "xxx", BaseSlotStatus.LeaderStatus.HEALTHY);
    FollowerSlotStatus followerSlotStatus =
        new FollowerSlotStatus(
            11, 30, "yyy", System.currentTimeMillis(), System.currentTimeMillis());
    LOGGER.info("leaderStatus={}, followerStatus={}", leaderSlotStatus, followerSlotStatus);

    List<BaseSlotStatus> list = Lists.newArrayList(leaderSlotStatus, followerSlotStatus);
    when(slotManager.getSlotTableEpochAndStatuses(anyString())).thenReturn(Tuple.of(100L, list));
    when(multiClusterSlotManager.getSlotTableEpoch()).thenReturn(Collections.emptyMap());

    Assert.assertEquals(impl.getCurrentSlotTableEpoch(), slotManager.getSlotTableEpoch());
    final long now = System.currentTimeMillis();
    HeartbeatRequest heartbeatRequest = impl.createRequest();
    LOGGER.info("hb={}", heartbeatRequest);

    Assert.assertEquals(heartbeatRequest.getDataCenter(), dataServerConfig.getLocalDataCenter());
    Assert.assertEquals(heartbeatRequest.getDuration(), 0);
    DataNode dataNode = (DataNode) heartbeatRequest.getNode();
    Assert.assertEquals(dataNode.getDataCenter(), dataServerConfig.getLocalDataCenter());
    Assert.assertEquals(dataNode.getNodeUrl().getIpAddress(), ServerEnv.IP);

    Assert.assertTrue(heartbeatRequest.getTimestamp() >= now);
    Assert.assertTrue(heartbeatRequest.getTimestamp() <= System.currentTimeMillis());

    Assert.assertEquals(heartbeatRequest.getSlotTableEpoch(), 100);
    Assert.assertEquals(heartbeatRequest.getSlotStatus().get(0), leaderSlotStatus);
    Assert.assertEquals(heartbeatRequest.getSlotStatus().get(1), followerSlotStatus);

    Assert.assertNull(heartbeatRequest.getSlotTable());
  }

  @Test
  public void testHandle() {
    impl = Mockito.spy(impl);
    DataNodeExchanger dataNodeExchanger = new DataNodeExchanger();
    SessionNodeExchanger sessionNodeExchanger = new SessionNodeExchanger();
    impl.setDataNodeExchanger(dataNodeExchanger);
    impl.setSessionNodeExchanger(sessionNodeExchanger);

    when(impl.getSessionServerList()).thenReturn(Sets.newHashSet("s1", "s2"));
    when(impl.getDataServerList()).thenReturn(Sets.newHashSet("d1", "d2"));

    BaseHeartBeatResponse resp =
        new BaseHeartBeatResponse(
            false,
            new VersionedList(10, Collections.emptyList()),
            null,
            new VersionedList(10, Collections.emptyList()),
            "xxx",
            100,
            Collections.emptyMap());

    impl.handleRenewResult(resp);
    Assert.assertEquals(sessionNodeExchanger.getServerIps(), impl.getSessionServerList());
    Assert.assertEquals(dataNodeExchanger.getServerIps(), impl.getDataServerList());
    Mockito.verify(slotManager, Mockito.times(0)).updateSlotTable(Mockito.anyObject());

    SlotTable slotTable = new SlotTable(10, Collections.emptyList());
    resp =
        new BaseHeartBeatResponse(
            false,
            new VersionedList(10, Collections.emptyList()),
            slotTable,
            new VersionedList(10, Collections.emptyList()),
            "xxx",
            100,
            Collections.emptyMap());

    impl.handleRenewResult(resp);
    Mockito.verify(slotManager, Mockito.times(1)).updateSlotTable(Mockito.anyObject());
  }

  @Test
  public void testNotifySlotTable() {
    when(slotManager.getSlotTableEpochAndStatuses(anyString()))
        .thenReturn(new Tuple<>(1L, Lists.newArrayList()));
    when(multiClusterSlotManager.getSlotTableEpoch()).thenReturn(Collections.emptyMap());

    impl.record(new SlotTable(1L, Lists.newArrayList()));
    long slotTableEpoch = impl.createRequest().getSlotTableEpoch();
    Assert.assertEquals(1L, slotTableEpoch);
    Assert.assertEquals(1L, impl.createRequest().getSlotTable().getEpoch());
  }

  private void init() {
    impl = new MetaServerServiceImpl();
    dataServerConfig = TestBaseUtils.newDataConfig("testDc");

    impl.setDataServerConfig(dataServerConfig);
    Assert.assertEquals(
        impl.getRenewIntervalSecs(), dataServerConfig.getSchedulerHeartbeatIntervalSecs());

    slotManager = Mockito.mock(SlotManager.class);
    multiClusterSlotManager = Mockito.mock(MultiClusterSlotManager.class);

    impl.setSlotManager(slotManager).setMultiClusterSlotManager(multiClusterSlotManager);
  }
}
