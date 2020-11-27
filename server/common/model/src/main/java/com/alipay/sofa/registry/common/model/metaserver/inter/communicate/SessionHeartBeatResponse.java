package com.alipay.sofa.registry.common.model.metaserver.inter.communicate;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 27, 2020
 */
public class SessionHeartBeatResponse extends BaseHeartBeatResponse {
    public SessionHeartBeatResponse(long metaServerEpoch, SlotTable slotTable, List<MetaNode> metaNodes) {
        super(metaServerEpoch, slotTable, metaNodes);
    }
}
