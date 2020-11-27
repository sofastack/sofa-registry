package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.server.meta.cluster.NodeCluster;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public interface SessionManager extends Observable, Lifecycle, LeaseManager<SessionNode>, NodeCluster<SessionNode> {
}
