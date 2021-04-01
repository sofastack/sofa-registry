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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import org.junit.Assert;
import org.junit.Test;

public class DataServerConnectionHandlerTest {

  @Test
  public void test() {
    DataServerConnectionHandler handler = new DataServerConnectionHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), null);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.SESSION);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.LISENTER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);

    handler.setSessionServerConnectionFactory(new SessionServerConnectionFactory());

    final TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);
    TestBaseUtils.assertException(
        UnsupportedOperationException.class,
        () -> {
          handler.doHandle(channel, null);
        });

    TestBaseUtils.assertException(
        UnsupportedOperationException.class,
        () -> {
          handler.buildFailedResponse("msg");
        });

    handler.disconnected(channel);
  }
}
