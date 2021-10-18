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

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch;
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch.CauseEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : FetchStopServerServiceTest.java, v 0.1 2021年10月14日 20:32 xiaojian.xj Exp $
 */
public class FetchStopServerServiceTest extends FetchStopServerService {

  SessionServerConfig sessionServerConfig;

  SessionServerBootstrap sessionServerBootstrap;

  FetchStopPushService fetchStopPushService;

  @Before
  public void beforeFetchStopServerServiceTest() {
    sessionServerConfig = mock(SessionServerConfig.class);
    sessionServerBootstrap = mock(SessionServerBootstrap.class);
    fetchStopPushService = mock(FetchStopPushService.class);

    this.setSessionServerConfig(sessionServerConfig)
        .setSessionServerBootstrap(sessionServerBootstrap)
        .setFetchStopPushService(fetchStopPushService);
  }

  @Test
  public void test() {
    Assert.assertFalse(isStopServer());

    StopServerSwitch stopServerSwitch = new StopServerSwitch(true, CauseEnum.FORCE.getCause());

    when(fetchStopPushService.isStopPushSwitch()).thenReturn(false);
    Assert.assertFalse(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox(JsonUtils.writeValueAsString(stopServerSwitch)),
                ValueConstants.STOP_SERVER_SWITCH_DATA_ID,
                2L)));
    Assert.assertFalse(isStopServer());
    verify(sessionServerBootstrap, times(0)).destroy();

    when(fetchStopPushService.isStopPushSwitch()).thenReturn(true);
    Assert.assertTrue(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox(JsonUtils.writeValueAsString(stopServerSwitch)),
                ValueConstants.STOP_SERVER_SWITCH_DATA_ID,
                2L)));

    verify(sessionServerBootstrap, times(1)).destroy();
  }
}
