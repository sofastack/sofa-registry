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
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaTest {
  @Test
  public void test() {
    javax.ws.rs.client.Client client = JerseyClient.getInstance().getClient();
    Assert.assertNull(AbstractMetaServerManager.queryLeaderInfo(Collections.EMPTY_LIST, client));
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
  }
}
