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
package com.alipay.sofa.registry.server.session.remoting.console.handler;

import static org.mockito.Mockito.mock;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.ClientOffRequest;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class ClientOffRequestHandlerTest {
  private SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");

  private ClientOffRequestHandler newHandler() {
    ClientOffRequestHandler handler = new ClientOffRequestHandler();
    handler.executorManager = new ExecutorManager(serverConfigBean);
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), ClientOffRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CONSOLE);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    Assert.assertFalse(((CommonResponse) handler.buildFailedResponse("msg")).isSuccess());
    return handler;
  }

  @Test
  public void testHandle() {
    ClientOffRequestHandler handler = newHandler();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    handler.connectionsService = mock(ConnectionsService.class);
    handler.sessionRegistry = mock(SessionRegistry.class);
    handler.executorManager = new ExecutorManager(serverConfigBean);
    CommonResponse obj = (CommonResponse) handler.doHandle(null, request());
    Assert.assertTrue(obj.isSuccess());
  }

  private static ClientOffRequest request() {
    ClientOffRequest req = new ClientOffRequest(Lists.newArrayList("1", "2"));
    Assert.assertTrue(req.toString(), req.toString().contains("1"));
    return req;
  }
}
