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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.providedata.CompressDatumService;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.junit.Assert;
import org.junit.Test;

public class GetDataHandlerTest {

  @Test
  public void testCheckParam() {
    GetDataHandler handler = newHandler();
    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(null, "xxx"));
        });

    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request("xx", null));
        });

    handler.checkParam(request("xx", "xx"));
  }

  private GetDataHandler newHandler() {
    GetDataHandler handler = new GetDataHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), GetDataRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.SESSION);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    SlotAccessGenericResponse failed =
        (SlotAccessGenericResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    handler.sessionLeaseManager = new SessionLeaseManager();
    SlotAccessorDelegate slotManager = mock(SlotAccessorDelegate.class);
    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);
    CompressDatumService compressDatumService = new CompressDatumService();

    handler
        .setSlotAccessor(slotManager)
        .setDatumStorageDelegate(datumStorageDelegate)
        .setDataChangeEventCenter(new DataChangeEventCenter())
        .setDataServerConfig(TestBaseUtils.newDataConfig("testDc"));
    handler.setCompressDatumService(compressDatumService);

    return handler;
  }

  @Test
  public void testHandle() {
    GetDataHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    GetDataRequest request = request("testDc", "testDataId");

    // get nil
    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept(), TestBaseUtils.accept());
    SlotAccessGenericResponse resp = handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
    Assert.assertNull(resp.getData());

    // get leader change
    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept(), TestBaseUtils.migrating(1, 10, 100));
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.migrating().getStatus());
    Assert.assertNull(resp.getData());

    // get success
    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept());
    Publisher pub = TestBaseUtils.createTestPublishers(1, 1).get(0);
    request = request("testDc", pub.getDataInfoId());
    handler.getDatumStorageDelegate().putPublisher("testDc", pub);
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
    SubDatum subDatum = (SubDatum) resp.getData();
    Assert.assertEquals(subDatum.mustGetPublishers().size(), 1);
    Assert.assertEquals(subDatum.mustGetPublishers().get(0).getRegisterId(), pub.getRegisterId());
    Assert.assertEquals(
        subDatum.mustGetPublishers().get(0).getRegisterTimestamp(), pub.getRegisterTimestamp());
    Assert.assertEquals(subDatum.mustGetPublishers().get(0).getVersion(), pub.getVersion());
  }

  @Test
  public void testHandleErrorSlotAccess() {
    GetDataHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    GetDataRequest request = request("testDc", "testDataId");

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.moved());
    SlotAccessGenericResponse resp = handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.moved().getStatus());

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.misMatch());
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.misMatch().getStatus());

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.migrating());
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.migrating().getStatus());
  }

  private static GetDataRequest request(String dataCenter, String dataInfoId) {
    return new GetDataRequest(ServerEnv.PROCESS_ID, dataInfoId, dataCenter, 10);
  }
}
