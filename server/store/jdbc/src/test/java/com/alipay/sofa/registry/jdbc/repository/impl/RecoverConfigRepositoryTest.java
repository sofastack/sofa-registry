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

import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigRepository.java, v 0.1 2021年10月08日 20:55 xiaojian.xj Exp $
 */
public class RecoverConfigRepositoryTest extends AbstractH2DbTestBase {

  @Resource private RecoverConfigJdbcRepository recoverConfigJdbcRepository;

  @Test
  public void test() {
    recoverConfigJdbcRepository.save("provide_data", "stopPush");
    recoverConfigJdbcRepository.save("app_revision", "all");

    Set<String> data = recoverConfigJdbcRepository.queryKey("provide_data");
    Assert.assertTrue(data.contains("stopPush"));
    Assert.assertFalse(data.contains("blacklist"));
    data = recoverConfigJdbcRepository.queryKey("app_revision");
    Assert.assertTrue(data.contains("all"));

    recoverConfigJdbcRepository.remove("provide_data", "stopPush");
    recoverConfigJdbcRepository.doRefresh();
    data = recoverConfigJdbcRepository.queryKey("provide_data");
    Assert.assertTrue(CollectionUtils.isEmpty(data));
  }
}
