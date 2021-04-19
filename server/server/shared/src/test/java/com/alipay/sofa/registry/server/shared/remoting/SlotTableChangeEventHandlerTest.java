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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.shared.TestUtils;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import org.junit.Assert;
import org.junit.Test;

public class SlotTableChangeEventHandlerTest {
  @Test
  public void testCheckParam() {
    SlotTableChangeEventHandler handler = newHandler();
    TestUtils.assertRunException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(null);
        });

    TestUtils.assertRunException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(new SlotTableChangeEvent(0));
        });
    handler.checkParam(new SlotTableChangeEvent(10));
  }

  private SlotTableChangeEventHandler newHandler() {
    SlotTableChangeEventHandler handler = new SlotTableChangeEventHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), SlotTableChangeEvent.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.META);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    CommonResponse failed = (CommonResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    return handler;
  }

  @Test
  public void testHandle() {
    SlotTableChangeEventHandler handler = newHandler();
    MetaServerService svc = mock(MetaServerService.class);
    handler.metaServerService = svc;
    when(svc.handleSlotTableChange(anyObject())).thenReturn(false);
    CommonResponse response = (CommonResponse) handler.doHandle(null, new SlotTableChangeEvent(10));
    Assert.assertFalse(response.isSuccess());

    when(svc.handleSlotTableChange(anyObject())).thenReturn(true);
    response = (CommonResponse) handler.doHandle(null, new SlotTableChangeEvent(10));
    Assert.assertTrue(response.isSuccess());
  }
}
