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
package com.alipay.sofa.registry.server.session.strategy.impl;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.providedata.ConfigProvideDataWatcher;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultSessionRegistryStrategyTest extends AbstractSessionServerTestBase {

  private DefaultSessionRegistryStrategy strategy = new DefaultSessionRegistryStrategy();
  private final String dataId = "testWatcherDataId";

  @Before
  public void beoforeDefaultSessionRegistryStrategyTest() {
    sessionServerConfig.setWatchConfigEnable(false);
    sessionServerConfig.setScanWatcherIntervalMillis(10);
    strategy.sessionServerConfig = sessionServerConfig;
    strategy.firePushService = mock(FirePushService.class);
    strategy.pushSwitchService = mock(PushSwitchService.class);
    strategy.sessionWatchers = mock(Watchers.class);
    strategy.configProvideDataWatcher = mock(ConfigProvideDataWatcher.class);
  }

  @Test
  public void testAfterPublisherRegister() {
    strategy.afterPublisherRegister(new Publisher());
  }

  @Test
  public void testAfterWatcherRegisterDisable() {
    sessionServerConfig.setWatchConfigEnable(false);
    Watcher w = TestUtils.newWatcher(dataId);
    strategy.afterWatcherRegister(w);
    verify(strategy.firePushService, times(0)).fireOnWatcher(any(), any());

    when(strategy.pushSwitchService.canIpPushLocal(anyString())).thenReturn(true);
    strategy.afterWatcherRegister(w);
    verify(strategy.firePushService, times(1)).fireOnWatcher(any(), any());
    verify(strategy.configProvideDataWatcher, times(0)).watch(any());
  }

  @Test
  public void testAfterWatcherRegisterEnable() {
    sessionServerConfig.setWatchConfigEnable(true);
    Watcher w = TestUtils.newWatcher(dataId);
    strategy.afterWatcherRegister(w);
    verify(strategy.configProvideDataWatcher, times(1)).watch(any());
    verify(strategy.firePushService, times(0)).fireOnWatcher(any(), any());

    when(strategy.pushSwitchService.canIpPushLocal(anyString())).thenReturn(true);
    strategy.afterWatcherRegister(w);
    verify(strategy.firePushService, times(0)).fireOnWatcher(any(), any());

    when(strategy.configProvideDataWatcher.get(anyString()))
        .thenReturn(new ProvideData(null, "dataId", 100L));
    strategy.afterWatcherRegister(w);
    verify(strategy.firePushService, times(1)).fireOnWatcher(any(), any());
  }

  @Test
  public void testFilter() {
    Assert.assertNull(strategy.filter());
    Watcher w = TestUtils.newWatcher(dataId);
    when(strategy.sessionWatchers.getDataList()).thenReturn(Collections.singletonList(w));
    Tuple<Set<String>, List<Watcher>> t = strategy.filter();
    Assert.assertEquals(t.o1, Sets.newHashSet(w.getDataInfoId()));
    Assert.assertEquals(t.o2.get(0), w);
    sessionServerConfig.setWatchConfigEnable(true);
    strategy.watcherScanDog.runUnthrowable();

    w.setClientVersion(BaseInfo.ClientVersion.MProtocolpackage);
    t = strategy.filter();
    Assert.assertEquals(0, t.o1.size());
    Assert.assertEquals(0, t.o2.size());

    strategy.watcherScanDog.runUnthrowable();
    strategy.afterWatcherRegister(w);
  }

  @Test
  public void testProcess() {
    Watcher w = TestUtils.newWatcher(dataId);
    Assert.assertTrue(strategy.processWatchWhenWatchConfigDisable(w));
    verify(strategy.firePushService, times(1)).fireOnWatcher(any(), any());
    w.updatePushedVersion(10);
    Assert.assertFalse(strategy.processWatchWhenWatchConfigDisable(w));

    // reset watcher
    w = TestUtils.newWatcher(dataId);
    Assert.assertFalse(strategy.processWatchWhenWatchConfigEnable(w));
    ProvideData data = new ProvideData(null, dataId, 10L);
    when(strategy.configProvideDataWatcher.get(anyString())).thenReturn(data);
    Assert.assertTrue(strategy.processWatchWhenWatchConfigEnable(w));
    verify(strategy.firePushService, times(2)).fireOnWatcher(any(), any());
    w.updatePushedVersion(10);

    Assert.assertFalse(strategy.processWatchWhenWatchConfigEnable(w));
    verify(strategy.firePushService, times(2)).fireOnWatcher(any(), any());

    data = new ProvideData(null, dataId, 20L);
    when(strategy.configProvideDataWatcher.get(anyString())).thenReturn(data);
    Assert.assertTrue(strategy.processWatchWhenWatchConfigEnable(w));
    verify(strategy.firePushService, times(3)).fireOnWatcher(any(), any());
    w.updatePushedVersion(20);
    Assert.assertFalse(strategy.processWatch(w, false));
    Assert.assertFalse(strategy.processWatch(w, true));
  }
}
