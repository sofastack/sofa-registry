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

import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
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
    appRevisionCleaner.defaultCommonConfig = mock(DefaultCommonConfig.class);
    appRevisionCleaner.metadataConfig = mock(MetadataConfig.class);
    when(appRevisionCleaner.metadataConfig.getRevisionRenewIntervalMinutes()).thenReturn(10000);
    when(appRevisionCleaner.defaultCommonConfig.getClusterId())
        .thenReturn("DEFAULT_LOCALDATACENTER");
  }

  @After
  public void afterTest() {
    appRevisionCleaner.cleaner.close();
    appRevisionCleaner.renewer.close();
  }

  @Test
  public void testRenew() throws Exception {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    doReturn(
            Maps.newHashMap("session1", new AppRevisionSlice(Sets.newLinkedHashSet("1", "2", "3"))))
        .when(mocked)
        .broadcastInvoke(any(), anyInt());
    mocked.renewer.getWaitingMillis();
    mocked.renew();
    mocked.renewer.runUnthrowable();
    mocked.init();
    mocked.renewer.close();
    mocked.cleaner.close();
    verify(mocked.appRevisionMapper, times(3)).heartbeat(anyString(), anyString());
  }

  @Test
  public void testClean() {
    AppRevisionCleaner mocked = spy(appRevisionCleaner);
    AppRevisionDomain domain = mock(AppRevisionDomain.class);
    doReturn(Lists.newArrayList(domain))
        .when(mocked.appRevisionMapper)
        .getExpired(anyString(), any(), anyInt());
    doReturn(1).when(mocked.appRevisionMapper).cleanDeleted(anyString(), any(), anyInt());
    mocked.markDeleted();
    mocked.cleanup();
    verify(domain, times(1)).setDeleted(true);
    verify(mocked.appRevisionMapper, times(1)).replace(domain);
    mocked.cleaner.getWaitingMillis();
    mocked.cleaner.runUnthrowable();
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
