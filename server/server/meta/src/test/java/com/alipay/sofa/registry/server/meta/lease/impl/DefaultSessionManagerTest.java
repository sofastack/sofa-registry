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

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class DefaultSessionManagerTest extends AbstractTest {

    private DefaultSessionManager sessionManager;

    private RaftExchanger         raftExchanger;

    @Before
    public void beforeDefaultSessionManagerTest() throws Exception {
        sessionManager = new DefaultSessionManager();
        sessionManager.setScheduled(scheduled);
        raftExchanger = mock(RaftExchanger.class);
        sessionManager.setRaftExchanger(raftExchanger);
        LifecycleHelper.initializeIfPossible(sessionManager);
        LifecycleHelper.startIfPossible(sessionManager);

        sessionManager
            .setRaftLeaseManager(sessionManager.new DefaultRaftLeaseManager<SessionNode>());
    }

    @After
    public void afterDefaultSessionManagerTest() throws Exception {
        LifecycleHelper.stopIfPossible(sessionManager);
        LifecycleHelper.disposeIfPossible(sessionManager);
    }

    @Test
    public void testGetServiceId() {
        Assert.assertEquals("DefaultSessionManager.LeaseManager", sessionManager.getServiceId());
    }

    @Test
    public void testGetEpoch() {
        Assert.assertEquals(0, sessionManager.getEpoch());
        sessionManager.renew(new SessionNode(randomURL(randomIp()), getDc()), 1000);
        Assert.assertNotEquals(0, sessionManager.getEpoch());
    }

    @Test
    public void testGetClusterMembers() {
        Assert.assertTrue(sessionManager.getClusterMembers().isEmpty());
    }
}