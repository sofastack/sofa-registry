package com.alipay.sofa.registry.server.shared.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Jan 21, 2021
 */
public class SlotTableUtils {

    private static final Logger logger = LoggerFactory.getLogger(SlotTableUtils.class);

    public static Map<String, Integer> getSlotTableLeaderCount(SlotTable slotTable) {
        Map<String, Integer> leaderCounter = Maps.newHashMap();
        for(Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
            Slot slot = entry.getValue();
            incrCount(leaderCounter, slot.getLeader());
        }
        return leaderCounter;
    }

    public static Map<String, Integer> getSlotTableSlotCount(SlotTable slotTable) {
        Map<String, Integer> slotCounter = Maps.newHashMap();
        for(Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
            Slot slot = entry.getValue();
            incrCount(slotCounter, slot.getLeader());
            for(String follower : slot.getFollowers()) {
                incrCount(slotCounter, follower);
            }
        }
        return slotCounter;
    }

    private static void incrCount(Map<String, Integer> counter, String dataServer) {
        Integer count = counter.get(dataServer);
        if(count == null) {
            count = 0;
        }
        counter.put(dataServer, count + 1);
    }

    public static boolean isValidSlotTable(SlotTable slotTable) {
        return checkNoDupLeaderAndFollowers(slotTable) && checkNoLeaderEmpty(slotTable);
    }

    private static boolean checkNoDupLeaderAndFollowers(SlotTable slotTable) {
        for(Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
            Slot slot = entry.getValue();
            if(slot.getFollowers().contains(slot.getLeader())) {
                logger.error("[checkNoDupLeaderAndFollowers] slot[{}] leader and follower duplicates", slot);
                return false;
            }
        }
        return true;
    }

    private static boolean checkNoLeaderEmpty(SlotTable slotTable) {
        for(Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
            Slot slot = entry.getValue();
            if(StringUtils.isEmpty(slot.getLeader())) {
                logger.error("[checkNoLeaderEmpty] slot[{}] empty leader", slot);
                return false;
            }
        }
        return true;
    }
}
