package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultMetaServerManagerTest extends AbstractTest {

    @Mock
    private CrossDcMetaServerManager crossDcMetaServerManager;

    @Mock
    private CurrentDcMetaServer currentDcMetaServer;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private DataServerManager dataServerManager;

    @Mock
    private NodeConfig nodeConfig;

    private DefaultMetaServerManager manager;

    @Before
    public void beforeDefaultMetaServerManagerTest() {
        MockitoAnnotations.initMocks(this);
        manager = new DefaultMetaServerManager();
        manager.setCrossDcMetaServerManager(crossDcMetaServerManager)
                .setCurrentDcMetaServer(currentDcMetaServer)
                .setSessionManager(sessionManager)
                .setDataServerManager(dataServerManager)
                .setNodeConfig(nodeConfig);
    }

    @Test
    public void testGetSummary() {
        manager.getSummary(Node.NodeType.DATA);
        verify(dataServerManager, times(1)).getClusterMembers();
        verify(sessionManager, never()).getClusterMembers();
    }

    @Test
    public void testGetSummary2() {
        manager.getSummary(Node.NodeType.SESSION);
//        verify(sessionManager, times(1)).getClusterMembers();
        verify(dataServerManager, never()).getClusterMembers();
    }

    @Test
    public void testGetSummary3() {
        manager.getSummary(Node.NodeType.META);
        verify(currentDcMetaServer, times(1)).getClusterMembers();
        verify(sessionManager, never()).getClusterMembers();
    }
}