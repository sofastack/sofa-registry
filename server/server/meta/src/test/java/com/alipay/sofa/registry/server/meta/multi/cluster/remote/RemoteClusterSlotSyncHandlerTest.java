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
package com.alipay.sofa.registry.server.meta.multi.cluster.remote;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;
import com.alipay.sofa.registry.test.TestUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : RemoteClusterSlotSyncHandlerTest.java, v 0.1 2023年02月17日 20:21 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteClusterSlotSyncHandlerTest {

  @InjectMocks private RemoteClusterSlotSyncHandler remoteClusterSlotSyncHandler;

  @Mock private ExecutorManager executorManager;

  @Mock private CurrentDcMetaServer currentDcMetaServer;

  @Mock private MetaLeaderService metaLeaderService;

  @Mock private MetaServerConfig metaServerConfig;

  @Mock private DefaultCommonConfig defaultCommonConfig;

  private static final String CLUSTER_ID = "test-cluster-id";
  private static final LeaderInfo LEADER_INFO =
      new LeaderInfo(System.currentTimeMillis(), "192.168.1.1", System.currentTimeMillis() + 10000);
  private static final Set<String> LOCAL_ZONES = Sets.newHashSet("zone1", "zone2");

  @Before
  public void init() {
    when(defaultCommonConfig.getDefaultClusterId()).thenReturn(CLUSTER_ID);
    when(metaLeaderService.getLeaderInfo()).thenReturn(LEADER_INFO);
    when(metaServerConfig.getLocalDataCenterZones()).thenReturn(LOCAL_ZONES);
  }

  @Test
  public void testHandle() {
    long tableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = TestUtils.newTable_0_1(tableEpoch, leaderEpoch);

    // not leader
    when(metaLeaderService.amILeader()).thenReturn(false);
    RemoteClusterSlotSyncRequest request = new RemoteClusterSlotSyncRequest(CLUSTER_ID, tableEpoch);
    GenericResponse<RemoteClusterSlotSyncResponse> response =
        remoteClusterSlotSyncHandler.doHandle(null, request);
    Assert.assertFalse(response.isSuccess());
    Assert.assertFalse(response.getData().isSyncOnLeader());
    Assert.assertFalse(response.getData().isLeaderWarmuped());
    Assert.assertFalse(response.getData().isSlotTableUpgrade());
    Assert.assertNull(response.getData().getSlotTable());

    // not warmup leader
    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(false);

    response = remoteClusterSlotSyncHandler.doHandle(null, request);
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(response.getData().isSyncOnLeader());
    Assert.assertFalse(response.getData().isLeaderWarmuped());
    Assert.assertFalse(response.getData().isSlotTableUpgrade());
    Assert.assertNull(response.getData().getSlotTable());

    // leader and warmup, tableEpoch conflict
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
    when(currentDcMetaServer.getSlotTable()).thenReturn(slotTable);
    request = new RemoteClusterSlotSyncRequest(CLUSTER_ID, slotTable.getEpoch() + 1);
    response = remoteClusterSlotSyncHandler.doHandle(null, request);
    Assert.assertFalse(response.isSuccess());
    Assert.assertNull(response.getData());

    // leader and warmup, not upgrade
    request = new RemoteClusterSlotSyncRequest(CLUSTER_ID, tableEpoch);
    response = remoteClusterSlotSyncHandler.doHandle(null, request);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getData().isSyncOnLeader());
    Assert.assertTrue(response.getData().isLeaderWarmuped());
    Assert.assertFalse(response.getData().isSlotTableUpgrade());
    Assert.assertNull(response.getData().getSlotTable());

    // leader and warmup, upgrade
    request = new RemoteClusterSlotSyncRequest(CLUSTER_ID, tableEpoch - 1);
    response = remoteClusterSlotSyncHandler.doHandle(null, request);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getData().isSyncOnLeader());
    Assert.assertTrue(response.getData().isLeaderWarmuped());
    Assert.assertTrue(response.getData().isSlotTableUpgrade());
    Assert.assertEquals(slotTable.filterLeaderInfo(), response.getData().getSlotTable());
    Assert.assertEquals(
        new DataCenterMetadata(CLUSTER_ID, LOCAL_ZONES),
        response.getData().getDataCenterMetadata());
  }
}
