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
import com.alipay.sofa.registry.common.model.sessionserver.QuerySubscriberRequest;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.store.Interests;
import org.junit.Assert;
import org.junit.Test;

public class QuerySubscriberRequestHandlerTest {
  private SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");

  private QuerySubscriberRequestHandler newHandler() {
    QuerySubscriberRequestHandler handler = new QuerySubscriberRequestHandler();
    handler.executorManager = new ExecutorManager(serverConfigBean);
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), QuerySubscriberRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CONSOLE);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    Assert.assertFalse(((CommonResponse) handler.buildFailedResponse("msg")).isSuccess());
    return handler;
  }

  @Test
  public void testHandle() {
    QuerySubscriberRequestHandler handler = newHandler();
    handler.sessionInterests = mock(Interests.class);

    CommonResponse obj = (CommonResponse) handler.doHandle(null, request());
    Assert.assertTrue(obj.isSuccess());
  }

  private static QuerySubscriberRequest request() {
    QuerySubscriberRequest req = new QuerySubscriberRequest("testDataId");
    Assert.assertTrue(req.toString(), req.toString().contains("testDataId"));
    return req;
  }
}
