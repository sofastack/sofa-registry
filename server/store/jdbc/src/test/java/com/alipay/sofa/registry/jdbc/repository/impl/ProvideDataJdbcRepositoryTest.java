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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;

public class ProvideDataJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Resource private ProvideDataRepository provideDataRepository;

  @Test
  public void testPut() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");
    boolean success = provideDataRepository.put(persistenceData);
    Assert.assertTrue(success);
    Assert.assertEquals("val", provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData.getVersion(), provideDataRepository.get(dataInfoId).getVersion());
  }

  @Test
  public void testUpdate() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData1 =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val1");
    boolean success = provideDataRepository.put(persistenceData1);
    Assert.assertTrue(success);
    Assert.assertEquals(
        persistenceData1.getData(), provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData1.getVersion(), provideDataRepository.get(dataInfoId).getVersion());

    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    PersistenceData persistenceData2 =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val2");

    success = provideDataRepository.put(persistenceData2, persistenceData1.getVersion());
    Assert.assertTrue(success);
    Assert.assertEquals(
        persistenceData2.getData(), provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData2.getVersion(), provideDataRepository.get(dataInfoId).getVersion());

    PersistenceData persistenceData3 =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val2");
    success = provideDataRepository.put(persistenceData3, persistenceData1.getVersion());
    Assert.assertFalse(success);
    Assert.assertEquals(
        persistenceData2.getData(), provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData2.getVersion(), provideDataRepository.get(dataInfoId).getVersion());
  }

  @Test
  public void testRemove() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");

    boolean success = provideDataRepository.put(persistenceData);
    Assert.assertTrue(success);
    Assert.assertEquals("val", provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData.getVersion(), provideDataRepository.get(dataInfoId).getVersion());
    boolean remove = provideDataRepository.remove(dataInfoId, persistenceData.getVersion());

    Assert.assertTrue(remove);
    Assert.assertTrue(provideDataRepository.get(dataInfoId) == null);
  }

  @Test
  public void testGetAll() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("testGetAll" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");
    boolean success = provideDataRepository.put(persistenceData);
    Assert.assertTrue(success);
    Assert.assertEquals("val", provideDataRepository.get(dataInfoId).getData());
    Assert.assertEquals(
        persistenceData.getVersion(), provideDataRepository.get(dataInfoId).getVersion());

    Map<String, PersistenceData> all = provideDataRepository.getAll();
    Assert.assertTrue(all.values().contains(persistenceData));
  }
}
