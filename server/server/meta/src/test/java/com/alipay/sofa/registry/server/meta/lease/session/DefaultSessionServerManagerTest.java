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

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultSessionServerManagerTest extends AbstractMetaServerTestBase {

  private DefaultSessionServerManager sessionManager;

  @Mock private MetaServerConfig metaServerConfig;

  private SlotManager slotManager = new SimpleSlotManager();

  @Before
  public void beforeDefaultSessionManagerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    makeMetaLeader();
    sessionManager =
        new DefaultSessionServerManager(metaServerConfig, slotManager, metaLeaderService);
    when(metaServerConfig.getExpireCheckIntervalMillis()).thenReturn(60);
  }

  @After
  public void afterDefaultSessionManagerTest() throws Exception {
    sessionManager.preDestory();
  }

  @Test
  public void testGetEpoch() throws TimeoutException, InterruptedException {
    Assert.assertEquals(0, sessionManager.getEpoch());
    sessionManager.renew(
        new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID), 1000);
    waitConditionUntilTimeOut(() -> sessionManager.getEpoch() > 0, 100);
    Assert.assertNotEquals(0, sessionManager.getEpoch());
  }

  @Test
  public void testGetClusterMembers() {
    Assert.assertTrue(sessionManager.getSessionServerMetaInfo().getClusterMembers().isEmpty());
  }

  @Test
  public void testRenew() throws Exception {
    sessionManager.postConstruct();
    String ip = randomIp();
    long timestamp = System.currentTimeMillis();
    SessionNode sessionNode =
        new SessionNode(randomURL(ip), getDc(), new ProcessId(ip, timestamp, 1, random.nextInt()));
    NotifyObserversCounter counter = new NotifyObserversCounter();
    sessionManager.addObserver(counter);

    makeMetaLeader();

    sessionManager.renew(sessionNode, 1);
    //        verify(leaseManager, times(1)).register(any());
    Assert.assertEquals(1, counter.getCounter());

    sessionManager.renew(sessionNode, 1);
    //        verify(leaseManager, times(1)).register(any());
    Assert.assertEquals(1, counter.getCounter());

    SessionNode sessionNode2 =
        new SessionNode(
            sessionNode.getNodeUrl(),
            getDc(),
            new ProcessId(sessionNode.getIp(), timestamp, 2, random.nextInt()));
    Assert.assertFalse(sessionManager.renew(sessionNode2, 1));
    Assert.assertEquals(2, counter.getCounter());
  }

  @Test
  public void testSessionServerManagerRefreshEpochOnlyOnceWhenNewRegistered()
      throws TimeoutException, InterruptedException {
    makeMetaLeader();
    SessionNode node = new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID);
    sessionManager.renew(node, 1000);
    Assert.assertEquals(1, sessionManager.getSessionServerMetaInfo().getClusterMembers().size());
  }

  @Test
  public void testCancel() {
    List<SessionNode> sessionNodes = randomSessionNodes(10);
    for (SessionNode sessionNode : sessionNodes) {
      sessionManager.renew(sessionNode, 1000);
    }
    NotifyObserversCounter counter = new NotifyObserversCounter();
    sessionManager.addObserver(counter);
    sessionManager.cancel(sessionManager.getLease(sessionNodes.get(1)));
    Assert.assertEquals(1, counter.getCounter());
    Assert.assertEquals(9, sessionManager.getSessionServerMetaInfo().getClusterMembers().size());
  }

  protected List<SessionNode> randomSessionNodes(int num) {
    List<SessionNode> result = Lists.newArrayList();
    for (int i = 0; i < num; i++) {
      result.add(new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID));
    }
    return result;
  }

  @Test
  public void testOnHeartbeat() {
    List<SessionNode> sessionNodes = randomSessionNodes(2);
    for (SessionNode sessionNode : sessionNodes) {
      sessionManager.renew(sessionNode, 1000);
    }
    SessionNode sessionNode = new SessionNode(new URL(randomIp()), getDc(), ServerEnv.PROCESS_ID);
    sessionManager.renew(sessionNode, 1000);
    SlotTable slotTable = randomSlotTable();

    Assert.assertEquals(SlotTable.INIT, slotManager.getSlotTable());
    sessionManager.onHeartbeat(
        new HeartbeatRequest<>(
                sessionNode,
                slotTable.getEpoch(),
                getDc(),
                System.currentTimeMillis(),
                SlotConfig.slotBasicInfo(),
                Collections.emptyMap())
            .setSlotTable(slotTable));
    Assert.assertNotEquals(SlotTable.INIT, slotManager.getSlotTable());
  }
}
