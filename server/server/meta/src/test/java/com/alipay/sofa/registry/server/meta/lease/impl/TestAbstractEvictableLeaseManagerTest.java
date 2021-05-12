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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuchen
 * @date Apr 6, 2021, 12:01:48 PM
 */
public class TestAbstractEvictableLeaseManagerTest extends AbstractMetaServerTestBase {

  private AbstractEvictableLeaseManager<SimpleNode> leaseManager;

  private AtomicInteger evictTime = new AtomicInteger(10);

  @Before
  public void beforeTestAbstractEvictableLeaseManagerTest()
      throws TimeoutException, InterruptedException {
    makeMetaNonLeader();
    leaseManager =
        new AbstractEvictableFilterableLeaseManager<SimpleNode>() {
          @Override
          protected int getEvictBetweenMilli() {
            return evictTime.get();
          }

          @Override
          protected int getIntervalMilli() {
            return 60 * 1000;
          }
        };
    leaseManager.metaLeaderService = metaLeaderService;
  }

  @Test
  public void testEvict() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    leaseManager = spy(leaseManager);
    leaseManager.register(
        new Lease<SimpleNode>(new SimpleNode(randomIp()), 2, TimeUnit.MILLISECONDS));
    Thread.sleep(3);
    Assert.assertFalse(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertFalse(leaseManager.getExpiredLeases().isEmpty());
    leaseManager.evict();
    Assert.assertTrue(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertTrue(leaseManager.getExpiredLeases().isEmpty());
    verify(leaseManager, atLeast(2)).refreshEpoch(anyLong());
    verify(leaseManager, atLeast(1)).cancel(any());
  }

  @Test
  public void testEvictTooQuick() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    evictTime.set(60 * 1000);
    leaseManager = spy(leaseManager);
    leaseManager.register(
        new Lease<SimpleNode>(new SimpleNode(randomIp()), 2, TimeUnit.MILLISECONDS));
    Thread.sleep(3);
    Assert.assertFalse(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertFalse(leaseManager.getExpiredLeases().isEmpty());
    leaseManager.evict();
    Assert.assertTrue(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertTrue(leaseManager.getExpiredLeases().isEmpty());
    verify(leaseManager, atLeast(2)).refreshEpoch(anyLong());
    verify(leaseManager, atLeast(1)).cancel(any());

    leaseManager.register(
        new Lease<SimpleNode>(new SimpleNode(randomIp()), 2, TimeUnit.MILLISECONDS));
    Thread.sleep(3);
    leaseManager.evict();
    Assert.assertFalse(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertFalse(leaseManager.getExpiredLeases().isEmpty());
    verify(leaseManager, atMost(1)).cancel(any());
  }

  @Test
  public void testEmptyExpireEvict() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    evictTime.set(60 * 1000);
    leaseManager = spy(leaseManager);
    leaseManager.evict();
    Assert.assertTrue(leaseManager.getLeaseMeta().getClusterMembers().isEmpty());
    Assert.assertTrue(leaseManager.getExpiredLeases().isEmpty());
    verify(leaseManager, never()).refreshEpoch(anyLong());
    verify(leaseManager, never()).cancel(any());
  }

  @Test
  public void testScheduledEvict() throws Exception {
    makeMetaLeader();
    evictTime.set(10);
    leaseManager.register(
        new Lease<SimpleNode>(new SimpleNode(randomIp()), 2, TimeUnit.MILLISECONDS));
    Thread.sleep(3);
    leaseManager.initialize();
    leaseManager.start();
    waitConditionUntilTimeOut(() -> leaseManager.localRepo.isEmpty(), 100);
    leaseManager.stop();
    leaseManager.dispose();
  }
}
