package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
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
public class DefaultDataServerManager extends AbstractRaftEnabledLeaseManager<DataNode> implements DataServerManager {

    private static final String DEFAULT_DATA_MANAGER_SERVICE_ID = "DefaultDataServerManager.LeaseManager";

    @Override
    public long getEpoch() {
        return this.raftLeaseManager.getEpoch();
    }

    @Override
    public List<DataNode> getClusterMembers() {
        List<DataNode> result = Lists.newLinkedList();
        raftLeaseManager.getLeaseStore().forEach((ip,lease)->{result.add(lease.getRenewal());});
        return result;
    }

    @Override
    protected String getServiceId() {
        return DEFAULT_DATA_MANAGER_SERVICE_ID;
    }
}
