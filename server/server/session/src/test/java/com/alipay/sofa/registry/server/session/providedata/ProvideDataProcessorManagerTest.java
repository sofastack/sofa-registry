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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataProcessorManagerTest.java, v 0.1 2021年06月04日 15:30 xiaojian.xj Exp $
 */
public class ProvideDataProcessorManagerTest {

  @Test
  public void test() {
    ProvideDataProcessorManager provideDataProcessorManager = new ProvideDataProcessorManager();
    FetchStopPushService fetchStopPushService = mock(FetchStopPushService.class);

    when(fetchStopPushService.support(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID))
        .thenReturn(true);
    provideDataProcessorManager.addProvideDataProcessor(fetchStopPushService);
    provideDataProcessorManager.processData(
        new ProvideData(null, ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, anyLong()));
    verify(fetchStopPushService, times(1)).processData(anyObject());
  }
}
