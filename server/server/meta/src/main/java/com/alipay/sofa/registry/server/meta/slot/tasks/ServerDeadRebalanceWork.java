package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class ServerDeadRebalanceWork implements RebalanceTask {

    private SlotManager slotManager;

    private DataServerManager dataServerManager;

    private DataNode deadServer;

    private PromotionStrategy promotionStrategy;

    private FollowerSelector selector;

    private SlotTable nextSlotTable;

    public ServerDeadRebalanceWork(SlotManager slotManager, DataServerManager dataServerManager, DataNode deadServer) {
        this.slotManager = slotManager;
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
        DataNodeSlot dataNodeSlot = slotManager.getDataNodeManagedSlot(deadServer, false);
        SlotTable slotTable = slotManager.getSlotTable();
        // first step, select one from previous followers and promote to be slot leader
        // second step, add followers for the slot as previous has been removed
        nextSlotTable = promoteFollowers(slotTable, dataNodeSlot.getLeaders());

        List<Integer> slots = dataNodeSlot.getFollowers();
        slots.addAll(dataNodeSlot.getLeaders());
        nextSlotTable = reassignFollowers(nextSlotTable, slots);

        if(slotManager instanceof DefaultSlotManager) {
            ((DefaultSlotManager) slotManager).setSlotTable(nextSlotTable);
        } else {
            throw new IllegalStateException("slot manager need to be able to set slot-table");
        }
    }

    private SlotTable promoteFollowers(SlotTable slotTable, List<Integer> slotNums) {
        long newSlotTableEpoch = slotTable.getEpoch();
        Map<Integer, Slot> slotMap = slotTable.getSlotMap();
        for(Integer slotNum : slotNums) {
            Slot slot = slotMap.get(slotNum);
            // select one candidate from followers as new leader
            String newLeader = promotionStrategy.promotes(slot.getFollowers());
            Set<String> newFollowers = slot.getFollowers();
            newFollowers.remove(newLeader);
            // replace the slot info
            Slot newSlot = new Slot(slot.getId(), newLeader, DatumVersionUtil.nextId(), newFollowers);
            slotMap.put(slotNum, newSlot);
            newSlotTableEpoch = newSlot.getLeaderEpoch();
        }
        return new SlotTable(newSlotTableEpoch, slotMap);
    }

    private SlotTable reassignFollowers(SlotTable slotTable, List<Integer> slotNums) {
        long newSlotTableEpoch = slotTable.getEpoch();
        Map<Integer, Slot> slotMap = slotTable.getSlotMap();
        for(Integer slotNum : slotNums) {
            Slot slot = slotMap.get(slotNum);
            // select new follower from alive data servers
            String newFollower = selector.select(slotNum);
            slot.getFollowers().add(newFollower);
            // generate new slot, as epoch has been changed
            Slot newSlot = new Slot(slot.getId(), slot.getLeader(), DatumVersionUtil.nextId(), slot.getFollowers());
            slotMap.put(slotNum, newSlot);

            newSlotTableEpoch = newSlot.getLeaderEpoch();
        }
        return new SlotTable(newSlotTableEpoch, slotMap);
    }
}
