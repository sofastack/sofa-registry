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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DatumCacheTest {
  private static final String testDataId = TestBaseUtils.TEST_DATA_ID;
  private static final String testDataInfoId = TestBaseUtils.TEST_DATA_INFO_ID;
  private static final String testDc = "localDc";

  @Test
  public void test() {
    DatumCache cache = TestBaseUtils.newLocalDatumCache(testDc, true);
    LocalDatumStorage storage = (LocalDatumStorage) cache.getLocalDatumStorage();

    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    storage.put(publisher);

    Datum datum = cache.get("xx", publisher.getDataInfoId());
    TestBaseUtils.assertEquals(datum, publisher);
    Map<String, Map<String, Datum>> datumMap = cache.getAll();
    TestBaseUtils.assertEquals(datumMap.get(testDc).get(publisher.getDataInfoId()), publisher);

    Map<String, Map<String, List<Publisher>>> publisherMaps = cache.getAllPublisher();
    Assert.assertTrue(publisherMaps.get(testDc).get(publisher.getDataInfoId()).contains(publisher));

    Map<String, Publisher> publisherMap = cache.getByConnectId(publisher.connectId());
    Assert.assertTrue(publisherMap.get(publisher.getRegisterId()) == publisher);

    DatumVersion v = cache.getVersion("xx", publisher.getDataInfoId());
    Assert.assertEquals(v.getValue(), datum.getVersion());

    final int slotId = SlotFunctionRegistry.getFunc().slotOf(publisher.getDataInfoId());
    Map<String, DatumVersion> versionMap = cache.getVersions("xx", slotId, null);
    Assert.assertEquals(versionMap.get(publisher.getDataInfoId()).getValue(), datum.getVersion());
    v = cache.updateVersion("xx", publisher.getDataInfoId());
    Assert.assertTrue(v.getValue() > datum.getVersion());

    cache.clean("xx", publisher.getDataInfoId());

    datum = cache.get("xx", publisher.getDataInfoId());
    Assert.assertTrue(datum.getPubMap().isEmpty());
    Assert.assertTrue(v.getValue() < datum.getVersion());
  }
}
