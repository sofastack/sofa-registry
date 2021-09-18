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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.metaserver.CompressDatumSwitch;
import com.alipay.sofa.registry.common.model.metaserver.CompressPushSwitch;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CompressResourceTest {
  private CompressResource compressResource;
  @Mock private ProvideDataService provideDataService;
  @Mock private DefaultProvideDataNotifier provideDataNotifier;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    compressResource = new CompressResource();
    compressResource.provideDataService = provideDataService;
    compressResource.provideDataNotifier = provideDataNotifier;
    PersistenceData ret = new PersistenceData();
    when(provideDataService.queryProvideData(anyString()))
        .thenReturn(new DBResponse<>(ret, OperationStatus.SUCCESS));
    when(provideDataService.saveProvideData(any())).thenReturn(true);
  }

  @Test
  public void testSetPushSwitch() {
    compressResource.setPushSwitch(new CompressPushSwitch());
    verify(provideDataNotifier, times(1)).notifyProvideDataChange(any());
    when(provideDataService.saveProvideData(any())).thenThrow(new RuntimeException());
    Assert.assertFalse(compressResource.setPushSwitch(new CompressPushSwitch()).isSuccess());
  }

  @Test
  public void testGetPushSwitch() {
    compressResource.getPushSwitch();
  }

  @Test
  public void testSetDatumSwitch() {
    compressResource.setDatumSwitch(new CompressDatumSwitch());
    verify(provideDataNotifier, times(1)).notifyProvideDataChange(any());
    when(provideDataService.saveProvideData(any())).thenThrow(new RuntimeException());
    Assert.assertFalse(compressResource.setDatumSwitch(new CompressDatumSwitch()).isSuccess());
  }

  @Test
  public void testGetDatumSwitch() {
    compressResource.getDatumSwitch();
  }
}
