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

import com.alipay.remoting.InvokeContext;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb;
import com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.bolt.serializer.ProtobufSerializer;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.SubscriberHandlerStrategy;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberHandlerTest {

  private SubscriberHandler newHandler() {
    SubscriberHandler handler = new SubscriberHandler();
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), SubscriberRegister.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    handler.subscriberHandlerStrategy = mock(SubscriberHandlerStrategy.class);
    return handler;
  }

  @Test
  public void testHandle() {
    SubscriberHandler handler = newHandler();

    RegisterResponse response = (RegisterResponse) handler.doHandle(null, request());
    Assert.assertFalse(response.isSuccess());
    verify(handler.subscriberHandlerStrategy, times(1))
        .handleSubscriberRegister(anyObject(), anyObject(), any());
  }

  @Test
  public void testPb() {
    SubscriberPbHandler pbHandler = new SubscriberPbHandler();
    pbHandler.subscriberHandler = newHandler();
    Assert.assertNotNull(pbHandler.getExecutor());
    Assert.assertEquals(pbHandler.interest(), SubscriberRegisterPb.class);
    Assert.assertEquals(pbHandler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(pbHandler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(pbHandler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    SubscriberRegisterPb pb = SubscriberRegisterPb.newBuilder().build();
    TestUtils.MockBlotChannel channel = TestUtils.newChannel(9600, "127.0.0.1", 9888);

    // not RegisterResponse
    RegisterResponsePb responsePb = (RegisterResponsePb) pbHandler.doHandle(channel, pb);
    Assert.assertFalse(responsePb.getSuccess());
    verify(pbHandler.subscriberHandler.subscriberHandlerStrategy, times(1))
        .handleSubscriberRegister(anyObject(), anyObject(), any());
    Assert.assertEquals(
        channel.getConnection().getAttribute(InvokeContext.BOLT_CUSTOM_SERIALIZER),
        new Byte(ProtobufSerializer.PROTOCOL_PROTOBUF));
  }

  private static SubscriberRegister request() {
    SubscriberRegister register = new SubscriberRegister();
    return register;
  }
}
