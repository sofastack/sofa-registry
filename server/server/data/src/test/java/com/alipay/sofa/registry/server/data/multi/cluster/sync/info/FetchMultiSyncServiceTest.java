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
package com.alipay.sofa.registry.server.data.multi.cluster.sync.info;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.slot.filter.MultiSyncDataAcceptorManager;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : FetchMultiSyncServiceTest.java, v 0.1 2023年02月16日 21:44 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchMultiSyncServiceTest {

  private static final String DATACENTER = "testdc";
  private static final String REMOTE_DATACENTER_0 = "REMOTE_DATACENTER_0";
  private static final String REMOTE_DATACENTER_1 = "REMOTE_DATACENTER_1";
  private static final String REMOTE_DATACENTER_2 = "REMOTE_DATACENTER_2";

  private static final MultiClusterSyncInfo syncInfo0_version1 =
      new MultiClusterSyncInfo(
          DATACENTER,
          REMOTE_DATACENTER_0,
          "aaa",
          true,
          true,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          1);

  private static final MultiClusterSyncInfo syncInfo1_version1 =
      new MultiClusterSyncInfo(
          DATACENTER,
          REMOTE_DATACENTER_1,
          "aaa",
          true,
          true,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          1);

  private static final MultiClusterSyncInfo syncInfo0_version2 =
      new MultiClusterSyncInfo(
          DATACENTER,
          REMOTE_DATACENTER_0,
          "bbb",
          false,
          false,
          Sets.newHashSet("111"),
          Sets.newHashSet("222"),
          Sets.newHashSet("333"),
          2);

  private static final MultiClusterSyncInfo syncInfo2_version2 =
      new MultiClusterSyncInfo(
          DATACENTER,
          REMOTE_DATACENTER_2,
          "aaa",
          true,
          true,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          Collections.EMPTY_SET,
          2);

  private static Set<MultiClusterSyncInfo> syncInfos_0_1 =
      Sets.newHashSet(syncInfo0_version1, syncInfo1_version1);

  private static Set<MultiClusterSyncInfo> syncInfos_0_2 =
      Sets.newHashSet(syncInfo0_version2, syncInfo2_version2);

  @InjectMocks private FetchMultiSyncService fetchMultiSyncService;

  @Mock private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Mock private MultiClusterSyncRepository multiClusterSyncRepository;

  @Mock private MultiSyncDataAcceptorManager multiSyncDataAcceptorManager;

  @Test
  public void testFetchMultiSyncService() {

    // [datacenter0=1,datacenter1=1]
    when(multiClusterDataServerConfig.getMultiClusterConfigReloadMillis()).thenReturn(100);
    when(multiClusterSyncRepository.queryLocalSyncInfos()).thenReturn(syncInfos_0_1);
    fetchMultiSyncService.start();

    ConcurrentUtils.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);
    verify(multiSyncDataAcceptorManager, times(1)).updateFrom(anyCollection());

    Assert.assertTrue(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_0));
    Assert.assertTrue(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_0));
    Assert.assertTrue(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_1));
    Assert.assertTrue(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_1));
    Assert.assertFalse(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_2));
    Assert.assertFalse(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_2));

    MultiSegmentSyncSwitch multiSyncSwitch0 =
        fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_0);
    MultiSegmentSyncSwitch multiSyncSwitch1 =
        fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_1);
    MultiSegmentSyncSwitch multiSyncSwitch2 =
        fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_2);
    verifySyncInfo(multiSyncSwitch0, syncInfo0_version1);
    verifySyncInfo(multiSyncSwitch1, syncInfo1_version1);
    Assert.assertNull(multiSyncSwitch2);

    // [datacenter0=2,datacenter1=2]
    when(multiClusterSyncRepository.queryLocalSyncInfos()).thenReturn(syncInfos_0_2);
    ConcurrentUtils.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);
    verify(multiSyncDataAcceptorManager, times(2)).updateFrom(anyCollection());

    Assert.assertFalse(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_0));
    Assert.assertFalse(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_0));
    Assert.assertFalse(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_1));
    Assert.assertFalse(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_1));
    Assert.assertTrue(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_2));
    Assert.assertTrue(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_2));

    multiSyncSwitch0 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_0);
    multiSyncSwitch1 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_1);
    multiSyncSwitch2 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_2);
    verifySyncInfo(multiSyncSwitch0, syncInfo0_version2);
    Assert.assertNull(multiSyncSwitch1);
    verifySyncInfo(multiSyncSwitch2, syncInfo2_version2);

    // [datacenter0=1,datacenter1=1]
    when(multiClusterSyncRepository.queryLocalSyncInfos()).thenReturn(syncInfos_0_1);
    ConcurrentUtils.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);

    Assert.assertFalse(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_0));
    Assert.assertFalse(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_0));
    Assert.assertFalse(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_1));
    Assert.assertFalse(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_1));
    Assert.assertTrue(fetchMultiSyncService.multiSync(REMOTE_DATACENTER_2));
    Assert.assertTrue(fetchMultiSyncService.multiPush(REMOTE_DATACENTER_2));

    multiSyncSwitch0 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_0);
    multiSyncSwitch1 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_1);
    multiSyncSwitch2 = fetchMultiSyncService.getMultiSyncSwitch(REMOTE_DATACENTER_2);
    verifySyncInfo(multiSyncSwitch0, syncInfo0_version2);
    Assert.assertNull(multiSyncSwitch1);
    verifySyncInfo(multiSyncSwitch2, syncInfo2_version2);
  }

  private void verifySyncInfo(MultiSegmentSyncSwitch syncSwitch, MultiClusterSyncInfo syncInfo) {
    Assert.assertEquals(syncSwitch.isMultiSync(), syncInfo.isEnableSyncDatum());
    Assert.assertEquals(syncSwitch.isMultiPush(), syncInfo.isEnablePush());
    Assert.assertEquals(syncSwitch.getRemoteDataCenter(), syncInfo.getRemoteDataCenter());
    Assert.assertEquals(syncSwitch.getSynPublisherGroups(), syncInfo.getSynPublisherGroups());
    Assert.assertEquals(syncSwitch.getSyncDataInfoIds(), syncInfo.getSyncDataInfoIds());
    Assert.assertEquals(syncSwitch.getIgnoreDataInfoIds(), syncInfo.getIgnoreDataInfoIds());
    Assert.assertEquals(syncSwitch.getDataVersion(), syncInfo.getDataVersion());
  }
}
