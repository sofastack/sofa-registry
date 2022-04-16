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
package com.alipay.sofa.registry.server.meta.lease.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultMetaLeaderElector;
import com.google.common.collect.ImmutableMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CrossDcMetaServerManagerTest extends AbstractMetaServerTestBase {

  private DefaultCrossDcMetaServerManager crossDcMetaServerManager;

  @Mock private NodeConfig nodeConfig;

  @Mock private MetaServerConfig metaServerConfig;

  @Mock private Exchange boltExchange;

  @Before
  public void beforeCrossDcMetaServerManagerTest() {
    MockitoAnnotations.initMocks(this);
    crossDcMetaServerManager =
        new DefaultCrossDcMetaServerManager()
            .setMetaServerConfig(metaServerConfig)
            .setExchange(boltExchange)
            .setExecutors(executors)
            .setNodeConfig(nodeConfig)
            .setScheduled(scheduled);
    when(metaServerConfig.getCrossDcMetaSyncIntervalMillis()).thenReturn(10000);
  }

  @After
  public void afterCrossDcMetaServerManagerTest() throws Exception {
    crossDcMetaServerManager.preDestory();
  }

  @Test
  public void testGetOrCreate() throws InterruptedException {
    int tasks = 100;
    when(nodeConfig.getDataCenterMetaServers(getDc()))
        .thenReturn(Sets.newLinkedHashSet(randomIp(), randomIp(), randomIp()));
    CountDownLatch latch = new CountDownLatch(tasks);
    CyclicBarrier barrier = new CyclicBarrier(tasks);
    for (int i = 0; i < tasks; i++) {
      executors.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                barrier.await();
              } catch (Exception ignore) {
              }
              crossDcMetaServerManager.getOrCreate(getDc());
              latch.countDown();
            }
          });
    }
    latch.await();
    Assert.assertEquals(1, crossDcMetaServerManager.getCrossDcMetaServers().size());
    Assert.assertTrue(crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().canStart());
    Assert.assertTrue(
        crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isInitialized());
  }

  @Test
  public void testRemove() {}

  @Test
  public void testIsLeader() throws Exception {
    when(nodeConfig.getDataCenterMetaServers(getDc()))
        .thenReturn(Sets.newLinkedHashSet(randomIp(), randomIp(), randomIp()));
    when(nodeConfig.getMetaNodeIP())
        .thenReturn(
            ImmutableMap.of(
                getDc(),
                Lists.newArrayList(randomIp(), randomIp(), randomIp()),
                "dc2",
                Lists.newArrayList(randomIp(), randomIp(), randomIp()),
                "dc3",
                Lists.newArrayList(randomIp(), randomIp(), randomIp())));
    crossDcMetaServerManager.metaLeaderService = mock(DefaultMetaLeaderElector.class);
    crossDcMetaServerManager.postConstruct();
    crossDcMetaServerManager.becomeLeader();
    waitConditionUntilTimeOut(
        () -> crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isStarted(), 1000);
    // wait for concurrent modification
    Thread.sleep(10);
    Assert.assertTrue(
        crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isStarted());
    for (CrossDcMetaServer metaServer : crossDcMetaServerManager.getCrossDcMetaServers().values()) {
      Assert.assertTrue(metaServer.getLifecycleState().isStarted());
    }
  }

  @Test
  public void testNotLeader() throws InitializeException, TimeoutException, InterruptedException {
    when(nodeConfig.getDataCenterMetaServers(getDc()))
        .thenReturn(Sets.newLinkedHashSet(randomIp(), randomIp(), randomIp()));
    when(nodeConfig.getMetaNodeIP())
        .thenReturn(
            ImmutableMap.of(
                getDc(),
                Lists.newArrayList(randomIp(), randomIp(), randomIp()),
                "dc2",
                Lists.newArrayList(randomIp(), randomIp(), randomIp()),
                "dc3",
                Lists.newArrayList(randomIp(), randomIp(), randomIp())));
    LifecycleHelper.initializeIfPossible(crossDcMetaServerManager);
    crossDcMetaServerManager.becomeLeader();
    waitConditionUntilTimeOut(
        () -> crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isStarted(), 1000);
    // wait for concurrent modification
    Thread.sleep(10);
    Assert.assertTrue(
        crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isStarted());
    crossDcMetaServerManager.loseLeader();
    waitConditionUntilTimeOut(
        () -> crossDcMetaServerManager.getOrCreate(getDc()).getLifecycleState().isStopped(), 1000);
    // wait for concurrent modification
    Thread.sleep(10);
    for (CrossDcMetaServer metaServer : crossDcMetaServerManager.getCrossDcMetaServers().values()) {
      Assert.assertTrue(metaServer.getLifecycleState().isStopped());
    }
  }
}
