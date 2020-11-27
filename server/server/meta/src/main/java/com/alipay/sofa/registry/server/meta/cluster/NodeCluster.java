package com.alipay.sofa.registry.server.meta.cluster;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface NodeCluster<T extends Node> {

    @ReadOnLeader
    long getEpoch();

    @ReadOnLeader
    List<T> getClusterMembers();

}
