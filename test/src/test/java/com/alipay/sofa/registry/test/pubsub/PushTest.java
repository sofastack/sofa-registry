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
package com.alipay.sofa.registry.test.pubsub;

import static org.junit.Assert.assertEquals;

import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author xiaojian.xj
 * @version : PushTest.java, v 0.1 2021年08月11日 11:25 xiaojian.xj Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class PushTest extends BaseIntegrationTest {

  private String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OPEN_STR = "2.2.2.2;3.3.3.3;" + localAddress;

  /**
   * Publisher test.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void pushTest() throws InterruptedException {
    LOGGER.info("--------------------------------splitter-----------------------------");
    String dataId = "test-dataId-pushTest-" + System.nanoTime();
    String value = "test push";

    PublisherRegistration registration = new PublisherRegistration(dataId);
    Publisher publisher = registryClient1.register(registration, value);
    Thread.sleep(4000L);

    value = "value111";
    publisher.republish(value);
    Thread.sleep(4000L);

    MySubscriberDataObserver observer = new MySubscriberDataObserver();
    SubscriberRegistration subReg = new SubscriberRegistration(dataId, observer);
    subReg.setScopeEnum(ScopeEnum.dataCenter);

    registryClient1.register(subReg);

    Thread.sleep(5000L);
    assertEquals(dataId, observer.dataId);
    assertEquals(LOCAL_REGION, observer.userData.getLocalZone());
    assertEquals(1, observer.userData.getZoneData().size());
    assertEquals(1, observer.userData.getZoneData().values().size());
    assertEquals(true, observer.userData.getZoneData().containsKey(LOCAL_REGION));
    assertEquals(1, observer.userData.getZoneData().get(LOCAL_REGION).size());
    assertEquals(value, observer.userData.getZoneData().get(LOCAL_REGION).get(0));

    /** client open */
    CommonResponse response = sessionClientManagerResource.clientOn(CLIENT_OPEN_STR);
    Assert.assertTrue(response.isSuccess());

    Thread.sleep(5000L);
  }
}
