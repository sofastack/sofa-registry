package com.alipay.sofa.registry.server.meta.slot.tasks.reassign;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.comparator.ComparatorVisitor;
import com.alipay.sofa.registry.comparator.Pair;
import com.alipay.sofa.registry.lifecycle.Initializable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.util.DataNodeComparator;
import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;
import com.alipay.sofa.registry.util.MathUtils;
import io.netty.util.internal.MathUtil;
import javafx.util.Builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class SlotAssigner implements ComparatorVisitor<String>, Builder<SlotTable>, Runnable, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SlotAssigner.class);

    private final SlotTable prevSlotTable;

    private final Set<String> currentDataServers;

    private final DataNodeComparator comparator;

    private SlotAssignerState state = SlotAssignerState.Init;

    private final SlotTableBuilder builder = new SlotTableBuilder();

    private final MigrateSlotGroup migrateSlotGroup = new MigrateSlotGroup();

    private final int leaderLimit;

    private final int followerLimit;

    public SlotAssigner(SlotTable prevSlotTable, Collection<String> dataServers, DataNodeComparator comparator) {
        this.prevSlotTable = prevSlotTable;
        this.currentDataServers = new HashSet<>(dataServers);
        this.comparator = comparator;
        this.leaderLimit = MathUtils.divideCeil(SlotConfig.SLOT_NUM, currentDataServers.size());
        this.followerLimit = MathUtils.divideCeil((SlotConfig.SLOT_REPLICAS - 1) * SlotConfig.SLOT_NUM,
                currentDataServers.size());
    }

    @Override
    public void run() {
        while(state != SlotAssignerState.End) {
            if(logger.isInfoEnabled()) {
                logger.info("[run] state: {} begin", state.name());
            }

            state.doAction(this);

            if(logger.isInfoEnabled()) {
                logger.info("[run] state: {} end, change to {}", state.name(), state.nextStep().name());
            }

            state = state.nextStep();
        }
    }

    @Override
    public void initialize() {
        for(Map.Entry<Integer, Slot> entry : prevSlotTable.getSlotMap().entrySet()) {
            int slotId = entry.getKey();
            Slot slot = entry.getValue();
            SlotBuilder slotBuilder = builder.getOrCreate(slotId);
            slotBuilder.addLeader(slot.getLeader());
            slot.getFollowers().forEach(slotBuilder::addFollower);
        }
    }

    @Override
    public void visitAdded(String added) {
        assign(added);
    }

    @Override
    public void visitModified(Pair<String, String> modified) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitRemoved(String removed) {
        DataNodeSlot targetRemovedSlots = prevSlotTable.transfer(removed, false).get(0);
        targetRemovedSlots.getLeaders().forEach(slotId->{
            migrateSlotGroup.addLeader(slotId);
            builder.getOrCreate(slotId).removeLeader(removed);
        });
        targetRemovedSlots.getFollowers().forEach(slotId->{
            migrateSlotGroup.addFollower(slotId);
            builder.getOrCreate(slotId).removeFollower(removed);
        });
    }

    @Override
    public void visitRemaining(String remain) {
        DataNodeSlot dataNodeSlot = prevSlotTable.transfer(remain, false).get(0);
        int leaderSize = dataNodeSlot.getLeaders().size();
        int followerSize = dataNodeSlot.getFollowers().size();
        for(int i = leaderSize; i > leaderLimit; i--) {
            int slotId = dataNodeSlot.getLeaders().get(i-1);
            migrateSlotGroup.addLeader(slotId);
            builder.getOrCreate(slotId).removeLeader(remain);
        }
        for(int i = followerSize; i > followerLimit; i--) {
            int slotId = dataNodeSlot.getFollowers().get(i-1);
            migrateSlotGroup.addFollower(slotId);
            builder.getOrCreate(slotId).removeFollower(remain);
        }
    }

    @Override
    public SlotTable build() {
        return builder.build();
    }

    public void assign(String dataNode) {
        DataNodeSlot dataNodeSlot = prevSlotTable.transfer(dataNode, false).get(0);
        int leaderSize = dataNodeSlot.getLeaders().size();
        int followerSize = dataNodeSlot.getFollowers().size();
        for(int i = leaderSize; i < leaderLimit && !migrateSlotGroup.getLeaders().isEmpty(); i++) {
            int leaderSlotId = migrateSlotGroup.pickupLeader();
            builder.getOrCreate(leaderSlotId).addLeader(dataNode);
        }
        for(int i = followerSize; i < followerLimit && !migrateSlotGroup.getFollowers().isEmpty(); i++) {
            int followerSlotId = migrateSlotGroup.pickupFollower(dataNodeSlot.getFollowers());
            builder.getOrCreate(followerSlotId).addFollower(dataNode);
        }
    }

    public DataNodeComparator getComparator() {
        return comparator;
    }

    public Set<String> getCurrentDataServers() {
        return currentDataServers;
    }
}
