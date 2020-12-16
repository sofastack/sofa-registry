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
package com.alipay.sofa.registry.server.meta.lease.session;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class DefaultSessionServerManagerTest extends AbstractTest {

    private DefaultSessionServerManager sessionManager;

    private RaftExchanger               raftExchanger;

    @Mock
    private MetaServerConfig            metaServerConfig;

    @Before
    public void beforeDefaultSessionManagerTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        sessionManager = new DefaultSessionServerManager();
        sessionManager.setMetaServerConfig(metaServerConfig)
            .setSessionLeaseManager(new SessionLeaseManager())
            .setRaftSessionLeaseManager(new SessionLeaseManager()).setScheduled(scheduled);
        when(metaServerConfig.getSchedulerHeartbeatTimeout()).thenReturn(60);
        when(metaServerConfig.getSchedulerHeartbeatExpBackOffBound()).thenReturn(60);
        raftExchanger = mock(RaftExchanger.class);
        sessionManager.postConstruct();
    }

    @After
    public void afterDefaultSessionManagerTest() throws Exception {
        sessionManager.preDestory();
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

    @Test
    public void testRenew() throws TimeoutException, InterruptedException {
        SessionLeaseManager leaseManager = spy(new SessionLeaseManager());
        sessionManager.setRaftSessionLeaseManager(leaseManager)
            .setSessionLeaseManager(leaseManager);
        SessionNode sessionNode = new SessionNode(randomURL(randomIp()), getDc());
        long timestamp = System.currentTimeMillis();
        sessionNode
            .setProcessId(new ProcessId(sessionNode.getIp(), timestamp, 1, random.nextInt()));
        NotifyObserversCounter counter = new NotifyObserversCounter();
        sessionManager.addObserver(counter);

        makeRaftLeader();

        Assert.assertFalse(sessionManager.renew(sessionNode, 1));
        verify(leaseManager, times(1)).register(any());
        Assert.assertEquals(1, counter.getCounter());

        Assert.assertTrue(sessionManager.renew(sessionNode, 1));
        verify(leaseManager, times(1)).register(any());
        Assert.assertEquals(1, counter.getCounter());

        SessionNode sessionNode2 = new SessionNode(sessionNode.getNodeUrl(), getDc());
        sessionNode2
            .setProcessId(new ProcessId(sessionNode.getIp(), timestamp, 2, random.nextInt()));
        Assert.assertTrue(sessionManager.renew(sessionNode2, 1));
        verify(leaseManager, times(2)).register(any());
        verify(leaseManager, times(1)).renew(any(), anyInt());
        Assert.assertEquals(2, counter.getCounter());
    }
}