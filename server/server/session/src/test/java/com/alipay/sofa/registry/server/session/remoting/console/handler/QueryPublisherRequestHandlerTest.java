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
package com.alipay.sofa.registry.server.session.remoting.console.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.QueryPublisherRequest;
import com.alipay.sofa.registry.common.model.sessionserver.SimplePublisher;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2024/12/23
 */
public class QueryPublisherRequestHandlerTest {

  @Test
  public void testHandle() {
    String dataInfoId = "test-data-info-id";

    List<Publisher> mockPublishers = new ArrayList<>();
    for (int index = 0; index < 3; index++) {
      Publisher mockPublisher = new Publisher();
      mockPublisher.setDataInfoId(dataInfoId);
      mockPublisher.setClientId("ClientId-" + index);
      mockPublisher.setSourceAddress(URL.valueOf("127.0.0." + index + ":1234"));
      mockPublisher.setAppName("App");
      mockPublishers.add(mockPublisher);
    }

    DataStore dataStore = mock(DataStore.class);
    when(dataStore.getDatas(Mockito.eq(dataInfoId))).thenReturn(mockPublishers);

    QueryPublisherRequestHandler handler = new QueryPublisherRequestHandler();
    handler
        .setExecutorManager(new ExecutorManager(TestUtils.newSessionConfig("testDc")))
        .setSessionDataStore(dataStore);

    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), QueryPublisherRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CONSOLE);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    Assert.assertFalse(((CommonResponse) handler.buildFailedResponse("msg")).isSuccess());

    QueryPublisherRequest notExistReq = new QueryPublisherRequest("not-exist");
    GenericResponse<List<SimplePublisher>> notExistResp =
        (GenericResponse) handler.doHandle(null, notExistReq);
    Assert.assertTrue(notExistResp.isSuccess());
    List<SimplePublisher> notExistPublishers = notExistResp.getData();
    Assert.assertTrue(CollectionUtils.isEmpty(notExistPublishers));

    QueryPublisherRequest existReq = new QueryPublisherRequest(dataInfoId);
    GenericResponse<List<SimplePublisher>> existResp =
        (GenericResponse) handler.doHandle(null, existReq);
    Assert.assertTrue(existResp.isSuccess());
    List<SimplePublisher> existPublishers = existResp.getData();
    Assert.assertFalse(CollectionUtils.isEmpty(existPublishers));
    Assert.assertEquals(3, existPublishers.size());

    for (int index = 0; index < existPublishers.size(); index++) {
      SimplePublisher existPublisher = existPublishers.get(index);

      String clientId = existPublisher.getClientId();
      String sourceAddr = existPublisher.getSourceAddress();
      String appName = existPublisher.getAppName();

      Assert.assertEquals("ClientId-" + index, clientId);
      Assert.assertEquals("127.0.0." + index + ":1234", sourceAddr);
      Assert.assertEquals("App", appName);
    }
  }
}
