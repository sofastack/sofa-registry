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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptAllManager;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DataSlotDiffDigestRequestHandlerTest {

  private static final String DATACENTER = "testDc";

  private static final SyncSlotAcceptorManager ACCEPT_ALL = new SyncSlotAcceptAllManager();

  @Test
  public void testCheckParam() {
    DataSlotDiffDigestRequestHandler handler = newHandler();
    handler.checkParam(request(1, null));
    handler.checkParam(request(1, Collections.emptyMap()));
  }

  private DataSlotDiffDigestRequestHandler newHandler() {
    DataSlotDiffDigestRequestHandler handler = new DataSlotDiffDigestRequestHandler();
    handler
        .setExecutorManager(new ExecutorManager(TestUtils.newSessionConfig(DATACENTER)))
        .setSyncSlotAcceptAllManager(ACCEPT_ALL);

    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), DataSlotDiffDigestRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.DATA);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    GenericResponse failed = (GenericResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());

    return handler;
  }

  @Test
  public void testHandle() {
    DataSlotDiffDigestRequestHandler handler = newHandler();
    SlotTableCache slotTableCache = mock(SlotTableCache.class);
    handler.setSlotTableCache(slotTableCache);

    TestUtils.MockBlotChannel channel = TestUtils.newChannel(9620, "localhost", 8888);

    DataSlotDiffDigestRequest request = request(1, Collections.emptyMap());
    // npe
    GenericResponse resp = (GenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertNull(resp.getData());

    DataStore sessionDataStore = mock(DataStore.class);
    when(sessionDataStore.getDataInfoIdPublishers(anyInt())).thenReturn(Collections.emptyMap());
    handler.setSessionDataStore(sessionDataStore);

    resp = (GenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertNotNull(resp.getData());
    Assert.assertTrue(resp.getData() instanceof DataSlotDiffDigestResult);
  }

  private static DataSlotDiffDigestRequest request(
      int slotId, Map<String, DatumDigest> datumDigest) {
    return new DataSlotDiffDigestRequest(DATACENTER, 1, slotId, 1, datumDigest, ACCEPT_ALL);
  }
}
