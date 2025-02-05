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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SlotFollowerDiffDigestRequestHandlerTest {

  private static final String DC = "DC";
  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @Test
  public void testCheckParam() {
    SlotFollowerDiffDigestRequestHandler handler = newHandler();
    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(-1, Collections.emptyMap()));
        });

    handler.checkParam(request(1, null));
    handler.checkParam(request(1, Collections.emptyMap()));
  }

  private SlotFollowerDiffDigestRequestHandler newHandler() {
    SlotFollowerDiffDigestRequestHandler handler = new SlotFollowerDiffDigestRequestHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), DataSlotDiffDigestRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.DATA);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    GenericResponse failed = (GenericResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    SlotManager slotManager = mock(SlotManager.class);
    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);

    handler
        .setSlotManager(slotManager)
        .setDatumStorageDelegate(datumStorageDelegate)
        .setDataServerConfig(TestBaseUtils.newDataConfig(DC));

    return handler;
  }

  @Test
  public void testHandle() {
    SlotFollowerDiffDigestRequestHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    DataSlotDiffDigestRequest request = request(1, Collections.emptyMap());

    // not leader
    when(handler.getSlotManager().isLeader(Mockito.eq(DC), anyInt())).thenReturn(false);
    GenericResponse resp = (GenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertNull(resp.getData());

    // is leader
    when(handler.getSlotManager().isLeader(Mockito.eq(DC), anyInt())).thenReturn(true);
    resp = (GenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertNotNull(resp.getData());

    // npe
    handler.setSlotManager(null);
    resp = (GenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertNull(resp.getData());
  }

  private static DataSlotDiffDigestRequest request(
      int slotId, Map<String, DatumDigest> datumDigest) {
    return new DataSlotDiffDigestRequest(DC, 1, slotId, 1, datumDigest, ACCEPT_ALL);
  }
}
