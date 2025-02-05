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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/** */
public class SubscriberStoreTest {

  @Test
  public void testSelectSubscribers() {
    String dataInfoId = "dataInfoId";
    String registerId = "registerId";

    String dataInfoId00 = "dataInfoId00";
    String registerId00 = "registerId00";

    SessionServerConfig sessionServerConfig = Mockito.mock(SessionServerConfig.class);
    Mockito.when(sessionServerConfig.getSessionServerDataCenter()).thenReturn("localDataCenter");
    SubscriberStoreImpl subscriberStore = new SubscriberStoreImpl(sessionServerConfig);

    long time = System.currentTimeMillis();
    Subscriber subscriber = Mockito.mock(Subscriber.class);
    Mockito.when(subscriber.getDataInfoId()).thenReturn(dataInfoId);
    Mockito.when(subscriber.getId()).thenReturn(registerId);
    Mockito.when(subscriber.getRegisterId()).thenReturn(registerId);
    Mockito.when(subscriber.getScope()).thenReturn(ScopeEnum.zone).thenReturn(ScopeEnum.global);
    Mockito.when(subscriber.getVersion()).thenReturn(1L);
    Mockito.when(subscriber.getClientRegisterTimestamp()).thenReturn(time);
    Mockito.when(subscriber.getSourceAddress()).thenReturn(new URL("192.168.1.2", 9000));
    Mockito.when(subscriber.getTargetAddress()).thenReturn(new URL("127.0.0.1", 34567));

    ConnectId connectId =
        new ConnectId(
            new URL("192.168.1.2", 9000).getIpAddress(),
            new URL("192.168.1.2", 9000).getPort(),
            new URL("127.0.0.1", 34567).getIpAddress(),
            new URL("127.0.0.1", 34567).getPort());

    Mockito.when(subscriber.connectId()).thenReturn(connectId);
    Mockito.when(subscriber.isMarkedPushEmpty(Matchers.anyString())).thenReturn(true);
    Mockito.when(subscriber.needPushEmpty(Matchers.anyString())).thenReturn(true);
    subscriberStore.add(subscriber);

    Subscriber subscriber00 = new Subscriber();
    subscriber00.setDataInfoId(dataInfoId00);
    subscriber00.setRegisterId(registerId00);
    subscriber00.setScope(ScopeEnum.global);
    subscriber00.setVersion(1L);
    subscriber00.setClientRegisterTimestamp(time);
    subscriber00.setSourceAddress(new URL("192.168.1.2", 9000));
    subscriber00.setTargetAddress(new URL("127.0.0.1", 34567));

    subscriberStore.add(subscriber00);

    SessionRegistry.SelectSubscriber result =
        subscriberStore.selectSubscribers(Collections.singleton("!localDataCenter"));
    Assert.assertEquals(2, result.getVersions().size());
    Map<String, DatumVersion> map = result.getVersions().get("!localDataCenter");
    for (Map.Entry<String, DatumVersion> entry : map.entrySet()) {
      Assert.assertTrue(entry.getKey().equals(dataInfoId) || entry.getKey().equals(dataInfoId00));
      Assert.assertEquals(0, entry.getValue().getValue());
    }
    Assert.assertEquals(0, result.getToPushEmpty().get("!localDataCenter").size());

    result = subscriberStore.selectSubscribers(Collections.singleton("localDataCenter"));
    Assert.assertEquals(2, result.getVersions().size());
    map = result.getVersions().get("localDataCenter");
    for (Map.Entry<String, DatumVersion> entry : map.entrySet()) {
      Assert.assertTrue(entry.getKey().equals(dataInfoId) || entry.getKey().equals(dataInfoId00));
      Assert.assertEquals(0, entry.getValue().getValue());
    }
    Assert.assertEquals(1, result.getToPushEmpty().get("localDataCenter").size());
  }

  @Test
  public void testCheckInterestVersion() {
    String dataInfoId = "dataInfoId";
    String registerId = "registerId";

    String dataInfoId00 = "dataInfoId00";
    String registerId00 = "registerId00";

    SessionServerConfig sessionServerConfig = Mockito.mock(SessionServerConfig.class);
    Mockito.when(sessionServerConfig.getSessionServerDataCenter()).thenReturn("localDataCenter");
    SubscriberStoreImpl subscriberStore = new SubscriberStoreImpl(sessionServerConfig);

    long time = System.currentTimeMillis();
    Subscriber subscriber = Mockito.mock(Subscriber.class);
    Mockito.when(subscriber.getDataInfoId()).thenReturn(dataInfoId);
    Mockito.when(subscriber.getId()).thenReturn(registerId);
    Mockito.when(subscriber.getRegisterId()).thenReturn(registerId);
    Mockito.when(subscriber.getScope()).thenReturn(ScopeEnum.zone).thenReturn(ScopeEnum.global);
    Mockito.when(subscriber.getVersion()).thenReturn(1L);
    Mockito.when(subscriber.getClientRegisterTimestamp()).thenReturn(time);
    Mockito.when(subscriber.getSourceAddress()).thenReturn(new URL("192.168.1.2", 9000));
    Mockito.when(subscriber.getTargetAddress()).thenReturn(new URL("127.0.0.1", 34567));

    ConnectId connectId =
        new ConnectId(
            new URL("192.168.1.2", 9000).getIpAddress(),
            new URL("192.168.1.2", 9000).getPort(),
            new URL("127.0.0.1", 34567).getIpAddress(),
            new URL("127.0.0.1", 34567).getPort());

    Mockito.when(subscriber.connectId()).thenReturn(connectId);
    Mockito.when(subscriber.isMarkedPushEmpty(Matchers.anyString())).thenReturn(true);
    Mockito.when(subscriber.needPushEmpty(Matchers.anyString())).thenReturn(true);
    subscriberStore.add(subscriber);

    Subscriber subscriber00 = new Subscriber();
    subscriber00.setDataInfoId(dataInfoId00);
    subscriber00.setRegisterId(registerId00);
    subscriber00.setScope(ScopeEnum.global);
    subscriber00.setVersion(1L);
    subscriber00.setClientRegisterTimestamp(time);
    subscriber00.setSourceAddress(new URL("192.168.1.2", 9000));
    subscriber00.setTargetAddress(new URL("127.0.0.1", 34567));

    subscriberStore.add(subscriber00);

    SubscriberStore.InterestVersionCheck check =
        subscriberStore.checkInterestVersion("localDataCenter", dataInfoId00 + "1", 0);
    Assert.assertEquals(SubscriberStore.InterestVersionCheck.NoSub, check);

    check = subscriberStore.checkInterestVersion("localDataCenter", dataInfoId, 0);
    Assert.assertEquals(SubscriberStore.InterestVersionCheck.Obsolete, check);

    Mockito.when(subscriber.checkVersion(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    check = subscriberStore.checkInterestVersion("localDataCenter", dataInfoId, 0);
    Assert.assertEquals(SubscriberStore.InterestVersionCheck.Interested, check);
  }
}
