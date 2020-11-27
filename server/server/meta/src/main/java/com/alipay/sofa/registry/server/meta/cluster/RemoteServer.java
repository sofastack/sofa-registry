package com.alipay.sofa.registry.server.meta.cluster;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface RemoteServer<T extends Node> {

    T getRemoteNode();
}
