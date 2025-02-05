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
package com.alipay.sofa.registry.jdbc.version.cinfig;

import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.jdbc.version.config.BaseConfigRepository;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : BaseConfigRepositoryTest.java, v 0.1 2023年02月03日 15:38 xiaojian.xj Exp $
 */
public class BaseConfigRepositoryTest extends BaseConfigRepository<ProvideDataDomain> {
  private static final Logger LOG = LoggerFactory.getLogger(BaseConfigRepositoryTest.class);

  public BaseConfigRepositoryTest() {
    super("BaseConfigRepositoryTest", LOG);
  }

  @Override
  protected ProvideDataDomain queryExistVersion(ProvideDataDomain entry) {
    throw new RuntimeException();
  }

  @Override
  protected long insert(ProvideDataDomain entry) {
    throw new RuntimeException();
  }

  @Override
  protected int updateWithExpectVersion(ProvideDataDomain entry, long exist) {
    throw new RuntimeException();
  }

  @Test
  public void testPutNull() {
    BaseConfigRepositoryTest repository = new BaseConfigRepositoryTest();
    Assert.assertFalse(repository.put(null));
    Assert.assertFalse(repository.put(null, System.currentTimeMillis()));
  }

  @Test
  public void testException() {
    BaseConfigRepositoryTest repository = new BaseConfigRepositoryTest();
    ProvideDataDomain domain = new ProvideDataDomain();
    Assert.assertFalse(repository.put(domain));
    Assert.assertFalse(repository.put(domain, System.currentTimeMillis()));
  }
}
