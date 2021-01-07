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
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.session.SessionLeaseManager;
import com.alipay.sofa.registry.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class DataLeaseManagerTest extends AbstractTest {

    private DataLeaseManager manager = new DataLeaseManager();

    @Test
    public void testIsLeader() throws InterruptedException {
        int leaseNum = 1000;
        for (int i = 0; i < leaseNum; i++) {
            manager.register(new Lease<DataNode>(new DataNode(randomURL(randomIp()), getDc()), 1));
        }
        Thread.sleep(1010);
        int size = manager.getClusterMembers().size();
        Assert.assertEquals(size, manager.getExpiredLeases().size());
        manager.isLeader();
        Thread.sleep(20);
        Assert.assertEquals(0, manager.getExpiredLeases().size());
    }

    @Test
    public void testNotLeader() {
        //nothing
        manager.notLeader();
    }

    @Test
    public void testCopyMySelf() {
        assertNotNull(manager.copyMySelf());
    }

    @Test
    public void testSnapshotProcess() throws IOException {
        new ConcurrentExecutor(1000, executors)
                .execute(()->manager.register(new Lease<DataNode>(new DataNode(randomURL(randomIp()), getDc()), 1)));
        manager.copy().save(DataLeaseManager.DATA_LEASE_MANAGER);
        DataLeaseManager loadManager = new DataLeaseManager();
        loadManager.load(DataLeaseManager.DATA_LEASE_MANAGER);
        Assert.assertEquals(manager.getClusterMembers().size(), loadManager.getClusterMembers().size());
        FileUtils.forceDelete(new File(DataLeaseManager.DATA_LEASE_MANAGER));
    }
}