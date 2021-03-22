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

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataTest.java, v 0.1 2021年03月15日 15:32 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ProvideDataTest extends BaseIntegrationTest {

  private ProvideDataRepository provideDataRepository;

  @Before
  public void beforeProvideDataTest() {
    MockitoAnnotations.initMocks(this);

    provideDataRepository =
        metaApplicationContext.getBean("provideDataJdbcRepository", ProvideDataRepository.class);
  }

  @Test
  public void testProvideData() {
    String key = "keyA" + System.currentTimeMillis();
    String value = "valueA" + System.currentTimeMillis();
    provideDataRepository.put(key, value);

    DBResponse dbResponse = provideDataRepository.get(key);
    Assert.assertTrue(StringUtil.equals(value, (String) dbResponse.getEntity()));
  }
}
