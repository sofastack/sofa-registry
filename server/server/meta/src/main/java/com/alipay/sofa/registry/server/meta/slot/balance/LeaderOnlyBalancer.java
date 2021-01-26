package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 21, 2021
 */
public class LeaderOnlyBalancer implements SlotBalancer {

    private static final Logger logger = LoggerFactory.getLogger(LeaderOnlyBalancer.class);

    private final SlotTableBuilder slotTableBuilder;

    private final List<String> currentDataServers;

    private final SlotManager slotManager;

    private final BalancePolicy balancePolicy = new NaiveBalancePolicy();

    public LeaderOnlyBalancer(SlotTableBuilder slotTableBuilder, List<String> currentDataServers, SlotManager slotManager) {
        this.slotTableBuilder = slotTableBuilder;
        this.currentDataServers = currentDataServers;
        this.slotManager = slotManager;
    }

    @Override
    public SlotTable balance() {
        int avg = slotManager.getSlotNums() / currentDataServers.size();
        Set<String> targetDataServers = findDataServersNeedLeaderSlots(avg);
        if(targetDataServers.isEmpty()) {
            logger.warn("[balance]no data-servers needs leader balance");
            return slotManager.getSlotTable();
        } else {
            logger.info("[balance]try balance(add) leader slots to {}", targetDataServers);
            SlotTable prevSlotTable = slotManager.getSlotTable();
            logger.info("[balance][begin] slot table leader stat: {}", SlotTableUtils.getSlotTableLeaderCount(prevSlotTable));
            logger.info("[balance][begin] slot table slots stat: {}", SlotTableUtils.getSlotTableSlotCount(prevSlotTable));
        }
        for(String dataServer : targetDataServers) {
            DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataServer);
            int maxMove = balancePolicy.getMaxMoveLeaderSlots(avg, dataNodeSlot.getLeaders().size());
            List<DataNodeSlot> candidates = slotTableBuilder.getDataNodeSlotsLeaderBeyond(avg);
            if(candidates.isEmpty()) {
                logger.warn("[balance] no candidates available(no data-server's leader is above avg)");
                return slotManager.getSlotTable();
            }
            candidates.sort(new Comparator<DataNodeSlot>() {
                @Override
                public int compare(DataNodeSlot dataNodeSlot1, DataNodeSlot dataNodeSlot2) {
                    return SortType.DES.getScore(dataNodeSlot1.getLeaders().size() - dataNodeSlot2.getLeaders().size());
                }
            });
            DataNodeSlot candidate = candidates.get(0);
            maxMove = Math.min(maxMove, candidate.getLeaders().size() - avg);
            while(maxMove-- > 0) {
                Integer slotId = candidate.getLeaders().get(maxMove);
                slotTableBuilder.flipLeaderTo(slotId, dataServer);
                logger.info("[balance] slot[{}] leader change from [{}] to [{}]", slotId, candidate.getDataNode(), dataServer);
            }
        }
        SlotTable slotTable = slotTableBuilder.build();
        logger.info("[balance][end] slot table leader stat: {}", SlotTableUtils.getSlotTableLeaderCount(slotTable));
        logger.info("[balance][end] slot table slots stat: {}", SlotTableUtils.getSlotTableSlotCount(slotTable));
        return slotTable;
    }

    public Set<String> findDataServersNeedLeaderSlots(int avg) {
        int threshold = balancePolicy.getLowWaterMarkSlotLeaderNums(avg);
        List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeSlotsLeaderBelow(threshold);
        Set<String> dataServers = new HashSet<>(dataNodeSlots.size());
        dataNodeSlots.forEach(dataNodeSlot -> dataServers.add(dataNodeSlot.getDataNode()));
        return dataServers;
    }
}
