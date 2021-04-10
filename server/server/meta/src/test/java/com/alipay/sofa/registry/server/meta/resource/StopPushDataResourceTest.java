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
package com.alipay.sofa.registry.server.meta.resource;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StopPushDataResourceTest {

  private StopPushDataResource stopPushDataResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataService provideDataService =
      spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void beforeStopPushDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    stopPushDataResource =
        new StopPushDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testClosePush() {
    stopPushDataResource.closePush();
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse<PersistenceData> dbResponse =
        provideDataService.queryProvideData(
            DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getDataInfoId());
    Assert.assertEquals(OperationStatus.SUCCESS, dbResponse.getOperationStatus());
    PersistenceData persistenceData = dbResponse.getEntity();
    Assert.assertTrue(Boolean.parseBoolean(persistenceData.getData()));
  }

  @Test
  public void testOpenPush() {
    stopPushDataResource.openPush();
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse<PersistenceData> dbResponse =
        provideDataService.queryProvideData(
            DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getDataInfoId());
    Assert.assertEquals(OperationStatus.SUCCESS, dbResponse.getOperationStatus());
    PersistenceData persistenceData = dbResponse.getEntity();
    Assert.assertFalse(Boolean.parseBoolean(persistenceData.getData()));
  }

  @Test
  public void testGetNodeTypes() {
    Assert.assertEquals(
        Sets.newHashSet(Node.NodeType.SESSION), stopPushDataResource.getNodeTypes());
  }
}
