package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class AbstractRebalanceTask implements RebalanceTask {

    private SlotManager slotManager;


    @Override
    public void run() {
        doSharding();
    }

    protected void doSharding() {
        List<Integer> slots = getSlotsToRebalance();
    }

    protected List<Integer> getSlotsToRebalance() {
        return Lists.newLinkedList();
    }
}
