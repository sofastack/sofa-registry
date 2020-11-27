package com.alipay.sofa.registry.server.meta.cluster;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface ClusterArranger<T extends Node> {

    void onServerAdded(T clusterServer);

    void onServerRemoved(T clusterServer);

    void onServerChanged(T oldClusterServer, T newClusterServer);
}
