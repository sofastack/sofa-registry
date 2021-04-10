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
package com.alipay.sofa.registry.server.data.remoting.metaserver.handler;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessorManager;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import org.junit.Assert;
import org.junit.Test;

public class NotifyProvideDataChangeHandlerTest {
  @Test
  public void testCheckParam() {
    NotifyProvideDataChangeHandler handler = newHandler();
    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(null, 10));
        });

    handler.checkParam(request("111", 10));
  }

  private NotifyProvideDataChangeHandler newHandler() {
    NotifyProvideDataChangeHandler handler = new NotifyProvideDataChangeHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), ProvideDataChangeEvent.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.META);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    CommonResponse failed = (CommonResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    return handler;
  }

  @Test
  public void testHandle() {
    NotifyProvideDataChangeHandler handler = newHandler();
    handler.setProvideDataProcessorManager(new ProvideDataProcessorManager());
    MetaServerService svc = mock(MetaServerService.class);
    when(svc.fetchData(anyString())).thenReturn(new ProvideData(null, "test", 100L));
    handler.setMetaServerService(svc);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    ProvideDataChangeEvent request = request("testDataId", 100);

    Object obj = handler.doHandle(channel, request);
    Assert.assertNull(obj);
  }

  private static ProvideDataChangeEvent request(String dataInfoId, long version) {
    return new ProvideDataChangeEvent(dataInfoId, version);
  }
}
