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
package com.alipay.sofa.registry.server.data.multi.cluster.slot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.Slot.Role;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.exception.UnSupportOperationException;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManagerImpl.RemoteSlotTableStates;
import com.alipay.sofa.registry.server.data.multi.cluster.sync.info.FetchMultiSyncService;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListenerManager;
import com.alipay.sofa.registry.server.data.slot.SlotDiffSyncer;
import com.alipay.sofa.registry.server.data.slot.SlotDiffSyncerTest;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImplTest;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotManagerImplTest.java, v 0.1 2023年02月09日 17:02 xiaojian.xj Exp $
 */
public class MultiClusterSlotManagerImplTest {

  private static final String DATACENTER = "testdc";
  private static final String REMOTE_DATACENTER = "REMOTE_DATACENTER";
  private static final String REMOTE_DATACENTER_1 = "REMOTE_DATACENTER_1";

  private static final DataCenterMetadata dataCenterMetadata =
      new DataCenterMetadata(REMOTE_DATACENTER, Sets.newHashSet("remote_zone1", "remote_zone2"));

  private static final MultiClusterDataServerConfig multiClusterDataServerConfig =
      TestBaseUtils.newMultiDataConfig();
  private static final MultiClusterExecutorManager multiClusterExecutorManager =
      new MultiClusterExecutorManager(multiClusterDataServerConfig);

  static Slot createLeader(int slotId, long leaderEpoch, String address) {
    return new Slot(slotId, address, leaderEpoch, Lists.newArrayList("xxx"));
  }

  static Slot createFollower(int slotId, long leaderEpoch, String address) {
    return new Slot(slotId, "xxx", leaderEpoch, Lists.newArrayList(address, "yyy"));
  }

  static Slot createSelfLeader(int slotId, long leaderEpoch) {
    return createLeader(slotId, leaderEpoch, ServerEnv.IP);
  }

  static Slot createSelfFollower(int slotId, long leaderEpoch) {
    return createFollower(slotId, leaderEpoch, ServerEnv.IP);
  }

  static SlotTable newTable_0_1(long tableEpoch, long leaderEpoch) {
    Slot slot0 = createSelfLeader(0, leaderEpoch);
    Slot slot1 = createSelfLeader(1, leaderEpoch);
    return new SlotTable(tableEpoch, Lists.newArrayList(slot0, slot1));
  }

  static SlotTable newTable_1_2(long tableEpoch, long leaderEpoch) {
    Slot slot1 = createSelfLeader(1, leaderEpoch);
    Slot slot2 = createSelfLeader(2, leaderEpoch);
    return new SlotTable(tableEpoch, Lists.newArrayList(slot1, slot2));
  }

  static SlotTable newTable_follower0_leader1_follower2(long tableEpoch, long leaderEpoch) {
    Slot slot0 = createSelfFollower(0, leaderEpoch);
    Slot slot1 = createSelfLeader(1, leaderEpoch);
    Slot slot2 = createSelfFollower(2, leaderEpoch);
    return new SlotTable(tableEpoch, Lists.newArrayList(slot0, slot1, slot2));
  }

