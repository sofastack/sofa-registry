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

    private RaftExchanger raftExchanger;

    @Before
    public void beforeDefaultSessionManagerTest() throws Exception {
        sessionManager = new DefaultSessionManager();
        sessionManager.setScheduled(scheduled);
        raftExchanger = mock(RaftExchanger.class);
        sessionManager.setRaftExchanger(raftExchanger);
        LifecycleHelper.initializeIfPossible(sessionManager);
        LifecycleHelper.startIfPossible(sessionManager);

        sessionManager.setRaftLeaseManager(sessionManager.new DefaultRaftLeaseManager<SessionNode>());
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