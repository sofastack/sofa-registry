/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */

public class DefaultSessionManager extends AbstractRaftEnabledLeaseManager<SessionNode> implements
                                                                                       SessionManager {

    private static final String DEFAULT_SESSION_MANAGER_SERVICE_ID = "DefaultSessionManager.LeaseManager";

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    @PreDestroy
    public void preDestory() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

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
        Map<String, Lease<SessionNode>> m = raftLeaseManager.getLeaseStore();
        m.forEach((ip, lease)->{result.add(lease.getRenewal());});
        return result;
    }
}
