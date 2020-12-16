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
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 *
 * This task forcus on re-balancing slot leaders
 * slots are migrated from all data-servers to !!!one!!! target data-server once upon time
 * The idea is as simple as below:
 * 1. find out a target data-server which holds leader slots below the (average slots)/2
 * 2. go through all data servers, find the shared slots target data-server as follower, meanwhile, others are leaders
 * 3. migrate leadership from heavy traffic data servers to target servers
 * we define heavy traffic data server as they holds more than average leader slots
 */
public class SlotLeaderRebalanceTask extends AbstractRebalanceTask implements RebalanceTask {

    public SlotLeaderRebalanceTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                                   DefaultDataServerManager dataServerManager) {
        super(localSlotManager, raftSlotManager, dataServerManager);
    }

    @Override
    protected long getTotalSlots() {
        return localSlotManager.getSlotNums();
    }

    @Override
    protected int getManagedSlots(DataNodeSlot dataNodeSlot) {
        return dataNodeSlot.getLeaders().size();
    }

    @Override
    protected int getMaxSlotsToMov(int averageSlot, DataNodeSlot target) {
        return averageSlot - target.getLeaders().size();
    }

    @Override
    protected int getCurrentSlotSize(DataNodeSlot dataNodeSlot) {
        return dataNodeSlot.getLeaders().size();
    }

    @Override
    protected Set<Integer> getMigrateSlots(DataNodeSlot fromSlotTable, DataNodeSlot toSlotTable) {
        // first of all, find all slots, that from is leader role as while as to is a follower role
        // to do so, the data server down time will be smaller as target data server already holds data of the slot
        Set<Integer> jointSet = Sets.newHashSet();
        jointSet.addAll(fromSlotTable.getLeaders());
        jointSet.retainAll(toSlotTable.getFollowers());
        return jointSet;
    }

    @Override
    protected Slot createMigratedSlot(Slot prevSlot, long epoch, DataNode from, DataNode to) {
        Set<String> newFollowers = Sets.newHashSet();
        newFollowers.addAll(prevSlot.getFollowers());
        newFollowers.remove(to.getIp());
        newFollowers.add(from.getIp());
        return new Slot(prevSlot.getId(), to.getIp(), epoch, newFollowers);
    }

}
