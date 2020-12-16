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

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class DefaultCurrentDcMetaServerTest extends AbstractTest {

    private DefaultCurrentDcMetaServer metaServer;

    @Mock
    private SessionServerManager       sessionServerManager;

    @Mock
    private DataServerManager          dataServerManager;

    @Mock
    private SlotManager                slotManager;

    @Mock
    private NodeConfig                 nodeConfig;

    private DefaultLocalMetaServer     localMetaServer;

    private CurrentDcMetaServer        raftMetaServer;

    @Before
    public void beforeDefaultCurrentDcMetaServerTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        localMetaServer = spy(new DefaultLocalMetaServer().setSlotManager(slotManager));
        raftMetaServer = spy(new DefaultLocalMetaServer().setSlotManager(slotManager));
        metaServer = new DefaultCurrentDcMetaServer().setDataServerManager(dataServerManager)
            .setSessionManager(sessionServerManager).setNodeConfig(nodeConfig)
            .setLocalMetaServer(localMetaServer).setRaftMetaServer(raftMetaServer);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        when(nodeConfig.getMetaNodeIP()).thenReturn(
            ImmutableMap.of(getDc(), Lists.newArrayList(randomIp(), randomIp(), randomIp())));
        metaServer.postConstruct();
    }

    @After
    public void afterDefaultCurrentDcMetaServerTest() throws Exception {
        metaServer.preDestory();
    }

    @Test
    public void testGetSessionServers() {
        when(sessionServerManager.getClusterMembers()).thenReturn(
            Lists.newArrayList(new SessionNode(randomURL(), getDc()), new SessionNode(randomURL(),
                getDc())));
        Assert.assertEquals(2, metaServer.getSessionServerManager().getClusterMembers().size());
        verify(sessionServerManager, times(1)).getClusterMembers();
    }

    @Test
    public void testUpdateClusterMembers() throws Exception {

        List<MetaNode> prevClusterNodes = metaServer.getClusterMembers();
        long prevEpoch = metaServer.getEpoch();
        metaServer.updateClusterMembers(Lists.newArrayList(new MetaNode(randomURL(randomIp()),
            getDc()), new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(
            randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()), getDc()),
            new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()),
                getDc())), DatumVersionUtil.nextId());
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
    public void testGetClusterMembers() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        metaServer.getClusterMembers();
        verify(raftMetaServer, never()).getClusterMembers();

        makeRaftNonLeader();
        metaServer.getClusterMembers();
        verify(raftMetaServer, times(1)).getClusterMembers();
    }

    @Test
    public void testGetSlotTable() throws TimeoutException, InterruptedException {
        when(slotManager.getSlotTable()).thenReturn(
            new SlotTable(DatumVersionUtil.nextId(), ImmutableMap.of(1, new Slot(1, randomIp(), 2,
                Lists.newArrayList(randomIp())))));

        makeRaftLeader();
        SlotTable slotTable = metaServer.getSlotTable();
        verify(slotManager, times(1)).getSlotTable();
        Assert.assertEquals(1, slotTable.getSlotIds().size());

        makeRaftNonLeader();
        slotTable = metaServer.getSlotTable();
        verify(slotManager, times(2)).getSlotTable();
        Assert.assertEquals(1, slotTable.getSlotIds().size());
    }

    @Test
    public void testCancel() throws InterruptedException, TimeoutException {
        MetaNode metaNode = new MetaNode(randomURL(), getDc());
        metaServer.renew(metaNode);

        makeRaftLeader();
        metaServer.cancel(metaNode);
        verify(localMetaServer, never()).cancel(any());
        verify(raftMetaServer, times(1)).cancel(any());
        metaServer.renew(metaNode);

        makeRaftNonLeader();
        metaServer.cancel(metaNode);
        verify(localMetaServer, never()).cancel(any());
        verify(raftMetaServer, times(2)).cancel(any());
    }

    @Test
    public void testGetEpoch() throws TimeoutException, InterruptedException {
        metaServer.renew(new MetaNode(randomURL(), getDc()));
        makeRaftLeader();
        Assert.assertTrue(metaServer.getEpoch() <= DatumVersionUtil.nextId());
        verify(localMetaServer, times(1)).getEpoch();
        verify(raftMetaServer, never()).getEpoch();

        makeRaftNonLeader();
        metaServer.getEpoch();
        verify(localMetaServer, times(1)).getEpoch();
        verify(raftMetaServer, times(1)).getEpoch();
    }

    @Test
    public void testRenew() {
        NotifyObserversCounter notifyCounter = new NotifyObserversCounter();
        metaServer.addObserver(notifyCounter);
        MetaNode metaNode = new MetaNode(randomURL(), getDc());

        metaServer.renew(metaNode);
        metaServer.renew(metaNode);

        verify(raftMetaServer, times(2)).renew(any());
        verify(localMetaServer, never()).renew(any());
        Assert.assertEquals(2, notifyCounter.getCounter());
    }

    @Test
    public void testGetDataManager() {
        DataServerManager manager = metaServer.getDataServerManager();
        Assert.assertEquals(dataServerManager, manager);
    }

}