package com.alipay.sofa.registry.server.meta.metaserver;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.datacenter.DataCenterAware;
import com.alipay.sofa.registry.server.meta.cluster.ActionOnLeaderOnly;
import com.alipay.sofa.registry.server.meta.MetaServer;
import com.alipay.sofa.registry.server.meta.cluster.RemoteServers;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface CrossDcMetaServer extends DataCenterAware, RemoteServers<MetaNode>, MetaServer, ActionOnLeaderOnly {

    default List<MetaNode> getRemotes() {
        return getClusterMembers();
    }
}
