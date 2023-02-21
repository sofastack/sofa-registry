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
package com.alipay.sofa.registry.server.shared.meta;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.TestUtils;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetaServerServiceTest {
  private static final String TEST_DATA_CENTER = "testDC";

  @Mock private CommonConfig commonConfig;

  @Before
  public void init() {
    when(commonConfig.getLocalDataCenter()).thenReturn(TEST_DATA_CENTER);
  }

  @Test
  public void testCheckFailCounter() {
    MockServerService mockServerService = new MockServerService();
    mockServerService
        .setMetaLeaderExchanger(mock(MetaLeaderExchanger.class))
        .setCommonConfig(commonConfig);

    Assert.assertFalse(mockServerService.checkRenewFailCounter());
    mockServerService.renewFailCounter.set(mockServerService.MAX_RENEW_FAIL_COUNT - 1);
    Assert.assertFalse(mockServerService.checkRenewFailCounter());
    mockServerService.renewFailCounter.set(mockServerService.MAX_RENEW_FAIL_COUNT);
    Assert.assertTrue(mockServerService.checkRenewFailCounter());
    Assert.assertEquals(mockServerService.renewFailCounter.get(), 0);
  }

  @Test
  public void testHandleHeartbeatResp() {
    MockServerService mockServerService = new MockServerService();
    MetaLeaderExchanger metaLeaderExchanger = mock(MetaLeaderExchanger.class);
    mockServerService.setMetaLeaderExchanger(metaLeaderExchanger).setCommonConfig(commonConfig);

    TestUtils.assertRunException(
        RuntimeException.class,
        () -> mockServerService.handleHeartbeatFailed("test", new IllegalArgumentException()));
    Assert.assertEquals(mockServerService.renewFailCounter.getAndSet(0), 1);

    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.handleHeartbeatResponse(null));
    Mockito.verify(metaLeaderExchanger, Mockito.times(0))
        .learn(Mockito.anyString(), Mockito.anyObject());

    // false and data is null
    TestUtils.assertRunException(
        RuntimeException.class,
        () -> mockServerService.handleHeartbeatResponse(new GenericResponse<>()));
    Mockito.verify(metaLeaderExchanger, Mockito.times(0))
        .learn(Mockito.anyString(), Mockito.anyObject());

    // not leader
    GenericResponse<BaseHeartBeatResponse> resp = new GenericResponse<>();
    BaseHeartBeatResponse heartBeatResponse =
        new BaseHeartBeatResponse(false, VersionedList.EMPTY, null, "test", 100);
    resp.setData(heartBeatResponse);
    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.handleHeartbeatResponse(resp));
    Mockito.verify(metaLeaderExchanger, Mockito.times(1))
        .learn(Mockito.anyString(), Mockito.anyObject());

    // is leader and false
    heartBeatResponse =
        new BaseHeartBeatResponse(
            true,
            new VersionedList(2, Lists.newArrayList(new MetaNode(new URL("192.168.1.1"), "dc1"))),
            new SlotTable(10, Collections.emptyList()),
            new VersionedList(
                1,
                Lists.newArrayList(
                    new SessionNode(new URL("192.168.1.2"), "zoneA", ServerEnv.PROCESS_ID),
                    new SessionNode(
                        new URL("192.168.1.3"), "zoneB", new ProcessId("test", 1, 1, 1)))),
            "test",
            100,
            Collections.emptyMap());
    resp.setData(heartBeatResponse);
    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.handleHeartbeatResponse(resp));
    Mockito.verify(metaLeaderExchanger, Mockito.times(1))
        .learn(Mockito.anyString(), Mockito.anyObject());

    // is leader and true
    mockServerService.renewFailCounter.incrementAndGet();
    resp.setSuccess(true);
    mockServerService.handleHeartbeatResponse(resp);
    Mockito.verify(metaLeaderExchanger, Mockito.times(2))
        .learn(Mockito.anyString(), Mockito.anyObject());
    Assert.assertEquals(mockServerService.renewFailCounter.get(), 0);
    Assert.assertEquals(1, mockServerService.getSessionServerEpoch());
    Assert.assertEquals(
        mockServerService.getSessionServerList(), Sets.newHashSet("192.168.1.2", "192.168.1.3"));
    Assert.assertEquals(mockServerService.getDataCenters(), Sets.newHashSet("dc1"));
    Map<String, SessionNode> sessionNodeMap = mockServerService.getSessionNodes();
    Assert.assertEquals(sessionNodeMap.size(), 2);
    Assert.assertEquals(sessionNodeMap.keySet(), mockServerService.getSessionServerList());
    List<String> zones = mockServerService.getSessionServerList("");
    Assert.assertEquals(zones.size(), 2);
    Assert.assertTrue(zones.contains("192.168.1.2"));
    Assert.assertTrue(zones.contains("192.168.1.3"));

    Assert.assertEquals(2, mockServerService.getSessionProcessIds().size());
    Assert.assertTrue(mockServerService.getSessionProcessIds().contains(ServerEnv.PROCESS_ID));
    zones = mockServerService.getSessionServerList("zoneC");
    Assert.assertEquals(zones.size(), 0);

    zones = mockServerService.getSessionServerList("zoneA");
    Assert.assertEquals(zones.size(), 1);
    Assert.assertTrue(zones.contains("192.168.1.2"));
  }

  @Test
  public void testSuspend() {
    MockServerService mockServerService = new MockServerService();
    mockServerService.startRenewer();
    WakeUpLoopRunnable loop = mockServerService.renewer;
    Assert.assertFalse(loop.isSuspended());
    mockServerService.suspendRenewer();
    Assert.assertTrue(loop.isSuspended());
    mockServerService.resumeRenewer();
    Assert.assertFalse(loop.isSuspended());
  }

  @Test
  public void testFetchData() {
    MockServerService mockServerService = new MockServerService();
    MetaLeaderExchanger metaLeaderExchanger = mock(MetaLeaderExchanger.class);
    mockServerService.setMetaLeaderExchanger(metaLeaderExchanger).setCommonConfig(commonConfig);

    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.fetchData("testDataId"));

    when(metaLeaderExchanger.getLeader(anyString()))
        .thenReturn(new LeaderInfo(System.currentTimeMillis(), "127.0.0.1"));
    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.fetchData("testDataId"));

    Response response =
        new Response() {
          @Override
          public Object getResult() {
            return null;
          }
        };
    when(metaLeaderExchanger.sendRequest(Mockito.anyString(), Mockito.anyObject()))
        .thenReturn(response);
    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.fetchData("testDataId"));
    final ProvideData provideData = new ProvideData(null, "testDataId", 10L);
    response =
        new Response() {
          @Override
          public Object getResult() {
            return provideData;
          }
        };
    when(metaLeaderExchanger.sendRequest(Mockito.anyString(), Mockito.anyObject()))
        .thenReturn(response);
    ProvideData got = mockServerService.fetchData("testDataId");
    Assert.assertEquals(provideData, got);
  }

  @Test
  public void testBlacklist() {
    MockServerService mockServerService = new MockServerService();
    mockServerService
        .setMetaLeaderExchanger(mock(MetaLeaderExchanger.class))
        .setCommonConfig(commonConfig);

    mockServerService.addSelfToMetaBlacklist();
    mockServerService.removeSelfFromMetaBlacklist();
  }

  @Test
  public void testRenewNode() {
    GenericResponse<BaseHeartBeatResponse> resp = new GenericResponse<>();
    BaseHeartBeatResponse heartBeatResponse =
        new BaseHeartBeatResponse(
            true,
            new VersionedList(2, Lists.newArrayList(new MetaNode(new URL("192.168.1.1"), "dc1"))),
            new SlotTable(10, Collections.emptyList()),
            new VersionedList(
                1,
                Lists.newArrayList(
                    new SessionNode(new URL("192.168.1.2"), "zoneA", ServerEnv.PROCESS_ID),
                    new SessionNode(
                        new URL("192.168.1.3"), "zoneB", new ProcessId("test", 1, 1, 1)))),
            "test",
            100,
            Collections.emptyMap());
    resp.setSuccess(true);
    resp.setData(heartBeatResponse);

    MockServerService mockServerService = new MockServerService();
    MetaLeaderExchanger mockMetaExchange = mock(MetaLeaderExchanger.class);
    when(mockMetaExchange.getLeader(commonConfig.getLocalDataCenter()))
        .thenReturn(new LeaderInfo(System.currentTimeMillis(), "1.1.1.1"));
    when(mockMetaExchange.sendRequest(anyString(), anyObject())).thenReturn(() -> resp);

    mockServerService.setMetaLeaderExchanger(mockMetaExchange).setCommonConfig(commonConfig);

    boolean renewNode = mockServerService.renewNode();
    Assert.assertTrue(renewNode);

    Mockito.verify(mockMetaExchange, Mockito.times(1))
        .learn(Mockito.anyString(), Mockito.anyObject());
    Assert.assertEquals(mockServerService.renewFailCounter.get(), 0);
    Assert.assertEquals(1, mockServerService.getSessionServerEpoch());
    Assert.assertEquals(
        mockServerService.getSessionServerList(), Sets.newHashSet("192.168.1.2", "192.168.1.3"));
    Assert.assertEquals(mockServerService.getDataCenters(), Sets.newHashSet("dc1"));
    Map<String, SessionNode> sessionNodeMap = mockServerService.getSessionNodes();
    Assert.assertEquals(sessionNodeMap.size(), 2);
    Assert.assertEquals(sessionNodeMap.keySet(), mockServerService.getSessionServerList());
    List<String> zones = mockServerService.getSessionServerList("");
    Assert.assertEquals(zones.size(), 2);
    Assert.assertTrue(zones.contains("192.168.1.2"));
    Assert.assertTrue(zones.contains("192.168.1.3"));

    Assert.assertEquals(2, mockServerService.getSessionProcessIds().size());
    Assert.assertTrue(mockServerService.getSessionProcessIds().contains(ServerEnv.PROCESS_ID));
    zones = mockServerService.getSessionServerList("zoneC");
    Assert.assertEquals(zones.size(), 0);

    zones = mockServerService.getSessionServerList("zoneA");
    Assert.assertEquals(zones.size(), 1);
    Assert.assertTrue(zones.contains("192.168.1.2"));
  }

  @Test
  public void testFetchSystemProperty() {
    MockServerService mockServerService = new MockServerService();
    MetaLeaderExchanger mockMetaExchange = mock(MetaLeaderExchanger.class);
    when(mockMetaExchange.getLeader(commonConfig.getLocalDataCenter()))
        .thenReturn(new LeaderInfo(System.currentTimeMillis(), "1.1.1.1"));

    mockServerService.setMetaLeaderExchanger(mockMetaExchange).setCommonConfig(commonConfig);

    long version = System.currentTimeMillis();
    FetchSystemPropertyResult result =
        new FetchSystemPropertyResult(
            true, new ProvideData(new ServerDataBox("aaa"), "testDataId", version));
    when(mockMetaExchange.sendRequest(anyString(), anyObject())).thenReturn(() -> result);

    FetchSystemPropertyResult fetch = mockServerService.fetchSystemProperty("testDataId", version);
    Assert.assertEquals(result, fetch);
  }

  private static final class MockServerService
      extends AbstractMetaServerService<BaseHeartBeatResponse> {

    @Override
    public int getRenewIntervalSecs() {
      return 10000;
    }

    @Override
    protected void handleRenewResult(BaseHeartBeatResponse result) {}

    @Override
    protected HeartbeatRequest createRequest() {
      return null;
    }

    @Override
    protected NodeType nodeType() {
      return NodeType.DATA;
    }

    @Override
    protected String cell() {
      return "testCell";
    }

    @Override
    protected long getCurrentSlotTableEpoch() {
      return 0;
    }
  }
}
