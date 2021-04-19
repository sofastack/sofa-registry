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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb;
import com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb;
import com.alipay.sofa.registry.core.model.SyncConfigRequest;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSyncConfigHandlerStrategy;
import org.junit.Assert;
import org.junit.Test;

public class SyncConfigHandlerTest {

  private SyncConfigHandler newHandler() {
    SyncConfigHandler handler = new SyncConfigHandler();
    handler.executorManager = new ExecutorManager(TestUtils.newSessionConfig("testDc"));
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), SyncConfigRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    handler.syncConfigHandlerStrategy = new DefaultSyncConfigHandlerStrategy();
    return handler;
  }

  @Test
  public void testHandle() {
    SyncConfigHandler handler = newHandler();
    // always true
    SyncConfigResponse response = (SyncConfigResponse) handler.doHandle(null, request());
    Assert.assertTrue(response.isSuccess());
  }

  @Test
  public void testPbFail() {
    SyncConfigResponsePb pb = SyncConfigPbHandler.fail();
    Assert.assertFalse(pb.getResult().getSuccess());
  }

  @Test
  public void testPb() {
    SyncConfigPbHandler pbHandler = new SyncConfigPbHandler();
    pbHandler.syncConfigHandler = newHandler();
    Assert.assertNotNull(pbHandler.getExecutor());
    Assert.assertEquals(pbHandler.interest(), SyncConfigRequestPb.class);
    Assert.assertEquals(pbHandler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(pbHandler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(pbHandler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    SyncConfigRequestPb pb = SyncConfigRequestPb.newBuilder().build();
    // always true
    SyncConfigResponsePb responsePb = (SyncConfigResponsePb) pbHandler.doHandle(null, pb);
    Assert.assertTrue(responsePb.getResult().getSuccess());
  }

  private static SyncConfigRequest request() {
    SyncConfigRequest register = new SyncConfigRequest();
    return register;
  }
}
