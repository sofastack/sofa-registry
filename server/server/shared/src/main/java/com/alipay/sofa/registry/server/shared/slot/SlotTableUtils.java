package com.alipay.sofa.registry.server.shared.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Jan 21, 2021
 */
public class SlotTableUtils {

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
}
