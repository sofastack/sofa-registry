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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.dataserver.BatchRequest;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffPublisher;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class BatchPutDataHandlerTest {
  private final ConnectId connectId = ConnectId.of("localhost:8888", "localhost:9999");

  @Test
  public void testCheckParam() {
    final BatchPutDataHandler handler = newHandler();
    BatchRequest request = request(connectId, false);
    handler.checkParam(request);

    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(null, false));
        });

    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(connectId, true));
        });
  }

  private BatchPutDataHandler newHandler() {
    BatchPutDataHandler handler = new BatchPutDataHandler();
    Assert.assertEquals(handler.interest(), BatchRequest.class);
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.SESSION);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    SlotAccessGenericResponse failed =
        (SlotAccessGenericResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    handler.sessionLeaseManager = new SessionLeaseManager();
    SlotAccessorDelegate slotManager = mock(SlotAccessorDelegate.class);

    handler
        .setSlotAccessor(slotManager)
        .setDatumStorageDelegate(TestBaseUtils.newLocalDatumDelegate("testDc", true))
        .setDataChangeEventCenter(new DataChangeEventCenter())
        .setDataServerConfig(TestBaseUtils.newDataConfig("testDc"));
    return handler;
  }

  @Test
  public void testHandle() {
    BatchPutDataHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept());
    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.doHandle(channel, request(connectId, true));
        });

    BatchRequest request = request(connectId, false);

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept());
    SlotAccessGenericResponse resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
  }

  @Test
  public void testHandleErrorSlotAccess() {
    BatchPutDataHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    BatchRequest request = request(connectId, false);

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.moved());
    SlotAccessGenericResponse resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.moved().getStatus());

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.misMatch());
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.misMatch().getStatus());
  }

  private static BatchRequest request(ConnectId connectId, boolean illegalArg) {
    Publisher pub1 = TestBaseUtils.createTestPublisher("testDataId");
    Publisher pub2 = TestBaseUtils.createTestPublisher("testDataId");

    ClientOffPublisher off = new ClientOffPublisher(connectId);
    off.addPublisher(pub2);
    Assert.assertFalse(off.isEmpty());
    Assert.assertEquals(off.getConnectId(), connectId);
    Assert.assertEquals(
        off.getPublisherMap().get(pub2.getDataInfoId()).get(pub2.getRegisterId()),
        pub2.registerVersion());
    List<Object> list = Lists.newArrayList(pub1, off);
    if (illegalArg) {
      list.add(new Object());
    }
    // add unpub
    Publisher pub3 = TestBaseUtils.createTestPublisher("testDataId");
    list.add(UnPublisher.of(pub3));
    // add temp
    Publisher pub4 = TestBaseUtils.createTestPublisher("testDataId");
    pub4.setPublishType(PublishType.TEMPORARY);
    list.add(pub4);
    BatchRequest req = new BatchRequest(ServerEnv.PROCESS_ID, 10, list);
    return req;
  }
}
