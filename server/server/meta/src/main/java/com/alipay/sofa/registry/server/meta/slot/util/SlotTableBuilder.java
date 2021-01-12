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
package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.google.common.collect.Maps;
import javafx.util.Builder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class SlotTableBuilder implements Builder<SlotTable> {

    private Map<Integer, SlotBuilder> buildingSlots = Maps.newHashMap();

    private Map<Integer, Slot>        stableSlots   = Maps.newHashMap();

    public SlotBuilder getOrCreate(int slotId) {
        buildingSlots.putIfAbsent(slotId, new SlotBuilder(slotId, SlotConfig.SLOT_REPLICAS - 1));
        return buildingSlots.get(slotId);
    }

    public SlotTableBuilder addStableSlot(Slot slot) {
        stableSlots.put(slot.getId(), slot);
        return this;
    }

    @Override
    public SlotTable build() {
        AtomicLong epoch = new AtomicLong(0L);
        buildingSlots.forEach((slotId, slotBuilder)->{
            stableSlots.put(slotId, slotBuilder.build());
            epoch.set(Math.max(epoch.get(), stableSlots.get(slotId).getLeaderEpoch()));
        });
        return new SlotTable(epoch.get(), Maps.newHashMap(stableSlots));
    }
}
