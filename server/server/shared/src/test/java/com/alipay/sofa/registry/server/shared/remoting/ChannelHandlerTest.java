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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.shared.TestUtils;
import org.junit.Test;

public class ChannelHandlerTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChannelHandlerTest.class);

  @Test
  public void test() {
    MockHandler handler = new MockHandler(LOGGER, LOGGER);
    handler.connected(null);
    handler.disconnected(null);
    handler.caught(null, null, null);
    TestUtils.assertRunException(
        UnsupportedOperationException.class, () -> handler.received(null, null));

    TestUtils.assertRunException(IllegalStateException.class, () -> handler.doHandle(null, null));

    TestUtils.assertRunException(RuntimeException.class, () -> handler.buildFailedResponse(null));
  }

  private static class MockHandler extends AbstractChannelHandler {

    MockHandler(Logger connectLog, Logger exchangeLog) {
      super(connectLog, exchangeLog);
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
      return Node.NodeType.CLIENT;
    }

    @Override
    public Object doHandle(Channel channel, Object request) {
      throw new IllegalStateException();
    }

    @Override
    public Class interest() {
      return MockHandler.class;
    }
  }
}
