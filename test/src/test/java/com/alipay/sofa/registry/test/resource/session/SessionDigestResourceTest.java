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
package com.alipay.sofa.registry.test.resource.session;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdResp;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class SessionDigestResourceTest extends BaseIntegrationTest {
  private static final String dataId = "test-dataId-" + System.nanoTime();
  private static final String value = "SessionDigestResourceTest";
  private static final MySubscriberDataObserver observer = new MySubscriberDataObserver();

  protected static List<AppRevision> appRevisionList;

  protected AppRevisionHandlerStrategy appRevisionHandlerStrategy;

  protected MetadataCacheRegistry metadataCacheRegistry;

  @Before
  public void before() {
    appRevisionHandlerStrategy =
        sessionApplicationContext.getBean(
            "appRevisionHandlerStrategy", AppRevisionHandlerStrategy.class);

    metadataCacheRegistry =
        sessionApplicationContext.getBean("metadataCacheRegistry", MetadataCacheRegistry.class);
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    clientOff();

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    SubscriberRegistration subReg = new SubscriberRegistration(dataId, observer);
    subReg.setScopeEnum(ScopeEnum.dataCenter);
    registryClient1.register(subReg);
    Thread.sleep(3000L);

    assertEquals(dataId, observer.dataId);
    assertEquals(LOCAL_REGION, observer.userData.getLocalZone());
    assertEquals(1, observer.userData.getZoneData().size());
    assertEquals(1, observer.userData.getZoneData().values().size());
    assertEquals(true, observer.userData.getZoneData().containsKey(LOCAL_REGION));
    assertEquals(1, observer.userData.getZoneData().get(LOCAL_REGION).size());
    assertEquals(value, observer.userData.getZoneData().get(LOCAL_REGION).get(0));

    appRevisionList = new ArrayList<>();
    for (int i = 1; i <= 1; i++) {
      long l = System.currentTimeMillis();
      String suffix = l + "-" + i;

      String appname = "foo" + suffix;
      String revision = "1111" + suffix;

      AppRevision appRevision = new AppRevision();
      appRevision.setAppName(appname);
      appRevision.setRevision(revision);
      appRevision.setClientVersion("1.0");

      Map<String, List<String>> baseParams = Maps.newHashMap();
      baseParams.put(
          "metaBaseParam1",
          new ArrayList<String>() {
            {
              add("metaBaseValue1");
            }
          });
      appRevision.setBaseParams(baseParams);

      Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
      String dataInfo1 =
          DataInfo.toDataInfoId(
              "SessionDigestResourceTest-func1" + suffix,
              ValueConstants.DEFAULT_GROUP,
              ValueConstants.DEFAULT_INSTANCE_ID);
      String dataInfo2 =
          DataInfo.toDataInfoId(
              "SessionDigestResourceTest-func2" + suffix,
              ValueConstants.DEFAULT_GROUP,
              ValueConstants.DEFAULT_INSTANCE_ID);

      AppRevisionInterface inf1 = new AppRevisionInterface();
      AppRevisionInterface inf2 = new AppRevisionInterface();
      interfaceMap.put(dataInfo1, inf1);
      interfaceMap.put(dataInfo2, inf2);
      appRevision.setInterfaceMap(interfaceMap);

      inf1.setId("1");
      Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
      serviceParams1.put(
          "metaParam2",
          new ArrayList<String>() {
            {
              add("metaValue2");
            }
          });
      inf1.setServiceParams(serviceParams1);

      inf2.setId("2");
      Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
      serviceParams1.put(
          "metaParam3",
          new ArrayList<String>() {
            {
              add("metaValue3");
            }
          });
      inf1.setServiceParams(serviceParams2);

      appRevisionList.add(appRevision);
    }
  }

  @AfterClass
  public static void afterClass() {
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.SECONDS);
  }

  @Test
  public void testGetSessionDataByDataInfoId() throws Exception {
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
    assertEquals(1, subscriberMap.size());
    assertEquals(1, subscriberMap.get("SUB").size());
    assertEquals(dataId, subscriberMap.get("SUB").get(0).getDataId());

    Map map =
        sessionChannel
            .getWebTarget()
            .path("digest/all/data/query")
            .queryParam(
                "dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON)
            .get(Map.class);
    assertEquals(2, map.size());
    assertEquals(1, ((List) map.get("SUB")).size());
    assertEquals(1, ((List) map.get("PUB")).size());
    assertEquals(dataId, ((Map) ((List) map.get("SUB")).get(0)).get("dataId"));
    assertEquals(dataId, ((Map) ((List) map.get("PUB")).get(0)).get("dataId"));
    assertEquals(
        value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0).getBytes()));
  }

  @Test
  public void testetSessionDataByConnectId() throws Exception {
    List<String> connectIds = new ArrayList<>();
    connectIds.add(
        NetUtil.genHost(LOCAL_ADDRESS, getSourcePort(registryClient1))
            + ValueConstants.CONNECT_ID_SPLIT
            + NetUtil.genHost(LOCAL_ADDRESS, 9600));
    Map<String, List<Publisher>> publisherMap =
        sessionChannel
            .getWebTarget()
            .path("digest/pub/connect/query")
            .request(APPLICATION_JSON)
            .post(
                Entity.entity(connectIds, MediaType.APPLICATION_JSON),
                new GenericType<Map<String, List<Publisher>>>() {});
    assertEquals(1, publisherMap.size());
    assertEquals(1, publisherMap.get("PUB").size());
    assertEquals(dataId, publisherMap.get("PUB").get(0).getDataId());
    assertEquals(
        value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0).getBytes()));

    Map<String, List<Subscriber>> subscriberMap =
        sessionChannel
            .getWebTarget()
            .path("digest/sub/connect/query")
            .request(APPLICATION_JSON)
            .post(
                Entity.entity(connectIds, MediaType.APPLICATION_JSON),
                new GenericType<Map<String, List<Subscriber>>>() {});
    assertEquals(1, subscriberMap.size());
    assertEquals(1, subscriberMap.get("SUB").size());
    assertEquals(dataId, subscriberMap.get("SUB").get(0).getDataId());

    Map map =
        sessionChannel
            .getWebTarget()
            .path("digest/all/connect/query")
            .request(APPLICATION_JSON)
            .post(Entity.entity(connectIds, MediaType.APPLICATION_JSON), Map.class);
    assertEquals(2, map.size());
    assertEquals(1, ((List) map.get("SUB")).size());
    assertEquals(1, ((List) map.get("PUB")).size());
    assertEquals(dataId, ((Map) ((List) map.get("SUB")).get(0)).get("dataId"));
    assertEquals(dataId, ((Map) ((List) map.get("PUB")).get(0)).get("dataId"));
    assertEquals(
        value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0).getBytes()));
  }

  @Test
  public void testGetSessionDataCount() {
    String result =
        sessionChannel
            .getWebTarget()
            .path("digest/data/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    assertEquals("Subscriber count: 1, Publisher count: 1, Watcher count: 0", result);
  }

  @Test
  public void testGetPushSwitch() {
    Map result =
        sessionChannel
            .getWebTarget()
            .path("digest/pushSwitch")
            .request(APPLICATION_JSON)
            .get(Map.class);
    assertEquals(1, result.size());
    assertEquals("open", result.get("pushSwitch"));
  }

  @Test
  public void testGetDataInfoIdList() {
    Set<String> result =
        sessionChannel
            .getWebTarget()
            .path("digest/getDataInfoIdList")
            .request(APPLICATION_JSON)
            .get(Set.class);

    assertTrue(
        result.toString(),
        result.contains(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP)));
  }

  @Test
  public void testCheckSumDataInfoIdList() {
    int result =
        sessionChannel
            .getWebTarget()
            .path("digest/checkSumDataInfoIdList")
            .request(APPLICATION_JSON)
            .get(int.class);
    assertTrue(String.valueOf(result), result != 0);
  }

  @Test
  public void testQueryPubSubDataInfoIds() {
    String LOCAL_ADDRESS = NetUtil.getLocalAddress().getHostAddress();
    GenericResponse<PubSubDataInfoIdResp> response =
        sessionDigestResource.queryDetail(LOCAL_ADDRESS);
    Assert.assertTrue(response.isSuccess());
    PubSubDataInfoIdResp data = response.getData();
    String dataInfoId = DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP);
    Set<String> pubs = data.getPubDataInfoIds().get(LOCAL_ADDRESS);
    Set<String> subs = data.getSubDataInfoIds().get(LOCAL_ADDRESS);
    Assert.assertTrue(pubs.contains(dataInfoId));
    Assert.assertTrue(subs.contains(dataInfoId));
  }

  public void registerAppRevision() throws ExecutionException, InterruptedException {
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
    List<Future<RegisterResponse>> responses = new ArrayList<>();

    // register
    for (AppRevision appRevisionRegister : appRevisionList) {
      Future<RegisterResponse> response =
          fixedThreadPool.submit(
              (Callable)
                  () -> {
                    RegisterResponse result = new RegisterResponse();
                    appRevisionHandlerStrategy.handleAppRevisionRegister(
                        appRevisionRegister, result, "");
                    return result;
                  });
      responses.add(response);
    }

    for (Future<RegisterResponse> future : responses) {
      Assert.assertTrue(future.get().isSuccess());
    }

    metadataCacheRegistry.waitSynced();
  }

  @Test
  public void testGetMetadata() throws ExecutionException, InterruptedException {
    registerAppRevision();

    Set<String> revisionIds =
        sessionChannel
            .getWebTarget()
            .path("digest/metadata/allRevisionIds")
            .request(APPLICATION_JSON)
            .get(Set.class);

    Map<String, Map<String, InterfaceMapping>> mappings =
        sessionChannel
            .getWebTarget()
            .path("digest/metadata/allServiceMapping")
            .request(APPLICATION_JSON)
            .get(Map.class);

    for (AppRevision revision : appRevisionList) {
      Assert.assertTrue(revisionIds.contains(revision.getRevision()));
      for (String itf : revision.getInterfaceMap().keySet()) {
        System.out.println(itf);
        System.out.println(mappings.keySet());
        Assert.assertTrue(mappings.containsKey(itf));
      }
    }
  }
}
