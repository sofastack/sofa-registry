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
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.metrics.SystemLoad;
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
        new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID, 0), 1000);
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
        new SessionNode(
            randomURL(ip), getDc(), new ProcessId(ip, timestamp, 1, random.nextInt()), 0);
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
            new ProcessId(sessionNode.getIp(), timestamp, 2, random.nextInt()),
            0);
    Assert.assertFalse(sessionManager.renew(sessionNode2, 1));
    Assert.assertEquals(2, counter.getCounter());
  }

  @Test
  public void testSessionServerManagerRefreshEpochOnlyOnceWhenNewRegistered()
      throws TimeoutException, InterruptedException {
    makeMetaLeader();
    SessionNode node = new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID, 0);
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
      result.add(new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID, 0));
    }
    return result;
  }

  @Test
  public void testOnHeartbeat() {
    List<SessionNode> sessionNodes = randomSessionNodes(2);
    for (SessionNode sessionNode : sessionNodes) {
      sessionManager.renew(sessionNode, 1000);
    }
    SessionNode sessionNode =
        new SessionNode(new URL(randomIp()), getDc(), ServerEnv.PROCESS_ID, 0);
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

  @Test
  public void testWeightChange() {
    List<SessionNode> sessionNodes = randomSessionNodes(10);
    for (SessionNode sessionNode : sessionNodes) {
      sessionManager.register(new Lease<>(sessionNode, 10000));
    }
    SessionNode sessionNode =
        new SessionNode(
            sessionNodes.get(0).getNodeUrl(),
            sessionNodes.get(0).getRegionId(),
            sessionNodes.get(0).getProcessId(),
            1);
    sessionManager.renew(sessionNode, 1000);
    Assert.assertEquals(10, sessionManager.getSessionServerMetaInfo().getClusterMembers().size());
  }

  @Test
  public void testSystemLoadChange() {
    String ip = randomIp();
    long timestamp = System.currentTimeMillis();
    ProcessId processId = new ProcessId(ip, timestamp, 1, random.nextInt());
    URL url = randomURL(ip);

    // Create session node with initial SystemLoad
    SystemLoad initialLoad = new SystemLoad(50.0, 2.0);
    SessionNode sessionNode = new SessionNode(url, getDc(), processId, 100, initialLoad);

    // Register the session node
    sessionManager.register(new Lease<>(sessionNode, 10000));
    Assert.assertEquals(1, sessionManager.getSessionServerMetaInfo().getClusterMembers().size());

    // Verify initial system load
    Lease<SessionNode> lease = sessionManager.getLease(sessionNode);
    Assert.assertNotNull(lease);
    Assert.assertNotNull(lease.getRenewal().getSystemLoad());
    Assert.assertEquals(50.0, lease.getRenewal().getSystemLoad().getCpuAverage(), 0.001);
    Assert.assertEquals(2.0, lease.getRenewal().getSystemLoad().getLoadAverage(), 0.001);

    // Create new session node with updated SystemLoad (same URL, processId, weight)
    SystemLoad updatedLoad = new SystemLoad(80.0, 5.0);
    SessionNode updatedSessionNode = new SessionNode(url, getDc(), processId, 100, updatedLoad);

    // Renew with updated system load
    sessionManager.renew(updatedSessionNode, 1000);

    // Verify that the system load was updated
    Lease<SessionNode> updatedLease = sessionManager.getLease(updatedSessionNode);
    Assert.assertNotNull(updatedLease);
    Assert.assertNotNull(updatedLease.getRenewal().getSystemLoad());
    Assert.assertEquals(80.0, updatedLease.getRenewal().getSystemLoad().getCpuAverage(), 0.001);
    Assert.assertEquals(5.0, updatedLease.getRenewal().getSystemLoad().getLoadAverage(), 0.001);
  }

  @Test
  public void testSystemLoadChangeFromNullToNonNull() {
    String ip = randomIp();
    long timestamp = System.currentTimeMillis();
    ProcessId processId = new ProcessId(ip, timestamp, 1, random.nextInt());
    URL url = randomURL(ip);

    // Create session node without SystemLoad (null)
    SessionNode sessionNode = new SessionNode(url, getDc(), processId, 100);

    // Register the session node
    sessionManager.register(new Lease<>(sessionNode, 10000));
    Lease<SessionNode> lease = sessionManager.getLease(sessionNode);
    Assert.assertNull(lease.getRenewal().getSystemLoad());

    // Create new session node with SystemLoad
    SystemLoad newLoad = new SystemLoad(60.0, 3.0);
    SessionNode updatedSessionNode = new SessionNode(url, getDc(), processId, 100, newLoad);

    // Renew with system load
    sessionManager.renew(updatedSessionNode, 1000);

    // Verify that the system load was updated
    Lease<SessionNode> updatedLease = sessionManager.getLease(updatedSessionNode);
    Assert.assertNotNull(updatedLease.getRenewal().getSystemLoad());
    Assert.assertEquals(60.0, updatedLease.getRenewal().getSystemLoad().getCpuAverage(), 0.001);
    Assert.assertEquals(3.0, updatedLease.getRenewal().getSystemLoad().getLoadAverage(), 0.001);
  }

  @Test
  public void testSystemLoadChangeFromNonNullToNull() {
    String ip = randomIp();
    long timestamp = System.currentTimeMillis();
    ProcessId processId = new ProcessId(ip, timestamp, 1, random.nextInt());
    URL url = randomURL(ip);

    // Create session node with SystemLoad
    SystemLoad initialLoad = new SystemLoad(70.0, 4.0);
    SessionNode sessionNode = new SessionNode(url, getDc(), processId, 100, initialLoad);

    // Register the session node
    sessionManager.register(new Lease<>(sessionNode, 10000));
    Lease<SessionNode> lease = sessionManager.getLease(sessionNode);
    Assert.assertNotNull(lease.getRenewal().getSystemLoad());

    // Create new session node without SystemLoad (null)
    SessionNode updatedSessionNode = new SessionNode(url, getDc(), processId, 100, null);

    // Renew without system load
    sessionManager.renew(updatedSessionNode, 1000);

    // Verify that the system load was updated to null
    Lease<SessionNode> updatedLease = sessionManager.getLease(updatedSessionNode);
    Assert.assertNull(updatedLease.getRenewal().getSystemLoad());
  }
}
