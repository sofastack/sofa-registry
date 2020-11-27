package com.alipay.sofa.registry.server.meta.metaserver;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.server.meta.MetaServer;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public interface CurrentDcMetaServer extends MetaServer, LeaseManager<Node>, Observable {

    List<SessionNode> getSessionServers();

    void updateClusterMembers(List<MetaNode> newMembers);

}
