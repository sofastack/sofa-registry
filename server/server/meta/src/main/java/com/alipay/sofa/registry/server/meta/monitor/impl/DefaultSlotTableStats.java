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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableStats;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Jan 28, 2021
 */
public class DefaultSlotTableStats extends AbstractLifecycle implements SlotTableStats {

    private final SlotManager             slotManager;

    private final Map<Integer, SlotStats> slotStatses = Maps.newConcurrentMap();

    public DefaultSlotTableStats(SlotManager slotManager) {
        this.slotManager = slotManager;
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
            slotStatses.put(slotId,
                new DefaultSlotStats(slotManager.getSlotTable().getSlot(slotId)));
        }
    }

    @Override
    public boolean isSlotTableStable() {
        for (Map.Entry<Integer, SlotStats> entry : slotStatses.entrySet()) {
            SlotStats slotStats = entry.getValue();
            if (!slotStats.isLeaderStable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void checkSlotStatuses(List<SlotStatus> slotStatuses) {
        for (SlotStatus slotStatus : slotStatuses) {
            int slotId = slotStatus.getSlotId();
            SlotStats slotStats = slotStatses.get(slotId);
            if (slotStats.getSlot().getLeaderEpoch() > slotStatus.getSlotLeaderEpoch()) {
                logger
                    .warn(
                        "[checkSlotStatuses] won't update slot status, slot[{}] leader-epoch[{}] is less than current[{}]",
                        slotId, slotStatus.getSlotLeaderEpoch(), slotStats.getSlot()
                            .getLeaderEpoch());
                continue;
            }
            slotStats.updateSlotState(slotStatus.getLeaderStatus());
        }
    }

    @Override
    public void updateSlotTable(SlotTable slotTable) {
        slotTable.getSlotMap().forEach((slotId, slot) -> {
            SlotStats slotStats = slotStatses.get(slotId);
            if(slotStats.getSlot().getLeaderEpoch() < slot.getLeaderEpoch()) {
                slotStatses.put(slotId, new DefaultSlotStats(slot));
            }
        });
    }
}
