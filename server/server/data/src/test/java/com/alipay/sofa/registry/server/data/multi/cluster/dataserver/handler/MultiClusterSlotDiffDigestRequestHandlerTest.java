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
package com.alipay.sofa.registry.server.data.multi.cluster.dataserver.handler;

import static com.alipay.sofa.registry.server.data.TestBaseUtils.randPublishers;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccess.Status;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotDiffDigestRequestHandlerTest.java, v 0.1 2023年02月08日 16:39 xiaojian.xj
 *     Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiClusterSlotDiffDigestRequestHandlerTest {

  private static final String DC = "DC";
  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @InjectMocks
  private MultiClusterSlotDiffDigestRequestHandler multiClusterSlotDiffDigestRequestHandler;

  @Mock protected SlotManager slotManager;

  @Mock protected DataServerConfig dataServerConfig;

  @Mock private DatumStorageDelegate datumStorageDelegate;

  private static DataSlotDiffDigestRequest request(
      int slotId, Map<String, DatumDigest> datumDigest) {
    return new DataSlotDiffDigestRequest(DC, 1, slotId, 1, datumDigest, ACCEPT_ALL);
  }

  @Test
  public void testHandle() {
    DataSlotDiffDigestRequest request = request(1, Collections.emptyMap());

    // not slot leader
    when(slotManager.isLeader(anyString(), anyInt())).thenReturn(false);
    GenericResponse response =
        (GenericResponse) multiClusterSlotDiffDigestRequestHandler.doHandle(null, request);
    Assert.assertFalse(response.isSuccess());
    verify(slotManager, times(0)).triggerUpdateSlotTable(anyLong());
    verify(slotManager, times(0)).checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong());

    // slot leader but Migrating
    when(slotManager.isLeader(anyString(), anyInt())).thenReturn(true);
    when(slotManager.checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(
            new SlotAccess(
                1, System.currentTimeMillis(), Status.Migrating, System.currentTimeMillis()));
    response = (GenericResponse) multiClusterSlotDiffDigestRequestHandler.doHandle(null, request);
    Assert.assertFalse(response.isSuccess());
    verify(slotManager, times(0)).triggerUpdateSlotTable(anyLong());
    verify(slotManager, times(1)).checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong());

    // slot leader and accept
    when(slotManager.checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong()))
        .thenReturn(
            new SlotAccess(
                1, System.currentTimeMillis(), Status.Accept, System.currentTimeMillis()));
    Map<String, Integer> m = Maps.newHashMap();
    m.put("a", 100);
    m.put("b", 200);
    Map<String, Map<String, Publisher>> publishers = randPublishers(m);
    Map<String, DatumSummary> summaryMap = PublisherUtils.getDatumSummary(publishers, ACCEPT_ALL);
    Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(summaryMap);
    when(datumStorageDelegate.getPublishers(anyString(), anyInt())).thenReturn(publishers);

    request = request(1, digestMap);
    response = (GenericResponse) multiClusterSlotDiffDigestRequestHandler.doHandle(null, request);
    Assert.assertTrue(response.isSuccess());
    DataSlotDiffDigestResult result = (DataSlotDiffDigestResult) response.getData();
    Assert.assertTrue(result.getAddedDataInfoIds().isEmpty());
    Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());
    verify(slotManager, times(3)).checkSlotAccess(anyString(), anyInt(), anyLong(), anyLong());
  }
}
