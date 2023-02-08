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
import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingRequest;
import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultAppRevisionHandlerStrategy;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class ServiceAppMappingPbHandlerTest {
  @Test
  public void testCheckParam() {
    ServiceAppMappingPbHandler handler = newHandler();
    handler.checkParam(request());
  }

  private ServiceAppMappingPbHandler newHandler() {
    ServiceAppMappingPbHandler handler = new ServiceAppMappingPbHandler();
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), ServiceAppMappingRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    handler.appRevisionHandlerStrategy = mock(AppRevisionHandlerStrategy.class);
    return handler;
  }

  @Test
  public void testHandle() {
    ServiceAppMappingPbHandler handler = newHandler();
    Channel channel = new TestUtils.MockBlotChannel(9600, "192.168.0.3", 56555);
    handler.doHandle(channel, request());
    verify(handler.appRevisionHandlerStrategy, times(1)).queryApps(anyList(), anyString());
  }

  @Test
  public void testStopPush() {
    PushSwitchService pushSwitchService = mock(PushSwitchService.class);
    when(pushSwitchService.canIpPushLocal(anyString())).thenReturn(false);
    DefaultAppRevisionHandlerStrategy strategy = new DefaultAppRevisionHandlerStrategy();
    strategy.setPushSwitchService(pushSwitchService);

    ServiceAppMappingResponse response = strategy.queryApps(Lists.newArrayList("123"), "");
    Assert.assertEquals(ValueConstants.METADATA_STATUS_DATA_NOT_FOUND, response.getStatusCode());
  }

  private static ServiceAppMappingRequest request() {
    ServiceAppMappingRequest.Builder builder = ServiceAppMappingRequest.newBuilder();
    builder.addServiceIds("testServiceIds");
    return builder.build();
  }
}
