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
package com.alipay.sofa.registry.server.session.remoting.handler;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsRequest;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import org.junit.Assert;
import org.junit.Test;

public class GetRevisionPbHandlerTest {
  @Test
  public void testCheckParam() {
    GetRevisionPbHandler handler = newHandler();
    handler.checkParam(request());
  }

  private GetRevisionPbHandler newHandler() {
    GetRevisionPbHandler handler = new GetRevisionPbHandler();
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), GetRevisionsRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    handler.appRevisionHandlerStrategy = mock(AppRevisionHandlerStrategy.class);
    return handler;
  }

  @Test
  public void testHandle() {
    GetRevisionPbHandler handler = newHandler();

    handler.doHandle(null, request());
    verify(handler.appRevisionHandlerStrategy, times(1)).queryRevision(anyList());
  }

  private static GetRevisionsRequest request() {
    GetRevisionsRequest.Builder builder = GetRevisionsRequest.newBuilder();
    builder.addRevisions("testRevision");
    return builder.build();
  }
}
