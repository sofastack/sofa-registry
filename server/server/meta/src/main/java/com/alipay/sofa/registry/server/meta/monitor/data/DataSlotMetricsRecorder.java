package com.alipay.sofa.registry.server.meta.monitor.data;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.monitor.PrometheusMetrics;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Mar 03, 2021
 */
public class DataSlotMetricsRecorder implements SlotTableRecorder {
    @Override
    public void record(SlotTable slotTable) {
        List<DataNodeSlot> dataNodeSlots = slotTable.transfer(null, false);
        dataNodeSlots.forEach(dataNodeSlot -> {
            PrometheusMetrics.DataSlot.setLeaderNumbers(dataNodeSlot.getDataNode(), dataNodeSlot.getLeaders().size());
            PrometheusMetrics.DataSlot.setFollowerNumbers(dataNodeSlot.getDataNode(), dataNodeSlot.getFollowers().size());
        });
    }
}
