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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultCurrentDcMetaServerTest extends AbstractTest {

    private DefaultCurrentDcMetaServer metaServer;

    @Mock
    private RaftExchanger              raftExchanger;

    @Mock
    private SessionManager             sessionManager;

    @Mock
    private DataServerManager          dataServerManager;

    @Mock
    private SlotManager                slotManager;

    @Before
    public void beforeDefaultCurrentDcMetaServerTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        metaServer = new DefaultCurrentDcMetaServer().setRaftExchanger(raftExchanger)
            .setDataServerManager(dataServerManager).setSessionManager(sessionManager)
            .setSlotManager(slotManager);
        LifecycleHelper.initializeIfPossible(metaServer);
        LifecycleHelper.startIfPossible(metaServer);
        metaServer.setRaftStorage(metaServer.new MetaServersRaftStorage());
    }

    @After
    public void afterDefaultCurrentDcMetaServerTest() throws Exception {
        LifecycleHelper.stopIfPossible(metaServer);
        LifecycleHelper.disposeIfPossible(metaServer);
    }

    @Test
    public void testGetSessionServers() {
        when(sessionManager.getClusterMembers()).thenReturn(
            Lists.newArrayList(new SessionNode(randomURL(), getDc()), new SessionNode(randomURL(),
                getDc())));
        Assert.assertEquals(2, metaServer.getSessionServers().size());
        verify(sessionManager, times(1)).getClusterMembers();
    }

    //manually test
    @Test
    //    @Ignore
    public void testUpdateClusterMembers() throws Exception {
        RaftExchanger raftExchanger = startRaftExchanger();
        metaServer = new DefaultCurrentDcMetaServer().setRaftExchanger(raftExchanger);

        LifecycleHelper.initializeIfPossible(metaServer);
        LifecycleHelper.startIfPossible(metaServer);

        List<MetaNode> prevClusterNodes = metaServer.getClusterMembers();
        long prevEpoch = metaServer.getEpoch();
        metaServer.updateClusterMembers(Lists.newArrayList(new MetaNode(randomURL(randomIp()),
            getDc()), new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(
            randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()), getDc()),
            new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()),
                getDc())));
        long currentEpoch = metaServer.getEpoch();
        // wait for raft communication
        Thread.sleep(100);
        List<MetaNode> currentClusterNodes = metaServer.getClusterMembers();

        LifecycleHelper.stopIfPossible(metaServer);
        LifecycleHelper.disposeIfPossible(metaServer);

        Assert.assertTrue(currentEpoch > prevEpoch);
        Assert.assertNotEquals(currentClusterNodes.size(), prevClusterNodes.size());
    }

    @Test
    public void testGetSlotTable() {
        when(slotManager.getSlotTable()).thenReturn(
            new SlotTable(DatumVersionUtil.nextId(), ImmutableMap.of(1, new Slot(1, randomIp(), 2,
                Lists.newArrayList(randomIp())))));
        SlotTable slotTable = metaServer.getSlotTable();
        verify(slotManager, times(1)).getSlotTable();
        Assert.assertEquals(1, slotTable.getSlotIds().size());
    }

    @Test
    public void testCancel() {
        Map<String, Node> map = Maps.newHashMap();
        MetaNode node1 = new MetaNode(randomURL(randomIp()), getDc());
        DataNode node2 = new DataNode(randomURL(randomIp()), getDc());
        SessionNode node3 = new SessionNode(randomURL(randomIp()), getDc());
        Answer<Node> answer = new Answer<Node>() {
            @Override
            public Node answer(InvocationOnMock invocationOnMock) throws Throwable {
                Node node = invocationOnMock.getArgumentAt(0, Node.class);
                if (invocationOnMock.getMethod().getName().contains("cancel")) {
                    map.remove(node.getNodeUrl().getIpAddress());
                } else {
                    map.put(node.getNodeUrl().getIpAddress(), node);
                }
                return null;
            }
        };
        when(dataServerManager.renew(any(), anyInt())).then(answer);
        when(sessionManager.renew(any(), anyInt())).then(answer);
        when(dataServerManager.cancel(any())).then(answer);
        when(sessionManager.cancel(any())).then(answer);
        metaServer.renew(node1, 1);
        metaServer.renew(node2, 1);
        metaServer.renew(node3, 1);
        Assert.assertEquals(2, map.size());

        metaServer.cancel(node1);
        metaServer.cancel(node2);
        metaServer.cancel(node3);
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testRenew() {
        Map<String, Node> map = Maps.newHashMap();
        MetaNode node1 = new MetaNode(randomURL(randomIp()), getDc());
        DataNode node2 = new DataNode(randomURL(randomIp()), getDc());
        SessionNode node3 = new SessionNode(randomURL(randomIp()), getDc());
        Answer<Node> answer = new Answer<Node>() {
            @Override
            public Node answer(InvocationOnMock invocationOnMock) throws Throwable {
                Node node = invocationOnMock.getArgumentAt(0, Node.class);
                if (invocationOnMock.getMethod().getName().contains("cancel")) {
                    map.remove(node.getNodeUrl().getIpAddress());
                } else {
                    map.put(node.getNodeUrl().getIpAddress(), node);
                }
                return null;
            }
        };
        when(dataServerManager.renew(any(), anyInt())).then(answer);
        when(sessionManager.renew(any(), anyInt())).then(answer);
        when(dataServerManager.cancel(any())).then(answer);
        when(sessionManager.cancel(any())).then(answer);
        metaServer.renew(node1, 1);
        metaServer.renew(node2, 1);
        metaServer.renew(node3, 1);
        Assert.assertEquals(2, map.size());
        verify(dataServerManager, times(1)).renew(any(), anyInt());
        verify(sessionManager, times(1)).renew(any(), anyInt());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEvict() {
        metaServer.evict();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLeaseManager() {
        metaServer.getLeaseManager(Node.NodeType.CLIENT);
    }

    @Test
    public void testGetLeaseManagerPositive() {
        Assert
            .assertTrue(metaServer.getLeaseManager(Node.NodeType.META) instanceof DefaultCurrentDcMetaServer.CurrentMetaServerRaftStorage);
        Assert
            .assertTrue(metaServer.getLeaseManager(Node.NodeType.DATA) instanceof DataServerManager);
        Assert
            .assertTrue(metaServer.getLeaseManager(Node.NodeType.SESSION) instanceof SessionManager);
    }

    @Test
    public void testGetEpoch() {
        Assert.assertEquals(0, metaServer.getEpoch());
        metaServer.renew(new MetaNode(randomURL(), getDc()), 1);
        Assert.assertTrue(metaServer.getEpoch() <= System.currentTimeMillis());
    }
}