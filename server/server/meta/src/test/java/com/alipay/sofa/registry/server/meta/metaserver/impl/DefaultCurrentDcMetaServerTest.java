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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultCurrentDcMetaServerTest extends AbstractMetaServerTestBase {

  private DefaultCurrentDcMetaServer metaServer;

  @Mock private SessionServerManager sessionServerManager;

  @Mock private DataServerManager dataServerManager;

  @Mock private SlotManager slotManager;

  @Mock private NodeConfig nodeConfig;

  @Before
  public void beforeDefaultCurrentDcMetaServerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    metaServer =
        new DefaultCurrentDcMetaServer()
            .setDataServerManager(dataServerManager)
            .setSessionManager(sessionServerManager)
            .setNodeConfig(nodeConfig)
            .setSlotManager(slotManager);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    when(nodeConfig.getMetaNodeIP())
        .thenReturn(
            ImmutableMap.of(getDc(), Lists.newArrayList(randomIp(), randomIp(), randomIp())));
    metaServer.postConstruct();
  }

  @After
  public void afterDefaultCurrentDcMetaServerTest() throws Exception {
    metaServer.preDestory();
  }

  @Test
  public void testGetSessionServers() {
    when(sessionServerManager.getSessionServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new SessionNode(randomURL(), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(), getDc(), ServerEnv.PROCESS_ID))));
    Assert.assertEquals(
        2,
        metaServer.getSessionServerManager().getSessionServerMetaInfo().getClusterMembers().size());
    verify(sessionServerManager, times(1)).getSessionServerMetaInfo();
  }

  @Test
  public void testUpdateClusterMembers() throws Exception {

    List<MetaNode> prevClusterNodes = metaServer.getClusterMembers();
    long prevEpoch = metaServer.getEpoch();
    metaServer.updateClusterMembers(
        new VersionedList<>(
            DatumVersionUtil.nextId(),
            Lists.newArrayList(
                new MetaNode(randomURL(randomIp()), getDc()),
                new MetaNode(randomURL(randomIp()), getDc()),
                new MetaNode(randomURL(randomIp()), getDc()),
                new MetaNode(randomURL(randomIp()), getDc()),
                new MetaNode(randomURL(randomIp()), getDc()),
                new MetaNode(randomURL(randomIp()), getDc()))));
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
    makeMetaLeader();
    metaServer.getClusterMembers();

    makeMetaNonLeader();
    metaServer.getClusterMembers();
  }

  @Test
  public void testGetSlotTable() throws TimeoutException, InterruptedException {
    when(slotManager.getSlotTable())
        .thenReturn(
            new SlotTable(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(new Slot(1, randomIp(), 2, Lists.newArrayList(randomIp())))));

    makeMetaLeader();
    SlotTable slotTable = metaServer.getSlotTable();
    verify(slotManager, times(1)).getSlotTable();
    Assert.assertEquals(1, slotTable.getSlotIds().size());

    makeMetaNonLeader();
    slotTable = metaServer.getSlotTable();
    verify(slotManager, times(2)).getSlotTable();
    Assert.assertEquals(1, slotTable.getSlotIds().size());
  }

  @Test
  public void testCancel() throws InterruptedException, TimeoutException {
    MetaNode metaNode = new MetaNode(randomURL(), getDc());
    metaServer.renew(metaNode);

    makeMetaLeader();
    metaServer.cancel(metaNode);
    metaServer.renew(metaNode);

    makeMetaNonLeader();
    metaServer.cancel(metaNode);
  }

  @Test
  public void testGetEpoch() throws TimeoutException, InterruptedException {
    metaServer.renew(new MetaNode(randomURL(), getDc()));
    makeMetaLeader();
    Assert.assertTrue(metaServer.getEpoch() <= DatumVersionUtil.nextId());

    makeMetaNonLeader();
    metaServer.getEpoch();
  }

  @Test
  public void testRenew() {
    NotifyObserversCounter notifyCounter = new NotifyObserversCounter();
    metaServer.addObserver(notifyCounter);
    MetaNode metaNode = new MetaNode(randomURL(), getDc());

    metaServer.renew(metaNode);
    metaServer.renew(metaNode);

    Assert.assertEquals(2, notifyCounter.getCounter());
  }

  @Test
  public void testGetDataManager() {
    DataServerManager manager = metaServer.getDataServerManager();
    Assert.assertEquals(dataServerManager, manager);
  }
}
