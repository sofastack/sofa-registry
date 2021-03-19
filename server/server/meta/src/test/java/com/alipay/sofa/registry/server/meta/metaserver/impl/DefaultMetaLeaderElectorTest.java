package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTest;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.when;


public class DefaultMetaLeaderElectorTest extends AbstractMetaServerTest {

  private DefaultMetaLeaderElector metaLeaderElector;

  @Mock
  private LeaderElector leaderElector;

  @Mock
  private MetaServerConfig metaServerConfig;

  @Before
  public void beforeDefaultMetaLeaderElectorTest() {
    MockitoAnnotations.initMocks(this);
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig, null);
    when(metaServerConfig.getMetaLeaderWarmupMilli()).thenReturn(200L);
  }

  @Test
  public void testIsWarmup() throws InterruptedException {
    when(metaServerConfig.getMetaLeaderWarmupMilli()).thenReturn(2000L);
    when(leaderElector.amILeader()).thenReturn(true);
    metaLeaderElector.leaderNotify();
    Assert.assertFalse(metaLeaderElector.isWarmup());
    when(metaServerConfig.getMetaLeaderWarmupMilli()).thenReturn(1L);
    Thread.sleep(1);
    Assert.assertTrue(metaLeaderElector.isWarmup());
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
    when(leaderElector.getLeader()).thenReturn(ServerEnv.IP);
    Assert.assertEquals(ServerEnv.IP, metaLeaderElector.getLeader());
  }

  @Test
  public void testGetLeaderEpoch() {
    when(leaderElector.getLeaderEpoch()).thenReturn(0L);
    Assert.assertEquals(0L, metaLeaderElector.getLeaderEpoch());
  }

  @Test
  public void testLeaderNotify() {
    AtomicInteger leaderCounter = new AtomicInteger(0);
    AtomicInteger followerCounter = new AtomicInteger(0);
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig, Lists.newArrayList(new MetaLeaderService.MetaLeaderElectorListener() {
      @Override
      public void becomeLeader() {
        leaderCounter.incrementAndGet();
      }

      @Override
      public void loseLeader() {
        followerCounter.incrementAndGet();
      }
    }));
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
    metaLeaderElector = new DefaultMetaLeaderElector(leaderElector, metaServerConfig, Lists.newArrayList(new MetaLeaderService.MetaLeaderElectorListener() {
      @Override
      public void becomeLeader() {
        leaderCounter.incrementAndGet();
      }

      @Override
      public void loseLeader() {
        followerCounter.incrementAndGet();
      }
    }));
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

}