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
package com.alipay.sofa.registry.server.session.push;

import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.cache.Sizer;
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FirePushServiceTest {
  private String zone = "testZone";
  private String dataId = "testDataId";

  @Test
  public void testFire() {
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    FirePushService svc = new FirePushService(config);
    svc.pushSwitchService = TestUtils.newPushSwitchService(config);
    svc.sessionInterests = Mockito.mock(Interests.class);
    svc.pushProcessor = Mockito.mock(PushProcessor.class);
    svc.circuitBreakerService = Mockito.mock(CircuitBreakerService.class);
    svc.dataCenterMetadataCache = TestUtils.newDataCenterMetaCache(config);

    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, "testDataNode", System.currentTimeMillis());
    Assert.assertFalse(svc.fireOnChange("testDataId", ctx));
    svc.changeProcessor = Mockito.mock(ChangeProcessor.class);

    Assert.assertTrue(svc.fireOnChange("testDataId", ctx));
    Mockito.verify(svc.changeProcessor, Mockito.times(1))
        .fireChange(Mockito.anyString(), Mockito.anyObject(), Mockito.anyObject());

    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    svc.pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), true);
    svc.fireOnPushEmpty(subscriber, "testDc");
    Mockito.verify(svc.pushProcessor, Mockito.times(0))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    svc.pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);

    svc.fireOnPushEmpty(subscriber, "testDc");
    Mockito.verify(svc.pushProcessor, Mockito.times(1))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    Assert.assertFalse(svc.fireOnDatum(null, null));
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());
    Assert.assertTrue(svc.fireOnDatum(datum, null));
    // no sub
    Mockito.verify(svc.pushProcessor, Mockito.times(1))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    // registerFetchExecutor is null
    Assert.assertFalse(svc.fireOnRegister(subscriber));
    svc.init();
    Assert.assertTrue(svc.fireOnRegister(subscriber));
  }

  @Test
  public void testPushEmpty() {
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    FirePushService svc = new FirePushService(config);
    svc.pushSwitchService = Mockito.mock(PushSwitchService.class);
    svc.sessionInterests = Mockito.mock(Interests.class);
    svc.circuitBreakerService = Mockito.mock(CircuitBreakerService.class);
    svc.changeProcessor = Mockito.mock(ChangeProcessor.class);
    svc.pushProcessor = Mockito.mock(PushProcessor.class);
    svc.pushProcessor.pushSwitchService = Mockito.mock(PushSwitchService.class);

    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    when(svc.pushSwitchService.canIpPushLocal(anyString())).thenReturn(true);
    when(svc.pushSwitchService.canIpPushMulti(anyString(), anySetOf(String.class)))
        .thenReturn(true);
    when(svc.pushProcessor.pushSwitchService.canIpPushLocal(anyString())).thenReturn(true);
    when(svc.pushProcessor.pushSwitchService.canIpPushMulti(anyString(), anySetOf(String.class)))
        .thenReturn(true);

    Assert.assertTrue(svc.fireOnPushEmpty(subscriber, "testDc", 1L));

    svc.fireOnPushEmpty(subscriber, "testDc", System.currentTimeMillis());
    Assert.assertEquals(subscriber.markPushEmpty("testDc", System.currentTimeMillis()), 1L);
  }

  @Test
  public void testHandleFireOnWatchException() {
    Watcher watcher = TestUtils.newWatcher(dataId);
    FirePushService.handleFireOnWatchException(watcher, new Exception());
    FirePushService.handleFireOnWatchException(watcher, new FastRejectedExecutionException("test"));
  }

  private FirePushService mockFirePushService() {
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    FirePushService svc = new FirePushService(config);
    svc.sessionInterests = Mockito.mock(Interests.class);
    svc.pushProcessor = Mockito.mock(PushProcessor.class);
    svc.sessionDatumCacheService = Mockito.mock(CacheService.class);
    svc.pushSwitchService = Mockito.mock(PushSwitchService.class);
    svc.circuitBreakerService = Mockito.mock(CircuitBreakerService.class);
    svc.dataCenterMetadataCache = Mockito.mock(DataCenterMetadataCache.class);
    return svc;
  }

  @Test
  public void testExecuteOnChange() {
    FirePushService svc = mockFirePushService();

    final long now = System.currentTimeMillis();
    // datum is null
    TriggerPushContext ctx = new TriggerPushContext("testDc", 100, "testDataNode", now);
    Assert.assertFalse(svc.doExecuteOnChange("testDataId", ctx));

    MultiSubDatum datum =
        TestUtils.newMultiSubDatum("testDc", "testDataId", 200, Collections.emptyList());

    // get the datum
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    Value v = new Value((Sizer) datum);
    when(svc.sessionDatumCacheService.getValueIfPresent(Mockito.anyObject())).thenReturn(v);
    when(svc.sessionInterests.getDatas(Mockito.anyObject()))
        .thenReturn(Collections.singletonList(subscriber));
    svc.pushSwitchService = TestUtils.newPushSwitchService("testDc");

    svc.pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);
    svc.circuitBreakerService = Mockito.mock(CircuitBreakerService.class);
    Assert.assertTrue(svc.doExecuteOnChange("testDataId", ctx));
    Mockito.verify(svc.pushProcessor, Mockito.times(1))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    // get datum is old
    datum = TestUtils.newMultiSubDatum("testDc", "testDataId", 80, Collections.emptyList());
    v = new Value((Sizer) datum);
    when(svc.sessionDatumCacheService.getValueIfPresent(Mockito.anyObject())).thenReturn(v);
    when(svc.sessionDatumCacheService.getValue(Mockito.anyObject())).thenReturn(v);
    Assert.assertFalse(svc.doExecuteOnChange("testDataId", ctx));
  }

  @Test
  public void testChangeHandler() {
    final long now = System.currentTimeMillis();
    TriggerPushContext ctx = new TriggerPushContext("testDc", 100, "testDataNode", now);
    FirePushService svc = mockFirePushService();
    when(svc.sessionDatumCacheService.getValue(Mockito.anyObject()))
        .thenThrow(new RuntimeException());
    Assert.assertFalse(svc.changeHandler.onChange("testDataId", ctx));
    MultiSubDatum datum =
        TestUtils.newMultiSubDatum("testDc", "testDataId", 200, Collections.emptyList());
    Value v = new Value((Sizer) datum);
    when(svc.sessionDatumCacheService.getValueIfPresent(Mockito.anyObject())).thenReturn(v);
    Assert.assertTrue(svc.changeHandler.onChange("testDataId", ctx));
  }
}
