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

import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-02 15:25 yuzhi.lyz Exp $
 */
public final class DefaultDataSlotManager implements DataSlotManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataSlotManager.class);

    private final SlotFunction slotFunction = new MD5SlotFunction();

    @Autowired
    private MetaNodeExchanger metaNodeExchanger;

    private final    Lock      lock      = new ReentrantLock();
    private volatile SlotState slotState = SlotState.INIT;

    @Override
    public SlotAccess checkSlotAccess(String dataInfoId, long srcSlotEpoch) {
        final SlotState curState = slotState;
        final long currentEpoch = curState.table.getEpoch();
        if (currentEpoch < srcSlotEpoch) {
            triggerUpdateSlotTable(srcSlotEpoch);
        }

        final short slotId = slotFunction.slotOf(dataInfoId);
        final Slot targetSlot = curState.table.getSlot(slotId);
        if (targetSlot == null || !DataServerConfig.IP.equals(targetSlot.getLeader())) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Moved);
        }
        if (curState.migrating.contains(slotId)) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Migrating);
        }
        return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Accept);
    }

    @Override
    public boolean updateSlotTable(SlotTable slotTable) {
        // avoid the update concurrent
        lock.lock();
        try {
            final SlotState curState = slotState;
            final long curEpoch = curState.table.getEpoch();
            if (curEpoch >= slotTable.getEpoch()) {
                LOGGER.info("epoch has updated, expect={}, current={}", slotTable.getEpoch(), curEpoch);
                return false;
            }

            final SlotState newState = new SlotState(slotTable, curState.migrating);
            final Map<Short, Slot> added = SlotTable.getSlotsAdded(curState.table, slotTable);
            // mark the new leader is migrating
            newState.migrating.addAll(added.keySet());

            final Map<Short, Slot> deled = SlotTable.getSlotsDeled(curState.table, slotTable);
            newState.migrating.removeAll(deled.keySet());

            final Map<Short, Slot> updated = SlotTable.getSlotUpdated(curState.table, slotTable);
            updated.forEach((slotId, slot) -> {
                final Slot curSlot = curState.table.getSlot(slotId);
                if (!DataServerConfig.IP.equals(slot.getLeader())) {
                    // not leader, clean migrating if has set
                    newState.migrating.remove(slotId);
                    // as same as added.follower
                    added.put(slotId, slot);
                } else {
                    // is leader, check prev state
                    // new leader
                    if (curSlot.getLeaderEpoch() != slot.getLeaderEpoch()) {
                        newState.migrating.add(slotId);
                        // as same as added.leader
                        added.put(slotId, slot);
                    } else {
                        // the follower changes, ignore that, let the follower to pull data
                    }
                }
            });
            // update the current slotstate
            this.slotState = newState;
            handleSlotsAdded(slotState, added);
            handleSlotsAdded(slotState, added);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void handleSlotsAdded(SlotState state, Map<Short, Slot> slots) {
        slots.forEach((shortId, slot) -> {
            if (DataServerConfig.IP.equals(slot.getLeader())) {
                handleNewLeader(state, slot);
            } else {
                handleNewFollower(state, slot);
            }
        });
    }

    private void handleNewLeader(SlotState state, Slot slot) {
        // mark leader migrating
        state.migrating.add(slot.getId());
    }

    private void handleNewFollower(SlotState state, Slot slot) {

    }

    private void handleSlotsDeleted(SlotState state, Map<Short, Slot> slots) {
    }

    private static final class SlotState {
        static final SlotState  INIT      = new SlotState(new SlotTable(-1, Collections.emptyMap()),
                Collections.emptySet());
        final        SlotTable  table;
        final        Set<Short> migrating = new HashSet<>();

        SlotState(SlotTable table, Set<Short> migratingSlots) {
            this.table = table;
            this.migrating.addAll(migratingSlots);
        }
    }

    private void triggerUpdateSlotTable(long expectEpoch) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
