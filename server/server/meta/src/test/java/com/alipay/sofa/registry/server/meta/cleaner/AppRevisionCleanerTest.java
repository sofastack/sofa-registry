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
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.domain.DateNowDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
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
    appRevisionCleaner.appRevisionMapper = mock(AppRevisionMapper.class);
    appRevisionCleaner.sessionServerService = mock(DefaultSessionServerService.class);
    appRevisionCleaner.defaultCommonConfig = mock(DefaultCommonConfig.class);
    appRevisionCleaner.metadataConfig = mock(MetadataConfig.class);
    appRevisionCleaner.consecutiveSuccess = new ConsecutiveSuccess(2, 1000);
    when(appRevisionCleaner.metadataConfig.getRevisionRenewIntervalMinutes()).thenReturn(10000);
    when(appRevisionCleaner.defaultCommonConfig.getClusterId())
        .thenReturn("DEFAULT_LOCALDATACENTER");
    doReturn(new DateNowDomain(new Date())).when(appRevisionCleaner.appRevisionMapper).getNow();
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
    doReturn(new DateNowDomain(now)).when(mocked.appRevisionMapper).getNow();
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
    mocked.init();
    mocked.start();
    mocked.renewer.close();
    mocked.cleaner.close();
    verify(mocked.appRevisionMapper, times(6)).heartbeat(anyString(), anyString());
  }

  @Test
  public void testClean() throws Exception {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    AppRevisionDomain domain = mock(AppRevisionDomain.class);
    doReturn(Lists.newArrayList(domain))
        .when(mocked.appRevisionMapper)
        .getExpired(anyString(), any(), anyInt());
    doReturn(new DateNowDomain(new Date())).when(mocked.appRevisionMapper).getNow();
    doReturn(1).when(mocked.appRevisionMapper).cleanDeleted(anyString(), any(), anyInt());
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
    verify(mocked.appRevisionMapper, times(1)).replace(domain);
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
}
