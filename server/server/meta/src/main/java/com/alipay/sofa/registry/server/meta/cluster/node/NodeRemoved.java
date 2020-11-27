package com.alipay.sofa.registry.server.meta.cluster.node;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class NodeRemoved<T extends Node> extends AbstractNodeEvent<T> {

    public NodeRemoved(T node) {
        super(node);
    }
}
