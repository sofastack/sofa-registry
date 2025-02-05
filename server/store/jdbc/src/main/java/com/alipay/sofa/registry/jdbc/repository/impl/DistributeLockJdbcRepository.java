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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.elector.DistributeLockInfo;
import com.alipay.sofa.registry.jdbc.config.MetaElectorConfig;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.DistributeLockRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : DistributeLockJdbcRepository.java, v 0.1 2022年08月06日 17:07 xiaojian.xj Exp $
 */
public class DistributeLockJdbcRepository implements DistributeLockRepository, RecoverConfig {

  @Autowired DistributeLockMapper distributeLockMapper;

  @Autowired MetaElectorConfig metaElectorConfig;

  @Autowired DefaultCommonConfig defaultCommonConfig;

  @Override
  public DistributeLockInfo queryDistLock(String lockName) {
    return distributeLockMapper.queryDistLock(
        defaultCommonConfig.getClusterId(tableName()), lockName);
  }

  @Override
  public String tableName() {
    return TableEnum.DISTRIBUTE_LOCK.getTableName();
  }
}
