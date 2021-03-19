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

import com.alipay.sofa.registry.jdbc.domain.DistributeLockDomain;
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
   * @param dataCenter
   * @param lockName
   * @return
   */
  public DistributeLockDomain queryDistLock(
      @Param("dataCenter") String dataCenter, @Param("lockName") String lockName);

  /**
   * compete lock, it will throw exception if lockName existed
   *
   * @param lock
   */
  public void competeLockOnInsert(DistributeLockDomain lock) throws Exception;

  /**
   * compete lock with cas
   *
   * @param competeLock
   * @return
   */
  public void competeLockOnUpdate(FollowCompeteLockDomain competeLock);

  /** renew lock last update time */
  public void ownerHeartbeat(DistributeLockDomain lock);

  /** force reset owner and duration */
  public void forceRefresh(DistributeLockDomain lock);
}
