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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class DataDigestResourceTest extends BaseIntegrationTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        clientOff();
        dataId = "test-dataId-" + System.currentTimeMillis();
        value = "DataDigestResourceTest";
        Thread.sleep(2000L);

        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(2000L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);
        registryClient1.register(subReg);
        Thread.sleep(2000L);
    }

    @AfterClass
    public static void afterClass() {
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    @Test
    public void testGetDatumByDataInfoId() throws Exception {
        Map<String, Datum> datumMap = dataChannel.getWebTarget().path("digest/datum/query")
            .queryParam("dataId", dataId).queryParam("group", DEFAULT_GROUP)
            .queryParam("instanceId", DEFAULT_INSTANCE_ID)
            .queryParam("dataCenter", LOCAL_DATACENTER).request(APPLICATION_JSON)
            .get(new GenericType<Map<String, Datum>>() {
            });
        assertTrue(datumMap.size() == 1);
        assertEquals(dataId, datumMap.get(LOCAL_DATACENTER).getDataId());
        assertEquals(1, datumMap.get(LOCAL_DATACENTER).getPubMap().size());
        assertEquals(value, bytes2Object(datumMap.get(LOCAL_DATACENTER).getPubMap().values()
            .iterator().next().getDataList().get(0).getBytes()));
    }

    @Test
    public void testGetPublishersByConnectId() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(LOCAL_ADDRESS, String.valueOf(getSourcePort(registryClient1)));
        Map<String, Map<String, Publisher>> publisherMap = dataChannel
            .getWebTarget()
            .path("digest/connect/query")
            .request(APPLICATION_JSON)
            .post(Entity.entity(parameters, MediaType.APPLICATION_JSON),
                new GenericType<Map<String, Map<String, Publisher>>>() {
                });
        assertEquals(1, publisherMap.size());
        assertEquals(1,
            publisherMap.get(LOCAL_ADDRESS + ":" + String.valueOf(getSourcePort(registryClient1)))
                .size());
        assertEquals(dataId,
            publisherMap.get(LOCAL_ADDRESS + ":" + String.valueOf(getSourcePort(registryClient1)))
                .values().iterator().next().getDataId());
        assertEquals(
            value,
            bytes2Object(publisherMap
                .get(LOCAL_ADDRESS + ":" + String.valueOf(getSourcePort(registryClient1))).values()
                .iterator().next().getDataList().get(0).getBytes()));
    }

    @Test
    public void testGetDatumCount() {
        String countResult = dataChannel.getWebTarget().path("digest/datum/count")
            .request(APPLICATION_JSON).get(String.class);
        assertTrue(countResult.contains("[Publisher] size of publisher in DefaultDataCenter is 1"));
    }

    @Test
    public void testGetServerListAll() throws Exception {

        Map<String, List<String>> sessionMap = dataChannel.getWebTarget()
            .path("digest/session/serverList/query").request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<String>>>() {
            });
        assertEquals(1, sessionMap.size());
        assertEquals(1, sessionMap.get(LOCAL_DATACENTER).size());
        assertTrue(sessionMap.get(LOCAL_DATACENTER).get(0).contains(LOCAL_ADDRESS));

        Map<String, List<String>> metaMap = dataChannel.getWebTarget()
            .path("digest/meta/serverList/query").request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<String>>>() {
            });
        assertEquals(metaMap.get(LOCAL_DATACENTER).get(0), LOCAL_ADDRESS);

        Map<String, List<String>> dataMap = dataChannel.getWebTarget()
            .path("digest/data/serverList/query").request(APPLICATION_JSON)
            .get(new GenericType<Map<String, List<String>>>() {
            });
        assertEquals(dataMap.size(), 0);
    }
}