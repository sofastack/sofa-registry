package com.alipay.sofa.registry.common.model.metaserver.inter.communicate;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 27, 2020
 */
public class BaseHeartBeatResponse {

    private final long metaServerEpoch;

    private final SlotTable slotTable;

    private final List<MetaNode> metaNodes;

    public BaseHeartBeatResponse(long metaServerEpoch, SlotTable slotTable, List<MetaNode> metaNodes) {
        this.metaServerEpoch = metaServerEpoch;
        this.slotTable = slotTable;
        this.metaNodes = metaNodes;
    }

    public SlotTable getSlotTable() {
        return slotTable;
    }

    public List<MetaNode> getMetaNodes() {
        return metaNodes;
    }
}
