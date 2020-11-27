package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.server.meta.cluster.NodeCluster;

/**
 * @author chen.zhu
 * <p>
 * Nov 19, 2020
 */
public interface DataServerManager extends Lifecycle, Observable, NodeCluster<DataNode>, LeaseManager<DataNode> {

}
