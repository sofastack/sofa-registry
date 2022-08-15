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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultMetaLeaderElectorTest extends AbstractMetaServerTestBase {

  private DefaultMetaLeaderElector metaLeaderElector;

  @Mock private LeaderElector leaderElector;

  @Mock private MetaServerConfig metaServerConfig;

  protected LeaderInfo leaderInfo =
      new LeaderInfo(
          System.currentTimeMillis(), ServerEnv.IP, System.currentTimeMillis() + 20 * 1000);

  @Before
  public void beforeDefaultMetaLeaderElectorTest() {
    MockitoAnnotations.initMocks(this);
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig);
    when(metaServerConfig.getMetaLeaderWarmupMillis()).thenReturn(200L);
  }

  @Test
  public void testIsWarmup() throws InterruptedException {
    when(metaServerConfig.getMetaLeaderWarmupMillis()).thenReturn(2000L);
    when(leaderElector.amILeader()).thenReturn(true);
    metaLeaderElector.leaderNotify();
    Assert.assertFalse(metaLeaderElector.isWarmuped());
    when(metaServerConfig.getMetaLeaderWarmupMillis()).thenReturn(1L);
    Thread.sleep(30);
    Assert.assertTrue(metaLeaderElector.isWarmuped());
  }

  @Test
  public void testAmILeader() {
    when(leaderElector.amILeader()).thenReturn(true);
    Assert.assertTrue(metaLeaderElector.amILeader());
    when(leaderElector.amILeader()).thenReturn(false);
    Assert.assertFalse(metaLeaderElector.amILeader());
  }

  @Test
  public void testGetLeader() {
    when(leaderElector.getLeaderInfo()).thenReturn(leaderInfo);
    Assert.assertEquals(ServerEnv.IP, metaLeaderElector.getLeader());
  }

  @Test
  public void testGetLeaderEpoch() {
    when(leaderElector.getLeaderInfo()).thenReturn(leaderInfo);
    Assert.assertEquals(leaderInfo.getEpoch(), metaLeaderElector.getLeaderEpoch());
  }

  @Test
  public void testLeaderNotify() {
    AtomicInteger leaderCounter = new AtomicInteger(0);
    AtomicInteger followerCounter = new AtomicInteger(0);
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig);
    metaLeaderElector.registerListener(
        new MetaLeaderService.MetaLeaderElectorListener() {
          @Override
          public void becomeLeader() {
            leaderCounter.incrementAndGet();
          }

          @Override
          public void loseLeader() {
            followerCounter.incrementAndGet();
          }
        });
    metaLeaderElector.leaderNotify();
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(0, followerCounter.get());
    for (int i = 0; i < 100; i++) {
      metaLeaderElector.leaderNotify();
    }
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(0, followerCounter.get());
  }

  @Test
  public void testFollowNotify() {
    AtomicInteger leaderCounter = new AtomicInteger(0);
    AtomicInteger followerCounter = new AtomicInteger(0);
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig);
    metaLeaderElector.registerListener(
        new MetaLeaderService.MetaLeaderElectorListener() {
          @Override
          public void becomeLeader() {
            leaderCounter.incrementAndGet();
          }

          @Override
          public void loseLeader() {
            followerCounter.incrementAndGet();
          }
        });
    metaLeaderElector.leaderNotify();
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(0, followerCounter.get());
    metaLeaderElector.leaderNotify();
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(0, followerCounter.get());
    metaLeaderElector.followNotify();
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(1, followerCounter.get());
    for (int i = 0; i < 100; i++) {
      metaLeaderElector.followNotify();
    }
    Assert.assertEquals(1, leaderCounter.get());
    Assert.assertEquals(1, followerCounter.get());
  }

  @Test
  public void testRegisterListener() throws Exception {
    metaLeaderElector.postConstruct();
    verify(leaderElector, atLeast(1)).registerLeaderAware(metaLeaderElector);
  }
}
