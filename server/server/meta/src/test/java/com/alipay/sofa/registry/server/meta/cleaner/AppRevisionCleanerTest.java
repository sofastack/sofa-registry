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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.cache.ConsecutiveSuccess;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.repository.impl.AppRevisionJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.DateNowJdbcRepository;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataService;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import java.util.Collections;
import java.util.Date;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppRevisionCleanerTest extends AbstractMetaServerTestBase {
  private AppRevisionCleaner appRevisionCleaner;

  @Before
  public void beforeTest() throws Exception {
    makeMetaLeader();
    appRevisionCleaner = new AppRevisionCleaner(metaLeaderService);
    appRevisionCleaner.metaServerConfig = new MetaServerConfigBean(commonConfig);
    appRevisionCleaner.dateNowRepository = mock(DateNowJdbcRepository.class);
    appRevisionCleaner.appRevisionRepository = mock(AppRevisionJdbcRepository.class);
    appRevisionCleaner.sessionServerService = mock(DefaultSessionServerService.class);
    appRevisionCleaner.metadataConfig = mock(MetadataConfig.class);
    appRevisionCleaner.provideDataService = mock(DefaultProvideDataService.class);
    appRevisionCleaner.consecutiveSuccess = new ConsecutiveSuccess(2, 1000);
    when(appRevisionCleaner.metadataConfig.getRevisionRenewIntervalMinutes()).thenReturn(10000);

    doReturn(
            new DBResponse<>(
                PersistenceDataBuilder.createPersistenceData(
                    ValueConstants.APP_REVISION_CLEANER_ENABLED_DATA_ID, "true"),
                OperationStatus.SUCCESS))
        .when(appRevisionCleaner.provideDataService)
        .queryProvideData(anyString());
    // doReturn(new DateNowDomain(new Date())).when(appRevisionCleaner.appRevisionMapper).getNow();
    doReturn(
            new DBResponse<>(
                PersistenceDataBuilder.createPersistenceData(
                    ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
                    "{\"serviceParams\":false,\"serviceParamsLarge\":true}"),
                OperationStatus.SUCCESS))
        .when(appRevisionCleaner.provideDataService)
        .queryProvideData(anyString());
  }

  @After
  public void afterTest() {
    appRevisionCleaner.cleaner.close();
    appRevisionCleaner.renewer.close();
  }

  @Test
  public void testDateBeforeNow() {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    Date now = new Date();
    doReturn(now).when(mocked.dateNowRepository).getNow();
    Date before = mocked.dateBeforeNow(1);
    Assert.assertEquals(before.getTime(), now.getTime() - 60000);
  }

  @Test
  public void testRenew() throws Exception {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    doReturn(
            Maps.newHashMap("session1", new AppRevisionSlice(Sets.newLinkedHashSet("1", "2", "3"))))
        .when(mocked.sessionServerService)
        .broadcastInvoke(any(), anyInt());
    mocked.renewer.getWaitingMillis();
    mocked.renew();
    mocked.renewer.runUnthrowable();
    verify(mocked.appRevisionRepository, times(6)).heartbeatDB(anyString());
    mocked.init();
    mocked.start();
    mocked.renewer.close();
    mocked.cleaner.close();
  }

  @Test
  public void testClean() throws Exception {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    AppRevision domain = mock(AppRevision.class);
    doReturn(Lists.newArrayList(domain))
        .when(mocked.appRevisionRepository)
        .getExpired(any(), anyInt());
    doReturn(new Date()).when(mocked.dateNowRepository).getNow();
    doReturn(1).when(mocked.appRevisionRepository).cleanDeleted(any(), anyInt());
    doReturn(
            Collections.singletonMap(
                "localhost", new AppRevisionSlice(Collections.singleton("test-123"))))
        .when(mocked.sessionServerService)
        .broadcastInvoke(any(), anyInt());
    mocked.renew();
    mocked.renew();
    mocked.markDeleted();
    mocked.cleanup();
    verify(domain, times(1)).setDeleted(true);
    verify(mocked.appRevisionRepository, times(1)).replace(domain);
    mocked.cleaner.getWaitingMillis();
    mocked.cleaner.runUnthrowable();
    mocked.becomeLeader();
    mocked.loseLeader();
  }

  @Test
  public void testNextSlotId() {
    for (int i = 0; i < 1000; i++) {
      int slotId = appRevisionCleaner.nextSlotId();
      if (slotId < 0 || slotId >= 256) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testDigest() {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    doReturn(Maps.newHashMap("aaaa", 30)).when(mocked.appRevisionRepository).countByApp();
    mocked.digestAppRevision();
  }
}
