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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.shared.TestUtils;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaServerServiceTest {

  @Test
  public void testHandleHeartbeatResp() {
    MockServerService mockServerService = new MockServerService();
    mockServerService.setMetaServerManager(Mockito.mock(MetaServerManager.class));

    TestUtils.assertRunException(
        RuntimeException.class,
        () -> mockServerService.handleHeartbeatFailed("test", new IllegalArgumentException()));
    Assert.assertEquals(mockServerService.renewFailCounter.getAndSet(0), 1);

    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.handleHeartbeatResponse(null));
    Mockito.verify(mockServerService.metaServerManager, Mockito.times(0))
        .refresh(Mockito.anyObject());

    // false and data is null
    TestUtils.assertRunException(
        RuntimeException.class,
        () -> mockServerService.handleHeartbeatResponse(new GenericResponse<>()));
    Mockito.verify(mockServerService.metaServerManager, Mockito.times(0))
        .refresh(Mockito.anyObject());

    // not leader
    GenericResponse<BaseHeartBeatResponse> resp = new GenericResponse<>();
    BaseHeartBeatResponse heartBeatResponse =
        new BaseHeartBeatResponse(
            false, VersionedList.EMPTY, null, VersionedList.EMPTY, "test", 100);
    resp.setData(heartBeatResponse);
    TestUtils.assertRunException(
        RuntimeException.class, () -> mockServerService.handleHeartbeatResponse(resp));
    Mockito.verify(mockServerService.metaServerManager, Mockito.times(1))
        .refresh(Mockito.anyObject());

    // is leader and false
    heartBeatResponse =
        new BaseHeartBeatResponse(
            true,
            VersionedList.EMPTY,
            new SlotTable(10, Collections.emptyList()),
            VersionedList.EMPTY,
            "test",
            100);
    resp.setData(heartBeatResponse);
    mockServerService.handleHeartbeatResponse(resp);
    Mockito.verify(mockServerService.metaServerManager, Mockito.times(1))
        .refresh(Mockito.anyObject());

    // is leader and true
    mockServerService.renewFailCounter.incrementAndGet();
    resp.setSuccess(true);
    mockServerService.handleHeartbeatResponse(resp);
    Mockito.verify(mockServerService.metaServerManager, Mockito.times(2))
        .refresh(Mockito.anyObject());
    Assert.assertEquals(mockServerService.renewFailCounter.get(), 0);
  }

  private static final class MockServerService
      extends AbstractMetaServerService<BaseHeartBeatResponse> {

    @Override
    public int getRenewIntervalSecs() {
      return 10000;
    }

    @Override
    protected void handleRenewResult(BaseHeartBeatResponse result) {}

    @Override
    protected HeartbeatRequest createRequest() {
      return null;
    }

    @Override
    protected long getCurrentSlotTableEpoch() {
      return 0;
    }
  }
}
