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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.providedata.FetchShutdownService;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.session.resource.EmergencyApiResource;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version : EmergencyApiResourceTest.java, v 0.1 2021年10月25日 20:47 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class EmergencyApiResourceTest extends BaseIntegrationTest {

  private MetaServerService metaServerService = mock(MetaServerService.class);

  @Test
  public void testClosePushByRepository() throws Exception {
    startServerIfNecessary();
    EmergencyApiResource emergencyApiResource =
        (EmergencyApiResource) sessionApplicationContext.getBean("emergencyApiResource");
    ProvideDataRepository provideDataRepository =
        (ProvideDataRepository) sessionApplicationContext.getBean("provideDataJdbcRepository");

    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(persistenceData.getData(), "false");
    emergencyApiResource.setMetaNodeService(metaServerService);
    when(metaServerService.renewNode()).thenReturn(false);
    when(metaServerService.getMetaServerLeader()).thenReturn("1.1.1.1");

    CommonResponse commonResponse = emergencyApiResource.closePushByRepository();
    Assert.assertTrue(commonResponse.isSuccess());
    persistenceData = provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(persistenceData.getData(), "true");

    Thread.sleep(5000);
    openPush();
    Thread.sleep(5000);
    persistenceData = provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(persistenceData.getData(), "false");
  }

  @Test
  public void shutdownByRepository() throws Exception {
    startServerIfNecessary();

    String token = "6c62lk8dmQoE5B8X";
    EmergencyApiResource emergencyApiResource =
        (EmergencyApiResource) sessionApplicationContext.getBean("emergencyApiResource");
    ProvideDataRepository provideDataRepository =
        (ProvideDataRepository) sessionApplicationContext.getBean("provideDataJdbcRepository");
    FetchShutdownService fetchShutdownService =
        (FetchShutdownService) sessionApplicationContext.getBean("fetchShutdownService");

    SessionServerBootstrap sessionServerBootstrap = mock(SessionServerBootstrap.class);
    fetchShutdownService.setSessionServerBootstrap(sessionServerBootstrap);

    PersistenceData stopPush =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(stopPush.getData(), "false");
    CommonResponse commonResponse = emergencyApiResource.shutdownByRepository(token);
    Assert.assertFalse(commonResponse.isSuccess());

    Thread.sleep(5000);
    closePush();
    Thread.sleep(5000);

    stopPush = provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(stopPush.getData(), "true");

    emergencyApiResource.setMetaNodeService(metaServerService);
    when(metaServerService.renewNode()).thenReturn(false);
    when(metaServerService.getMetaServerLeader()).thenReturn("1.1.1.1");

    commonResponse = emergencyApiResource.shutdownByRepository(token);
    Assert.assertTrue(commonResponse.isSuccess());

    PersistenceData shutdown = provideDataRepository.get(ValueConstants.SHUTDOWN_SWITCH_DATA_ID);
    Assert.assertEquals(shutdown.getData(), "true");

    openPush();
    Thread.sleep(5000);

    stopPush = provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(stopPush.getData(), "false");

    PersistenceData shutdownUpdate =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.SHUTDOWN_SWITCH_DATA_ID, "false");
    provideDataRepository.put(shutdownUpdate, shutdown.getVersion());

    shutdown = provideDataRepository.get(ValueConstants.SHUTDOWN_SWITCH_DATA_ID);
    Assert.assertEquals(shutdown.getData(), "false");
  }

  @Test
  public void stopZoneClosePush() throws Exception {
    startServerIfNecessary();

    EmergencyApiResource emergencyApiResource =
        (EmergencyApiResource) sessionApplicationContext.getBean("emergencyApiResource");
    ProvideDataRepository provideDataRepository =
        (ProvideDataRepository) sessionApplicationContext.getBean("provideDataJdbcRepository");

    CommonResponse commonResponse = emergencyApiResource.zoneClosePush();
    Assert.assertTrue(commonResponse.isSuccess());

    FetchStopPushService fetchStopPushService =
        (FetchStopPushService) sessionApplicationContext.getBean("fetchStopPushService");
    Assert.assertTrue(fetchStopPushService.isStopPushSwitch());

    Thread.sleep(5000);
    openPush();
    Thread.sleep(5000);

    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(persistenceData.getData(), "false");
  }
}
