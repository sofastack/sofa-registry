package com.alipay.sofa.registry.server.meta.cluster;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.server.meta.cluster.RemoteServer;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface RemoteServers<T extends Node> {

    List<T> getRemotes();
}
