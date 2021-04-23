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

import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.Value;
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
    FirePushService svc = new FirePushService();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    svc.sessionServerConfig = config;
    svc.sessionInterests = Mockito.mock(Interests.class);
    svc.pushProcessor = Mockito.mock(PushProcessor.class);

    Assert.assertFalse(svc.fireOnChange("testDc", "testDataId", 100));
    svc.changeProcessor = Mockito.mock(ChangeProcessor.class);

    Assert.assertTrue(svc.fireOnChange("testDc", "testDataId", 100));
    Mockito.verify(svc.changeProcessor, Mockito.times(1))
        .fireChange(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(), Mockito.anyLong());

    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    config.setStopPushSwitch(true);
    svc.fireOnPushEmpty(subscriber);
    Mockito.verify(svc.pushProcessor, Mockito.times(0))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    config.setStopPushSwitch(false);

    svc.fireOnPushEmpty(subscriber);
    Mockito.verify(svc.pushProcessor, Mockito.times(1))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    Assert.assertFalse(svc.fireOnDatum(null));
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());
    Assert.assertTrue(svc.fireOnDatum(datum));
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
  public void testHandleFireOnRegisterException() {
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    FirePushService.handleFireOnRegisterException(subscriber, new Exception());
    FirePushService.handleFireOnRegisterException(
        subscriber, new FastRejectedExecutionException("test"));
  }

  private FirePushService mockFirePushService() {
    FirePushService svc = new FirePushService();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    svc.sessionServerConfig = config;
    svc.sessionInterests = Mockito.mock(Interests.class);
    svc.pushProcessor = Mockito.mock(PushProcessor.class);
    svc.sessionCacheService = Mockito.mock(CacheService.class);
    return svc;
  }

  @Test
  public void testExecuteOnChange() {
    FirePushService svc = mockFirePushService();

    // datum is null
    Assert.assertFalse(svc.doExecuteOnChange("testDc", "testDataId", 100));
    SubDatum datum = TestUtils.newSubDatum("testDataId", 200, Collections.emptyList());

    // get the datum
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    Mockito.when(svc.sessionCacheService.getValueIfPresent(Mockito.anyObject()))
        .thenReturn(new Value(datum));
    Mockito.when(svc.sessionInterests.getDatas(Mockito.anyObject()))
        .thenReturn(Collections.singletonList(subscriber));
    Assert.assertTrue(svc.doExecuteOnChange("testDc", "testDataId", 100));
    Mockito.verify(svc.pushProcessor, Mockito.times(1))
        .firePush(
            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    // get datum is old
    datum = TestUtils.newSubDatum("testDataId", 80, Collections.emptyList());
    Mockito.when(svc.sessionCacheService.getValueIfPresent(Mockito.anyObject()))
        .thenReturn(new Value(datum));
    Mockito.when(svc.sessionCacheService.getValue(Mockito.anyObject()))
        .thenReturn(new Value(datum));
    Assert.assertFalse(svc.doExecuteOnChange("testDc", "testDataId", 100));
  }

  @Test
  public void testChangeHandler() {
    FirePushService svc = mockFirePushService();
    Mockito.when(svc.sessionCacheService.getValue(Mockito.anyObject()))
        .thenThrow(new RuntimeException());
    Assert.assertFalse(svc.changeHandler.onChange("testDc", "testDataId", 100));
    SubDatum datum = TestUtils.newSubDatum("testDataId", 200, Collections.emptyList());
    Mockito.when(svc.sessionCacheService.getValueIfPresent(Mockito.anyObject()))
        .thenReturn(new Value(datum));
    Assert.assertTrue(svc.changeHandler.onChange("testDc", "testDataId", 100));
  }

  @Test
  public void testOnSubscriber() {
    FirePushService svc = mockFirePushService();
    Subscriber subscriber = TestUtils.newZoneSubscriber("testZone");
    subscriber.checkAndUpdateVersion("testDc", 100);
    Assert.assertFalse(svc.doExecuteOnSubscriber("testDc", subscriber));
    FirePushService.RegisterTask task = svc.new RegisterTask(subscriber);
    task.run();
  }
}
