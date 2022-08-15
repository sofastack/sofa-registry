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
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.dataserver.GetDataVersionRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class GetDataVersionsHandlerTest {
  @Test
  public void testCheckParam() {
    GetDataVersionsHandler handler = newHandler();
    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          handler.checkParam(request(Collections.EMPTY_MAP, -1));
        });
    handler.checkParam(request(Collections.EMPTY_MAP, 10));
  }

  private GetDataVersionsHandler newHandler() {
    GetDataVersionsHandler handler = new GetDataVersionsHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), GetDataVersionRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.SESSION);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    SlotAccessGenericResponse failed =
        (SlotAccessGenericResponse) handler.buildFailedResponse("msg");
    Assert.assertFalse(failed.isSuccess());
    handler.sessionLeaseManager = new SessionLeaseManager();
    SlotAccessorDelegate slotManager = mock(SlotAccessorDelegate.class);
    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);

    handler
        .setSlotAccessor(slotManager)
        .setDatumStorageDelegate(datumStorageDelegate)
        .setDataChangeEventCenter(new DataChangeEventCenter())
        .setDataServerConfig(TestBaseUtils.newDataConfig("testDc"));

    return handler;
  }

  @Test
  public void testHandle() {

    GetDataVersionsHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    Publisher pub = TestBaseUtils.createTestPublishers(10, 1).get(0);
    DatumVersion v = new DatumVersion(DatumVersionUtil.nextId());
    GetDataVersionRequest request =
        request(Collections.singletonMap(pub.getDataInfoId() + 1, v), 10);

    // get status change
    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept(), TestBaseUtils.migrating(1, 10, 10));

    SlotAccessGenericResponse resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());

    // get not exist
    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.accept(), TestBaseUtils.accept());

    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
    Map<String, DatumVersion> ret = (Map<String, DatumVersion>) resp.getData();
    Assert.assertEquals(ret.size(), 1);
    DatumVersion retV = ret.get(pub.getDataInfoId() + 1);
    Assert.assertTrue(retV.getValue() > v.getValue());
    Assert.assertEquals(
        handler.getDatumStorageDelegate().get("testDc", pub.getDataInfoId() + 1).getPubMap().size(),
        0);

    // get less than store's version
    handler.getDatumStorageDelegate().putPublisher("testDc", pub);
    long putV = handler.getDatumStorageDelegate().get("testDc", pub.getDataInfoId()).getVersion();
    request = request(Collections.singletonMap(pub.getDataInfoId(), v), 10);
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
    ret = (Map<String, DatumVersion>) resp.getData();
    Assert.assertEquals(ret.size(), 1);
    retV = ret.get(pub.getDataInfoId());
    Assert.assertEquals(retV.getValue(), putV);

    // get more than store's version
    v = new DatumVersion(DatumVersionUtil.nextId());
    request = request(Collections.singletonMap(pub.getDataInfoId(), v), 10);
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertTrue(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.accept().getStatus());
    ret = (Map<String, DatumVersion>) resp.getData();
    Assert.assertEquals(ret.size(), 1);
    retV = ret.get(pub.getDataInfoId());
    Assert.assertTrue(retV.getValue() > putV);
    Assert.assertTrue(retV.getValue() > v.getValue());
    putV = handler.getDatumStorageDelegate().get("testDc", pub.getDataInfoId()).getVersion();
    Assert.assertEquals(retV.getValue(), putV);
  }

  @Test
  public void testHandleErrorSlotAccess() {
    GetDataVersionsHandler handler = newHandler();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 8888);

    GetDataVersionRequest request = request(Collections.emptyMap(), 1);

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

    when(handler
            .getSlotAccessorDelegate()
            .checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(TestBaseUtils.migrating());
    resp = (SlotAccessGenericResponse) handler.doHandle(channel, request);
    Assert.assertFalse(resp.isSuccess());
    Assert.assertEquals(resp.getSlotAccess().getStatus(), TestBaseUtils.migrating().getStatus());
  }

  private static GetDataVersionRequest request(Map<String, DatumVersion> versionMap, int slotId) {
    return new GetDataVersionRequest("testDc", ServerEnv.PROCESS_ID, slotId, versionMap);
  }
}
