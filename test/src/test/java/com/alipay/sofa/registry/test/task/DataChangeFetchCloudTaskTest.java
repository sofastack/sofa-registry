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
package com.alipay.sofa.registry.test.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.DataChangeFetchCloudTask;
import com.alipay.sofa.registry.server.session.scheduler.task.PushTaskClosure;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.test.BaseIntegrationTest;

/**
 * @author kezhu.wukz
 * @since 2020/03/03
 */
@RunWith(SpringRunner.class)
public class DataChangeFetchCloudTaskTest extends BaseIntegrationTest {

    private String testDataInfoId;
    private String dataId;
    private String value;

    @Before
    public void beforeDataChangeFetchCloudTaskTest() throws InterruptedException {
        dataId = "DataChangeFetchCloudTaskTest-" + System.currentTimeMillis();
        testDataInfoId = DataInfo.toDataInfoId(dataId, ValueConstants.DEFAULT_INSTANCE_ID,
            ValueConstants.DEFAULT_GROUP);
        value = dataId;

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
    public void afterDataChangeFetchCloudTaskTest() {
        //remove sub
        registryClient1.unregister(dataId, ValueConstants.DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, ValueConstants.DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

    @Test
    public void getTaskClosureTest() {
        String dataCenter = "MockDC";

        Interests sessionInterests = (Interests) sessionApplicationContext
            .getBean("sessionInterests");
        SessionServerConfig sessionServerConfig = (SessionServerConfig) sessionApplicationContext
            .getBean("sessionServerConfig");
        TaskListenerManager taskListenerManager = (TaskListenerManager) sessionApplicationContext
            .getBean("taskListenerManager");
        ExecutorManager executorManager = (ExecutorManager) sessionApplicationContext
            .getBean("executorManager");
        CacheService sessionCacheService = (CacheService) sessionApplicationContext
            .getBean("sessionCacheService");
        MetaServerService metaServerService = (MetaServerService) sessionApplicationContext
            .getBean("metaServerService");
        TaskEvent event = new TaskEvent(testDataInfoId,
            TaskEvent.TaskType.DATA_CHANGE_FETCH_CLOUD_TASK);

        SessionTask dataChangeFetchTask = new DataChangeFetchCloudTask(sessionServerConfig,
            taskListenerManager, sessionInterests, executorManager, sessionCacheService,
            metaServerService);
        dataChangeFetchTask.setTaskEvent(event);

        //put a new dataCenter, version set 1

        sessionInterests.checkAndUpdateInterestVersions(dataCenter, testDataInfoId, 1L);
        List<String> dataCenters = sessionInterests.getDataCenters();
        assertTrue(dataCenters.size() > 0);
        assertTrue(dataCenters.contains(dataCenter));

        // version will uodate to 0
        Map<String, Datum> datumMap = new HashMap<>();
        {
            Datum datum = new Datum();
            datum.setVersion(2);
            datumMap.put("testDataCenter1", datum);
        }
        {
            Datum datum = new Datum();
            datum.setVersion(2);
            datumMap.put("testDataCenter2", datum);
        }
        PushTaskClosure taskClosure = ((DataChangeFetchCloudTask) dataChangeFetchTask)
            .getTaskClosure(datumMap);
        taskClosure.run(ProcessingResult.Success, dataChangeFetchTask);

        // check version should be 0
        assertTrue(sessionInterests.checkInterestVersions(dataCenter, testDataInfoId, 1L));

    }
}
