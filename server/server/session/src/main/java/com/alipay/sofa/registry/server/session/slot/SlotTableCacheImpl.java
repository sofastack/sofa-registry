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
package com.alipay.sofa.registry.server.session.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 10:07 yuzhi.lyz Exp $
 */
public final class SlotTableCacheImpl implements SlotTableCache {
    private static final Logger LOGGER       = LoggerFactory.getLogger(SlotTableCacheImpl.class);

    private final SlotFunction  slotFunction = SlotFunctionRegistry.getFunc();
    private volatile SlotTable  slotTable    = SlotTable.INIT;

    @Override
    public int slotOf(String dataInfoId) {
        return slotFunction.slotOf(dataInfoId);
    }

    @Override
    public Slot getSlot(String dataInfoId) {
        int slotId = slotOf(dataInfoId);
        return slotTable.getSlot(slotId);
    }

    @Override
    public String getLeader(String dataInfoId) {
        final Slot slot = getSlot(dataInfoId);
        return slot == null ? null : slot.getLeader();
    }

    @Override
    public String getLeader(int slotId) {
        final Slot slot = slotTable.getSlot(slotId);
        return slot == null ? null : slot.getLeader();
    }

    @Override
    public void triggerUpdateSlotTable(long epoch) {
        // TODO
    }

    @Override
    public long getEpoch() {
        return slotTable.getEpoch();
    }

    @Override
    public synchronized boolean updateSlotTable(SlotTable slotTable) {
        final long curEpoch = this.slotTable.getEpoch();
        if (curEpoch >= slotTable.getEpoch()) {
            return false;
        }
        this.slotTable = slotTable;
        LOGGER.info("updating slot table, expect={}, current={}", slotTable.getEpoch(), curEpoch);
        return true;
    }
}
