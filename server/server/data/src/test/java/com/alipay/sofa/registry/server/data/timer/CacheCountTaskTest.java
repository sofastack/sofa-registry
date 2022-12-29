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
package com.alipay.sofa.registry.server.data.timer;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import org.junit.Assert;
import org.junit.Test;

public class CacheCountTaskTest {

  @Test
  public void test() throws Exception {
    CacheCountTask task = new CacheCountTask();
    DataServerConfig cfg = TestBaseUtils.newDataConfig("testDc");
    task.setDataServerConfig(cfg);

    // npe
    Assert.assertFalse(task.count());

    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);
    task.setDatumCache(datumStorageDelegate);

    cfg.setCacheCountIntervalSecs(0);
    Assert.assertFalse(task.init());
    // empty
    Assert.assertTrue(task.count());

    cfg.setCacheCountIntervalSecs(1);
    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    datumStorageDelegate.getLocalDatumStorage().putPublisher("testDc", pub);
    // has item
    Assert.assertTrue(task.count());
    Assert.assertTrue(task.init());
  }
}
