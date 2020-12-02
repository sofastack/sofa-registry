package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Dec 01, 2020
 *
 * This task focus on re-balancing(assigning) slots to low traffic data-servers
 *
 */
public class SlotReassignTask extends AbstractRebalanceTask implements RebalanceTask {

    public SlotReassignTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                            DataServerManager dataServerManager) {
        super(localSlotManager, raftSlotManager, dataServerManager);
    }

    @Override
    protected long getTotalSlots() {
        return localSlotManager.getSlotNums() * localSlotManager.getSlotReplicaNums();
    }

    @Override
    protected int getManagedSlots(DataNodeSlot dataNodeSlot) {
        return dataNodeSlot.getLeaders().size() + dataNodeSlot.getFollowers().size();
    }

    @Override
    protected int getMaxSlotsToMov(int averageSlot, DataNodeSlot target) {
        return averageSlot - target.getLeaders().size() - target.getFollowers().size();
    }

    @Override
    protected int getCurrentSlotSize(DataNodeSlot dataNodeSlot) {
        return dataNodeSlot.getLeaders().size() + dataNodeSlot.getFollowers().size();
    }

    @Override
    protected Set<Integer> getMigrateSlots(DataNodeSlot fromSlotTable, DataNodeSlot toSlotTable) {
        Set<Integer> disjointSet = Sets.newHashSet();
        disjointSet.addAll(fromSlotTable.getFollowers());
        disjointSet.removeAll(toSlotTable.getFollowers());
        disjointSet.removeAll(toSlotTable.getLeaders());
        return disjointSet;
    }

    @Override
    protected Slot createMigratedSlot(Slot prevSlot, long epoch, DataNode from, DataNode to) {
        Set<String> newFollowers = prevSlot.getFollowers();
        newFollowers.remove(to.getIp());
        newFollowers.add(from.getIp());
        return new Slot(prevSlot.getId(), to.getIp(), epoch, newFollowers);
    }

}
