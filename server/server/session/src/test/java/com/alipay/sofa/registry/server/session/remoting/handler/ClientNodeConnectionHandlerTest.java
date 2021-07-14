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

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ClientNodeConnectionHandlerTest {

  private ClientNodeConnectionHandler newHandler() {
    ClientNodeConnectionHandler handler = new ClientNodeConnectionHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), null);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.LISTENER);
    TestUtils.assertRunException(RuntimeException.class, () -> handler.buildFailedResponse("msg"));
    return handler;
  }

  @Test
  public void testHandle() {
    ClientNodeConnectionHandler handler = newHandler();
    handler.sessionRegistry = mock(Registry.class);
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Channel channel = TestUtils.newChannel(9600, "127.0.0.1", 9888);
    handler.start();
    handler.fireCancelClient(channel);
    ConcurrentUtils.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    verify(handler.sessionRegistry, times(1)).clean(anyList());
    handler.disconnected(channel);
  }
}
