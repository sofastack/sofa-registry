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
package com.alipay.sofa.registry.test.resource.data;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertEquals;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.data.resource.DatumParam;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author kezhu.wukz
 * @since 2020/1/22
 */
@RunWith(SpringRunner.class)
public class DatumApiResourceTest extends BaseIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatumApiResourceTest.class);

  private static String publisherRegisterId = "registerId-123456789";

  private String dataId = "test-dataId-" + System.currentTimeMillis();
  private String value = "DataDigestResourceTest";
  private String dataInfoId = DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP);

  @Before
  public void before() throws Exception {
    LOGGER.info("DatumApiResourceTest beforeCall");

    clientOff();
    Thread.sleep(2000L);

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    MySubscriberDataObserver observer = new MySubscriberDataObserver();
    SubscriberRegistration subReg = new SubscriberRegistration(dataId, observer);
    subReg.setScopeEnum(ScopeEnum.dataCenter);
    registryClient1.register(subReg);
    Thread.sleep(2000L);

    assertEquals(dataId, observer.dataId);
    assertEquals(LOCAL_REGION, observer.userData.getLocalZone());
    assertEquals(1, observer.userData.getZoneData().size());
    assertEquals(1, observer.userData.getZoneData().values().size());
    assertEquals(true, observer.userData.getZoneData().containsKey(LOCAL_REGION));
    assertEquals(1, observer.userData.getZoneData().get(LOCAL_REGION).size());
    assertEquals(value, observer.userData.getZoneData().get(LOCAL_REGION).get(0));
  }

  @After
  public void after() throws Exception {
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    //        clientOff();
  }

  @Test
  public void testException() {
    DatumParam datumParam = new DatumParam();

    {
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/get")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testException for datum/api/get:" + response);
      Assert.assertTrue(response.getMessage().contains("empty"));
    }

    {
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/delete")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testException for datum/api/delete:" + response);
      Assert.assertTrue(response.getMessage().contains("empty"));
    }
    {
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/pub/add")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testException for datum/api/pub/add:" + response);
      Assert.assertTrue(response.getMessage().contains("empty"));
    }
    {
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/pub/delete")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testException for datum/api/pub/delete:" + response);
      Assert.assertTrue(response.getMessage().contains("empty"));
    }
  }

  @Test
  public void testGet() {
    // test get
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId);
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/get")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);

      LOGGER.info("testGet:" + response);
      Assert.assertTrue(response.getMessage().contains(dataId));
    }
    // test get not found
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId + System.currentTimeMillis());
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/get")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);

      LOGGER.info("testGet:" + response);
      Assert.assertTrue(response.getMessage().contains("not found"));
    }
  }

  @Test
  public void testAddDeletePub() {
    // add
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId);
      String testZone = "testZone";
      datumParam.setPublisherCell(testZone);
      datumParam.setPublisherDataBox("test");
      datumParam.setPublisherConnectId(LOCAL_ADDRESS + ":12345_" + LOCAL_ADDRESS + ":9600");
      datumParam.setPublisherRegisterId(publisherRegisterId);
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/pub/add")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);

      LOGGER.info("testAddPub:" + response);
      Assert.assertTrue(response.getMessage(), response.getMessage().contains(dataId));
      Assert.assertTrue(response.getMessage(), response.getMessage().contains(testZone));
    }
    // delete
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId);
      String testZone = "testZone";
      datumParam.setPublisherCell(testZone);
      datumParam.setPublisherRegisterId(publisherRegisterId);
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/pub/delete")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testDeletePub:" + response);
      Assert.assertFalse(response.getMessage().contains(publisherRegisterId));
    }
  }

  @Test
  public void testDelete() {
    // add
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId);
      String testZone = "testZone";
      datumParam.setPublisherCell(testZone);
      datumParam.setPublisherDataBox("test");
      datumParam.setPublisherConnectId(LOCAL_ADDRESS + ":12345_" + LOCAL_ADDRESS + ":9600");
      datumParam.setPublisherRegisterId(publisherRegisterId);
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/pub/add")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);

      LOGGER.info("testAddPub:" + response);
      Assert.assertTrue(response.getMessage().contains(dataId));
      Assert.assertTrue(response.getMessage().contains(testZone));
    }
    // delete
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataInfoId(dataInfoId);
      CommonResponse response =
          dataChannel
              .getWebTarget()
              .path("datum/api/delete")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), CommonResponse.class);
      LOGGER.info("testDelete:" + response);
      Assert.assertFalse(response.getMessage().contains(publisherRegisterId));
    }
  }

  @Test
  public void testGetDatumVersions() {
    // test
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataCenter(LOCAL_DATACENTER);
      Map<String, Integer> response =
          dataChannel
              .getWebTarget()
              .path("datum/api/getDatumVersions")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), Map.class);

      LOGGER.info("testGetDatumVersions:" + response);
      Assert.assertTrue(response.get(dataInfoId) != null);
    }
    // test not found
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataCenter(LOCAL_DATACENTER + System.currentTimeMillis());
      Map<String, Integer> response =
          dataChannel
              .getWebTarget()
              .path("datum/api/getDatumVersions")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), Map.class);

      LOGGER.info("testGetDatumVersions:" + response);
      Assert.assertTrue(response.get(dataInfoId) == null);
    }
  }

  @Test
  public void testGetDatumVersion() {
    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataCenter(LOCAL_DATACENTER);
      datumParam.setDataInfoId(dataInfoId);
      String response =
          dataChannel
              .getWebTarget()
              .path("datum/api/getDatumVersion")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), String.class);

      LOGGER.info("testGetDatumVersions:" + response);
      Assert.assertTrue(response.contains(dataInfoId));
    }

    {
      DatumParam datumParam = new DatumParam();
      datumParam.setDataCenter(LOCAL_DATACENTER + System.currentTimeMillis());
      datumParam.setDataInfoId(dataInfoId);
      String response =
          dataChannel
              .getWebTarget()
              .path("datum/api/getDatumVersion")
              .request()
              .post(Entity.entity(datumParam, MediaType.APPLICATION_JSON), String.class);

      LOGGER.info("testGetDatumVersions:" + response);
      Assert.assertTrue(response.contains(dataInfoId));
    }
  }

  @Test
  public void testGetDatumSize() {
    DatumParam datumParam = new DatumParam();
    datumParam.setDataCenter(LOCAL_DATACENTER);
    datumParam.setDataInfoId(dataInfoId);
    Map<String, Integer> response =
        dataChannel.getWebTarget().path("datum/api/getDatumSizes").request().get(Map.class);

    LOGGER.info("testGetDatumSize:" + response);
    Assert.assertTrue(response.get(LOCAL_DATACENTER).intValue() > 0);
  }
}
