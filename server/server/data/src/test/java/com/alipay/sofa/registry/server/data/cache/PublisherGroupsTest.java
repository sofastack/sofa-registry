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
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
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

public class PublisherGroupsTest {
  private final String testDataId = TestBaseUtils.TEST_DATA_ID;
  private final String testDataInfoId = TestBaseUtils.TEST_DATA_INFO_ID;
  private final String testDc = "testDc";

  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @Test
  public void testEmpty() {
    PublisherGroups groups = new PublisherGroups(testDc);
    assertEmpty(groups, testDataInfoId);
    Assert.assertTrue(groups.toString().length() != 0);
  }

  @Test
  public void testCreate() {
    PublisherGroups groups = new PublisherGroups(testDc);
    PublisherGroup group = groups.createGroupIfAbsent(testDataInfoId);
    Assert.assertNotNull(group);
    PublisherGroup group1 = groups.createGroupIfAbsent(testDataInfoId);
    Assert.assertTrue(group == group1);
    group1 = groups.createGroupIfAbsent(testDataInfoId + "tt");
    Assert.assertTrue(group != group1);
  }

  @Test
  public void testPut() {
    PublisherGroups groups = new PublisherGroups(testDc);
    PublisherGroup group = groups.createGroupIfAbsent(testDataInfoId);
    Assert.assertNotNull(group);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    DatumVersion v = groups.put(publisher.getDataInfoId(), Collections.singletonList(publisher));
    Assert.assertNotNull(v);
    // add again
    v = groups.put(publisher.getDataInfoId(), Collections.singletonList(publisher));
    Assert.assertNull(v);

    Datum datum = groups.getDatum(publisher.getDataInfoId());
    Assert.assertNotNull(datum);
    TestBaseUtils.assertEquals(datum, publisher);
    v = groups.getVersion(publisher.getDataInfoId());
    Assert.assertEquals(datum.getVersion(), v.getValue());
    Map<String, DatumVersion> vers = groups.getVersions(null);
    Assert.assertEquals(vers.size(), 1);
    Assert.assertEquals(vers.get(publisher.getDataInfoId()), v);
    Map<String, Publisher> publisherMap = groups.getByConnectId(publisher.connectId());
    Assert.assertEquals(publisherMap.size(), 1);
    Assert.assertEquals(publisherMap.get(publisher.getRegisterId()), publisher);
    Map<String, Datum> datumMap = groups.getAllDatum();
    Assert.assertEquals(datumMap.size(), 1);
    Assert.assertTrue(datumMap.containsKey(publisher.getDataInfoId()));
    Map<String, List<Publisher>> publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 1);
    Assert.assertEquals(publishers.get(publisher.getDataInfoId()).get(0), publisher);

    String sessionIp = "noFound";
    Map<String, Map<String, DatumSummary>> sessionSummary = Maps.newHashMap();
    groups.foreach(
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummary, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    Map<String, DatumSummary> summaryMap = sessionSummary.get(sessionIp);
    Assert.assertEquals(summaryMap.size(), 0);

    summaryMap = Maps.newHashMap();
    groups.foreach(DatumBiConsumer.publisherGroupsBiConsumer(summaryMap, ACCEPT_ALL));

    Assert.assertEquals(summaryMap.size(), 1);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());

    sessionIp = publisher.getTargetAddress().getIpAddress();

    sessionSummary = Maps.newHashMap();
    groups.foreach(
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummary, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    summaryMap = sessionSummary.get(sessionIp);

    Assert.assertEquals(summaryMap.size(), 1);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());

    Set<ProcessId> processIds = groups.getSessionProcessIds();
    Assert.assertEquals(processIds.size(), 1);
    Assert.assertTrue(processIds.contains(publisher.getSessionProcessId()));

    Publisher publisher2 = TestBaseUtils.createTestPublisher(testDataId + "aa");
    v = groups.put(publisher2.getDataInfoId(), Collections.singletonList(publisher2));
    Assert.assertNotNull(v);

