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
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.*;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class ServerDeadRebalanceWork implements RebalanceTask {

    private static final Logger      logger = LoggerFactory
                                                .getLogger(ServerDeadRebalanceWork.class);

    private SlotManager              raftSlotManager;

    private LocalSlotManager         localSlotManager;

    private DefaultDataServerManager dataServerManager;

    private DataNode                 deadServer;

    private long                     nextEpoch;

    public ServerDeadRebalanceWork(SlotManager raftSlotManager, LocalSlotManager localSlotManager,
                                   DefaultDataServerManager dataServerManager, DataNode deadServer) {
        this.raftSlotManager = raftSlotManager;
        this.localSlotManager = localSlotManager;
        this.dataServerManager = dataServerManager;
        this.deadServer = deadServer;
    }

    /**
     * For dead data servers, we need to do(with priorities):
     * 1. promote followers to leaders for which slots dead data server is running a leader role
     * 2. re-assign followers to slots this dead server maintains (including leader slots and follower slots)
     */
    @Override
    public void run() {
        if (logger.isInfoEnabled()) {
            logger.info("[run] begin to run, dead server - {}", deadServer);
        }
        DataNodeSlot dataNodeSlot = localSlotManager.getDataNodeManagedSlot(deadServer, false);
        Map<Integer, Slot> slotMap = localSlotManager.getSlotTable().getSlotMap();
        // first step, select one from previous followers and promote to be slot leader
        // second step, add followers for the slot as previous has been removed
        promoteFollowers(slotMap, dataNodeSlot.getLeaders());

        List<Integer> slots = dataNodeSlot.getFollowers();
        slots.addAll(dataNodeSlot.getLeaders());
        reassignFollowers(slotMap, slots);

        if (logger.isInfoEnabled()) {
            logger.info("[run] end of slot table calculation, dead server - {}", deadServer);
        }
        raftSlotManager.refresh(new SlotTable(nextEpoch, slotMap));
        if (logger.isInfoEnabled()) {
            logger.info("[run] end of running, dead server - {}", deadServer);
        }
    }

    private void promoteFollowers(Map<Integer, Slot> slotMap, List<Integer> slotNums) {
        Set<String> selectedFollowers = Sets.newHashSet();
        for (Integer slotNum : slotNums) {
            Slot slot = slotMap.get(slotNum);
            // select one candidate from followers as new leader
            String newLeader = promotesFollower(slot.getFollowers(), selectedFollowers);
            if(newLeader == null) {
                Set<String> candidates = Sets.newHashSet();
                dataServerManager.getClusterMembers().forEach(dataNode -> candidates.add(dataNode.getIp()));
                newLeader = randomSelect(candidates);
            }
            Set<String> newFollowers = Sets.newHashSet();
            newFollowers.addAll(slot.getFollowers());
            newFollowers.remove(newLeader);
            // replace the slot info
            nextEpoch = DatumVersionUtil.nextId();
            Slot newSlot = new Slot(slot.getId(), newLeader, nextEpoch, newFollowers);
            slotMap.put(slotNum, newSlot);
        }
    }

    private String promotesFollower(Set<String> followers, Set<String> selectedFollowers) {
        if (followers == null || followers.isEmpty()) {
            return null;
        }
        String result = followers.iterator().next();
        int minLeaderSlots = localSlotManager
            .getDataNodeManagedSlot(new DataNode(new URL(result), deadServer.getDataCenter()), true)
            .getLeaders().size();
        for (String follower : followers) {
            if (follower.equals(result)) {
                continue;
            }
            DataNodeSlot slot = localSlotManager.getDataNodeManagedSlot(new DataNode(new URL(
                follower), deadServer.getDataCenter()), true);
            if (slot.getLeaders().size() < minLeaderSlots && !selectedFollowers.contains(follower)) {
                result = follower;
                minLeaderSlots = slot.getLeaders().size();
            }
        }
        selectedFollowers.add(result);
        return result;
    }

    private void reassignFollowers(Map<Integer, Slot> slotMap, List<Integer> slotNums) {
        for (Integer slotNum : slotNums) {
            Slot slot = slotMap.get(slotNum);
            // select new follower from alive data servers
            String newFollower = findNewFollower(slotMap, slot.getId());
            Set<String> newFollowers = Sets.newHashSet();
            newFollowers.addAll(slot.getFollowers());
            newFollowers.add(newFollower);
            // generate new slot, as epoch has been changed
            nextEpoch = DatumVersionUtil.nextId();
            Slot newSlot = new Slot(slot.getId(), slot.getLeader(), nextEpoch, slot.getFollowers());
            slotMap.put(slotNum, newSlot);
        }
    }

    private String findNewFollower(Map<Integer, Slot> slotMap, int targetSlotId) {
        List<DataNode> dataNodes = dataServerManager.getClusterMembers();
        Slot targetSlot = slotMap.get(targetSlotId);
        Set<String> candidates = Sets.newHashSet();
        dataNodes.forEach(dataNode -> {candidates.add(dataNode.getIp());});
        candidates.removeAll(targetSlot.getFollowers());
        candidates.remove(targetSlot.getLeader());

        return randomSelect(candidates);
    }

    private String randomSelect(Set<String> candidates) {
        int randomIndex = Math.abs(new Random().nextInt(candidates.size())) & candidates.size();
        Iterator<String> iterator = candidates.iterator();
        String result = null;
        while (iterator.hasNext() && randomIndex-- > 0) {
            result = iterator.next();
        }
        return result;
    }

    @Override
    public String toString() {
        return "ServerDeadRebalanceWork{" + "deadServer=" + deadServer + '}';
    }
}
