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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.client.base.BaseTest;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To test all kinds of sequence of register action
 *
 * @author hui.shih
 * @version $Id: RegisterOrderTest.java, v 0.1 2018-03-07 16:28 hui.shih Exp $$
 */
public class RegisterOrderTest extends BaseTest {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegistryClientTest.class);

  private String dataId = "com.alipay.sofa.registry.client.provider.RegisterOrderTest";

  /** Teardown. */
  @After
  public void teardown() {
    mockServer.stop();
  }

  @Test
  public void publishFrequently() throws InterruptedException {

    String dataPrefix = "publishFrequently";

    Publisher publisher =
        registryClient.register(new PublisherRegistration(dataId), dataPrefix + 0);

    int republishCount = 100;
    for (int i = 1; i <= republishCount; i++) {
      publisher.republish(dataPrefix + i);
    }

    Thread.sleep(2000L);

    DefaultPublisher defaultPublisher = (DefaultPublisher) publisher;
    String registeId = defaultPublisher.getRegistId();

    // check client status
    assertTrue(defaultPublisher.isRegistered());
    assertTrue(defaultPublisher.isEnabled());
    assertEquals(republishCount + 1, defaultPublisher.getPubVersion().intValue());

    // check server status
    List<DataBox> data = mockServer.queryPubliser(registeId).getDataList();
    assertEquals(1, data.size());
    assertEquals(dataPrefix + republishCount, data.get(0).getData());
  }

  @Test
  public void unregisterAndRepublish() throws InterruptedException {

    String dataPrefix = "unregisterAndRepublish";

    // step 1
    Publisher publisher =
        registryClient.register(new PublisherRegistration(dataId), dataPrefix + 0);
    // step 2
    publisher.unregister();

    try {
      // step 3
      publisher.republish(dataPrefix + 1);
      fail("No exception thrown.");
    } catch (Exception ex) {
      // Must throw a RuntimeException
      assertTrue(ex instanceof IllegalStateException);
    }

    Thread.sleep(2000L);

    assertTrue(publisher.isRegistered());
    assertFalse(publisher.isEnabled());
  }

  @Test
  public void publishAndUnregister() throws InterruptedException {

    String data = "publishAndUnregister";

    // step 1
    Publisher publisher = registryClient.register(new PublisherRegistration(dataId), data);

    Thread.sleep(2000L);

    String registId = publisher.getRegistId();
    PublisherRegister publisherRegister = mockServer.queryPubliser(registId);

    assertNotNull(publisherRegister);
    assertEquals(data, publisherRegister.getDataList().get(0).getData());

    // step 2
    publisher.unregister();

    Thread.sleep(2000L);

    assertNull(mockServer.queryPubliser(registId));
  }

  @Test
  public void publishAndRefused() throws InterruptedException {

    String data = "publishAndRefused";

    // step 1
    Publisher publisher = registryClient.register(new PublisherRegistration(data), data);

    Thread.sleep(2000L);

    String registId = publisher.getRegistId();
    PublisherRegister publisherRegister = mockServer.queryPubliser(registId);

    assertNull(publisherRegister);

    // step 2
    try {
      publisher.republish(data);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalStateException);
      assertEquals(
          "Publisher is refused by server. Try to check your configuration.", e.getMessage());
    }
  }

  @Test
  public void subscribeAndUnregister() throws InterruptedException {

    SubscriberDataObserver dataObserver =
        new SubscriberDataObserver() {
          @Override
          public void handleData(String dataId, UserData data) {
            LOGGER.info("handle data, dataId: {}, data: {}", dataId, data);
          }
        };

    // step 1
    Subscriber subscriber =
        registryClient.register(new SubscriberRegistration(dataId, dataObserver));

    Thread.sleep(2000L);

    String registId = subscriber.getRegistId();
    SubscriberRegister subscriberRegister = mockServer.querySubscriber(registId);
    assertNotNull(subscriberRegister);

    // step 2
    subscriber.unregister();

    Thread.sleep(2000L);

    assertNull(mockServer.queryPubliser(registId));
  }

  @Test
  public void subscribeAndRefused() throws InterruptedException {

    SubscriberDataObserver dataObserver =
        new SubscriberDataObserver() {
          @Override
          public void handleData(String dataId, UserData data) {
            LOGGER.info("handle data, dataId: {}, data: {}", dataId, data);
          }
        };

    // step 1
    Subscriber subscriber =
        registryClient.register(new SubscriberRegistration("subscribeAndRefused", dataObserver));

    Thread.sleep(2000L);

    String registId = subscriber.getRegistId();
    SubscriberRegister subscriberRegister = mockServer.querySubscriber(registId);
    assertNull(subscriberRegister);

    assertTrue(subscriber instanceof AbstractInternalRegister);
    assertTrue(((AbstractInternalRegister) subscriber).isRefused());
  }
}
