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
package com.alipay.sofa.registry.server.data.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class SlotManagerImplTest {

    @Test
    public void testUpdate() {
        SlotManagerImpl sm = mock(10, false, false).slotManager;
        SlotTable slotTable = new SlotTable(1, Collections.emptyList());
        Assert.assertTrue(sm.updateSlotTable(slotTable));
        Assert.assertFalse(sm.updateSlotTable(slotTable));

        slotTable = newTable_0_1(2, 2);
        Assert.assertTrue(sm.updateSlotTable(slotTable));

        try {
            sm.updateSlotTable(newTable_0_1(3, 1));
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("not expect Slot.LeaderEpoch"));
        }
        slotTable = newTable_0_1(3, 3);
        Assert.assertTrue(sm.updateSlotTable(slotTable));

        sm.processUpdating();
        Assert.assertEquals(sm.getSlotTableEpoch(), slotTable.getEpoch());
        slotEquals(slotTable, sm);
    }

    @Test
    public void testSlots() {
        Mock mock = mock(10, false, false);
        SlotManagerImpl sm = mock.slotManager;
        SlotTable slotTable = newTable_0_1(3, 3);
        Assert.assertTrue(sm.updateSlotTable(slotTable));
        sm.processUpdating();
        LocalDatumStorage storage = (LocalDatumStorage) mock.mockSync.syncer.getDatumStorage();
        // check slots, 2 slots [0,1]
        Assert.assertTrue(storage.updateVersion(0));
        Assert.assertTrue(storage.updateVersion(1));
        Assert.assertFalse(storage.updateVersion(2));

        Assert.assertTrue(sm.isLeader(0));
        Assert.assertTrue(sm.isFollower(1));

        Assert.assertFalse(sm.isFollower(0));
        Assert.assertFalse(sm.isLeader(1));

        slotTable = newTable_1_2(4, 4);
        Assert.assertTrue(sm.updateSlotTable(slotTable));
        sm.processUpdating();
        // check slots, 2 slots [1,2]
        Assert.assertFalse(storage.updateVersion(0));
        Assert.assertTrue(storage.updateVersion(1));
        Assert.assertTrue(storage.updateVersion(2));
        Assert.assertFalse(storage.updateVersion(3));

        Assert.assertTrue(sm.isLeader(1));
        Assert.assertTrue(sm.isFollower(2));

        Assert.assertFalse(sm.isFollower(1));
        Assert.assertFalse(sm.isLeader(2));

    }

    @Test
    public void testCheckAccess() {
        Mock mock = mock(10, false, false);
        SlotManagerImpl sm = mock.slotManager;
        SlotTable slotTable = newTable_0_1(3, 3);
        sm.updateSlotTable(slotTable);
        sm.processUpdating();

        SlotAccess access0 = sm.checkSlotAccess(0, 100, slotTable.getSlot(0).getLeaderEpoch());
        Assert.assertTrue(access0.isMigrating());
        SlotAccess access1 = sm.checkSlotAccess(1, 100, slotTable.getSlot(1).getLeaderEpoch());
        Assert.assertTrue(access1.isMoved());
    }

    @Test
    public void testCheckAccessFull() {
        Mock mock = mock(10, false, false);
        SlotManagerImpl sm = mock.slotManager;
        SlotAccess access = sm.checkSlotAccess(0, 100, null, 100);
        Assert.assertTrue(access.isMoved());
        SlotManagerImpl.SlotState slotState = new SlotManagerImpl.SlotState(createFollower(0, 100));
        access = sm.checkSlotAccess(0, 100, slotState, 100);
        Assert.assertTrue(access.isMoved());

        slotState = new SlotManagerImpl.SlotState(createLeader(0, 100));
        access = sm.checkSlotAccess(0, 100, slotState, 100);
        Assert.assertTrue(access.isMigrating());

        slotState = new SlotManagerImpl.SlotState(createLeader(0, 100));
        slotState.migrated = true;
        access = sm.checkSlotAccess(0, 100, slotState, 101);
        Assert.assertTrue(access.isMisMatch());

        slotState = new SlotManagerImpl.SlotState(createLeader(0, 100));
        slotState.migrated = true;
        access = sm.checkSlotAccess(0, 100, slotState, 100);
        Assert.assertTrue(access.isAccept());
    }

    static void slotEquals(SlotTable table, SlotManagerImpl sm) {
        Map<Integer, Slot> slotMap = table.getSlotMap();
        for (Slot slot : slotMap.values()) {
            Slot s = sm.getSlot(slot.getId());
            Assert.assertEquals(s, slot);
        }
        Assert.assertEquals(sm.getSlotStatuses().size(), slotMap.size());
    }

    static Slot createLeader(int slotId, long leaderEpoch) {
        return new Slot(slotId, ServerEnv.IP, leaderEpoch, Lists.newArrayList("xxx"));
    }

    static Slot createFollower(int slotId, long leaderEpoch) {
        return new Slot(slotId, "xxx", leaderEpoch, Lists.newArrayList(ServerEnv.IP, "yyy"));
    }

    static SlotTable newTable_0_1(int tableEpoch, int leaderEpoch) {
        Slot slot0 = createLeader(0, leaderEpoch);
        Slot slot1 = createFollower(1, leaderEpoch);
        SlotTable slotTable = new SlotTable(tableEpoch, Lists.newArrayList(slot0, slot1));
        return slotTable;
    }

    static SlotTable newTable_1_2(int tableEpoch, int leaderEpoch) {
        Slot slot1 = createLeader(1, leaderEpoch);
        Slot slot2 = createFollower(2, leaderEpoch);
        SlotTable slotTable = new SlotTable(tableEpoch, Lists.newArrayList(slot1, slot2));
        return slotTable;
    }

    static Mock mock(int slotId, boolean initSync, boolean initExecutor) {
        SlotDiffSyncerTest.MockSync mockSync = SlotDiffSyncerTest.mockSync(slotId, "testDc",
            initSync);
        SlotDiffSyncer syncer = mockSync.syncer;
        SlotManagerImpl slotManager = new SlotManagerImpl();
        slotManager.setDataChangeEventCenter(new DataChangeEventCenter());
        slotManager.setSessionLeaseManager(new SessionLeaseManager());
        slotManager.setSlotGenericResource(new SlotGenericResource());
        slotManager.setLocalDatumStorage(syncer.getDatumStorage());
        slotManager.setDataServerConfig(syncer.getDataServerConfig());
        slotManager.initSlotChangeListener();
        if (initExecutor) {
            slotManager.initExecutors();
        }

        Mock mock = new Mock();
        mock.mockSync = mockSync;
        mock.slotManager = slotManager;
        return mock;
    }

    static final class Mock {
        SlotDiffSyncerTest.MockSync mockSync;
        SlotManagerImpl             slotManager;

    }

}
