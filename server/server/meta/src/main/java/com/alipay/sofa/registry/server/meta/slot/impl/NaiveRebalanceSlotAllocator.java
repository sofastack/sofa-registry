package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
public class NaiveRebalanceSlotAllocator implements SlotAllocator {

    private SlotManager slotManager;

    private SlotTable slotTable;

    public NaiveRebalanceSlotAllocator(SlotManager slotManager) {
        this.slotManager = slotManager;
    }

    @Override
    public SlotTable getSlotTable() {
        return slotTable;
    }

    public NaiveRebalanceSlotAllocator execute() {

        return this;
    }
}
