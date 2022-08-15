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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.pubiterator.DatumBiConsumer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class LocalDatumStorageTest {
  private static final String testDataId = TestBaseUtils.TEST_DATA_ID;
  private static final String testDataInfoId = TestBaseUtils.TEST_DATA_INFO_ID;
  private static final String testDc = "localDc";

  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @Test
  public void testNew() {
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, false);
    assertEmpty(storage);
  }

  private void assertEmpty(DatumStorage storage) {
    Datum datum = storage.get(testDc, testDataInfoId);
    Assert.assertNull(datum);

    DatumVersion v = storage.getVersion(testDc, testDataInfoId);
    Assert.assertNull(v);

    Map<String, Datum> datumMap = storage.getAll(testDc);
    Assert.assertTrue(datumMap.isEmpty());

    Map<String, List<Publisher>> publisherMap = storage.getAllPublisher(testDc);
    Assert.assertTrue(publisherMap.isEmpty());

    Map<String, DatumVersion> versionMap = storage.getVersions(testDc, 0, null);
    Assert.assertTrue(versionMap.isEmpty());

    Map<String, Map<String, Publisher>> publisherMaps = storage.getPublishers(testDc, 0);
    Assert.assertTrue(publisherMaps.isEmpty());

    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    v = storage.putPublisher(testDc, publisher);
    Assert.assertNull(v);
    v = storage.createEmptyDatumIfAbsent(testDc, testDataInfoId);
    Assert.assertNull(v);
    v = storage.putPublisher(testDc, publisher.getDataInfoId(), Lists.newArrayList(publisher));
    Assert.assertNull(v);
  }

  @Test
  public void testPut() {

    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, true);

    String sessionIp = "notFound";
    Map<String, Map<String, DatumSummary>> sessionSummaryMap = Maps.newHashMap();

    storage.foreach(
        testDc,
        0,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummaryMap, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    Map<String, DatumSummary> summaryMap = sessionSummaryMap.get(sessionIp);
    Assert.assertEquals(sessionSummaryMap.size(), 1);
    Assert.assertEquals(summaryMap.size(), 0);

    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    DatumVersion v = storage.putPublisher(testDc, publisher);
    Assert.assertNotNull(v);
    Datum datum = storage.get(testDc, publisher.getDataInfoId());
    TestBaseUtils.assertEquals(datum, publisher);
    v = storage.getVersion(testDc, publisher.getDataInfoId());
    Assert.assertEquals(v.getValue(), datum.getVersion());

    Map<String, Publisher> publisherMap = storage.getByConnectId(publisher.connectId());
    Assert.assertEquals(publisherMap.size(), 1);
    Assert.assertEquals(publisherMap.get(publisher.getRegisterId()), publisher);

    Map<String, List<Publisher>> publisherMaps = storage.getAllPublisher(testDc);
    Assert.assertEquals(publisherMaps.size(), 1);
    Assert.assertEquals(publisherMaps.get(publisher.getDataInfoId()).size(), 1);
    Assert.assertTrue(publisherMaps.get(publisher.getDataInfoId()).contains(publisher));

    Map<String, Integer> summary = storage.getPubCount(testDc);
    Assert.assertEquals(summary.size(), 1);
    Assert.assertEquals(1, summary.get(publisher.getDataInfoId()).intValue());

    Map<String, Datum> datumMap = storage.getAll(testDc);
    Assert.assertEquals(datumMap.size(), 1);
    TestBaseUtils.assertEquals(datumMap.get(publisher.getDataInfoId()), publisher);

    final int slotId = SlotFunctionRegistry.getFunc().slotOf(publisher.getDataInfoId());
    Map<String, Map<String, Publisher>> slotMaps = storage.getPublishers(testDc, slotId);
    Assert.assertEquals(slotMaps.size(), 1);
    Assert.assertEquals(slotMaps.get(publisher.getDataInfoId()).size(), 1);
    Assert.assertEquals(
        slotMaps.get(publisher.getDataInfoId()).get(publisher.getRegisterId()), publisher);

    Map<String, DatumVersion> versionMap = storage.getVersions(testDc, slotId, null);
    Assert.assertEquals(versionMap.size(), 1);
    Assert.assertEquals(versionMap.get(publisher.getDataInfoId()).getValue(), datum.getVersion());

    versionMap = storage.getVersions(testDc, slotId + 1, null);
    Assert.assertEquals(versionMap.size(), 0);

    versionMap = storage.getVersions(testDc, slotId - 1, null);
    Assert.assertEquals(versionMap.size(), 0);

    Set<ProcessId> processIds = storage.getSessionProcessIds(testDc);
    Assert.assertEquals(processIds.size(), 1);
    Assert.assertTrue(processIds.contains(publisher.getSessionProcessId()));

    sessionIp = "notFound";
    sessionSummaryMap = Maps.newHashMap();
    storage.foreach(
        testDc,
        slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummaryMap, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    summaryMap = sessionSummaryMap.get(sessionIp);
    Assert.assertEquals(sessionSummaryMap.size(), 1);
    Assert.assertEquals(summaryMap.size(), 0);

    sessionIp = publisher.getTargetAddress().getIpAddress();
    sessionSummaryMap = Maps.newHashMap();
    storage.foreach(
        testDc,
        slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummaryMap, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    summaryMap = sessionSummaryMap.get(sessionIp);
    Assert.assertEquals(summaryMap.size(), 1);
    Assert.assertEquals(
        summaryMap.get(publisher.getDataInfoId()).getDataInfoId(), publisher.getDataInfoId());
    Assert.assertEquals(summaryMap.get(publisher.getDataInfoId()).getPublisherVersions().size(), 1);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());

    summaryMap = Maps.newHashMap();
    storage.foreach(
        testDc, slotId, DatumBiConsumer.publisherGroupsBiConsumer(summaryMap, ACCEPT_ALL));

    Assert.assertEquals(summaryMap.size(), 1);
    Assert.assertEquals(
        summaryMap.get(publisher.getDataInfoId()).getDataInfoId(), publisher.getDataInfoId());
    Assert.assertEquals(summaryMap.get(publisher.getDataInfoId()).getPublisherVersions().size(), 1);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());

    for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
      storage.getSlotChangeListener(true).onSlotRemove(testDc, i, Slot.Role.Leader);
    }
    assertEmpty(storage);
  }

  @Test
  public void testRemove() {
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, true);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    Publisher publisher2 = TestBaseUtils.createTestPublisher(testDataId);

    Publisher publisher3 = TestBaseUtils.createTestPublisher(testDataId + "-3");

    storage.putPublisher(testDc, publisher);
    storage.putPublisher(testDc, publisher2);
    storage.putPublisher(testDc, publisher3);
    DatumVersion v =
        storage.removePublishers(
            testDc, publisher.getDataInfoId(), publisher2.getSessionProcessId());
    Assert.assertNotNull(v);
    Map<String, List<Publisher>> map = storage.getAllPublisher(testDc);
    Assert.assertEquals(map.size(), 2);
    Assert.assertEquals(map.get(publisher.getDataInfoId()).size(), 0);
    Assert.assertEquals(map.get(publisher3.getDataInfoId()).size(), 1);

    v =
        storage.removePublishers(
            testDc,
            publisher3.getDataInfoId(),
            publisher3.getSessionProcessId(),
            Collections.singletonMap(publisher3.getRegisterId(), publisher3.registerVersion()));
    Assert.assertNotNull(v);

    map = storage.getAllPublisher(testDc);
    Assert.assertEquals(map.size(), 2);
    Assert.assertEquals(map.get(publisher.getDataInfoId()).size(), 0);
    Assert.assertEquals(map.get(publisher3.getDataInfoId()).size(), 0);

    v = storage.putPublisher(testDc, publisher3);
    Assert.assertNull(v);

    Map<String, Integer> compacts = storage.compact(testDc, Long.MIN_VALUE);
    Assert.assertEquals(compacts.size(), 0);

    compacts = storage.compact(testDc, System.currentTimeMillis());
    Assert.assertEquals(compacts.size(), 1);
    Assert.assertTrue(compacts.containsKey(publisher3.getDataInfoId()));

    v = storage.putPublisher(testDc, publisher3);
    Assert.assertNotNull(v);
    map = storage.getAllPublisher(testDc);
    Assert.assertTrue(map.get(publisher3.getDataInfoId()).contains(publisher3));

    v =
        storage.removePublishers(
            testDc,
            publisher3.getDataInfoId(),
            publisher3.getSessionProcessId(),
            Collections.singletonMap(
                publisher3.getRegisterId(),
                RegisterVersion.of(
                    publisher3.getVersion(), publisher3.getRegisterTimestamp() - 1)));
    Assert.assertNull(v);

    v =
        storage.removePublishers(
            testDc,
            publisher3.getDataInfoId(),
            publisher3.getSessionProcessId(),
            Collections.singletonMap(
                publisher3.getRegisterId(),
                RegisterVersion.of(
                    publisher3.getVersion() - 1, publisher3.getRegisterTimestamp())));
    Assert.assertNull(v);

    v =
        storage.removePublishers(
            testDc,
            publisher3.getDataInfoId(),
            publisher3.getSessionProcessId(),
            Collections.singletonMap(
                publisher3.getRegisterId(),
                RegisterVersion.of(
                    publisher3.getVersion() + 1, publisher3.getRegisterTimestamp())));
    Assert.assertNotNull(v);
    map = storage.getAllPublisher(testDc);
    Assert.assertFalse(map.get(publisher3.getDataInfoId()).contains(publisher3));
  }

  @Test
  public void testUnpubAfterRemove() throws InterruptedException {
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, true);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    DatumVersion v = storage.putPublisher(testDc, publisher);
    Assert.assertNotNull(v);

    v =
        storage.removePublishers(
            testDc,
            publisher.getDataInfoId(),
            publisher.getSessionProcessId(),
            Collections.singletonMap(
                publisher.getRegisterId(),
                RegisterVersion.of(publisher.getVersion() + 1, publisher.getRegisterTimestamp())));
    Assert.assertNotNull(v);

    UnPublisher unpub = UnPublisher.of(publisher);
    unpub.setVersion(unpub.getVersion() + 2);
    v = storage.putPublisher(testDc, unpub);
    Assert.assertNull(v);

    UnPublisher unpub1 = UnPublisher.of(unpub);
    unpub1.setVersion(unpub1.getVersion() + 3);
    v = storage.putPublisher(testDc, unpub1);
    Assert.assertNull(v);
  }

  @Test
  public void testUpdateVersion() {
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, true);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    storage.putPublisher(testDc, publisher);
    DatumVersion v1 = storage.getVersion(testDc, publisher.getDataInfoId());
    DatumVersion v2 = storage.updateVersion(testDc, publisher.getDataInfoId());
    Assert.assertTrue(v2.getValue() > v1.getValue());
    final int slotId = SlotFunctionRegistry.getFunc().slotOf(publisher.getDataInfoId());
    storage.updateVersion(testDc, slotId);
    DatumVersion v3 = storage.getVersion(testDc, publisher.getDataInfoId());
    Assert.assertTrue(v3.getValue() > v2.getValue());
  }

  @Test
  public void testClean() {
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(testDc, true);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    Publisher publisher2 = TestBaseUtils.createTestPublisher(testDataId);
    storage.putPublisher(testDc, publisher);
    storage.putPublisher(testDc, publisher2);

    int slotId = SlotFunctionRegistry.getFunc().slotOf(publisher.getDataInfoId());
    Map<String, DatumVersion> versionMap =
        storage.cleanBySessionId(
            testDc, slotId + 1, publisher.getSessionProcessId(), CleanContinues.ALWAYS);
    Assert.assertEquals(versionMap.size(), 0);
    versionMap =
        storage.cleanBySessionId(
            testDc, slotId, publisher.getSessionProcessId(), CleanContinues.ALWAYS);
    Assert.assertEquals(versionMap.size(), 1);
    Assert.assertTrue(versionMap.containsKey(publisher.getDataInfoId()));
    Map<String, List<Publisher>> map = storage.getAllPublisher(testDc);
    Assert.assertEquals(map.size(), 1);
    Assert.assertEquals(map.get(publisher.getDataInfoId()).size(), 0);
  }
}
