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
package com.alipay.sofa.registry.jdbc.mapper;

import com.alipay.sofa.registry.common.model.elector.DistributeLockInfo;
import com.alipay.sofa.registry.jdbc.domain.FollowCompeteLockDomain;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: DistributeLockMapper.java, v 0.1 2021年03月12日 10:51 xiaojian.xj Exp $
 */
public interface DistributeLockMapper {

  /**
   * query by dataCenter and lockName
   *
   * @param dataCenter dataCenter
   * @param lockName lockName
   * @return DistributeLockInfo
   */
  public DistributeLockInfo queryDistLock(
      @Param("dataCenter") String dataCenter, @Param("lockName") String lockName);

  /**
   * compete lock, it will throw exception if lockName existed
   *
   * @param lock lock
   * @throws Exception Exception
   */
  public void competeLockOnInsert(DistributeLockInfo lock) throws Exception;

  /**
   * compete lock with cas
   *
   * @param competeLock competeLock
   */
  public void competeLockOnUpdate(FollowCompeteLockDomain competeLock);

  /**
   * renew lock last update time
   *
   * @param lock lock
   */
  public void ownerHeartbeat(DistributeLockInfo lock);

  /**
   * force reset owner and duration
   *
   * @param lock lock
   */
  public void forceRefresh(DistributeLockInfo lock);
}
