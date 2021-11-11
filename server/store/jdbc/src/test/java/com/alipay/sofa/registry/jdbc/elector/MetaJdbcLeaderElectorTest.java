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

import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.DistributeLockDomain;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import java.util.Date;
import java.util.concurrent.TimeoutException;
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

    DistributeLockDomain domain =
        distributeLockMapper.queryDistLock(
            defaultCommonConfig.getClusterId(TableEnum.DISTRIBUTE_LOCK.getTableName()),
            "META-MASTER");
    domain.setDuration(0L);
    leaderElector.onFollowWorking(domain, leaderElector.myself());
  }

  @Test
  public void testLeaderInfo() {
    DistributeLockDomain lock = new DistributeLockDomain();
    lock.setOwner("testOwner");
    lock.setGmtModified(new Date());
    lock.setDuration(1000);

    AbstractLeaderElector.LeaderInfo leaderInfo = MetaJdbcLeaderElector.leaderFrom(lock);
    Assert.assertEquals(leaderInfo.getLeader(), lock.getOwner());
    Assert.assertEquals(leaderInfo.getEpoch(), lock.getGmtModified().getTime());
    Assert.assertEquals(
        leaderInfo.getExpireTimestamp(), lock.getGmtModified().getTime() + 1000 / 2);
  }
}
