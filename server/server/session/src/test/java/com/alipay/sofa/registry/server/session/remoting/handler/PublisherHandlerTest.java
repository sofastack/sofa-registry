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
import com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb;
import com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.PublisherHandlerStrategy;
import org.junit.Assert;
import org.junit.Test;

public class PublisherHandlerTest {

  private PublisherHandler newHandler() {
    PublisherHandler handler = new PublisherHandler();
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), PublisherRegister.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    handler.publisherHandlerStrategy = mock(PublisherHandlerStrategy.class);
    return handler;
  }

  @Test
  public void testHandle() {
    PublisherHandler handler = newHandler();

    RegisterResponse response = (RegisterResponse) handler.doHandle(null, request());
    Assert.assertFalse(response.isSuccess());
    verify(handler.publisherHandlerStrategy, times(1))
        .handlePublisherRegister(anyObject(), anyObject(), any());
  }

  @Test
  public void testPb() {
    PublisherPbHandler pbHandler = new PublisherPbHandler();
    pbHandler.publisherHandler = newHandler();
    Assert.assertNotNull(pbHandler.getExecutor());
    Assert.assertEquals(pbHandler.interest(), PublisherRegisterPb.class);
    Assert.assertEquals(pbHandler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(pbHandler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(pbHandler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    PublisherRegisterPb pb = PublisherRegisterPb.newBuilder().build();
    // illegal handle
    RegisterResponsePb responsePb = (RegisterResponsePb) pbHandler.doHandle(null, pb);
    Assert.assertFalse(responsePb.getSuccess());

    verify(pbHandler.publisherHandler.publisherHandlerStrategy, times(1))
        .handlePublisherRegister(anyObject(), anyObject(), any());
  }

  private static PublisherRegister request() {
    PublisherRegister register = new PublisherRegister();
    return register;
  }
}
