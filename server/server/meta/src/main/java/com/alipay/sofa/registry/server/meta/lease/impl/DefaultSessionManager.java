package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@Component
@SmartSpringLifecycle
public class DefaultSessionManager extends AbstractRaftEnabledLeaseManager<SessionNode> implements SessionManager {

    private static final String DEFAULT_SESSION_MANAGER_SERVICE_ID = "DefaultSessionManager.LeaseManager";

    @Override
    protected String getServiceId() {
        return DEFAULT_SESSION_MANAGER_SERVICE_ID;
    }

    @Override
    public long getEpoch() {
        return raftLeaseManager.getEpoch();
    }

    @Override
    public List<SessionNode> getClusterMembers() {
        List<SessionNode> result = Lists.newLinkedList();
        raftLeaseManager.getLeaseStore().forEach((ip,lease)->{result.add(lease.getRenewal());});
        return result;
    }

}
