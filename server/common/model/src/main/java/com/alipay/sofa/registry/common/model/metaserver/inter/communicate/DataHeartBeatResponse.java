package com.alipay.sofa.registry.common.model.metaserver.inter.communicate;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 27, 2020
 */
public class DataHeartBeatResponse extends BaseHeartBeatResponse {

    private final List<SessionNode> sessionNodes;

    public DataHeartBeatResponse(long metaServerEpoch, SlotTable slotTable, List<MetaNode> metaNodes,
                                 List<SessionNode> sessionNodes) {
        super(metaServerEpoch, slotTable, metaNodes);
        this.sessionNodes = sessionNodes;
    }

    public List<SessionNode> getSessionNodes() {
        return sessionNodes;
    }
}
