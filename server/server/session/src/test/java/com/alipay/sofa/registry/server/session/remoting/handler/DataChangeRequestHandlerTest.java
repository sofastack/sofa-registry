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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.TraceTimes;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.store.Interests;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DataChangeRequestHandlerTest {

  @Test
  public void testCheckParam() {
    DataChangeRequestHandler handler = newHandler();
    handler.checkParam(request());
  }

  private DataChangeRequestHandler newHandler() {
    DataChangeRequestHandler handler = new DataChangeRequestHandler();
    Assert.assertEquals(handler.interest(), DataChangeRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.DATA);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    return handler;
  }

  @Test
  public void testHandle() {
    DataChangeRequestHandler handler = newHandler();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    handler.sessionServerConfig = serverConfigBean;
    handler.pushSwitchService = TestUtils.newPushSwitchService(serverConfigBean);
    handler.executorManager = new ExecutorManager(serverConfigBean);
    handler.firePushService = mock(FirePushService.class);
    handler.sessionInterests = mock(Interests.class);

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
    when(handler.sessionInterests.checkInterestVersion(anyString(), anyString(), anyLong()))
        .thenReturn(Interests.InterestVersionCheck.Obsolete);
    obj = handler.doHandle(null, request());
    Assert.assertNull(obj);
    verify(handler.firePushService, times(0)).fireOnChange(anyString(), anyObject());

    when(handler.sessionInterests.checkInterestVersion(anyString(), anyString(), anyLong()))
        .thenReturn(Interests.InterestVersionCheck.Interested);
    obj = handler.doHandle(null, request());
    Assert.assertNull(obj);
    verify(handler.firePushService, times(2)).fireOnChange(anyString(), anyObject());
  }

  private static DataChangeRequest request() {
    Map<String, DatumVersion> dataInfoIds = new HashMap<>();
    dataInfoIds.put("testId1", new DatumVersion(100));
    dataInfoIds.put("testId2", new DatumVersion(200));
    DataChangeRequest request = new DataChangeRequest("testDc", dataInfoIds, new TraceTimes());
    Assert.assertTrue(request.toString(), request.toString().contains("testDc"));
    return request;
  }
}
