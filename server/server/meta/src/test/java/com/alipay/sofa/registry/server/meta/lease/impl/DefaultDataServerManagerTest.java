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
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class DefaultDataServerManagerTest extends AbstractTest {

    private DefaultDataServerManager dataServerManager;

    private RaftExchanger            raftExchanger;

    @Before
    public void beforeDefaultdataServerManagerTest() throws Exception {
        dataServerManager = new DefaultDataServerManager();
        dataServerManager.setScheduled(scheduled);
        raftExchanger = mock(RaftExchanger.class);
        dataServerManager.setRaftExchanger(raftExchanger);
        LifecycleHelper.initializeIfPossible(dataServerManager);
        LifecycleHelper.startIfPossible(dataServerManager);

        dataServerManager
            .setRaftLeaseManager(dataServerManager.new DefaultRaftLeaseManager<DataNode>());
    }

    @After
    public void afterDefaultdataServerManagerTest() throws Exception {
        LifecycleHelper.stopIfPossible(dataServerManager);
        LifecycleHelper.disposeIfPossible(dataServerManager);
    }

    @Test
    public void testGetServiceId() {
        Assert.assertEquals("DefaultDataServerManager.LeaseManager",
            dataServerManager.getServiceId());
    }

    @Test
    public void testGetEpoch() {
        Assert.assertEquals(0, dataServerManager.getEpoch());
        dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
        Assert.assertNotEquals(0, dataServerManager.getEpoch());
    }

    @Test
    public void testGetClusterMembers() {
        Assert.assertTrue(dataServerManager.getClusterMembers().isEmpty());
        dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
        Assert.assertEquals(1, dataServerManager.getClusterMembers().size());
    }
}