  @Test
  public void testUpdateSlotTable() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // check datum storage
    DatumStorageDelegate delegate = mock.mockSync.syncer.getDatumStorageDelegate();
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 0).isEmpty());
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 1).isEmpty());
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 2).isEmpty());

    Publisher p0 = TestBaseUtils.createTestPublishers(0, 1).get(0);
    delegate.putPublisher(REMOTE_DATACENTER, p0);
    Assert.assertFalse(delegate.updateVersion(REMOTE_DATACENTER, 0).isEmpty());
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 1).isEmpty());

    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 0).containsKey(p0.getDataInfoId()));
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 0).size() == 1);

    Publisher p1 = TestBaseUtils.createTestPublishers(1, 1).get(0);
    delegate.putPublisher(REMOTE_DATACENTER, p1);
    Assert.assertFalse(delegate.updateVersion(REMOTE_DATACENTER, 0).isEmpty());
    Assert.assertFalse(delegate.updateVersion(REMOTE_DATACENTER, 1).isEmpty());

    // slot[0,1] update fail, slotTableEpoch=1, leaderEpoch=2, notUpgrade
    slotTableEpoch = 1;
    leaderEpoch = 2;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER,
            RemoteSlotTableStatus.notUpgrade(slotTable.getEpoch(), dataCenterMetadata));

    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, 1, 1);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // slot[0,1] update fail, slotTableEpoch=2, leaderEpoch=2, conflict
    slotTableEpoch = 2;
    leaderEpoch = 2;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(REMOTE_DATACENTER, RemoteSlotTableStatus.conflict(slotTable));

    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, 1, 1);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(1, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // slot[0,1] update fail, slotTableEpoch=2, leaderEpoch=0, conflict
    slotTableEpoch = 2;
    leaderEpoch = 0;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, 1, 1);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(1, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // slot[0,1] update success, slotTableEpoch=2, leaderEpoch=2
    slotTableEpoch = 2;
    leaderEpoch = 2;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // slot[1,2] update success, slotTableEpoch=3, leaderEpoch=3
    slotTableEpoch = 3;
    leaderEpoch = 3;
    slotTable = newTable_1_2(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0));
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2).getLeaderEpoch());
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    Publisher p2 = TestBaseUtils.createTestPublishers(2, 1).get(0);
    delegate.putPublisher(REMOTE_DATACENTER, p2);
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 0).isEmpty());
    Assert.assertFalse(delegate.updateVersion(REMOTE_DATACENTER, 1).isEmpty());
    Assert.assertFalse(delegate.updateVersion(REMOTE_DATACENTER, 2).isEmpty());

    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 1).containsKey(p1.getDataInfoId()));
    Assert.assertTrue(delegate.updateVersion(REMOTE_DATACENTER, 2).containsKey(p2.getDataInfoId()));
  }

  @Test
  public void testRemoveDataCenter() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1, (REMOTE_DATACENTER,
    // REMOTE_DATACENTER_1)
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);

    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus = new HashMap<>();
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER_1, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER_1).longValue());

    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));

    Assert.assertEquals(
        1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 0).getLeaderEpoch());
    Assert.assertEquals(
        1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 1));

    // slot[0,1] update success, slotTableEpoch=2, leaderEpoch=2, (REMOTE_DATACENTER)
    slotTableEpoch = 2;
    leaderEpoch = 2;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);

    remoteSlotTableStatus = new HashMap<>();
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));

    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 0));
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 0));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 1));
  }

  @Test
  public void testWatchLocalSegSlot() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // slot[1,2] update success, slotTableEpoch=2, leaderEpoch=2
    slotTableEpoch = 2;
    leaderEpoch = 2;
    slotTable = newTable_1_2(slotTableEpoch, leaderEpoch);

    SlotTable newSlotTable = newTable_follower0_leader1_follower2(slotTableEpoch, leaderEpoch);
    remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(newSlotTable, dataCenterMetadata));

    // update slotManager with slot[1,2] leader
    // update multiClusterSlotManager  with slot[1] leader, slot[2] follower
    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0));
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2).getLeaderEpoch());
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));
  }

  @Test
  public void testRemoveDataCenterFail() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    MultiClusterSyncRepository multiClusterSyncRepository = mock(MultiClusterSyncRepository.class);

    Set<MultiClusterSyncInfo> syncInfos =
        Sets.newHashSet(
            new MultiClusterSyncInfo(
                DATACENTER,
                REMOTE_DATACENTER,
                "aaa",
                true,
                true,
                Collections.EMPTY_SET,
                Collections.EMPTY_SET,
                Collections.EMPTY_SET,
                System.currentTimeMillis()),
            new MultiClusterSyncInfo(
                DATACENTER,
                REMOTE_DATACENTER_1,
                "aaa",
                true,
                true,
                Collections.EMPTY_SET,
                Collections.EMPTY_SET,
                Collections.EMPTY_SET,
                System.currentTimeMillis()));
    when(multiClusterSyncRepository.queryLocalSyncInfos()).thenReturn(syncInfos);
    multiClusterSlotManager.setMultiClusterSyncRepository(multiClusterSyncRepository);

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1, (REMOTE_DATACENTER,
    // REMOTE_DATACENTER_1)
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);

    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus = new HashMap<>();
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER_1, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER_1).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 1));

    // slot[0,1] update success, slotTableEpoch=2, leaderEpoch=2, (REMOTE_DATACENTER)
    slotTableEpoch = 2;
    leaderEpoch = 2;
    slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);

    remoteSlotTableStatus = new HashMap<>();
    remoteSlotTableStatus.put(
        REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());
    Assert.assertEquals(1, map.get(REMOTE_DATACENTER_1).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));

    Assert.assertEquals(
        1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 0).getLeaderEpoch());
    Assert.assertEquals(
        1, multiClusterSlotManager.getSlot(REMOTE_DATACENTER_1, 1).getLeaderEpoch());
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER_1, 1));
  }

  @Test(expected = UnSupportOperationException.class)
  public void testIsFollower() {
    new MultiClusterSlotManagerImpl().isFollower(REMOTE_DATACENTER, 1);
  }

  @Test
  public void testSyncRemote() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    MultiSegmentSyncSwitch syncSwitch =
        TestBaseUtils.newMultiSegmentSyncSwitchWithGroups(
            REMOTE_DATACENTER, Sets.newHashSet("SOFA", "SOFA_APP"));
    FetchMultiSyncService fetchMultiSyncService = new FetchMultiSyncService();
    fetchMultiSyncService.setSyncMap(Collections.singletonMap(REMOTE_DATACENTER, syncSwitch));
    multiClusterSlotManager.setFetchMultiSyncService(fetchMultiSyncService);

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    RemoteSlotTableStates slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    Assert.assertEquals(2, slotTableStates.slotStates.size());
    Assert.assertEquals(slotTable, slotTableStates.slotTable);
    // check slotId 0
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncRemoteTask.isFinished());
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncRemoteTask.isFailed());
    Assert.assertFalse(slotTableStates.slotStates.get(0).syncRemoteTask.isSuccess());
    Assert.assertFalse(slotTableStates.slotStates.get(0).synced);
    Assert.assertEquals(-1, slotTableStates.slotStates.get(0).lastSuccessSyncRemoteTime);

    // check slotId 1
    Assert.assertTrue(slotTableStates.slotStates.get(1).syncRemoteTask.isFinished());
    Assert.assertTrue(slotTableStates.slotStates.get(1).syncRemoteTask.isFailed());
    Assert.assertFalse(slotTableStates.slotStates.get(1).syncRemoteTask.isSuccess());
    Assert.assertFalse(slotTableStates.slotStates.get(1).synced);
    Assert.assertEquals(-1, slotTableStates.slotStates.get(1).lastSuccessSyncRemoteTime);
  }

  @Test
  public void testSyncRemoteDataInfoIds() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    // sync no data
    MultiSegmentSyncSwitch syncSwitch =
        TestBaseUtils.newMultiSegmentSyncSwitchWithDataInfoIds(
            REMOTE_DATACENTER, Sets.newHashSet());
    multiClusterSlotManager.setMultiSyncDataAcceptorManager(
        TestBaseUtils.newMultiSyncDataAcceptorManager(syncSwitch));

    FetchMultiSyncService fetchMultiSyncService = new FetchMultiSyncService();
    fetchMultiSyncService.setSyncMap(Collections.singletonMap(REMOTE_DATACENTER, syncSwitch));
    multiClusterSlotManager.setFetchMultiSyncService(fetchMultiSyncService);

    Publisher pub0 = TestBaseUtils.createTestPublishers(0, 1).get(0);
    multiClusterSlotManager.dataChangeNotify(
        REMOTE_DATACENTER, Sets.newHashSet(pub0.getDataInfoId()));

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    RemoteSlotTableStates slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    Assert.assertNull(slotTableStates.slotStates.get(0).syncDataIdTask);

    // sync dataid
    multiClusterSlotManager.getWatchDog().suspend();

    syncSwitch =
        TestBaseUtils.newMultiSegmentSyncSwitchWithDataInfoIds(
            REMOTE_DATACENTER, Sets.newHashSet(pub0.getDataInfoId()));
    multiClusterSlotManager.setMultiSyncDataAcceptorManager(
        TestBaseUtils.newMultiSyncDataAcceptorManager(syncSwitch));

    fetchMultiSyncService = new FetchMultiSyncService();
    fetchMultiSyncService.setSyncMap(Collections.singletonMap(REMOTE_DATACENTER, syncSwitch));
    multiClusterSlotManager.setFetchMultiSyncService(fetchMultiSyncService);
    multiClusterSlotManager.dataChangeNotify(
        REMOTE_DATACENTER, Sets.newHashSet(pub0.getDataInfoId()));

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    Assert.assertNull(slotTableStates.slotStates.get(0).syncDataIdTask);
    Assert.assertEquals(
        Sets.newHashSet(pub0.getDataInfoId()),
        slotTableStates.slotStates.get(0).pendingDataInfoIds.get());

    multiClusterSlotManager.getWatchDog().resume();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Assert.assertNotNull(slotTableStates.slotStates.get(0).syncDataIdTask);
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncDataIdTask.isFinished());
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncDataIdTask.isFailed());
    int syncingSize =
        slotTableStates.slotStates.get(0).syncDataIdTask.getRunnable().getSyncing().size();
    int pendingSize = slotTableStates.slotStates.get(0).pendingDataInfoIds.get().size();
    Assert.assertEquals(1, syncingSize + pendingSize);

    KeyedTask mockKT = mock(KeyedTask.class);
    when(mockKT.isFinished()).thenReturn(true);
    when(mockKT.isFailed()).thenReturn(false);
    slotTableStates.slotStates.get(0).syncDataIdTask = mockKT;
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    pendingSize = slotTableStates.slotStates.get(0).pendingDataInfoIds.get().size();
    Assert.assertEquals(0, pendingSize);
  }

  @Test
  public void testSyncRemoteDataInfoIdsMaxAndNoRetry() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    Set<String> pubs = TestBaseUtils.createTestDataInfoIds(0, 1000);
    multiClusterSlotManager.dataChangeNotify(REMOTE_DATACENTER, pubs);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    RemoteSlotTableStates slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    Assert.assertNull(slotTableStates.slotStates.get(0).syncDataIdTask);

    // sync dataid
    multiClusterSlotManager.getWatchDog().suspend();

    MultiSegmentSyncSwitch syncSwitch =
        TestBaseUtils.newMultiSegmentSyncSwitchWithDataInfoIds(REMOTE_DATACENTER, pubs);
    multiClusterSlotManager.setMultiSyncDataAcceptorManager(
        TestBaseUtils.newMultiSyncDataAcceptorManager(syncSwitch));

    FetchMultiSyncService fetchMultiSyncService = new FetchMultiSyncService();
    fetchMultiSyncService.setSyncMap(Collections.singletonMap(REMOTE_DATACENTER, syncSwitch));
    multiClusterSlotManager.setFetchMultiSyncService(fetchMultiSyncService);
    multiClusterSlotManager.dataChangeNotify(REMOTE_DATACENTER, pubs);

    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    Assert.assertNull(slotTableStates.slotStates.get(0).syncDataIdTask);
    Assert.assertEquals(pubs, slotTableStates.slotStates.get(0).pendingDataInfoIds.get());

    multiClusterSlotManager.getWatchDog().resume();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Assert.assertNotNull(slotTableStates.slotStates.get(0).syncDataIdTask);
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncDataIdTask.isFinished());
    Assert.assertTrue(slotTableStates.slotStates.get(0).syncDataIdTask.isFailed());
    int syncingSize =
        slotTableStates.slotStates.get(0).syncDataIdTask.getRunnable().getSyncing().size();
    int pendingSize = slotTableStates.slotStates.get(0).pendingDataInfoIds.get().size();
    Assert.assertEquals(1000, syncingSize);
    Assert.assertEquals(0, pendingSize);

    String pub0 = pubs.stream().findFirst().get();
    multiClusterSlotManager.dataChangeNotify(REMOTE_DATACENTER, Sets.newHashSet(pub0));
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    syncingSize =
        slotTableStates.slotStates.get(0).syncDataIdTask.getRunnable().getSyncing().size();
    pendingSize = slotTableStates.slotStates.get(0).pendingDataInfoIds.get().size();
    Assert.assertEquals(1, syncingSize + pendingSize);
  }

  @Test
  public void testCheckSlotAccess() {
    MockMultiClusterSlotManagerImpl mock =
        mockMultiClusterSlotManagerImpl(10, false, false, Collections.EMPTY_SET);
    MultiClusterSlotManagerImpl multiClusterSlotManager = mock.multiClusterSlotManager;
    SlotManagerImpl slotManager = mock.slotManager;

    // slot[0,1] update success, slotTableEpoch=1, leaderEpoch=1
    long slotTableEpoch = 1;
    long leaderEpoch = 1;
    SlotTable slotTable = newTable_0_1(slotTableEpoch, leaderEpoch);
    Map<String, RemoteSlotTableStatus> remoteSlotTableStatus =
        Collections.singletonMap(
            REMOTE_DATACENTER, RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata));

    Assert.assertTrue(slotManager.updateSlotTable(slotTable));
    multiClusterSlotManager.updateSlotTable(remoteSlotTableStatus);

    Assert.assertFalse(multiClusterSlotManager.isLeader(DATACENTER, 1));
    Assert.assertNull(multiClusterSlotManager.getSlot(DATACENTER, 1));
    slotManager.processUpdating();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    checkSlotTableEpochAndStatuses(multiClusterSlotManager, slotTableEpoch, leaderEpoch);

    Map<String, Long> map = multiClusterSlotManager.getSlotTableEpoch();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(slotTableEpoch, map.get(REMOTE_DATACENTER).longValue());

    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 0).getLeaderEpoch());
    Assert.assertEquals(
        leaderEpoch, multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 1).getLeaderEpoch());
    Assert.assertNull(multiClusterSlotManager.getSlot(REMOTE_DATACENTER, 2));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 0));
    Assert.assertTrue(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 1));
    Assert.assertFalse(multiClusterSlotManager.isLeader(REMOTE_DATACENTER, 2));

    Assert.assertNull(
        multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER_1, 0, 2, leaderEpoch));

    // migrating
    SlotAccess access0 =
        multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 0, 2, leaderEpoch);
    Assert.assertTrue(access0.isMigrating());

    SlotAccess access1 =
        multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 1, 2, leaderEpoch);
    Assert.assertTrue(access1.isMigrating());

    SlotAccess access2 =
        multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 2, 2, leaderEpoch);
    Assert.assertTrue(access2.isMoved());

    // mismatch
    RemoteSlotTableStates slotTableStates =
        multiClusterSlotManager.getSlotTableStorage(REMOTE_DATACENTER).getSlotTableStates();
    slotTableStates.slotStates.get(0).synced = true;
    slotTableStates.slotStates.get(1).synced = true;

    access0 = multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 0, 2, 2);
    Assert.assertTrue(access0.isMisMatch());

    access1 = multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 1, 2, 2);
    Assert.assertTrue(access1.isMisMatch());

    // accept
    access0 = multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 0, 2, leaderEpoch);
    Assert.assertTrue(access0.isAccept());

    access1 = multiClusterSlotManager.checkSlotAccess(REMOTE_DATACENTER, 1, 2, leaderEpoch);
    Assert.assertTrue(access1.isAccept());
  }

  private void checkSlotTableEpochAndStatuses(
      MultiClusterSlotManagerImpl multiClusterSlotManager,
      long expectSlotTableEpoch,
      long expectLeaderEpoch) {
    Tuple<Long, List<BaseSlotStatus>> slotTableEpochAndStatuses =
        multiClusterSlotManager.getSlotTableEpochAndStatuses(REMOTE_DATACENTER);
    List<BaseSlotStatus> slotStatuses = slotTableEpochAndStatuses.o2;
    Assert.assertEquals(expectSlotTableEpoch, slotTableEpochAndStatuses.o1.longValue());
    Assert.assertEquals(2, slotStatuses.size());
    for (BaseSlotStatus slotStatus : slotStatuses) {
      Assert.assertEquals(expectLeaderEpoch, slotStatus.getSlotLeaderEpoch());
      Assert.assertEquals(Role.Leader, slotStatus.getRole());
    }
  }

  static MockMultiClusterSlotManagerImpl mockMultiClusterSlotManagerImpl(
      int slotId, boolean initSync, boolean initExecutor, Set<String> sessions) {
    SlotDiffSyncerTest.MockSync mockSync =
        SlotDiffSyncerTest.mockSync(slotId, DATACENTER, initSync);
    SlotDiffSyncer syncer = mockSync.syncer;
    MultiClusterSlotManagerImpl multiClusterSlotManager = new MultiClusterSlotManagerImpl();
    multiClusterSlotManager.setDataServerConfig(syncer.getDataServerConfig());

    multiClusterSlotManager.setMultiClusterDataServerConfig(multiClusterDataServerConfig);
    multiClusterSlotManager.setDataChangeEventCenter(new DataChangeEventCenter());
    multiClusterSlotManager.setDatumStorageDelegate(
        (DatumStorageDelegate) syncer.getDatumStorageDelegate());
    SlotChangeListenerManager listenerManager = new SlotChangeListenerManager();
    listenerManager.setDatumStorageDelegate(syncer.getDatumStorageDelegate());
    multiClusterSlotManager.setSlotChangeListenerManager(listenerManager);

    multiClusterSlotManager.setMultiClusterExecutorManager(multiClusterExecutorManager);

    MultiSegmentSyncSwitch syncSwitch =
        TestBaseUtils.newMultiSegmentSyncSwitchWithGroups(
            REMOTE_DATACENTER, Sets.newHashSet("SOFA", "SOFA_APP"));
    multiClusterSlotManager.setMultiSyncDataAcceptorManager(
        TestBaseUtils.newMultiSyncDataAcceptorManager(syncSwitch));
    FetchMultiSyncService fetchMultiSyncService = new FetchMultiSyncService();
    fetchMultiSyncService.setSyncMap(Collections.singletonMap(DATACENTER, syncSwitch));
    multiClusterSlotManager.setFetchMultiSyncService(fetchMultiSyncService);

    MultiClusterSyncRepository multiClusterSyncRepository = mock(MultiClusterSyncRepository.class);

    MultiClusterSyncInfo multiClusterSyncInfo =
        new MultiClusterSyncInfo(
            DATACENTER,
            syncSwitch.getRemoteDataCenter(),
            "aaa",
            syncSwitch.isMultiSync(),
            syncSwitch.isMultiPush(),
            syncSwitch.getSyncDataInfoIds(),
            syncSwitch.getSynPublisherGroups(),
            syncSwitch.getIgnoreDataInfoIds(),
            syncSwitch.getDataVersion());
    when(multiClusterSyncRepository.queryLocalSyncInfos())
        .thenReturn(Sets.newHashSet(multiClusterSyncInfo));
    multiClusterSlotManager.setMultiClusterSyncRepository(multiClusterSyncRepository);

    SlotManagerImpl slotManager =
        SlotManagerImplTest.mockSM(slotId, initSync, initExecutor, sessions).slotManager;
    multiClusterSlotManager.setSlotManager(slotManager);

    MockMultiClusterSlotManagerImpl mock = new MockMultiClusterSlotManagerImpl();
    mock.mockSync = mockSync;
    mock.slotManager = slotManager;
    mock.multiClusterSlotManager = multiClusterSlotManager;

    if (initExecutor) {
      mock.slotManager.init();
    }
    mock.multiClusterSlotManager.init();
    return mock;
  }

  static final class MockMultiClusterSlotManagerImpl {
    SlotDiffSyncerTest.MockSync mockSync;
    MultiClusterSlotManagerImpl multiClusterSlotManager;
    SlotManagerImpl slotManager;
  }
}
