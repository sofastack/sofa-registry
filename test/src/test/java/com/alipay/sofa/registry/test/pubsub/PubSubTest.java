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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static org.junit.Assert.assertEquals;

/**
 * @author xuanbei 18/12/2
 */
@RunWith(SpringRunner.class)
public class PubSubTest extends BaseIntegrationTest {
    /**
     * Publisher test.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void publisherTest() throws InterruptedException {
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test publish";

        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(2000L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);

        registryClient1.register(subReg);

        Thread.sleep(60 * 1000L);
        //        Thread.sleep(2000000L);
        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    /**
     * Subscriber test.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void subscriberTest() throws InterruptedException {
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test subscriber";

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);
        registryClient1.register(subReg);
        Thread.sleep(2000L);

        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(2000L);

        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    /**
     * Multi Client test.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void multiClientTest() throws InterruptedException {

        // registryClient1 publish data, registryClient2 subscriber
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test multi client publish";

        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(2000L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.zone);

        registryClient2.register(subReg);

        Thread.sleep(2000L);
        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        registryClient2.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);

        // registryClient1 subscriber, registryClient2 publish data
        dataId = "test-dataId-" + System.currentTimeMillis();
        value = "test multi client subscriber";

        subReg = new SubscriberRegistration(dataId, new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.global);
        registryClient1.register(subReg);
        Thread.sleep(2000L);

        registration = new PublisherRegistration(dataId);
        registryClient2.register(registration, value);
        Thread.sleep(2000L);

        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient2.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);

        // registryClient1 subscriber, registryClient1 and registryClient2 both publish data
        dataId = "test-dataId-" + System.currentTimeMillis();
        value = "test multi client subscriber";

        subReg = new SubscriberRegistration(dataId, new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.zone);
        registryClient1.register(subReg);
        Thread.sleep(2000L);

        registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        registryClient2.register(registration, value);
        Thread.sleep(2000L);

        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(2, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(1));

        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
        registryClient2.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }
}