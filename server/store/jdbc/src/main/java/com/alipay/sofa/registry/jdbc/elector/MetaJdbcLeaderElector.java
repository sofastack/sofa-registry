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
import com.alipay.sofa.registry.jdbc.config.MetaElectorConfig;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.FollowCompeteLockDomain;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: MetaJdbcLeaderElector.java, v 0.1 2021年03月12日 10:18 xiaojian.xj Exp $
 */
public class MetaJdbcLeaderElector extends AbstractLeaderElector implements RecoverConfig {

  private static final Logger LOG =
      LoggerFactory.getLogger("META-ELECTOR", "[MetaJdbcLeaderElector]");

  public static final String lockName = "META-MASTER";

  @Autowired DistributeLockMapper distributeLockMapper;

  @Autowired MetaElectorConfig metaElectorConfig;

  @Autowired DefaultCommonConfig defaultCommonConfig;

  /**
   * start elect, return current leader
   *
   * @return LeaderInfo
   */
  @Override
  protected LeaderInfo doElect() {
    DistributeLockInfo lock =
        distributeLockMapper.queryDistLock(defaultCommonConfig.getClusterId(tableName()), lockName);

    /** compete and return leader */
    if (lock == null) {
      return competeLeader(defaultCommonConfig.getClusterId(tableName()));
    }

    ElectorRole role = amILeader(lock.getOwner()) ? ElectorRole.LEADER : ElectorRole.FOLLOWER;
    if (role == ElectorRole.LEADER) {
      lock = onLeaderWorking(lock, myself());
    } else {
      lock = onFollowWorking(lock, myself());
    }
    LeaderInfo result = leaderFrom(lock);
    LOG.info("meta role : {}, leaderInfo: {}", role, result);
    return result;
  }

  /**
   * compete and return leader
   *
   * @param dataCenter
   * @return
   */
  private LeaderInfo competeLeader(String dataCenter) {
    DistributeLockInfo lock =
        new DistributeLockInfo(
            dataCenter, lockName, myself(), metaElectorConfig.getLockExpireDuration());
    try {
      // throw exception if insert fail
      distributeLockMapper.competeLockOnInsert(lock);
      // compete finish.
      lock = distributeLockMapper.queryDistLock(dataCenter, lockName);

      LOG.info("meta: {} compete success, become leader.", myself());
    } catch (Throwable t) {
      // compete leader error, query current leader
      lock = distributeLockMapper.queryDistLock(dataCenter, lockName);
      LOG.info("meta: {} compete error, leader is: {}.", myself(), lock.getOwner());
    }
    return leaderFrom(lock);
  }

  public static LeaderInfo leaderFrom(DistributeLockInfo lock) {
    return calcLeaderInfo(
        lock.getOwner(),
        lock.getGmtModifiedUnixMillis(),
        lock.getGmtModifiedUnixMillis(),
        lock.getDuration());
  }
  /**
   * query current leader
   *
   * @return LeaderInfo
   */
  @Override
  protected LeaderInfo doQuery() {
    DistributeLockInfo lock =
        distributeLockMapper.queryDistLock(defaultCommonConfig.getClusterId(tableName()), lockName);
    if (lock == null) {
      return LeaderInfo.HAS_NO_LEADER;
    }

    return leaderFrom(lock);
  }

  private DistributeLockInfo onLeaderWorking(DistributeLockInfo lock, String myself) {

    try {
      /** as leader, do heartbeat */
      distributeLockMapper.ownerHeartbeat(lock);
      LOG.info("leader heartbeat: {}", myself);
      return distributeLockMapper.queryDistLock(lock.getDataCenter(), lock.getLockName());
    } catch (Throwable t) {
      LOG.error("leader:{} heartbeat error.", myself, t);
    }
    return lock;
  }

  public DistributeLockInfo onFollowWorking(DistributeLockInfo lock, String myself) {
    /** as follow, do compete if lock expire */
    if (lock.expire()) {
      LOG.info("lock expire: {}, meta elector start: {}", lock, myself);
      distributeLockMapper.competeLockOnUpdate(
          new FollowCompeteLockDomain(
              lock.getDataCenter(),
              lock.getLockName(),
              lock.getOwner(),
              lock.getGmtModified(),
              myself,
              lock.getDuration(),
              lock.getTerm(),
              lock.getTermDuration()));
      DistributeLockInfo newLock =
          distributeLockMapper.queryDistLock(lock.getDataCenter(), lock.getLockName());
      LOG.info("elector finish, new lock: {}", lock);
      return newLock;
    }
    return lock;
  }

  @Override
  public String tableName() {
    return TableEnum.DISTRIBUTE_LOCK.getTableName();
  }
}
