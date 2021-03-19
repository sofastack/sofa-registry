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
package com.alipay.sofa.registry.client.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.client.base.BaseTest;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Default registry client test.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultRegistryClientTest.java, v 0.1 2017-11-24 20:05 zhuoyu.sjw Exp $$
 */
public class DefaultRegistryClientTest extends BaseTest {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegistryClientTest.class);

  private String dataId = "com.alipay.sofa.registry.client.provider.DefaultRegistryClientTest";

  private String appName = "registry-test";

  /** Teardown. */
  @After
  public void teardown() {
    mockServer.stop();
  }

  /** Register publisher. */
  @Test
  public void registerPublisher() throws InterruptedException {
    PublisherRegistration registration = new PublisherRegistration(dataId);
    Publisher publisher = registryClient.register(registration);
    assertNotNull(publisher);
    assertEquals(dataId, publisher.getDataId());

    Thread.sleep(1000L);

    RegisterCache registerCache = registryClient.getRegisterCache();

    Publisher publisherCache = registerCache.getPublisherByRegistId(publisher.getRegistId());
    assertNotNull(publisherCache);
    assertTrue(publisher instanceof DefaultPublisher);
    // set cache success
    assertEquals(publisher.getRegistId(), publisherCache.getRegistId());
    DefaultPublisher defaultPublisher = (DefaultPublisher) publisher;
    assertTrue(defaultPublisher.isRegistered());

    defaultPublisher.republish("republish test");

    Thread.sleep(2000L);
    // register success when republish
    assertTrue(defaultPublisher.isRegistered());
  }

  /** Register subscriber. */
  @Test
  public void registerSubscriber() throws InterruptedException {
    SubscriberDataObserver dataObserver =
        new SubscriberDataObserver() {
          @Override
          public void handleData(String dataId, UserData data) {
            LOGGER.info("handle data, dataId: {}, data: {}", dataId, data);
          }
        };
    SubscriberRegistration registration = new SubscriberRegistration(dataId, dataObserver);
    registration.setScopeEnum(ScopeEnum.dataCenter);
    Subscriber subscriber = registryClient.register(registration);
    assertNotNull(subscriber);
    assertEquals(dataId, subscriber.getDataId());

    Thread.sleep(2000L);

    RegisterCache registerCache = registryClient.getRegisterCache();

    Subscriber register = registerCache.getSubscriberByRegistId(subscriber.getRegistId());
    assertNotNull(register);
    // set cache success
    assertEquals(subscriber.getRegistId(), register.getRegistId());
    // register success
    assertTrue(register.isRegistered());
  }

  /** Register configurator. */
  @Test
  public void registerConfigurator() throws InterruptedException {
    ConfigDataObserver dataObserver =
        new ConfigDataObserver() {
          @Override
          public void handleData(String dataId, ConfigData configData) {
            LOGGER.info("handle data, dataId: {}, data: {}", dataId, configData);
          }
        };

    ConfiguratorRegistration registration = new ConfiguratorRegistration(dataId, dataObserver);

    Configurator configurator = registryClient.register(registration);
    assertNotNull(configurator);
    assertEquals(dataId, configurator.getDataId());

    Thread.sleep(2000L);

    RegisterCache registerCache = registryClient.getRegisterCache();

    Configurator register = registerCache.getConfiguratorByRegistId(configurator.getRegistId());
    assertNotNull(register);
    // set cache success
    assertEquals(configurator.getRegistId(), register.getRegistId());
    // register success
    assertTrue(register.isRegistered());
  }

  @Test
  public void unregisterSinglePublisherTest() throws InterruptedException {
    String dataId = "unregister-test-data-id";

    PublisherRegistration publisherRegistration = new PublisherRegistration(dataId);
    Publisher publisher = registryClient.register(publisherRegistration);

    int unregisterCount = registryClient.unregister(dataId, null, RegistryType.PUBLISHER);
    assertEquals(1, unregisterCount);

    Thread.sleep(2000L);

    Publisher temp =
        registryClient.getRegisterCache().getPublisherByRegistId(publisher.getRegistId());

    assertNull(temp);
  }

  @Test
  public void unregisterSingleSubscriberTest() throws InterruptedException {
    String dataId = "unregister-test-data-id";

    SubscriberDataObserver dataObserver = mock(SubscriberDataObserver.class);
    SubscriberRegistration subscriberRegistration =
        new SubscriberRegistration(dataId, dataObserver);

    Subscriber subscriber = registryClient.register(subscriberRegistration);

    int unregisterCount = registryClient.unregister(dataId, null, RegistryType.SUBSCRIBER);
    assertEquals(1, unregisterCount);

    Thread.sleep(2000L);

    Subscriber temp =
        registryClient.getRegisterCache().getSubscriberByRegistId(subscriber.getRegistId());

    assertNull(temp);
  }

  @Test
  public void unregisterSingleConfiguratorTest() throws InterruptedException {
    String dataId = "unregister-test-data-id";

    ConfigDataObserver dataObserver = mock(ConfigDataObserver.class);

    ConfiguratorRegistration registration = new ConfiguratorRegistration(dataId, dataObserver);

    Configurator configurator = registryClient.register(registration);

    int unregisterCount = registryClient.unregister(dataId, null, RegistryType.CONFIGURATOR);
    assertEquals(1, unregisterCount);

    Thread.sleep(2000L);

    Subscriber temp =
        registryClient.getRegisterCache().getSubscriberByRegistId(configurator.getRegistId());

    assertNull(temp);
  }

  @Test
  public void unregisterMultiTest() throws InterruptedException {
    String dataId = "unregister-test-data-id";

    // 1. register
    PublisherRegistration publisherRegistration1 = new PublisherRegistration(dataId);
    Publisher publisher1 = registryClient.register(publisherRegistration1);

    PublisherRegistration publisherRegistration2 = new PublisherRegistration(dataId);
    Publisher publisher2 = registryClient.register(publisherRegistration2);

    SubscriberDataObserver dataObserver = mock(SubscriberDataObserver.class);
    SubscriberRegistration subscriberRegistration1 =
        new SubscriberRegistration(dataId, dataObserver);

    Subscriber subscriber1 = registryClient.register(subscriberRegistration1);

    SubscriberRegistration subscriberRegistration2 =
        new SubscriberRegistration(dataId, dataObserver);

    Subscriber subscriber2 = registryClient.register(subscriberRegistration2);

    Thread.sleep(2000L);

    // 2. unregister publisher
    int unregisterCount = registryClient.unregister(dataId, null, RegistryType.PUBLISHER);
    assertEquals(2, unregisterCount);

    Thread.sleep(2000L);

    // 3. check publisher register cache
    RegisterCache registerCache = registryClient.getRegisterCache();
    Publisher tempPub = registerCache.getPublisherByRegistId(publisher1.getRegistId());
    assertNull(tempPub);

    tempPub = registerCache.getPublisherByRegistId(publisher2.getRegistId());
    assertNull(tempPub);

    // 4. unregister subscriber
    unregisterCount = registryClient.unregister(dataId, null, RegistryType.SUBSCRIBER);
    assertEquals(2, unregisterCount);

    Thread.sleep(2000L);

    // 5. check subscriber register cache
    Subscriber tempSub = registerCache.getSubscriberByRegistId(subscriber1.getRegistId());
    assertNull(tempSub);

    tempSub = registerCache.getSubscriberByRegistId(subscriber2.getRegistId());
    assertNull(tempSub);
  }

  @Test
  public void testSetAppNameByRegistration() {
    PublisherRegistration publisherRegistration = new PublisherRegistration(dataId);
    final String testApp = "test-app";
    publisherRegistration.setAppName(testApp);

    Publisher publisher = registryClient.register(publisherRegistration);
    PublisherRegister register = ((DefaultPublisher) publisher).assembly();

    assertEquals(testApp, register.getAppName());

    SubscriberRegistration subscriberRegistration =
        new SubscriberRegistration(dataId, mock(SubscriberDataObserver.class));
    subscriberRegistration.setAppName(testApp);

    Subscriber subscriber = registryClient.register(subscriberRegistration);
    SubscriberRegister subscriberRegister = ((DefaultSubscriber) subscriber).assembly();

    assertEquals(testApp, subscriberRegister.getAppName());
  }

  @Test
  public void testSetAppNameByConfig() {
    PublisherRegistration publisherRegistration = new PublisherRegistration(dataId);

    Publisher publisher = registryClient.register(publisherRegistration);
    PublisherRegister register = ((DefaultPublisher) publisher).assembly();

    assertEquals(appName, register.getAppName());
  }
}
