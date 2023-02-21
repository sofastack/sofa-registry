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
package com.alipay.sofa.registry.server.data.multi.cluster.storage;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : MultiClusterDatumServiceTest.java, v 0.1 2023年02月16日 15:38 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiClusterDatumServiceTest {

  private static final String REMOTE = "remote_dc";
  private static final String DATAINFOID = "test_id";
  private static final String GROUP = "test_group";

  @InjectMocks private MultiClusterDatumService multiClusterDatumService;

  @Mock private DatumStorageDelegate datumStorageDelegate;

  @Mock private SlotAccessorDelegate slotAccessorDelegate;

  @Mock private MultiClusterSyncRepository multiClusterSyncRepository;

  @Mock private DataChangeEventCenter dataChangeEventCenter;

  @Test
  public void testClearDataInfoId() {
    RemoteDatumClearEvent request = RemoteDatumClearEvent.dataInfoIdEvent(REMOTE, DATAINFOID);

    // enableSync = true, not clear, not notify
    MultiClusterSyncInfo syncInfo = new MultiClusterSyncInfo();
    syncInfo.setEnableSyncDatum(true);
    when(multiClusterSyncRepository.query(anyString())).thenReturn(syncInfo);
    multiClusterDatumService.clear(request);

    verify(datumStorageDelegate, times(0)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(0)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(0)).onChange(anySet(), anyObject(), anyString());

    // enableSync = false, not leader, not clear, not notify
    syncInfo = new MultiClusterSyncInfo();
    syncInfo.setEnableSyncDatum(false);
    when(multiClusterSyncRepository.query(anyString())).thenReturn(syncInfo);
    when(slotAccessorDelegate.isLeader(anyString(), anyInt())).thenReturn(false);

    multiClusterDatumService.clear(request);
    verify(datumStorageDelegate, times(0)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(0)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(0)).onChange(anySet(), anyObject(), anyString());

    // enableSync = false, leader, clear, not notify
    when(slotAccessorDelegate.isLeader(anyString(), anyInt())).thenReturn(true);
    when(datumStorageDelegate.clearPublishers(anyString(), anyString())).thenReturn(null);

    multiClusterDatumService.clear(request);
    verify(datumStorageDelegate, times(1)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(0)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(0)).onChange(anySet(), anyObject(), anyString());

    // enableSync = false, clear, notify
    when(datumStorageDelegate.clearPublishers(anyString(), anyString()))
        .thenReturn(new DatumVersion(System.currentTimeMillis()));
    multiClusterDatumService.clear(request);
    verify(datumStorageDelegate, times(2)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(0)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(1)).onChange(anySet(), anyObject(), anyString());
  }

  @Test
  public void testClearGroup() {
    RemoteDatumClearEvent request = RemoteDatumClearEvent.groupEvent(REMOTE, GROUP);

    // enableSync = true, not clear, not notify
    MultiClusterSyncInfo syncInfo = new MultiClusterSyncInfo();
    syncInfo.setEnableSyncDatum(true);
    when(multiClusterSyncRepository.query(anyString())).thenReturn(syncInfo);
    multiClusterDatumService.clear(request);

    verify(datumStorageDelegate, times(0)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(0)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(0)).onChange(anySet(), anyObject(), anyString());

    // enableSync = false, leader, clear, not notify
    syncInfo = new MultiClusterSyncInfo();
    syncInfo.setEnableSyncDatum(false);
    when(multiClusterSyncRepository.query(anyString())).thenReturn(syncInfo);
    when(datumStorageDelegate.clearGroupPublishers(anyString(), anyString())).thenReturn(null);

    multiClusterDatumService.clear(request);
    verify(datumStorageDelegate, times(0)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(1)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(0)).onChange(anySet(), anyObject(), anyString());

    // enablePush = false, clear, notify
    when(datumStorageDelegate.clearGroupPublishers(anyString(), anyString()))
        .thenReturn(Collections.singletonMap(REMOTE, new DatumVersion(System.currentTimeMillis())));
    multiClusterDatumService.clear(request);
    verify(datumStorageDelegate, times(0)).clearPublishers(anyString(), anyString());
    verify(datumStorageDelegate, times(2)).clearGroupPublishers(anyString(), anyString());
    verify(dataChangeEventCenter, times(1)).onChange(anySet(), anyObject(), anyString());
  }
}
