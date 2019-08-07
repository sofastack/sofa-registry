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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class SessionDigestResourceTest extends BaseIntegrationTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        clientOff();
        dataId = "test-dataId-" + System.currentTimeMillis();
        value = "SessionDigestResourceTest";

        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(500L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);
        registryClient1.register(subReg);
        Thread.sleep(500L);
    }

    @AfterClass
    public static void afterClass() {
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    @Test
    public void testGetSessionDataByDataInfoId() throws Exception {
        Map<String, List<Publisher>> publisherMap = sessionChannel
            .getWebTarget()
            .path("digest/pub/data/query")
            .queryParam("dataInfoId",
                DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON).get(new GenericType<Map<String, List<Publisher>>>() {
            });
        assertEquals(1, publisherMap.size());
        assertEquals(1, publisherMap.get("PUB").size());
        assertEquals(dataId, publisherMap.get("PUB").get(0).getDataId());
        assertEquals(value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0)
            .getBytes()));

        Map<String, List<Subscriber>> subscriberMap = sessionChannel
            .getWebTarget()
            .path("digest/sub/data/query")
            .queryParam("dataInfoId",
                DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON).get(new GenericType<Map<String, List<Subscriber>>>() {
            });
        assertEquals(1, subscriberMap.size());
        assertEquals(1, subscriberMap.get("SUB").size());
        assertEquals(dataId, subscriberMap.get("SUB").get(0).getDataId());

        Map map = sessionChannel
            .getWebTarget()
            .path("digest/all/data/query")
            .queryParam("dataInfoId",
                DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
            .request(APPLICATION_JSON).get(Map.class);
        assertEquals(2, map.size());
        assertEquals(1, ((List) map.get("SUB")).size());
        assertEquals(1, ((List) map.get("PUB")).size());
        assertEquals(dataId, ((Map) ((List) map.get("SUB")).get(0)).get("dataId"));
        assertEquals(dataId, ((Map) ((List) map.get("PUB")).get(0)).get("dataId"));
        assertEquals(value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0)
            .getBytes()));
    }

    @Test
    public void testetSessionDataByConnectId() throws Exception {
        List<String> connectIds = new ArrayList<>();
        connectIds.add(NetUtil.genHost(LOCAL_ADDRESS, getSourcePort(registryClient1)));
        Map<String, List<Publisher>> publisherMap = sessionChannel
            .getWebTarget()
            .path("digest/pub/connect/query")
            .request(APPLICATION_JSON)
            .post(Entity.entity(connectIds, MediaType.APPLICATION_JSON),
                new GenericType<Map<String, List<Publisher>>>() {
                });
        assertEquals(1, publisherMap.size());
        assertEquals(1, publisherMap.get("PUB").size());
        assertEquals(dataId, publisherMap.get("PUB").get(0).getDataId());
        assertEquals(value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0)
            .getBytes()));

        Map<String, List<Subscriber>> subscriberMap = sessionChannel
            .getWebTarget()
            .path("digest/sub/connect/query")
            .request(APPLICATION_JSON)
            .post(Entity.entity(connectIds, MediaType.APPLICATION_JSON),
                new GenericType<Map<String, List<Subscriber>>>() {
                });
        assertEquals(1, subscriberMap.size());
        assertEquals(1, subscriberMap.get("SUB").size());
        assertEquals(dataId, subscriberMap.get("SUB").get(0).getDataId());

        Map map = sessionChannel.getWebTarget().path("digest/all/connect/query")
            .request(APPLICATION_JSON)
            .post(Entity.entity(connectIds, MediaType.APPLICATION_JSON), Map.class);
        assertEquals(2, map.size());
        assertEquals(1, ((List) map.get("SUB")).size());
        assertEquals(1, ((List) map.get("PUB")).size());
        assertEquals(dataId, ((Map) ((List) map.get("SUB")).get(0)).get("dataId"));
        assertEquals(dataId, ((Map) ((List) map.get("PUB")).get(0)).get("dataId"));
        assertEquals(value, bytes2Object(publisherMap.get("PUB").get(0).getDataList().get(0)
            .getBytes()));
    }

    @Test
    public void testGetSessionDataCount() {
        String result = sessionChannel.getWebTarget().path("digest/data/count")
            .request(APPLICATION_JSON).get(String.class);
        assertEquals("Subscriber count: 1, Publisher count: 1, Watcher count: 0", result);
    }

    @Test
    public void testGetPushSwitch() {
        Map result = sessionChannel.getWebTarget().path("digest/pushSwitch")
            .request(APPLICATION_JSON).get(Map.class);
        assertEquals(1, result.size());
        assertEquals("open", result.get("pushSwitch"));
    }

    @Test
    public void testGetDataInfoIdList() {
        Set<String> result = sessionChannel.getWebTarget().path("digest/getDataInfoIdList")
            .request(APPLICATION_JSON).get(Set.class);

        assertTrue(result.contains(DataInfo
            .toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP)));
    }

    @Test
    public void testCheckSumDataInfoIdList() {
        int result = sessionChannel.getWebTarget().path("digest/checkSumDataInfoIdList")
            .request(APPLICATION_JSON).get(int.class);
        assertTrue(result != 0);
    }
}
