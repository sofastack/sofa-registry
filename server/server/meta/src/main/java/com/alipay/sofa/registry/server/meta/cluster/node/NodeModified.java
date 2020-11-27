package com.alipay.sofa.registry.server.meta.cluster.node;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class NodeModified<T extends Node> implements NodeEvent<T> {

    private final T oldNode, newNode;

    public NodeModified(T oldNode, T newNode) {
        this.oldNode = oldNode;
        this.newNode = newNode;
    }

    public T getOldNode() {
        return oldNode;
    }

    public T getNewNode() {
        return newNode;
    }

}
