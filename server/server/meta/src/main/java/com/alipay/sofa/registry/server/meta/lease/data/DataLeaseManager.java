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
package com.alipay.sofa.registry.server.meta.lease.data;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.jraft.LeaderAware;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultLeaseManager;
import com.alipay.sofa.registry.jraft.annotation.RaftService;

/**
 * @author chen.zhu
 * <p>
 * Dec 14, 2020
 */
@RaftService(uniqueId = DataLeaseManager.DATA_LEASE_MANAGER, interfaceType = LeaseManager.class)
public class DataLeaseManager extends DefaultLeaseManager<DataNode> implements LeaderAware {

    private static final String DATA_LEASE_SNAPSHOT_FILE_PREFIX = "DataLeaseManager";

    public static final String  DATA_LEASE_MANAGER              = "DataLeaseManager";

    public DataLeaseManager() {
        super(DATA_LEASE_SNAPSHOT_FILE_PREFIX);
    }

    @Override
    public void isLeader() {
        if (logger.isInfoEnabled()) {
            logger.info("[isLeader] renew all data nodes");
        }
        renewAllNodes();
    }

    @Override
    public void notLeader() {

    }

    private void renewAllNodes() {
        this.repo.forEach((ipAddress, lease)->lease.renew());
    }

    @Override
    protected DataLeaseManager copyMySelf() {
        return new DataLeaseManager();
    }
}
