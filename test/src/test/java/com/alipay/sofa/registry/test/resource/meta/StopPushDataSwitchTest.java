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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.ReSubscribers;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class StopPushDataSwitchTest extends BaseIntegrationTest {
    @Test
    public void testStopPushDataSwitch() throws Exception {
        // open stop push switch
        assertTrue(metaChannel.getWebTarget().path("stopPushDataSwitch/open")
            .request(APPLICATION_JSON).get(Result.class).isSuccess());
        BaseIntegrationTest.dataId = null;

        // register Publisher & Subscriber, Subscriber get no data
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test stop publish data switch";
        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(500L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);
        registryClient1.register(subReg);
        Thread.sleep(1000L);
        assertNull(BaseIntegrationTest.dataId);

        // close stop push switch
        assertTrue(metaChannel.getWebTarget().path("stopPushDataSwitch/close")
            .request(APPLICATION_JSON).get(Result.class).isSuccess());
        Thread.sleep(1000L);

        // Subscriber get data, test data
        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        // unregister Publisher & Subscriber
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    /**
     * com.alipay.sofa.registry.server.session.scheduler.task.ProvideDataChangeFetchTask#fireReSubscriber has sleep 1 minute, invoke directly to save time.
     * @throws Exception
     */
    @Test
    public void testStopPushDataSwitchByCode() throws Exception {
        // open stop push switch
        assertTrue(metaChannel.getWebTarget().path("stopPushDataSwitch/open").request(APPLICATION_JSON)
                .get(Result.class)
                .isSuccess());
        BaseIntegrationTest.dataId = null;

        // register Publisher & Subscriber, Subscriber get no data
        String dataId = "test-dataId-hahhahahahha-" + System.currentTimeMillis();
        String value = "test stop publish data switch by code";
        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(500L);

        SubscriberRegistration subReg = new SubscriberRegistration(dataId, new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.dataCenter);
        registryClient1.register(subReg);
        Thread.sleep(1000L);
        assertNull(BaseIntegrationTest.dataId);

        // invoke code directly
        Interests sessionInterests = sessionApplicationContext.getBean(Interests.class);
        TaskListenerManager taskListenerManager = sessionApplicationContext.getBean(TaskListenerManager.class);
        SessionServerConfig sessionServerConfig = sessionApplicationContext.getBean(SessionServerConfig.class);
        if (sessionInterests instanceof ReSubscribers) {
            ReSubscribers reSubscriber = (ReSubscribers) sessionInterests;

            Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>> reSubscribers = reSubscriber
                    .getReSubscribers();
            if (reSubscribers != null && !reSubscribers.isEmpty()) {
                reSubscribers.forEach((dataInfoId, subscribers) -> {
                    if (!CollectionUtils.isEmpty(subscribers)) {
                        sessionServerConfig.setStopPushSwitch(false);
                        TaskEvent taskEvent = new TaskEvent(dataInfoId, TaskEvent.TaskType.SUBSCRIBER_MULTI_FETCH_TASK);
                        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers.values());
                        taskListenerManager.sendTaskEvent(taskEvent);
                    }

                });
                reSubscriber.clearReSubscribers();
            }
        }
        Thread.sleep(1000);

        // Subscriber get data, test data
        assertEquals(dataId, this.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));

        // unregister Publisher & Subscriber, close stop push switch
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
        assertTrue(metaChannel.getWebTarget().path("stopPushDataSwitch/close").request(APPLICATION_JSON)
                .get(Result.class)
                .isSuccess());
    }
}
