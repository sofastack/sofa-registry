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

import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Test;

public class PushDataGeneratorTest {
  private String zone = "testZone";

  @Test
  public void testException() {
    PushDataGenerator generator = new PushDataGenerator();
    generator.sessionServerConfig = TestUtils.newSessionConfig("testDc", zone);
    Map<String, Subscriber> subscriberMap = Maps.newHashMap();
    Subscriber sub1 = TestUtils.newZoneSubscriber(zone);
    Subscriber sub2 = TestUtils.newZoneSubscriber(zone);
    sub2.setScope(ScopeEnum.dataCenter);
    subscriberMap.put(sub1.getRegisterId(), sub1);
    subscriberMap.put(sub2.getRegisterId(), sub2);

    TestUtils.assertRunException(
        RuntimeException.class, () -> generator.createPushData(null, subscriberMap));

    sub2.setScope(ScopeEnum.zone);
    sub2.setClientVersion(BaseInfo.ClientVersion.MProtocolpackage);
    TestUtils.assertRunException(
        IllegalArgumentException.class, () -> generator.createPushData(null, subscriberMap));

    sub2.setClientVersion(BaseInfo.ClientVersion.StoreData);
    TestUtils.assertRunException(
        NullPointerException.class, () -> generator.createPushData(null, subscriberMap));
  }

  @Test
  public void test() {
    PushDataGenerator generator = new PushDataGenerator();
    generator.sessionServerConfig = TestUtils.newSessionConfig("testDc", zone);
    Map<String, Subscriber> subscriberMap = Maps.newHashMap();
    Subscriber sub1 = TestUtils.newZoneSubscriber(zone);
    Subscriber sub2 = TestUtils.newZoneSubscriber(zone);
    subscriberMap.put(sub1.getRegisterId(), sub1);
    subscriberMap.put(sub2.getRegisterId(), sub2);
    SubPublisher pub = TestUtils.newSubPublisher(10, 20, "TESTZONE");
    SubPublisher pub2 = TestUtils.newSubPublisher(10, 20, "TESTZONE");
    List<SubPublisher> list = Lists.newArrayList(pub, pub2);
    SubDatum subDatum = TestUtils.newSubDatum("testDataId", 200, list);
    PushData<ReceivedData> pushData = generator.createPushData(subDatum, subscriberMap);
    ReceivedData receivedData = pushData.getPayload();
    Assert.assertEquals(receivedData.getVersion().longValue(), subDatum.getVersion());
    Assert.assertEquals(
        Sets.newHashSet(receivedData.getSubscriberRegistIds()), subscriberMap.keySet());
    Assert.assertEquals(2, pushData.getDataCount());
    Assert.assertEquals(2, receivedData.getData().values().stream().mapToInt(List::size).sum());
  }
}
