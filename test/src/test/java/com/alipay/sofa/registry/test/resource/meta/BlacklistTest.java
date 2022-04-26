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
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistConstants;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.util.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author shangyu.wh
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class BlacklistTest extends BaseIntegrationTest {

  @Test
  public void testBlacklistUpdate() throws Exception {

    Map<String, Map<String, Set<String>>> map = new HashMap<>();
    Set<String> set1 = new HashSet<>();
    Set<String> set2 = new HashSet<>();
    set1.add("193.165.0.1");
    set1.add("193.165.0.2");
    set2.add("193.165.0.3");
    set2.add("193.165.0.4");

    Map<String, Set<String>> map1 = Maps.newHashMap();
    map1.put(BlacklistConstants.IP_FULL, set1);

    Map<String, Set<String>> map2 = Maps.newHashMap();
    map2.put(BlacklistConstants.IP_FULL, set2);

    map.put(BlacklistConstants.FORBIDDEN_PUB, map1);
    map.put(BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX, map2);

    ObjectMapper mapper = new ObjectMapper();

    Result response =
        getMetaChannel()
            .getWebTarget()
            .path("blacklist/update")
            .request()
            .post(
                Entity.entity(mapper.writeValueAsString(map), MediaType.APPLICATION_JSON),
                Result.class);

    assertTrue(response.isSuccess());
  }

  @Test
  public void testBlacklistUpdatePub() throws Exception {

    Map<String, Map<String, Set<String>>> map = new HashMap<>();
    Set<String> set1 = new HashSet<>();
    Set<String> set2 = new HashSet<>();
    String local = NetUtil.getLocalAddress().getHostAddress();
    set1.add(local);
    set2.add(local);

    Map<String, Set<String>> map1 = Maps.newHashMap();
    map1.put(BlacklistConstants.IP_FULL, set1);

    // Map<String, Set<String>> map2 = Maps.newHashMap();
    // map2.put(BlacklistConstants.IP_FULL, set2);

    map.put(BlacklistConstants.FORBIDDEN_PUB, map1);

    ObjectMapper mapper = new ObjectMapper();

    Result response =
        getMetaChannel()
            .getWebTarget()
            .path("blacklist/update")
            .request()
            .post(
                Entity.entity(mapper.writeValueAsString(map), MediaType.APPLICATION_JSON),
                Result.class);

    assertTrue(response.isSuccess());

    // wait for new list meta dispatch to session
    Thread.sleep(5000L);

    String dataId = "test-dataId-blacklist-" + System.currentTimeMillis();
    String value = "test blacklist";

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    MySubscriberDataObserver observer = new MySubscriberDataObserver();
    SubscriberRegistration subReg = new SubscriberRegistration(dataId, observer);
    subReg.setScopeEnum(ScopeEnum.dataCenter);

    registryClient1.register(subReg);

    Thread.sleep(3000L);
    assertEquals(dataId, observer.dataId);
    assertEquals(LOCAL_REGION, observer.userData.getLocalZone());

    Map<String, List<Publisher>> publisherMap =
        sessionChannel
            .getWebTarget()
            .path("digest/pub/data/query")
            .queryParam(
                "dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<Publisher>>>() {});
    assertEquals(0, publisherMap.size());

    Map<String, List<Subscriber>> subscriberMap =
        sessionChannel
            .getWebTarget()
            .path("digest/sub/data/query")
            .queryParam(
                "dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<Subscriber>>>() {});
    assertEquals(1, subscriberMap.size());
    assertEquals(1, subscriberMap.get("SUB").size());
    assertEquals(dataId, subscriberMap.get("SUB").get(0).getDataId());
  }

  @Test
  public void testBlacklistUpdateSub() throws Exception {

    Map<String, Map<String, Set<String>>> map = new HashMap<>();
    Set<String> set1 = new HashSet<>();
    Set<String> set2 = new HashSet<>();
    String local = NetUtil.getLocalAddress().getHostAddress();
    set1.add(local);
    set2.add(local);

    Map<String, Set<String>> map1 = Maps.newHashMap();
    map1.put(BlacklistConstants.IP_FULL, set1);

    Map<String, Set<String>> map2 = Maps.newHashMap();
    map2.put(BlacklistConstants.IP_FULL, set2);

    map.put(BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX, map2);

    ObjectMapper mapper = new ObjectMapper();
    Result response =
        getMetaChannel()
            .getWebTarget()
            .path("blacklist/update")
            .request()
            .post(
                Entity.entity(mapper.writeValueAsString(map), MediaType.APPLICATION_JSON),
                Result.class);

    assertTrue(response.isSuccess());

    // wait for new list meta dispatch to session
    Thread.sleep(3000L);

    String dataId = "test-dataId-blacklist2-" + System.currentTimeMillis();
    String value = "test blacklist2";

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);
    MySubscriberDataObserver observer = new MySubscriberDataObserver();
    SubscriberRegistration subReg = new SubscriberRegistration(dataId, observer);
    subReg.setScopeEnum(ScopeEnum.dataCenter);

    registryClient1.register(subReg);

    Thread.sleep(3000L);
    assertEquals(dataId, observer.dataId);
    assertEquals(LOCAL_REGION, observer.userData.getLocalZone());

    Map<String, List<Publisher>> publisherMap =
        sessionChannel
            .getWebTarget()
            .path("digest/pub/data/query")
            .queryParam(
                "dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<Publisher>>>() {});
    assertEquals(1, publisherMap.size());
    assertEquals(1, publisherMap.get("PUB").size());
    assertEquals(dataId, publisherMap.get("PUB").get(0).getDataId());
    assertEquals(
        value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0).getBytes()));

    Map<String, List<Subscriber>> subscriberMap =
        sessionChannel
            .getWebTarget()
            .path("digest/sub/data/query")
            .queryParam(
                "dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<Subscriber>>>() {});
    // default setting, black list not forbit the sub
    assertEquals(1, subscriberMap.size());
  }

  @AfterClass
  public static void afterClass() {
    registryClient1.unregister("test-dataId-blacklist", DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister("test-dataId-blacklist", DEFAULT_GROUP, RegistryType.PUBLISHER);
    registryClient1.unregister("test-dataId-blacklist2", DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister("test-dataId-blacklist2", DEFAULT_GROUP, RegistryType.PUBLISHER);
  }
}
