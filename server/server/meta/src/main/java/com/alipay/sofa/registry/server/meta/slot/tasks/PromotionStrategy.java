package com.alipay.sofa.registry.server.meta.slot.tasks;

import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public interface PromotionStrategy {

    String promotes(Set<String> candidates);

}
