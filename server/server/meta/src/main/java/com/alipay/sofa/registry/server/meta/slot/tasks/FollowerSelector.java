package com.alipay.sofa.registry.server.meta.slot.tasks;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public interface FollowerSelector {
    String select(int targetSlot);
}
