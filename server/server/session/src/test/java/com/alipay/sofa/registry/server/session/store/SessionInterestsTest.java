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

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SessionInterestsTest extends AbstractSessionServerTestBase {

  private SessionInterests interests = new SessionInterests();

  @Before
  public void beforeSessionInterestsTest() {
    interests.setSessionServerConfig(sessionServerConfig);
  }

  @Test
  public void testAdd() {
    Subscriber subscriber1 = randomSubscriber();
    subscriber1.setVersion(0);
    Subscriber subscriber2 = randomSubscriber();
    subscriber2.setVersion(1);
    Assert.assertNotNull(subscriber1.getDataInfoId());
    Assert.assertTrue(interests.add(subscriber1));
    Assert.assertTrue(interests.add(subscriber2));
  }

  @Test
  public void testCheckInterestVersion() {
    Map<String, Map<String, DatumVersion>> map =
        interests.selectSubscribers(Collections.singleton(getDc())).getVersions();
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(0, map.get(getDc()).size());

    Assert.assertSame(
        Interests.InterestVersionCheck.NoSub,
        interests.checkInterestVersion(getDc(), randomSubscriber().getDataInfoId(), 1L));
    String dataInfo = randomString(10);
    String instanceId = randomString(10);
    Subscriber subscriber = randomSubscriber(dataInfo, instanceId);
    interests.add(subscriber);
    Assert.assertEquals(
        Interests.InterestVersionCheck.Interested,
        interests.checkInterestVersion(
            getDc(),
            DataInfo.toDataInfoId(dataInfo, instanceId, "default-group"),
            System.currentTimeMillis() + 100));

    Assert.assertTrue(
        subscriber.checkAndUpdateCtx(
            Collections.singletonMap(getDc(), 100L), Collections.singletonMap(getDc(), 10)));
    Assert.assertFalse(subscriber.needPushEmpty(getDc()));
    subscriber.markPushEmpty(getDc(), 100);
    Assert.assertTrue(subscriber.needPushEmpty(getDc()));
    Assert.assertFalse(subscriber.needPushEmpty(getDc() + "1"));
    Assert.assertEquals(
        Interests.InterestVersionCheck.Obsolete,
        interests.checkInterestVersion(
            getDc(), DataInfo.toDataInfoId(dataInfo, instanceId, "default-group"), 80));

    Subscriber subscriber2 = randomSubscriber(dataInfo, instanceId);
    interests.add(subscriber2);

    Collection<Subscriber> dataList = interests.getDataList();
    for (Subscriber s : dataList) {
      if (s == subscriber2) {
        Assert.assertTrue(!s.hasPushed());
      }
    }

    subscriber2.checkAndUpdateCtx(
        Collections.singletonMap(getDc(), 80L), Collections.singletonMap(getDc(), 20));

    // get sub2.dc1
    map = interests.selectSubscribers(Collections.singleton(getDc() + "1")).getVersions();
    Assert.assertEquals(map.size(), 1);
    Map<String, DatumVersion> versionMap = map.get(getDc() + "1");
    Assert.assertEquals(versionMap.size(), 0);

    // get sub2
    map = interests.selectSubscribers(Collections.singleton(getDc())).getVersions();
    Assert.assertEquals(map.size(), 1);
    versionMap = map.get(getDc());
    Assert.assertEquals(versionMap.size(), 1);
    Assert.assertEquals(versionMap.get(subscriber.getDataInfoId()).getValue(), 80);

    // get multi sub2.dc1
    subscriber2.setAcceptMulti(true);
    map = interests.selectSubscribers(Collections.singleton(getDc() + "1")).getVersions();
    Assert.assertEquals(map.size(), 1);
    versionMap = map.get(getDc() + "1");
    Assert.assertEquals(versionMap.size(), 1);
    Assert.assertEquals(versionMap.get(subscriber.getDataInfoId()).getValue(), 0);
  }

  @Test
  public void testFilterIPs() {
    Assert.assertEquals(0, interests.filterIPs("", 0).size());
    Subscriber subscriber = randomSubscriber();
    interests.add(subscriber);
    Assert.assertEquals(0, interests.filterIPs("", 0).size());
    Assert.assertEquals(1, interests.filterIPs(subscriber.getGroup(), 0).size());
    Assert.assertEquals(
        0, interests.filterIPs(subscriber.getGroup(), 0).get(subscriber.getDataInfoId()).size());
    Assert.assertEquals(
        1, interests.filterIPs(subscriber.getGroup(), 1).get(subscriber.getDataInfoId()).size());
    Assert.assertEquals(
        0, interests.filterIPs(subscriber.getGroup(), -1).get(subscriber.getDataInfoId()).size());
    Assert.assertEquals(
        1, interests.filterIPs(subscriber.getGroup(), 100).get(subscriber.getDataInfoId()).size());
  }
}
