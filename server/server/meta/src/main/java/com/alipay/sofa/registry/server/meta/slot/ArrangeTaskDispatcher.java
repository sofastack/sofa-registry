package com.alipay.sofa.registry.server.meta.slot;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public interface ArrangeTaskDispatcher<T extends Node> {

    void serverAlive(T t);

    void serverDead(T t);

}
