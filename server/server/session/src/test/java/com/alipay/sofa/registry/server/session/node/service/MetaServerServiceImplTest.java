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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.DataNodeNotifyExchanger;
import com.alipay.sofa.registry.server.session.slot.SlotTableCacheImpl;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaServerServiceImplTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaServerServiceImplTest.class);
  // the table epoch is 10
  private MetaServerServiceImpl impl;
  private SessionServerConfigBean sessionServerConfigBean;
  private SlotTableCacheImpl slotTableCache;

  @Test
  public void testChangeEvent() {
    init();
    SlotTableChangeEvent event = new SlotTableChangeEvent(impl.getCurrentSlotTableEpoch());
    Assert.assertTrue(
        event.toString(),
        event.toString().contains(String.valueOf(impl.getCurrentSlotTableEpoch())));
    Assert.assertEquals(event.getSlotTableEpoch(), impl.getCurrentSlotTableEpoch());
    Assert.assertFalse(impl.handleSlotTableChange(event));
    Assert.assertTrue(
        impl.handleSlotTableChange(new SlotTableChangeEvent(impl.getCurrentSlotTableEpoch() + 1)));
  }

  @Test
  public void testCreateRequest() {
    init();

    Assert.assertEquals(
        impl.getCurrentSlotTableEpoch(),
        slotTableCache.getEpoch(sessionServerConfigBean.getSessionServerDataCenter()));
    final long now = System.currentTimeMillis();
    HeartbeatRequest heartbeatRequest = impl.createRequest();
    LOGGER.info("hb={}", heartbeatRequest);

    Assert.assertEquals(
        heartbeatRequest.getDataCenter(), sessionServerConfigBean.getSessionServerDataCenter());
    Assert.assertEquals(heartbeatRequest.getDuration(), 0);
    SessionNode node = (SessionNode) heartbeatRequest.getNode();
    Assert.assertEquals(node.getProcessId(), ServerEnv.PROCESS_ID);
    Assert.assertEquals(node.getNodeUrl().getIpAddress(), ServerEnv.IP);

    Assert.assertTrue(heartbeatRequest.getTimestamp() >= now);
    Assert.assertTrue(heartbeatRequest.getTimestamp() <= System.currentTimeMillis());

    Assert.assertEquals(heartbeatRequest.getSlotTableEpoch(), impl.getCurrentSlotTableEpoch());
    Assert.assertEquals(heartbeatRequest.getSlotBasicInfo().getSlotNum(), SlotConfig.SLOT_NUM);
    Assert.assertEquals(
        heartbeatRequest.getSlotBasicInfo().getSlotReplicas(), SlotConfig.SLOT_REPLICAS);

    Assert.assertNotNull(heartbeatRequest.getSlotTable());
  }

  @Test
  public void testHandle() {
    init();
    impl = Mockito.spy(impl);
    DataNodeExchanger dataNodeExchanger = new DataNodeExchanger();
    DataNodeNotifyExchanger dataNodeNotifyExchanger = new DataNodeNotifyExchanger();
    impl.setDataNodeExchanger(dataNodeExchanger);
    impl.setDataNodeNotifyExchanger(dataNodeNotifyExchanger);

    Mockito.when(impl.getDataServerList()).thenReturn(Sets.newHashSet("d1", "d2"));

    BaseHeartBeatResponse resp =
        new BaseHeartBeatResponse(
            true,
            new VersionedList(10, Collections.emptyList()),
            null,
            new VersionedList(10, Collections.emptyList()),
            "xxx",
            100,
            Collections.emptyMap());

    SlotTable slotTable = slotTableCache.getLocalSlotTable();
    // resp table is null, not modify the cache.table
    impl.handleRenewResult(resp);

    Assert.assertEquals(dataNodeNotifyExchanger.getServerIps(), impl.getDataServerList());
    Assert.assertEquals(dataNodeExchanger.getServerIps(), impl.getDataServerList());
    Assert.assertEquals(dataNodeExchanger.getServerIps(), impl.getDataServerList());
    Assert.assertEquals(slotTable, slotTableCache.getLocalSlotTable());

    slotTable = new SlotTable(20, Collections.emptyList());
    resp =
        new BaseHeartBeatResponse(
            true,
            new VersionedList(10, Collections.emptyList()),
            slotTable,
            new VersionedList(10, Collections.emptyList()),
            "xxx",
            100,
            Collections.emptyMap());

    impl.handleRenewResult(resp);
    Assert.assertEquals(slotTable, slotTableCache.getLocalSlotTable());
  }

  private void init() {
    impl = new MetaServerServiceImpl();
    sessionServerConfigBean = TestUtils.newSessionConfig("testDc");

    impl.setSessionServerConfig(sessionServerConfigBean);
    Assert.assertEquals(
        impl.getRenewIntervalSecs(), sessionServerConfigBean.getSchedulerHeartbeatIntervalSecs());
    slotTableCache = new SlotTableCacheImpl();

    slotTableCache.setSessionServerConfig(sessionServerConfigBean);
    SlotTable slotTable = new SlotTable(10, Collections.emptyList());
    SlotGenericResource slotGenericResource = new SlotGenericResource();
    slotGenericResource.record(slotTable);
    slotTableCache.updateLocalSlotTable(slotTable);
    impl.setSlotTableCache(slotTableCache);
    impl.setDataCenterMetadataCache(TestUtils.newDataCenterMetaCache(sessionServerConfigBean));
  }
}
