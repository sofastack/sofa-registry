/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotStatus;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Feb 23, 2021
 */
public class DataHeartbeatRequest extends HeartbeatRequest<DataNode> {

    private final List<SlotStatus> slotStatuses;

    public DataHeartbeatRequest(DataNode node, long slotTableEpoch, String dataCenter,
                                long timestamp, SlotConfig.SlotBasicInfo slotBasicInfo,
                                List<SlotStatus> slotStatuses) {
        super(node, slotTableEpoch, dataCenter, timestamp, slotBasicInfo);
        this.slotStatuses = slotStatuses;
    }

    /**
     * Gets get slot status.
     *
     * @return the get slot status
     */
    public List<SlotStatus> getSlotStatus() {
        return slotStatuses;
    }
}
