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
package com.alipay.sofa.registry.jdbc.elector;

import com.alipay.sofa.registry.common.model.elector.DistributeLockInfo;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.config.MetaElectorConfigBean;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuchen
 * @date Mar 15, 2021, 7:46:21 PM
 */
public class MetaJdbcLeaderElectorTest extends AbstractH2DbTestBase {

  private MetaJdbcLeaderElector leaderElector;

  private DistributeLockMapper distributeLockMapper;

  private DefaultCommonConfig defaultCommonConfig;

  @Before
  public void beforeMetaJdbcLeaderElectorTest() {
    leaderElector = applicationContext.getBean(MetaJdbcLeaderElector.class);
    distributeLockMapper = applicationContext.getBean(DistributeLockMapper.class);
    defaultCommonConfig = applicationContext.getBean(DefaultCommonConfig.class);
  }

  @Test
  public void testDoElect() throws TimeoutException, InterruptedException {
    Assert.assertNotNull(leaderElector);
    leaderElector.change2Follow();
    waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);
  }

  @Test
  public void testDoQuery() throws TimeoutException, InterruptedException {
    leaderElector.change2Follow();
    waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);
    AbstractLeaderElector.LeaderInfo leaderInfo = leaderElector.doQuery();
    Assert.assertEquals(leaderInfo.getLeader(), leaderElector.myself());
  }

  @Test
  public void testFollowWorking() throws TimeoutException, InterruptedException {

    leaderElector.change2Follow();
    waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);

    DistributeLockInfo domain =
        distributeLockMapper.queryDistLock(
            defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
            "META-MASTER");
    domain.setDuration(0L);
    leaderElector.onFollowWorking(domain, leaderElector.myself());
  }

  @Test
  public void testLeaderInfo() {
    DistributeLockInfo lock = new DistributeLockInfo();
    lock.setOwner("testOwner");
    lock.setGmtModified(new Date());
    lock.setDuration(1000);

    AbstractLeaderElector.LeaderInfo leaderInfo = MetaJdbcLeaderElector.leaderFrom(lock);
    Assert.assertEquals(leaderInfo.getLeader(), lock.getOwner());
    Assert.assertEquals(leaderInfo.getEpoch(), lock.getGmtModifiedUnixMillis());
    Assert.assertEquals(
        leaderInfo.getExpireTimestamp(), lock.getGmtModifiedUnixMillis() + 1000 / 2);
  }

  private static class ElectLoop extends LoopRunnable {
    private final MetaJdbcLeaderElector elector;

    private ElectLoop(MetaJdbcLeaderElector elector) {
      this.elector = elector;
    }

    @Override
    public void runUnthrowable() {
      elector.elect();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(250, TimeUnit.MILLISECONDS);
    }
  }

  private static class LeaderChecker extends LoopRunnable {
    private final List<ElectLoop> loops;

    private LeaderChecker(List<ElectLoop> loops) {
      this.loops = loops;
    }

    @Override
    public void runUnthrowable() {
      int leaderCount = 0;
      for (ElectLoop loop : loops) {
        if (loop.elector.amILeader()) {
          leaderCount++;
        }
      }
      Assert.assertTrue(StringFormatter.format("leaderCount {}", leaderCount), leaderCount <= 1);
    }

    @Override
    public void waitingUnthrowable() {
      int sleep = (int) (RandomUtils.nextFloat() * 150 + 150);
      ConcurrentUtils.sleepUninterruptibly(sleep, TimeUnit.MILLISECONDS);
    }
  }

  @Test
  public void testConcurrentComplete() {
    MetaElectorConfigBean metaElectorConfigBean = new MetaElectorConfigBean();
    metaElectorConfigBean.setLockExpireDuration(2000);
    List<ElectLoop> loops = Lists.newArrayList();
    for (int i = 0; i < 20; i++) {
      MetaJdbcLeaderElector e = new MetaJdbcLeaderElector();
      e.setAddress(StringFormatter.format("{}", i));
      e.distributeLockMapper = distributeLockMapper;

      e.defaultCommonConfig = defaultCommonConfig;
      e.metaElectorConfig = metaElectorConfigBean;
      ElectLoop loop = new ElectLoop(e);
      loops.add(loop);
    }
    // 0(旧版) 抢到leader
    loops.get(0).runUnthrowable();
    DistributeLockInfo domain =
        distributeLockMapper.queryDistLock(
            defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
            MetaJdbcLeaderElector.lockName);
    Assert.assertEquals("0", domain.getOwner());
    // 启动多个elector开始竞争
    int i = 0;
    for (ElectLoop loop : loops) {
      ConcurrentUtils.createDaemonThread(StringFormatter.format("testComplete-{}", i), loop)
          .start();
      i++;
    }
    LeaderChecker leaderChecker = new LeaderChecker(loops);
    ConcurrentUtils.createDaemonThread("LeaderChecker", leaderChecker).start();
    // 没有其他follower能抢到leader
    for (int j = 0; j < 30; j++) {
      ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
      domain =
          distributeLockMapper.queryDistLock(
              defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
              MetaJdbcLeaderElector.lockName);
      Assert.assertEquals("0", domain.getOwner());
      Assert.assertFalse(StringFormatter.format("{}", domain), domain.expire());
    }
    // 停止争抢
    for (ElectLoop loop : loops) {
      loop.suspend();
    }
    ConcurrentUtils.sleepUninterruptibly(3000, TimeUnit.MILLISECONDS);
    // 1(新版）抢到leader
    loops.get(1).runUnthrowable();
    domain =
        distributeLockMapper.queryDistLock(
            defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
            MetaJdbcLeaderElector.lockName);
    Assert.assertEquals("1", domain.getOwner());
    // 恢复争抢
    for (ElectLoop loop : loops) {
      loop.resume();
    }
    // 没有其他follower能抢到leader
    for (int j = 0; j < 30; j++) {
      ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
      domain =
          distributeLockMapper.queryDistLock(
              defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
              MetaJdbcLeaderElector.lockName);
      Assert.assertEquals("1", domain.getOwner());
      Assert.assertFalse(StringFormatter.format("{}", domain), domain.expire());
    }
    // 1 停止续约，其他follower抢到leader
    loops.get(1).suspend();
    ConcurrentUtils.sleepUninterruptibly(3000, TimeUnit.MILLISECONDS);
    domain =
        distributeLockMapper.queryDistLock(
            defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
            MetaJdbcLeaderElector.lockName);
    Assert.assertNotEquals("1", domain.getOwner());

    for (ElectLoop loop : loops) {
      loop.close();
    }
    leaderChecker.close();
  }
}
