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
package com.alipay.sofa.registry.test.resource.meta;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.*;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.sessionserver.GrayOpenPushSwitchRequest;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.client.Entity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class StopPushDataSwitchTest extends BaseIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StopPushDataSwitchTest.class);

  @Test
  public void testStopPushDataSwitch() throws Exception {
    // stop push
    ParaCheckUtil.checkNotNull(metaChannel, "metaChannel");
    assertTrue(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/open")
            .request(APPLICATION_JSON)
            .get(Result.class)
            .isSuccess());
    Thread.sleep(2000L);

    AtomicReference<String> dataIdResult = new AtomicReference<>();
    AtomicReference<UserData> userDataResult = new AtomicReference<>();

    // register Publisher & Subscriber, Subscriber get no data
    String dataId = "test-dataId-" + System.currentTimeMillis();
    String value = "test stop publish data switch";

    waitConditionUntilTimeOut(BaseIntegrationTest::isClosePush, 6000);

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    SubscriberRegistration subReg =
        new SubscriberRegistration(
            dataId,
            (dataIdOb, data) -> {
              LOGGER.info("sub:" + data);
              dataIdResult.set(dataIdOb);
              userDataResult.set(data);
            });
    subReg.setScopeEnum(ScopeEnum.dataCenter);
    registryClient1.register(subReg);
    Thread.sleep(3000L);
    assertNull(dataIdResult.get());

    // close stop push switch
    assertTrue(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/close")
            .request(APPLICATION_JSON)
            .get(Result.class)
            .isSuccess());

    waitConditionUntilTimeOut(() -> dataIdResult.get() != null, 6000);

    // Subscriber get data, test data
    assertEquals(dataId, dataIdResult.get());
    assertEquals(LOCAL_REGION, userDataResult.get().getLocalZone());
    assertEquals(1, userDataResult.get().getZoneData().size());
    assertEquals(1, userDataResult.get().getZoneData().values().size());
    assertEquals(true, userDataResult.get().getZoneData().containsKey(LOCAL_REGION));
    assertEquals(1, userDataResult.get().getZoneData().get(LOCAL_REGION).size());
    assertEquals(value, userDataResult.get().getZoneData().get(LOCAL_REGION).get(0));

    // unregister Publisher & Subscriber
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
  }

  @Test
  public void testGrayOpenPushSwitch() throws Exception {
    ParaCheckUtil.checkNotNull(metaChannel, "metaChannel");

    // stop push
    assertTrue(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/open")
            .request(APPLICATION_JSON)
            .get(Result.class)
            .isSuccess());
    Thread.sleep(2000L);
    String localAddress = NetUtil.getLocalAddress().getHostAddress();
    String otherAddress = "127.0.0.2";

    GrayOpenPushSwitchRequest req = new GrayOpenPushSwitchRequest();
    req.setIps(Arrays.asList(otherAddress));
    Assert.assertEquals(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/grayOpen")
            .request(APPLICATION_JSON)
            .post(Entity.json(JsonUtils.getJacksonObjectMapper().writeValueAsString(req)))
            .getStatus(),
        200);
    Thread.sleep(2000L);

    AtomicReference<String> dataIdResult = new AtomicReference<>();
    AtomicReference<UserData> userDataResult = new AtomicReference<>();

    // register Publisher & Subscriber, Subscriber get no data
    String dataId = "test-dataId-" + System.currentTimeMillis();
    String value = "test stop publish data switch";

    waitConditionUntilTimeOut(pushSwitchService.getFetchStopPushService()::isStopPushSwitch, 6000);
    waitConditionUntilTimeOut(() -> pushSwitchService.canIpPushLocal(otherAddress), 6000);
    Assert.assertFalse(pushSwitchService.canIpPushLocal(localAddress));

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    SubscriberRegistration subReg =
        new SubscriberRegistration(
            dataId,
            (dataIdOb, data) -> {
              LOGGER.info("sub:" + data);
              dataIdResult.set(dataIdOb);
              userDataResult.set(data);
            });
    subReg.setScopeEnum(ScopeEnum.dataCenter);
    registryClient1.register(subReg);
    Thread.sleep(6000L);
    assertNull(dataIdResult.get());

    req.setIps(Arrays.asList(localAddress));
    Assert.assertEquals(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/grayOpen")
            .request(APPLICATION_JSON)
            .post(Entity.json(JsonUtils.getJacksonObjectMapper().writeValueAsString(req)))
            .getStatus(),
        200);

    System.out.println("after gray open......");
    waitConditionUntilTimeOut(() -> dataIdResult.get() != null, 6000);

    // Subscriber get data, test data
    assertEquals(dataId, dataIdResult.get());
    assertEquals(LOCAL_REGION, userDataResult.get().getLocalZone());
    assertEquals(1, userDataResult.get().getZoneData().size());
    assertEquals(1, userDataResult.get().getZoneData().values().size());
    assertEquals(true, userDataResult.get().getZoneData().containsKey(LOCAL_REGION));
    assertEquals(1, userDataResult.get().getZoneData().get(LOCAL_REGION).size());
    assertEquals(value, userDataResult.get().getZoneData().get(LOCAL_REGION).get(0));

    assertTrue(
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/close")
            .request(APPLICATION_JSON)
            .get(Result.class)
            .isSuccess());

    waitConditionUntilTimeOut(StopPushDataSwitchTest::isOpenPush, 6000);
    // unregister Publisher & Subscriber
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
  }

  @Test
  public void testShutdownSwitch() throws Exception {
    // open stop push switch
    ParaCheckUtil.checkNotNull(metaChannel, "metaChannel");
    Map map =
        getMetaChannel()
            .getWebTarget()
            .path("/shutdown/query")
            .request(APPLICATION_JSON)
            .get(Map.class);
    Assert.assertFalse(map.isEmpty());
  }
}
