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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import java.util.Collections;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class DataPushRequestHandlerTest {

  private long init = -1L;

  @Test
  public void testCheckParam() {
    DataPushRequestHandler handler = newHandler();
    handler.checkParam(request());
  }

  private DataPushRequestHandler newHandler() {
    DataPushRequestHandler handler = new DataPushRequestHandler();
    Assert.assertEquals(handler.interest(), DataPushRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.DATA);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    return handler;
  }

  @Test
  public void testHandle() {
    DataPushRequestHandler handler = newHandler();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    handler.sessionServerConfig = serverConfigBean;
    handler.pushSwitchService = TestUtils.newPushSwitchService(serverConfigBean);
    handler.executorManager = new ExecutorManager(serverConfigBean);

    Assert.assertNotNull(handler.getExecutor());

    handler
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), true);
    // no npe, stopPush skip the handle
    Object obj = handler.doHandle(null, null);
    Assert.assertNull(obj);

    handler
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);
    // npe
    TestUtils.assertRunException(RuntimeException.class, () -> handler.doHandle(null, request()));
    handler.firePushService = mock(FirePushService.class);
    obj = handler.doHandle(null, request());
    Assert.assertNull(obj);
    verify(handler.firePushService, times(0)).fireOnChange(anyString(), anyObject());
  }

  private static DataPushRequest request() {
    SubDatum subDatum =
        SubDatum.normalOf(
            "dataInfoId",
            "dataCenter",
            100,
            Collections.emptyList(),
            "testDataId",
            "testInstanceId",
            "testGroup",
            Lists.newArrayList(System.currentTimeMillis()));
    DataPushRequest request = new DataPushRequest(subDatum);
    Assert.assertTrue(request.toString(), request.toString().contains("dataInfoId"));
    Assert.assertEquals(request.getDatum(), subDatum);
    return request;
  }
}
