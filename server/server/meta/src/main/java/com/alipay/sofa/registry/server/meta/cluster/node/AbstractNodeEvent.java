package com.alipay.sofa.registry.server.meta.cluster.node;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public abstract class AbstractNodeEvent<T extends Node> implements NodeEvent<T> {

    private final T node;

    public AbstractNodeEvent(T node) {
        this.node = node;
    }

    public T getNode() {
        return node;
    }
}
