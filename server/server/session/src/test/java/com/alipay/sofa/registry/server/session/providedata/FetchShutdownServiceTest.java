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

import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch.CauseEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : FetchShutdownServiceTest.java, v 0.1 2021年10月14日 20:32 xiaojian.xj Exp $
 */
public class FetchShutdownServiceTest extends FetchShutdownService {

  SessionServerConfig sessionServerConfig;

  SessionServerBootstrap sessionServerBootstrap;

  FetchStopPushService fetchStopPushService;

  @Before
  public void beforeFetchShutdownServiceTest() {
    sessionServerConfig = mock(SessionServerConfig.class);
    sessionServerBootstrap = mock(SessionServerBootstrap.class);
    fetchStopPushService = mock(FetchStopPushService.class);

    this.setSessionServerConfig(sessionServerConfig)
        .setSessionServerBootstrap(sessionServerBootstrap)
        .setFetchStopPushService(fetchStopPushService);
  }

  @Test
  public void test() {
    Assert.assertFalse(isShutdown());

    ShutdownSwitch shutdownSwitch = new ShutdownSwitch(true, CauseEnum.FORCE.getCause());

    when(fetchStopPushService.isStopPushSwitch()).thenReturn(false);
    Assert.assertFalse(doProcess(storage.get(), new ShutdownStorage(2L, shutdownSwitch)));
    Assert.assertFalse(isShutdown());
    verify(sessionServerBootstrap, times(0)).destroy();

    when(fetchStopPushService.isStopPushSwitch()).thenReturn(true);
    Assert.assertTrue(doProcess(storage.get(), new ShutdownStorage(2L, shutdownSwitch)));

    verify(sessionServerBootstrap, times(1)).destroy();
  }
}