    datum = groups.getDatum(publisher2.getDataInfoId());
    Assert.assertNotNull(datum);
    TestBaseUtils.assertEquals(datum, publisher2);
    v = groups.getVersion(publisher2.getDataInfoId());
    Assert.assertEquals(datum.getVersion(), v.getValue());
    vers = groups.getVersions(null);
    Assert.assertEquals(vers.size(), 2);
    Assert.assertEquals(vers.get(publisher2.getDataInfoId()), v);
    publisherMap = groups.getByConnectId(publisher.connectId());
    Assert.assertEquals(publisherMap.size(), 2);
    Assert.assertEquals(publisherMap.get(publisher.getRegisterId()), publisher);
    Assert.assertEquals(publisherMap.get(publisher2.getRegisterId()), publisher2);
    publisherMap = groups.getByConnectId(TestBaseUtils.notExistConnectId());
    Assert.assertEquals(publisherMap.size(), 0);
    datumMap = groups.getAllDatum();
    Assert.assertEquals(datumMap.size(), 2);
    Assert.assertTrue(datumMap.containsKey(publisher.getDataInfoId()));
    Assert.assertTrue(datumMap.containsKey(publisher2.getDataInfoId()));
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher.getDataInfoId()).get(0), publisher);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).get(0), publisher2);

    sessionIp = "noFound";
    sessionSummary = Maps.newHashMap();
    groups.foreach(
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummary, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    summaryMap = sessionSummary.get(sessionIp);
    Assert.assertEquals(summaryMap.size(), 0);

    groups.foreach(DatumBiConsumer.publisherGroupsBiConsumer(summaryMap, ACCEPT_ALL));

    Assert.assertEquals(summaryMap.size(), 2);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());
    Assert.assertEquals(
        summaryMap
            .get(publisher2.getDataInfoId())
            .getPublisherVersions()
            .get(publisher2.getRegisterId()),
        publisher2.registerVersion());

    sessionIp = publisher.getTargetAddress().getIpAddress();
    sessionSummary = Maps.newHashMap();
    groups.foreach(
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionSummary, Sets.newHashSet(sessionIp), ACCEPT_ALL));

    summaryMap = sessionSummary.get(sessionIp);

    Assert.assertEquals(summaryMap.size(), 2);
    Assert.assertEquals(
        summaryMap
            .get(publisher.getDataInfoId())
            .getPublisherVersions()
            .get(publisher.getRegisterId()),
        publisher.registerVersion());
    Assert.assertEquals(
        summaryMap
            .get(publisher2.getDataInfoId())
            .getPublisherVersions()
            .get(publisher2.getRegisterId()),
        publisher2.registerVersion());

    processIds = groups.getSessionProcessIds();
    Assert.assertEquals(processIds.size(), 1);
    Assert.assertTrue(processIds.contains(publisher.getSessionProcessId()));
    Assert.assertTrue(processIds.contains(publisher2.getSessionProcessId()));
  }

  @Test
  public void testUnpub() {
    PublisherGroups groups = new PublisherGroups(testDc);
    PublisherGroup group = groups.createGroupIfAbsent(testDataInfoId);
    Assert.assertNotNull(group);
    Publisher publisher = TestBaseUtils.createTestPublisher(testDataId);
    UnPublisher unPublisher = UnPublisher.of(publisher);

    DatumVersion v = groups.put(publisher.getDataInfoId(), Collections.singletonList(unPublisher));
    Assert.assertNull(v);
  }

  @Test
  public void testUpdateRemove() {
    PublisherGroups groups = new PublisherGroups(testDc);
    Publisher publisher1 = TestBaseUtils.createTestPublisher(testDataId);
    Publisher publisher2 = TestBaseUtils.createTestPublisher(testDataId);
    ProcessId processId2 = new ProcessId("ip2", System.currentTimeMillis(), 100, 200);
    publisher2.setSessionProcessId(processId2);

    DatumVersion v = groups.put(publisher2.getDataInfoId(), Collections.EMPTY_LIST);
    Assert.assertNull(v);
    v = groups.put(publisher2.getDataInfoId(), Lists.newArrayList(publisher1, publisher2));
    Assert.assertNotNull(v);
    Set<ProcessId> processIds = groups.getSessionProcessIds();
    Assert.assertEquals(processIds.size(), 2);
    Assert.assertTrue(processIds.contains(publisher1.getSessionProcessId()));
    Assert.assertTrue(processIds.contains(publisher2.getSessionProcessId()));

    Publisher publisher3 = TestBaseUtils.createTestPublisher(testDataId + "-3");
    publisher3.setTargetAddress(URL.valueOf("ip3:1000"));
    v = groups.put(publisher3.getDataInfoId(), Lists.newArrayList(publisher3));
    Assert.assertNotNull(v);
    processIds = groups.getSessionProcessIds();
    Assert.assertEquals(processIds.size(), 2);
    Assert.assertTrue(processIds.contains(publisher1.getSessionProcessId()));
    Assert.assertTrue(processIds.contains(publisher2.getSessionProcessId()));

    Map<String, Publisher> publisherMap = groups.getByConnectId(publisher3.connectId());
    Assert.assertEquals(publisherMap.size(), 1);
    Assert.assertEquals(publisherMap.get(publisher3.getRegisterId()), publisher3);

    publisher2 = TestBaseUtils.cloneBase(publisher2);
    publisher2.setVersion(publisher2.getVersion() + 1);

    publisher3 = TestBaseUtils.cloneBase(publisher3);
    publisher3.setRegisterTimestamp(publisher3.getRegisterTimestamp() + 1);

    Publisher publisher4 = TestBaseUtils.cloneBase(publisher1);
    publisher4.setVersion(publisher4.getVersion() - 1);

    v = groups.put(publisher3.getDataInfoId(), Lists.newArrayList(publisher3));
    Assert.assertNull(v);
    v =
        groups.put(
            publisher1.getDataInfoId(), Lists.newArrayList(publisher1, publisher2, publisher4));
    Assert.assertNull(v);

    Map<String, List<Publisher>> publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 1);

    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher1));
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertTrue(publishers.get(publisher3.getDataInfoId()).contains(publisher3));

    // not remove
    v = groups.remove(publisher3.getDataInfoId(), processId2);
    Assert.assertNull(v);

    v = groups.remove(publisher3.getDataInfoId(), publisher3.getSessionProcessId());
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    // update again
    v = groups.put(publisher3.getDataInfoId(), Lists.newArrayList(publisher3));
    Assert.assertNotNull(v);
    v = groups.remove(publisher3.getDataInfoId(), null);
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    // now contains publisher1 and publisher2
    v =
        groups.remove(
            publisher2.getDataInfoId(),
            new ProcessId("ip3", System.currentTimeMillis(), 100, 200),
            Collections.singletonMap(publisher1.getRegisterId(), publisher1.registerVersion()));
    Assert.assertNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    v =
        groups.remove(
            publisher2.getDataInfoId(),
            publisher1.getSessionProcessId(),
            Collections.singletonMap(
                publisher1.getRegisterId() + "aa", publisher1.registerVersion()));
    Assert.assertNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    v =
        groups.remove(
            publisher2.getDataInfoId(),
            publisher1.getSessionProcessId(),
            Collections.singletonMap(
                publisher1.getRegisterId() + "aa", publisher1.registerVersion()));
    Assert.assertNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    v =
        groups.remove(
            publisher2.getDataInfoId(),
            publisher1.getSessionProcessId(),
            Collections.singletonMap(publisher1.getRegisterId(), publisher1.registerVersion()));
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    // update the same publisher1 again failed without compact
    v = groups.put(publisher1.getDataInfoId(), Lists.newArrayList(publisher1));
    Assert.assertNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    Assert.assertEquals(groups.tombstoneNum(), 1);
    Map<String, Integer> compacts = groups.compact(Long.MAX_VALUE);
    Assert.assertEquals(groups.tombstoneNum(), 0);
    Assert.assertEquals(compacts.size(), 1);
    Assert.assertEquals(compacts.get(publisher1.getDataInfoId()).intValue(), 1);

    v = groups.put(publisher1.getDataInfoId(), Lists.newArrayList(publisher1));
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 2);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher1));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    compacts = groups.compact(Long.MAX_VALUE);
    Assert.assertEquals(compacts.size(), 0);

    v =
        groups.remove(
            publisher2.getDataInfoId(),
            null,
            Collections.singletonMap(publisher1.getRegisterId(), publisher1.registerVersion()));
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    // remove with null sessionProcessId, no compact
    compacts = groups.compact(Long.MAX_VALUE);
    Assert.assertEquals(compacts.size(), 0);

    DatumVersion v1 = groups.updateVersion(publisher2.getDataInfoId());
    Assert.assertTrue(v1.getValue() > v.getValue());
    groups.updateVersion();
    Assert.assertTrue(groups.getVersion(publisher2.getDataInfoId()).getValue() > v.getValue());

    Map<String, DatumVersion> map =
        groups.clean(new ProcessId("xxx", 100, 100, 100), CleanContinues.ALWAYS);
    Assert.assertTrue(map.isEmpty());

    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    // remain publisher2
    map = groups.clean(publisher1.getSessionProcessId(), CleanContinues.ALWAYS);
    Assert.assertTrue(map.isEmpty());

    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher2));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    map = groups.clean(publisher2.getSessionProcessId(), CleanContinues.ALWAYS);
    Assert.assertTrue(map.get(publisher2.getDataInfoId()).getValue() > v.getValue());

    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 0);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    v = groups.put(publisher1.getDataInfoId(), Lists.newArrayList(publisher1));
    Assert.assertNotNull(v);
    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 1);
    Assert.assertTrue(publishers.get(publisher2.getDataInfoId()).contains(publisher1));
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);

    compacts = groups.compact(Long.MAX_VALUE);
    Assert.assertEquals(compacts.size(), 0);

    map = groups.clean(null, CleanContinues.ALWAYS);
    Assert.assertTrue(map.get(publisher2.getDataInfoId()).getValue() > v.getValue());

    publishers = groups.getAllPublisher();
    Assert.assertEquals(publishers.size(), 2);
    Assert.assertEquals(publishers.get(publisher2.getDataInfoId()).size(), 0);
    Assert.assertEquals(publishers.get(publisher3.getDataInfoId()).size(), 0);
  }

  private void assertEmpty(PublisherGroups groups, String dataInfoId) {
    Assert.assertNull(groups.getDatum(dataInfoId));
    Assert.assertNull(groups.getVersion(dataInfoId));
    Assert.assertTrue(groups.getVersions(null).isEmpty());
    Assert.assertTrue(groups.getAllDatum().isEmpty());
    Assert.assertTrue(groups.getAllPublisher().isEmpty());
  }
}
