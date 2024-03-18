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
package com.alipay.sofa.registry.server.meta.cleaner;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.cache.ConsecutiveSuccess;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.repository.impl.DateNowJdbcRepository;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import java.util.Date;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterfaceAppsIndexCleanerTest extends AbstractMetaServerTestBase {
  private InterfaceAppsIndexCleaner interfaceAppsIndexCleaner;

  @Before
  public void beforeTest() throws Exception {
    makeMetaLeader();
    interfaceAppsIndexCleaner = new InterfaceAppsIndexCleaner(metaLeaderService);
    interfaceAppsIndexCleaner.dateNowRepository = mock(DateNowJdbcRepository.class);
    interfaceAppsIndexCleaner.appRevisionRepository = mock(AppRevisionRepository.class);
    interfaceAppsIndexCleaner.interfaceAppsRepository = mock(InterfaceAppsRepository.class);
    interfaceAppsIndexCleaner.metadataConfig = mock(MetadataConfig.class);
    interfaceAppsIndexCleaner.consecutiveSuccess = new ConsecutiveSuccess(2, 10000);
    interfaceAppsIndexCleaner.provideDataService = mock(DefaultProvideDataService.class);

    when(interfaceAppsIndexCleaner.metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes())
        .thenReturn(10000);
    interfaceAppsIndexCleaner.metaServerConfig = new MetaServerConfigBean(commonConfig);

    doReturn(
            new DBResponse<>(
                PersistenceDataBuilder.createPersistenceData(
                    ValueConstants.INTERFACE_APP_CLEANER_ENABLED_DATA_ID, "true"),
                OperationStatus.SUCCESS))
        .when(interfaceAppsIndexCleaner.provideDataService)
        .queryProvideData(anyString());

    doReturn(true).when(interfaceAppsIndexCleaner.provideDataService).saveProvideData(any());
  }

  @After
  public void afterTest() {
    interfaceAppsIndexCleaner.cleaner.close();
    interfaceAppsIndexCleaner.renewer.close();
  }

  @Test
  public void testRenew() {
    InterfaceAppsIndexCleaner mocked = spy(interfaceAppsIndexCleaner);
    AppRevisionDomain domain1 = new AppRevisionDomain();
    domain1.setAppName("app1");
    domain1.setBaseParams("{}");
    domain1.setServiceParams("{\"service1\":{}}");
    AppRevisionDomain domain2 = new AppRevisionDomain();
    domain2.setBaseParams("{}");
    domain2.setAppName("app2");
    domain2.setServiceParams("{\"service1\":{}, \"service2\": {}}");
    doReturn(Lists.newArrayList(domain1, domain2))
        .when(mocked.appRevisionRepository)
        .listFromStorage(anyInt(), anyInt());
    mocked.renew();
    mocked.renewer.getWaitingMillis();
    mocked.renewer.runUnthrowable();
    mocked.start();
    mocked.renewer.close();
  }

  @Test
  public void testClean() {
    InterfaceAppsIndexCleaner mocked = spy(interfaceAppsIndexCleaner);
    InterfaceAppsIndexDomain domain1 = mock(InterfaceAppsIndexDomain.class);
    InterfaceAppsIndexDomain domain2 = mock(InterfaceAppsIndexDomain.class);
    mocked.renew();
    mocked.renew();
    mocked.renew();
  }

  @Test
  public void testCleanInterface() throws Exception {
    InterfaceAppsIndexCleaner mocked = spy(interfaceAppsIndexCleaner);
    doReturn(1).when(mocked.interfaceAppsRepository).cleanDeleted(any(), anyInt());
    doReturn(new Date()).when(mocked.dateNowRepository).getNow();
    mocked.renew();
    mocked.renew();
    mocked.cleanup();
    makeMetaNonLeader();
    mocked.cleanup();
  }

  @Test
  public void testEnable() throws Exception {
    InterfaceAppsIndexCleaner mocked = spy(interfaceAppsIndexCleaner);
    doReturn(1).when(mocked.interfaceAppsRepository).cleanDeleted(any(), anyInt());
    doReturn(new Date()).when(mocked.dateNowRepository).getNow();
    mocked.setEnabled(true);
    mocked.startRenew();
    mocked.cleaner.getWaitingMillis();
    mocked.cleaner.runUnthrowable();
    mocked.startCleaner();
  }

  @Test(expected = RuntimeException.class)
  public void testEnableThrowEx() {
    doThrow(new RuntimeException())
        .when(interfaceAppsIndexCleaner.provideDataService)
        .saveProvideData(any());
    interfaceAppsIndexCleaner.setEnabled(true);
  }
}
