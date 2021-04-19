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

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.registry.Registry;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class CancelAddressRequestHandlerTest {
  @Test
  public void testCheckParam() {
    CancelAddressRequestHandler handler = newHandler();
    handler.checkParam(request());
  }

  private CancelAddressRequestHandler newHandler() {
    CancelAddressRequestHandler handler = new CancelAddressRequestHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), CancelAddressRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CLIENT);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    Result failed = (Result) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    return handler;
  }

  @Test
  public void testHandle() {
    CancelAddressRequestHandler handler = newHandler();
    handler.sessionRegistry = mock(Registry.class);
    Result result = (Result) handler.doHandle(null, request());
    Assert.assertTrue(result.isSuccess());
    verify(handler.sessionRegistry, times(1)).cancel(anyList());
  }

  private static CancelAddressRequest request() {
    List<ConnectId> list = Lists.newArrayList(ConnectId.of("xxx:1111", "yyyy:2222"));
    CancelAddressRequest request = new CancelAddressRequest(list);
    Assert.assertTrue(request.toString(), request.toString().contains("xxx:111"));

    CancelAddressRequest request2 = new CancelAddressRequest();
    request2.setConnectIds(list);
    Assert.assertEquals(request.getConnectIds(), request2.getConnectIds());

    return request;
  }
}
