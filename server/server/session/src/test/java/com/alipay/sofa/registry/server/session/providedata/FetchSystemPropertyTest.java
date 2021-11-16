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
package com.alipay.sofa.registry.server.session.providedata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.shared.providedata.SystemPropertyProcessorManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: FetchSystemPropertyTest.java, v 0.1 2021年06月04日 16:51 xiaojian.xj Exp $
 */
public class FetchSystemPropertyTest {

  FetchStopPushService fetchStopPushService;
  FetchGrayPushSwitchService fetchGrayPushSwitchService;
  FetchBlackListService fetchBlackListService;
  FetchClientOffAddressService fetchClientOffAddressService;

  @Before
  public void beforeTest() {
    fetchStopPushService = mock(FetchStopPushService.class);
    fetchGrayPushSwitchService = mock(FetchGrayPushSwitchService.class);
    fetchBlackListService = mock(FetchBlackListService.class);
    fetchClientOffAddressService = mock(FetchClientOffAddressService.class);

    when(fetchStopPushService.start()).thenReturn(true);
    when(fetchGrayPushSwitchService.start()).thenReturn(true);
    when(fetchBlackListService.start()).thenReturn(true);
    when(fetchClientOffAddressService.start()).thenReturn(true);

    when(fetchStopPushService.support(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID))
        .thenReturn(true);
    when(fetchGrayPushSwitchService.support(ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID))
        .thenReturn(true);
    when(fetchBlackListService.support(ValueConstants.BLACK_LIST_DATA_ID)).thenReturn(true);
    when(fetchClientOffAddressService.support(ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID))
        .thenReturn(true);

    when(fetchStopPushService.doFetch()).thenReturn(true);
    when(fetchGrayPushSwitchService.doFetch()).thenReturn(true);
    when(fetchBlackListService.doFetch()).thenReturn(true);
    when(fetchClientOffAddressService.doFetch()).thenReturn(true);
  }

  @Test
  public void systemPropertyProcessorManagerTest() {
    SystemPropertyProcessorManager provideDataProcessorManager =
        new SystemPropertyProcessorManager();

    provideDataProcessorManager.addSystemDataProcessor(fetchGrayPushSwitchService);
    provideDataProcessorManager.addSystemDataProcessor(fetchBlackListService);
    provideDataProcessorManager.addSystemDataPersistenceProcessor(fetchStopPushService);
    provideDataProcessorManager.addSystemDataPersistenceProcessor(fetchClientOffAddressService);
    Assert.assertTrue(provideDataProcessorManager.startFetchMetaSystemProperty());
    Assert.assertTrue(provideDataProcessorManager.startFetchPersistenceSystemProperty());

    Assert.assertTrue(
        provideDataProcessorManager.doFetch(ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID));

    verify(fetchClientOffAddressService, times(1)).doFetch();
  }
}
