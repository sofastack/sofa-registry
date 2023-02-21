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
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.store.api.elector.DistributeLockRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : DistributeLockJdbcRepositoryTest.java, v 0.1 2023年02月03日 15:19 xiaojian.xj Exp $
 */
public class DistributeLockJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private DistributeLockRepository distributeLockRepository;

  private DistributeLockMapper distributeLockMapper;

  @Before
  public void init() {
    distributeLockMapper = applicationContext.getBean(DistributeLockMapper.class);
  }

  @Test
  public void test() throws Exception {
    DistributeLockInfo lock = new DistributeLockInfo(CLUSTER_ID, "testLock", "1.1.1.1", 1000);
    distributeLockMapper.competeLockOnInsert(lock);
    DistributeLockInfo query = distributeLockRepository.queryDistLock("testLock");
    Assert.assertEquals(lock.getOwner(), query.getOwner());
  }
}
