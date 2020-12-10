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

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public abstract class AbstractRebalanceTask implements RebalanceTask {

    protected Logger                         logger = LoggerFactory.getLogger(getClass());

    protected final LocalSlotManager         localSlotManager;

    protected final SlotManager              raftSlotManager;

    protected final DefaultDataServerManager dataServerManager;

    protected long                           nextEpoch;

    public AbstractRebalanceTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                                 DefaultDataServerManager dataServerManager) {
        this.localSlotManager = localSlotManager;
        this.raftSlotManager = raftSlotManager;
        this.dataServerManager = dataServerManager;
    }

    @Override
    public void run() {
        if (!ServiceStateMachine.getInstance().isLeader()) {
            if (logger.isInfoEnabled()) {
                logger.info("[run] not leader, quit task");
            }
            return;
        }
        long totalSlots = getTotalSlots();
        List<DataNode> aliveDataServers = dataServerManager.getLocalClusterMembers();
        int averageSlots = (int) (totalSlots / aliveDataServers.size());

        Set<DataNode> candidates = findCandidates(averageSlots, aliveDataServers);
        if (candidates.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("[run]no candidates available, quit");
            }
            return;
        }
        Set<DataNode> potentialSource = Sets.newHashSet();
        potentialSource.addAll(aliveDataServers);
        potentialSource.removeAll(candidates);

        Map<Integer, Slot> slotMap = localSlotManager.getSlotTable().getSlotMap();
        for (DataNode target : candidates) {
            migrate(potentialSource, target, averageSlots, slotMap);
        }

        if (ServiceStateMachine.getInstance().isLeader()) {
            if (logger.isInfoEnabled()) {
                logger.info("[run] update slot table through raft cluster");
            }
            raftSlotManager.refresh(new SlotTable(nextEpoch, slotMap));
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[run] not leader, won't update slot table through raft cluster");
            }
        }
    }

    protected abstract long getTotalSlots();

    private Set<DataNode> findCandidates(long averageSlots, List<DataNode> dataNodes) {
        Set<DataNode> candidates = Sets.newHashSet();
        for (DataNode dataNode : dataNodes) {
            DataNodeSlot dataNodeSlot = localSlotManager.getDataNodeManagedSlot(dataNode, false);
            int wholeSlots = getManagedSlots(dataNodeSlot);
            if (wholeSlots <= averageSlots / 2) {
                candidates.add(dataNode);
            }
        }
        return candidates;
    }

    protected abstract int getManagedSlots(DataNodeSlot dataNodeSlot);

    private void migrate(Set<DataNode> srcDataServers, DataNode target, int averageSlot,
                         Map<Integer, Slot> slotMap) {
        Set<DataNode> removeFromSources = Sets.newHashSet();
        DataNodeSlot targetSlot = localSlotManager.getDataNodeManagedSlot(target, false);
        int maxMove = getMaxSlotsToMov(averageSlot, targetSlot);
        int totalMove = 0;
        for (DataNode dataServer : srcDataServers) {
            DataNodeSlot dataNodeSlot = localSlotManager.getDataNodeManagedSlot(dataServer, false);
            int currentSlotsSize = getCurrentSlotSize(dataNodeSlot);
            int toMove = Math.min(currentSlotsSize - averageSlot, maxMove - totalMove);
            if (toMove > 0) {
                int movedSlots = migrateSlots(dataServer, target, toMove, slotMap);
                totalMove += movedSlots;
                if (logger.isInfoEnabled()) {
                    logger.info("[migrate][move slots from server to dst]{}->{} ({} slots)",
                        dataServer, target, movedSlots);
                }
            }
            if (totalMove >= maxMove) {
                if (logger.isInfoEnabled()) {
                    logger.info("[migrate][totalMove > maxMove]{} > {}", totalMove, maxMove);
                }
                break;
            }
            removeFromSources.add(dataServer);
        }
        srcDataServers.removeAll(removeFromSources);
    }

    protected abstract int getMaxSlotsToMov(int averageSlot, DataNodeSlot target);

    protected abstract int getCurrentSlotSize(DataNodeSlot dataNodeSlot);

    private int migrateSlots(DataNode from, DataNode to, int toMove, Map<Integer, Slot> slotMap) {
        DataNodeSlot fromSlotTable = localSlotManager.getDataNodeManagedSlot(from, false);
        DataNodeSlot toSlotTable = localSlotManager.getDataNodeManagedSlot(to, false);
        // first of all, find all slots, that from is leader role as while as to is a follower role
        // to do so, the data server down time will be smaller as target data server already holds data of the slot
        Set<Integer> slotsToMigrate = getMigrateSlots(fromSlotTable, toSlotTable);

        int movedSlots = 0;
        for (Integer slotNum : slotsToMigrate) {
            // by changing slot, we promote slot epoch to next generation and epoch is also useful for slot-table
            // slot-table's epoch is designed as the maximum epoch beyond all slots
            nextEpoch = DatumVersionUtil.nextId();
            Slot newSlot = createMigratedSlot(slotMap.get(slotNum), nextEpoch, from, to);

            // replace slot and increase moved slots
            slotMap.put(slotNum, newSlot);
            movedSlots++;
            if (toMove == movedSlots) {
                break;
            }
        }
        return movedSlots;
    }

    protected abstract Set<Integer> getMigrateSlots(DataNodeSlot fromSlotTable,
                                                    DataNodeSlot toSlotTable);

    protected abstract Slot createMigratedSlot(Slot prevSlot, long epoch, DataNode from, DataNode to);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
