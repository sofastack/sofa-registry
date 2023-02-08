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
package com.alipay.sofa.registry.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerTest.java, v 0.1 2021年05月31日 11:40 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ClientManagerTest extends BaseIntegrationTest {

  private String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OFF_STR = "1.1.1.1;2.2.2.2;" + localAddress;
  private final String CLIENT_OPEN_STR = "2.2.2.2;3.3.3.3;" + localAddress;

  private final Set<String> CLIENT_OFF_SET = Sets.newHashSet(CLIENT_OFF_STR.split(";"));
  private final Set<String> CLIENT_OPEN_SET = Sets.newHashSet(CLIENT_OPEN_STR.split(";"));

  public static final Set<AddressVersion> CLIENT_OFF_WITH_SUB_SET =
      Sets.newHashSet(
          new AddressVersion("1.1.1.1", true),
          new AddressVersion("2.2.2.2", false),
          new AddressVersion("3.3.3.3", true));

  @Test
  public void testClientOff() throws InterruptedException, TimeoutException {
    String dataId = "test-meta-client-off-dataId-" + System.currentTimeMillis();
    String value = "test meta client off";

    DataInfo dataInfo =
        new DataInfo(ValueConstants.DEFAULT_INSTANCE_ID, dataId, ValueConstants.DEFAULT_GROUP);

    /** register */
    PublisherRegistration registration = new PublisherRegistration(dataId);
    com.alipay.sofa.registry.client.api.Publisher register =
        registryClient1.register(registration, value);
    Thread.sleep(5000L);

    // check session
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));

    // check data
    Assert.assertTrue(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    /** client off */
    CommonResponse response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(CLIENT_OFF_SET), 5000);
    waitConditionUntilTimeOut(
        () -> {
          GenericResponse<ClientManagerAddress> query = clientManagerResource.query();
          return query.isSuccess()
              && query
                  .getData()
                  .getClientOffAddress()
                  .keySet()
                  .equals(fetchClientOffAddressService.getClientOffAddress());
        },
        5000);

    Thread.sleep(3000L);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertFalse(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    register.republish(value);
    Thread.sleep(2000L);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertFalse(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    /** client open */
    response = clientManagerResource.clientOpen(CLIENT_OPEN_STR);
    Assert.assertTrue(response.isSuccess());

    SetView<String> difference = Sets.difference(CLIENT_OFF_SET, CLIENT_OPEN_SET);
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(difference), 5000);

    waitConditionUntilTimeOut(
        () -> {
          GenericResponse<ClientManagerAddress> query = clientManagerResource.query();
          return query.isSuccess()
              && query
                  .getData()
                  .getClientOffAddress()
                  .keySet()
                  .equals(fetchClientOffAddressService.getClientOffAddress());
        },
        5000);
    Thread.sleep(5000);

    // check session local cache
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    Assert.assertTrue(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));
  }

  @Test
  public void testClientOffWithSub() throws InterruptedException, TimeoutException {
    /** client off */
    CommonResponse response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(CLIENT_OFF_SET), 5000);

    for (String address : CLIENT_OFF_SET) {
      AddressVersion query = fetchClientOffAddressService.getAddress(address);
      Assert.assertTrue(query.isPub());
      Assert.assertTrue(query.isSub());
    }

    /** client off with sub */
    response =
        clientManagerResource.clientOffWithSub(
            JsonUtils.writeValueAsString(CLIENT_OFF_WITH_SUB_SET));
    Assert.assertTrue(response.isSuccess());

    Set<String> merge = Sets.newHashSet(CLIENT_OFF_SET);
    merge.addAll(
        CLIENT_OFF_WITH_SUB_SET.stream()
            .map(AddressVersion::getAddress)
            .collect(Collectors.toSet()));

    // check session client off list
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(merge), 5000);

    for (AddressVersion addressVersion : CLIENT_OFF_WITH_SUB_SET) {
      AddressVersion query = fetchClientOffAddressService.getAddress(addressVersion.getAddress());
      Assert.assertEquals(addressVersion.isPub(), query.isPub());
      Assert.assertEquals(addressVersion.isSub(), query.isSub());
    }

    /** client off */
    response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    TimeUnit.SECONDS.sleep(5);

    for (String address : CLIENT_OFF_SET) {
      AddressVersion query = fetchClientOffAddressService.getAddress(address);
      Assert.assertTrue(query.isPub());
      Assert.assertTrue(query.isSub());
    }

    /** client off */
    response = clientManagerResource.clientOpen(CLIENT_OPEN_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    TimeUnit.SECONDS.sleep(5);
    for (String address : CLIENT_OPEN_SET) {
      AddressVersion query = fetchClientOffAddressService.getAddress(address);
      Assert.assertNull(query);
    }
  }

  @Test
  public void testReduce() throws InterruptedException, TimeoutException {
    String dataId = "test-meta-client-off-reduce-dataId-" + System.currentTimeMillis();
    String value = "test meta client off";

    DataInfo dataInfo =
        new DataInfo(ValueConstants.DEFAULT_INSTANCE_ID, dataId, ValueConstants.DEFAULT_GROUP);

    /** register */
    PublisherRegistration registration = new PublisherRegistration(dataId);
    com.alipay.sofa.registry.client.api.Publisher register =
        registryClient1.register(registration, value);
    Thread.sleep(5000L);

    // check session
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));

    // check data
    Assert.assertTrue(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    /** client off */
    CommonResponse response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(CLIENT_OFF_SET), 5000);
    waitConditionUntilTimeOut(
        () -> {
          GenericResponse<ClientManagerAddress> query = clientManagerResource.query();
          return query.isSuccess()
              && query
                  .getData()
                  .getClientOffAddress()
                  .keySet()
                  .equals(fetchClientOffAddressService.getClientOffAddress());
        },
        5000);

    Thread.sleep(3000L);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertFalse(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    register.republish(value);
    Thread.sleep(2000L);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertFalse(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    /** reduce */
    response = clientManagerResource.reduce(CLIENT_OPEN_STR);
    Assert.assertTrue(response.isSuccess());

    SetView<String> difference = Sets.difference(CLIENT_OFF_SET, CLIENT_OPEN_SET);
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().containsAll(difference), 5000);

    waitConditionUntilTimeOut(
        () -> {
          GenericResponse<ClientManagerAddress> query = clientManagerResource.query();
          return query.isSuccess()
              && query
                  .getData()
                  .getClientOffAddress()
                  .keySet()
                  .equals(fetchClientOffAddressService.getClientOffAddress());
        },
        5000);
    Thread.sleep(5000);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    Assert.assertFalse(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));

    register.republish(value);
    Thread.sleep(2000L);

    // check session local cache
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertTrue(
        isExist(
            datumStorageDelegate.getAllPublisher(LOCAL_DATACENTER).get(dataInfo.getDataInfoId()),
            localAddress));
  }

  @Test
  public void testConfig() throws InterruptedException, TimeoutException {
    String localAddress = sessionChannel.getLocalAddress().getHostString();
    GenericResponse<Long> response = persistenceClientManagerResource.clientOpen(localAddress);
    Assert.assertTrue(response.isSuccess());

    waitConditionUntilTimeOut(
        () -> !fetchClientOffAddressService.getClientOffAddress().contains(localAddress), 5000);

    String dataId = "testDataId";
    MyConfigDataObserver dataObserver = new MyConfigDataObserver();
    ConfiguratorRegistration registration = new ConfiguratorRegistration(dataId, dataObserver);

    Configurator configurator = registryClient1.register(registration);
    assertNotNull(configurator);
    assertEquals(dataId, configurator.getDataId());
    ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.SECONDS);
    assertEquals(dataObserver.dataId, dataId);
  }
}
