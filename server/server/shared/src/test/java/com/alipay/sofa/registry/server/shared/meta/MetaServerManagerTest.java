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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.MetaLeaderQueryException;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.shared.TestUtils;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfigBean;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import com.google.common.collect.Lists;
import java.util.*;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaServerManagerTest {
  @Test
  public void testRestQueryLeader() {
    javax.ws.rs.client.Client client = JerseyClient.getInstance().getClient();
    Assert.assertNull(AbstractMetaServerManager.queryLeaderInfo(Collections.emptyList(), client));
    Assert.assertNull(
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    client = Mockito.mock(javax.ws.rs.client.Client.class);
    WebTarget webTarget = Mockito.mock(WebTarget.class);
    Mockito.when(client.target(Mockito.anyString())).thenReturn(webTarget);
    Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
    Mockito.when(webTarget.request()).thenReturn(builder);
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(builder.buildGet()).thenReturn(invocation);

    Response response = Mockito.mock(Response.class);
    Mockito.when(invocation.invoke()).thenReturn(response);

    Mockito.when(response.getStatus()).thenReturn(300);
    Assert.assertNull(
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    Mockito.when(response.getStatus()).thenReturn(200);
    GenericResponse genericResponse = new GenericResponse();
    Mockito.when(response.readEntity(GenericResponse.class)).thenReturn(genericResponse);
    Assert.assertNull(
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    genericResponse.setSuccess(true);
    Assert.assertNull(
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    Map<String, Object> m = new LinkedHashMap<>();
    m.put(AbstractMetaServerManager.EPOCH_KEY, 100L);
    m.put(AbstractMetaServerManager.LEADER_KEY, "");
    genericResponse.setData(m);

    Assert.assertNull(
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    m.put(AbstractMetaServerManager.LEADER_KEY, "test-leader");

    Assert.assertEquals(
        new LeaderInfo(100, "test-leader"),
        AbstractMetaServerManager.queryLeaderInfo(Lists.newArrayList("localhost"), client));

    MockServerManager mockServerManager = new MockServerManager();
    mockServerManager.setRsClient(client);
    DefaultCommonConfigBean defaultCommonConfigBean = new DefaultCommonConfigBean();
    defaultCommonConfigBean.setPersistenceProfileActive(SpringContext.META_STORE_API_RAFT);
    mockServerManager.defaultCommonConfig = defaultCommonConfigBean;
    // not refresh
    Assert.assertNull(mockServerManager.metaLeaderInfo);
    // leader is null, not update
    mockServerManager.refresh(new BaseHeartBeatResponse(true, null, 0));
    Assert.assertNull(mockServerManager.metaLeaderInfo);

    TestUtils.assertRunException(
        MetaLeaderQueryException.class, () -> mockServerManager.resetLeader());
    mockServerManager.domains = Lists.newArrayList("localhost");
    String leader = mockServerManager.getMetaServerLeader();
    Assert.assertEquals("test-leader", leader);
    Assert.assertNotNull(mockServerManager.metaLeaderInfo);
    Assert.assertEquals("test-leader", mockServerManager.getMetaServerLeader());

    // same leader epoch, not update
    LeaderInfo leaderInfo = mockServerManager.metaLeaderInfo;
    BaseHeartBeatResponse heartBeatResponse =
        new BaseHeartBeatResponse(true, "leader2", mockServerManager.metaLeaderInfo.getEpoch());
    mockServerManager.refresh(heartBeatResponse);
    Assert.assertEquals(leaderInfo, mockServerManager.metaLeaderInfo);
    Assert.assertEquals(leaderInfo.hashCode(), mockServerManager.metaLeaderInfo.hashCode());

    heartBeatResponse = new BaseHeartBeatResponse(true, "leader2", leaderInfo.getEpoch() + 1);
    mockServerManager.refresh(heartBeatResponse);
    Assert.assertEquals(
        new LeaderInfo(leaderInfo.getEpoch() + 1, "leader2"), mockServerManager.metaLeaderInfo);

    // reset the rsclient
    mockServerManager.init();
    TestUtils.assertRunException(
        MetaLeaderQueryException.class, () -> mockServerManager.resetLeader());
  }

  @Test
  public void testConnect() {
    MockServerManager mgr = new MockServerManager();
    mgr.connectFailed = true;
    mgr.setServerIps(Lists.newArrayList("ip1"));
    TestUtils.assertRunException(RuntimeException.class, () -> mgr.connectServer());
  }

  private static final class MockServerManager extends AbstractMetaServerManager {
    List<String> domains = Collections.emptyList();
    boolean connectFailed;

    @Override
    public Channel connect(URL url) {
      if (connectFailed) {
        throw new IllegalStateException();
      }
      return null;
    }

    @Override
    protected Collection<String> getConfiguredMetaServerDomains() {
      return domains;
    }

    @Override
    public int getRpcTimeoutMillis() {
      return 10000;
    }

    @Override
    public int getServerPort() {
      return 0;
    }
  }
}
