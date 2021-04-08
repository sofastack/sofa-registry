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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProvideDataJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private ProvideDataRepository provideDataJdbcRepository;

  @Test
  public void testPut() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");
    boolean success = provideDataJdbcRepository.put(persistenceData, persistenceData.getVersion());
    Assert.assertTrue(success);
    Assert.assertEquals("val", provideDataJdbcRepository.get(dataInfoId).getData());
    Assert.assertEquals(persistenceData.getVersion(), provideDataJdbcRepository.get(dataInfoId).getVersion());
  }

  @Test
  public void testRemove() {
    long version = System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");

    boolean success = provideDataJdbcRepository.put(persistenceData, version);
    Assert.assertTrue(success);
    Assert.assertEquals("val", provideDataJdbcRepository.get(dataInfoId).getData());
    Assert.assertEquals(version, provideDataJdbcRepository.get(dataInfoId).getVersion());
    boolean remove = provideDataJdbcRepository.remove(dataInfoId, version);

    Assert.assertTrue(remove);
    Assert.assertTrue(provideDataJdbcRepository.get(dataInfoId) == null);
  }
}
