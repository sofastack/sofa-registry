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
package com.alipay.sofa.registry.server.meta.multi.cluster;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.MetaLeaderNotWarmupException;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.server.meta.multi.cluster.DefaultMultiClusterSlotTableSyncer.RemoteClusterSlotState;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterMetaExchanger;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterSlotSyncResponse;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.test.TestUtils;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : DefaultMultiClusterSlotTableSyncerTest.java, v 0.1 2023年02月17日 11:27 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultMultiClusterSlotTableSyncerTest {

  @InjectMocks private DefaultMultiClusterSlotTableSyncer defaultMultiClusterSlotTableSyncer;

  @Mock private MetaLeaderService metaLeaderService;

  @Mock private MultiClusterMetaServerConfig multiClusterMetaServerConfig;

  @Mock private RemoteClusterMetaExchanger remoteClusterMetaExchanger;

  @Mock private ExecutorManager executorManager;

  private static final String TEST_DC = "test-dc1";
  private static final String TEST_DC_1 = "test-dc1";
  private static final String TEST_DC_2 = "test-dc2";
  private static final Set<String> REMOTES_1 = Sets.newHashSet(TEST_DC_1);
  private static final Set<String> REMOTES_1_2 = Sets.newHashSet(TEST_DC_1, TEST_DC_2);

  private static final String META_LEADER = "192.168.1.1";
  private static final long META_LEADER_EPOCH = System.currentTimeMillis();
  private static final int TABLE_EPOCH = 1;
  private static final int SLOT_LEADER_EPOCH = 1;
  private static final SlotTable SLOT_TABLE_0_1 =
      TestUtils.newTable_0_1(TABLE_EPOCH, SLOT_LEADER_EPOCH);
  private static final DataCenterMetadata METADATA =
      new DataCenterMetadata(TEST_DC, Sets.newHashSet("zone1", "zone2"));

  private static final KeyedThreadPoolExecutor executor =
      new KeyedThreadPoolExecutor("RemoteSlotSyncerExecutor", 10, 10);

  @Before
  public void init() {
    when(multiClusterMetaServerConfig.getMultiClusterConfigReloadMillis()).thenReturn(100);
    when(multiClusterMetaServerConfig.getRemoteSlotSyncerMillis()).thenReturn(100);
    when(executorManager.getMultiClusterConfigReloadExecutor())
        .thenReturn(
            new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));

    when(multiClusterMetaServerConfig.getRemoteSlotSyncerExecutorPoolSize()).thenReturn(10);
    when(multiClusterMetaServerConfig.getRemoteSlotSyncerExecutorQueueSize()).thenReturn(10);

    defaultMultiClusterSlotTableSyncer.init();
    defaultMultiClusterSlotTableSyncer.becomeLeader();
    when(executorManager.getRemoteSlotSyncerExecutor()).thenReturn(executor);
  }

  @Test
  public void testSyncSlotTable() {

    // TEST_DC_1,TEST_DC_2
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1_2);
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createUpgradeGenericResponse());
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(1, multiClusterSlotTable.size());

    // check TEST_DC
    RemoteClusterSlotState state = multiClusterSlotTable.get(TEST_DC);
    Assert.assertEquals(SLOT_TABLE_0_1, state.slotTable);
    Assert.assertEquals(METADATA, state.dataCenterMetadata);

    // check TEST_DC_1
    state = defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SLOT_TABLE_0_1, state.slotTable);
    Assert.assertEquals(METADATA, state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());

    // check TEST_DC_2
    state = defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_2);
    Assert.assertEquals(SLOT_TABLE_0_1, state.slotTable);
    Assert.assertEquals(METADATA, state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    multiClusterSlotTable = defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(1, multiClusterSlotTable.size());

    // check TEST_DC
    state = multiClusterSlotTable.get(TEST_DC);
    Assert.assertEquals(SLOT_TABLE_0_1, state.slotTable);
    Assert.assertEquals(METADATA, state.dataCenterMetadata);

    // check TEST_DC_1
    state = defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SLOT_TABLE_0_1, state.slotTable);
    Assert.assertEquals(METADATA, state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());

    // check TEST_DC_2
    state = defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_2);
    Assert.assertNull(state);
  }

  @Test
  public void testMetaNotLeader() {
    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createUpgradeGenericResponse());
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);
    when(metaLeaderService.amILeader()).thenReturn(false);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    System.out.println(multiClusterSlotTable);
    Assert.assertEquals(0, multiClusterSlotTable.size());
    Assert.assertNull(
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1));

    when(metaLeaderService.amILeader()).thenReturn(true);
  }

  @Test(expected = MetaLeaderNotWarmupException.class)
  public void testMetaNotWarmupException() {
    when(metaLeaderService.amIStableAsLeader()).thenReturn(false);
    defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
  }

  @Test
  public void testSendRequestError() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenThrow(new RuntimeException("expected exception."));

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    Assert.assertTrue(state.failCount.get() > 0);
  }

  @Test
  public void testHandleWrongResponse() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> RemoteClusterSlotSyncResponse.wrongLeader("1.1.1.1", 1));

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    Assert.assertTrue(state.failCount.get() > 0);
  }

  @Test
  public void testHandleNullDataResponse() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createEmptyDataGenericResponse());

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    Assert.assertTrue(state.failCount.get() > 0);
  }

  @Test
  public void testHandleWrongLeaderResponse() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createWrongLeaderGenericResponse());

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    Assert.assertTrue(state.failCount.get() > 0);
  }

  @Test
  public void testHandleLeaderNotWarmupResponse() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createLeaderNotWarmupedGenericResponse());

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    Assert.assertEquals(0, state.failCount.get());
  }

  @Test
  public void testResetMetaLeader() {
    when(remoteClusterMetaExchanger.sendRequest(anyString(), anyObject()))
        .thenReturn(() -> createWrongLeaderGenericResponse());

    // TEST_DC_1
    when(remoteClusterMetaExchanger.getAllRemoteClusters()).thenReturn(REMOTES_1);
    when(remoteClusterMetaExchanger.learn(anyString(), anyObject())).thenReturn(true);

    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

    ConcurrentUtils.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
    Map<String, RemoteClusterSlotState> multiClusterSlotTable =
        defaultMultiClusterSlotTableSyncer.getMultiClusterSlotTable();
    Assert.assertEquals(0, multiClusterSlotTable.size());

    // check TEST_DC_1
    RemoteClusterSlotState state =
        defaultMultiClusterSlotTableSyncer.getRemoteClusterSlotState().get(TEST_DC_1);
    Assert.assertEquals(SlotTable.INIT, state.slotTable);
    Assert.assertNull(state.dataCenterMetadata);
    Assert.assertTrue(state.task.isSuccess());
    verify(remoteClusterMetaExchanger, times(1)).resetLeader(anyString());
  }

  private GenericResponse<RemoteClusterSlotSyncResponse> createUpgradeGenericResponse() {
    GenericResponse<RemoteClusterSlotSyncResponse> genericResponse = new GenericResponse<>();
    genericResponse.setSuccess(true);
    genericResponse.setData(
        RemoteClusterSlotSyncResponse.upgrade(
            META_LEADER, META_LEADER_EPOCH, SLOT_TABLE_0_1, METADATA));
    return genericResponse;
  }

  private GenericResponse<RemoteClusterSlotSyncResponse> createWrongLeaderGenericResponse() {
    GenericResponse<RemoteClusterSlotSyncResponse> genericResponse = new GenericResponse<>();
    genericResponse.setSuccess(false);
    genericResponse.setData(
        RemoteClusterSlotSyncResponse.wrongLeader(META_LEADER + 1, META_LEADER_EPOCH + 1));
    return genericResponse;
  }

  private GenericResponse<RemoteClusterSlotSyncResponse> createLeaderNotWarmupedGenericResponse() {
    GenericResponse<RemoteClusterSlotSyncResponse> genericResponse = new GenericResponse<>();
    genericResponse.setSuccess(false);
    genericResponse.setData(
        RemoteClusterSlotSyncResponse.leaderNotWarmuped(META_LEADER + 1, META_LEADER_EPOCH + 1));
    return genericResponse;
  }

  private GenericResponse<RemoteClusterSlotSyncResponse> createNotUpgradeGenericResponse() {
    GenericResponse<RemoteClusterSlotSyncResponse> genericResponse = new GenericResponse<>();
    genericResponse.setSuccess(false);
    genericResponse.setData(
        RemoteClusterSlotSyncResponse.notUpgrade(META_LEADER, META_LEADER_EPOCH, METADATA));
    return genericResponse;
  }

  private GenericResponse<RemoteClusterSlotSyncResponse> createEmptyDataGenericResponse() {
    GenericResponse<RemoteClusterSlotSyncResponse> genericResponse = new GenericResponse<>();
    genericResponse.setSuccess(false);
    return genericResponse;
  }
}
