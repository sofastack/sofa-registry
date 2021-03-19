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
package com.alipay.sofa.registry.test.elector;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.jdbc.config.MetaElectorConfigBean;
import com.alipay.sofa.registry.jdbc.elector.MetaJdbcLeaderElector;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version $Id: MetaElectorTest.java, v 0.1 2021年03月15日 14:04 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class MetaElectorTest extends BaseIntegrationTest {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private MetaJdbcLeaderElector leaderElector1;

  private MetaJdbcLeaderElector leaderElector2;

  private MetaJdbcLeaderElector leaderElector3;

  private DistributeLockMapper distributeLockMapper;

  private MetaElectorConfigBean metaElectorConfigBean;

  private String dataCenter = "DefaultDataCenter:MetaElectorTest" + System.currentTimeMillis();

  private MetaJdbcLeaderElector.ElectorRoleService leaderService;

  private MetaJdbcLeaderElector.ElectorRoleService followService;

  private CountDownLatch countDownLatch = new CountDownLatch(1);

  @Before
  public void beforeMetaElectorTest() {
    MockitoAnnotations.initMocks(this);

    distributeLockMapper =
        metaApplicationContext.getBean("distributeLockMapper", DistributeLockMapper.class);
    metaElectorConfigBean =
        metaApplicationContext.getBean("metaElectorConfig", MetaElectorConfigBean.class);
    leaderService =
        metaApplicationContext.getBean(
            "leaderService", MetaJdbcLeaderElector.ElectorRoleService.class);
    followService =
        metaApplicationContext.getBean(
            "followService", MetaJdbcLeaderElector.ElectorRoleService.class);

    leaderElector1 = buildLeaderElector();
    leaderElector2 = buildLeaderElector();
    leaderElector3 = buildLeaderElector();

    when(leaderElector1.myself()).thenReturn("127.0.0.1:12101");
    when(leaderElector2.myself()).thenReturn("127.0.0.1:12102");
    when(leaderElector3.myself()).thenReturn("127.0.0.1:12103");
  }

  private MetaJdbcLeaderElector buildLeaderElector() {
    MetaJdbcLeaderElector leaderElector = spy(new MetaJdbcLeaderElector());
    metaElectorConfigBean.setLockExpireDuration(3000L);
    metaElectorConfigBean.setDataCenter(dataCenter);
    leaderElector.addElectorRoleService(leaderService);
    leaderElector.addElectorRoleService(followService);
    leaderElector.setDistributeLockMapper(distributeLockMapper);
    leaderElector.setMetaElectorConfig(metaElectorConfigBean);
    return leaderElector;
  }

  @Test
  public void testElector() throws InterruptedException {
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
    ConcurrentUtils.createDaemonThread("LeaderElectorTrigger", new LeaderElectorTrigger()).start();

    leaderElector1.change2Follow();
    leaderElector2.change2Follow();
    leaderElector3.change2Follow();

    Thread.sleep(5 * 1000);
    logger.info("leaderElector1 leader: {}", leaderElector1.getLeader());
    logger.info("leaderElector2 leader: {}", leaderElector2.getLeader());
    logger.info("leaderElector3 leader: {}", leaderElector3.getLeader());
    Assert.assertTrue(
        leaderElector1.amILeader() || leaderElector2.amILeader() || leaderElector3.amILeader());

    leaderElector1.change2Observer();
    leaderElector2.change2Observer();

    Thread.sleep(5 * 1000);

    logger.info("leaderElector1 leader: {}", leaderElector1.getLeader());
    logger.info("leaderElector2 leader: {}", leaderElector2.getLeader());
    logger.info("leaderElector3 leader: {}", leaderElector3.getLeader());
    Assert.assertTrue(!leaderElector1.amILeader());
    Assert.assertTrue(StringUtil.equals(leaderElector1.getLeader(), leaderElector3.myself()));
    Assert.assertTrue(!leaderElector2.amILeader());
    Assert.assertTrue(StringUtil.equals(leaderElector2.getLeader(), leaderElector3.myself()));
    Assert.assertTrue(leaderElector3.amILeader());
    Assert.assertTrue(StringUtil.equals(leaderElector3.getLeader(), leaderElector3.myself()));
  }

  class LeaderElectorTrigger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

      fixedThreadPool.submit(
          () -> {
            leaderElector1.elect();
          });
      fixedThreadPool.submit(
          () -> {
            leaderElector2.elect();
          });
      fixedThreadPool.submit(
          () -> {
            leaderElector3.elect();
          });
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  public static URL randomURL() {
    return new URL("127.0.0.1", randomPort());
  }

  public static int randomPort() {
    return randomPort(10000, 20000, null);
  }

  public static int randomPort(int min, int max, Set<Integer> different) {

    Random random = new Random();

    for (int i = min; i <= max; i++) {
      int port = min + random.nextInt(max - min + 1);
      if ((different == null || !different.contains(port)) && isUsable(port)) {
        return port;
      }
    }

    throw new IllegalStateException(String.format("random port not found:(%d, %d)", min, max));
  }

  protected static boolean isUsable(int port) {

    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(port));
      return true;
    } catch (IOException e) {
    }
    return false;
  }
}
