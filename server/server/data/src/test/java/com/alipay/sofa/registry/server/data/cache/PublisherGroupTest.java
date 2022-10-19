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

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.pubiterator.DatumBiConsumer;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;

public class PublisherGroupTest {

  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @Test
  public void testBaseOp() {
    final String dataId = "testDataInfoId";
    long start = DatumVersionUtil.nextId();
    Publisher publisher = TestBaseUtils.createTestPublisher(dataId);
    PublisherGroup group = new PublisherGroup(publisher.getDataInfoId(), "dc");

    Assert.assertEquals(group.dataInfoId, publisher.getDataInfoId());
    Assert.assertEquals(group.dataId, publisher.getDataId());
    Assert.assertEquals(group.instanceId, publisher.getInstanceId());
    Assert.assertEquals(group.group, publisher.getGroup());
    Assert.assertEquals(group.dataCenter, "dc");
    Assert.assertTrue(group.getVersion().getValue() > start);

    Datum datum = group.toDatum();

    Assert.assertEquals(group.dataInfoId, datum.getDataInfoId());
    Assert.assertEquals(group.dataId, datum.getDataId());
    Assert.assertEquals(group.instanceId, datum.getInstanceId());
    Assert.assertEquals(group.group, datum.getGroup());
    Assert.assertEquals(group.dataCenter, datum.getDataCenter());
    Assert.assertEquals(group.getPublishers().size(), datum.getPubMap().size());
    Assert.assertEquals(group.getVersion().getValue(), datum.getVersion());

    DatumVersion version = group.getVersion();
    DatumVersion v = group.addPublisher(publisher);
    Assert.assertTrue(v.getValue() > version.getValue());
    Assert.assertEquals(group.getPublishers().size(), 1);
    datum = group.toDatum();
    Assert.assertEquals(group.getPublishers().size(), datum.getPubMap().size());
    Assert.assertTrue(group.getPublishers().get(0) == datum.getPubMap().values().iterator().next());

    // add same pub, not change
    v = group.addPublisher(publisher);
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    // add version older pub
    Publisher older = TestBaseUtils.cloneBase(publisher);
    older.setVersion(publisher.getVersion() - 1);
    v = group.addPublisher(older);
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    // add registerTimestamp older pub
    older = TestBaseUtils.cloneBase(publisher);
    older.setRegisterTimestamp(publisher.getRegisterTimestamp() - 1);
    v = group.addPublisher(older);
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    // add newer version
    Publisher newer = TestBaseUtils.createTestPublisher(dataId);
    newer.setRegisterId(publisher.getRegisterId());
    v = group.addPublisher(newer);
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertTrue(group.getPublishers().get(0) == newer);

    v = group.addPublisher(newer);
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertTrue(group.getPublishers().get(0) == newer);

    newer = TestBaseUtils.cloneBase(newer);
    List<ServerDataBox> dataList = Lists.newArrayList();
    dataList.add(new ServerDataBox("testDataBox1"));
    newer.setDataList(dataList);
    newer.setVersion(newer.getVersion() + 1);

    v = group.addPublisher(newer);
    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertTrue(group.getPublishers().get(0) == newer);

    final ProcessId mockProcessId = new ProcessId("xxx", System.currentTimeMillis(), 1, 1);
    v = group.clean(mockProcessId, CleanContinues.ALWAYS);
    Assert.assertNull(v);

    v = group.clean(null, CleanContinues.ALWAYS);
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    group.addPublisher(newer);
    v = group.clean(ServerEnv.PROCESS_ID, CleanContinues.ALWAYS);
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    group.addPublisher(newer);
    v = group.remove(ServerEnv.PROCESS_ID, Collections.EMPTY_MAP);
    Assert.assertNull(v);

    v = group.remove(mockProcessId, Collections.EMPTY_MAP);
    Assert.assertNull(v);

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion() - 1, newer.getRegisterTimestamp())));
    Assert.assertNull(v);
    Assert.assertFalse(group.getPublishers().isEmpty());

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion(), newer.getRegisterTimestamp() - 1)));
    Assert.assertNull(v);
    Assert.assertFalse(group.getPublishers().isEmpty());

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion() - 1, newer.getRegisterTimestamp() - 1)));
    Assert.assertNull(v);
    Assert.assertFalse(group.getPublishers().isEmpty());

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion() + 1, newer.getRegisterTimestamp())));
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    group.compact(Long.MAX_VALUE);
    group.addPublisher(newer);

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion(), newer.getRegisterTimestamp() + 1)));
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    group.compact(Long.MAX_VALUE);
    group.addPublisher(newer);

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId() + "aa",
                new RegisterVersion(newer.getVersion(), newer.getRegisterTimestamp())));
    Assert.assertNull(v);
    Assert.assertFalse(group.getPublishers().isEmpty());

    v =
        group.remove(
            ServerEnv.PROCESS_ID,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion(), newer.getRegisterTimestamp())));
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    // has tombstone
    v = group.addPublisher(newer);
    Assert.assertNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    int compactCount = group.compact(Long.MIN_VALUE);
    Assert.assertEquals(0, compactCount);
    compactCount = group.compact(Long.MAX_VALUE);
    Assert.assertEquals(1, compactCount);
    // add again

    v = group.addPublisher(newer);
    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    // remove processId=null, no tombstone
    v =
        group.remove(
            null,
            Collections.singletonMap(
                newer.getRegisterId(),
                new RegisterVersion(newer.getVersion(), newer.getRegisterTimestamp())));
    Assert.assertNotNull(v);
    Assert.assertTrue(group.getPublishers().isEmpty());

    v = group.addPublisher(newer);
    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    Assert.assertTrue(group.toString().length() != 0);
  }

  @Test
  public void testUpdate() {
    final String dataId = "testDataInfoId";
    Publisher publisher = TestBaseUtils.createTestPublisher(dataId);
    PublisherGroup group = new PublisherGroup(publisher.getDataInfoId(), "dc");
    DatumVersion startV = group.getVersion();
    DatumVersion v = group.put(Lists.newArrayList(publisher, publisher));
    Assert.assertNotNull(v);
    Assert.assertTrue(v.getValue() > startV.getValue());
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertEquals(group.getPublishers().get(0), publisher);

    Publisher older = TestBaseUtils.cloneBase(publisher);
    older.setVersion(older.getVersion() - 1);
    v = group.put(Lists.newArrayList(older));
    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertEquals(group.getPublishers().get(0), publisher);

    Publisher newer = TestBaseUtils.cloneBase(publisher);
    newer.setVersion(publisher.getVersion() + 1);
    v = group.put(Lists.newArrayList(older, newer));

    Assert.assertNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertEquals(group.getPublishers().get(0), newer);

    newer = TestBaseUtils.cloneBase(newer);
    List<ServerDataBox> dataList = Lists.newArrayList();
    dataList.add(new ServerDataBox("testDataBox"));
    newer.setDataList(dataList);

    newer.setVersion(newer.getVersion() + 1);
    v = group.put(Lists.newArrayList(older, newer));

    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);
    Assert.assertEquals(group.getPublishers().get(0), newer);

    Assert.assertEquals(group.getSessionProcessIds().size(), 1);
    Assert.assertTrue(group.getSessionProcessIds().contains(ServerEnv.PROCESS_ID));

    String sessionIp = "notFound";
    Map<String, DatumSummary> sessionSummary = Maps.newHashMap();
    Map<String, Map<String, RegisterVersion>> sessionPubVersions = Maps.newHashMap();

    group.foreach(
        DatumBiConsumer.publisherGroupBiConsumer(
            publisher.getDataInfoId(), sessionPubVersions, Sets.newHashSet(sessionIp), ACCEPT_ALL));
    for (Entry<String, Map<String, RegisterVersion>> entry : sessionPubVersions.entrySet()) {
      sessionSummary.put(entry.getKey(), new DatumSummary(group.dataInfoId, entry.getValue()));
    }

    DatumSummary summary = sessionSummary.get(sessionIp);
    Assert.assertEquals(sessionSummary.size(), 1);
    Assert.assertEquals(summary.getDataInfoId(), group.dataInfoId);
    Assert.assertEquals(summary.size(), 0);
    Assert.assertEquals(summary.getPublisherVersions().size(), 0);

    sessionIp = publisher.getTargetAddress().getIpAddress();

    sessionSummary = Maps.newHashMap();
    sessionPubVersions = Maps.newHashMap();
    group.foreach(
        DatumBiConsumer.publisherGroupBiConsumer(
            publisher.getDataInfoId(), sessionPubVersions, Sets.newHashSet(sessionIp), ACCEPT_ALL));
    for (Entry<String, Map<String, RegisterVersion>> entry : sessionPubVersions.entrySet()) {
      sessionSummary.put(entry.getKey(), new DatumSummary(group.dataInfoId, entry.getValue()));
    }

    summary = sessionSummary.get(sessionIp);
    Assert.assertEquals(summary.getPublisherVersions().size(), 1);

    final ProcessId mockProcessId =
        new ProcessId(ServerEnv.PROCESS_ID.getHostAddress(), System.currentTimeMillis(), 1, 1);

    Publisher add = TestBaseUtils.createTestPublisher(dataId);
    add.setTargetAddress(URL.valueOf("192.168.1.1:1000"));
    add.setSessionProcessId(mockProcessId);
    v = group.put(Lists.newArrayList(add));
    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 2);
    Assert.assertTrue(group.getPublishers().contains(newer));
    Assert.assertTrue(group.getPublishers().contains(add));

    sessionIp = publisher.getTargetAddress().getIpAddress();

    sessionSummary = Maps.newHashMap();
    sessionPubVersions = Maps.newHashMap();
    group.foreach(
        DatumBiConsumer.publisherGroupBiConsumer(
            publisher.getDataInfoId(), sessionPubVersions, Sets.newHashSet(sessionIp), ACCEPT_ALL));
    for (Entry<String, Map<String, RegisterVersion>> entry : sessionPubVersions.entrySet()) {
      sessionSummary.put(entry.getKey(), new DatumSummary(group.dataInfoId, entry.getValue()));
    }
    summary = sessionSummary.get(sessionIp);
    Assert.assertEquals(summary.getPublisherVersions().size(), 2);

    Map<String, RegisterVersion> publisherVersions = Maps.newHashMap();
    group.foreach(
        DatumBiConsumer.publisherGroupBiConsumer(
            publisher.getDataInfoId(), publisherVersions, ACCEPT_ALL));
    summary = new DatumSummary(group.dataInfoId, publisherVersions);

    Assert.assertEquals(summary.getPublisherVersions().size(), 2);
    Assert.assertEquals(group.getSessionProcessIds().size(), 2);
    Assert.assertTrue(group.getSessionProcessIds().contains(ServerEnv.PROCESS_ID));
    Assert.assertTrue(group.getSessionProcessIds().contains(mockProcessId));

    Map<String, Publisher> conns = group.getByConnectId(ConnectId.of("unknown:999", "unknown:999"));
    Assert.assertTrue(conns.isEmpty());

    conns = group.getByConnectId(add.connectId());
    Assert.assertEquals(conns.size(), 1);
    Assert.assertEquals(conns.get(add.getRegisterId()), add);

    conns = group.getByConnectId(newer.connectId());
    Assert.assertEquals(conns.size(), 1);
    Assert.assertEquals(conns.get(newer.getRegisterId()), newer);

    v =
        group.remove(
            mockProcessId, Collections.singletonMap(add.getRegisterId(), add.registerVersion()));
    Assert.assertNotNull(v);
    Assert.assertEquals(group.getPublishers().size(), 1);

    Assert.assertEquals(group.getSessionProcessIds().size(), 1);
    Assert.assertTrue(group.getSessionProcessIds().contains(ServerEnv.PROCESS_ID));

    sessionSummary = Maps.newHashMap();
    sessionPubVersions = Maps.newHashMap();
    group.foreach(
        DatumBiConsumer.publisherGroupBiConsumer(
            publisher.getDataInfoId(), sessionPubVersions, Sets.newHashSet(sessionIp), ACCEPT_ALL));
    for (Entry<String, Map<String, RegisterVersion>> entry : sessionPubVersions.entrySet()) {
      sessionSummary.put(entry.getKey(), new DatumSummary(dataId, entry.getValue()));
    }
    summary = sessionSummary.get(sessionIp);
    Assert.assertEquals(summary.getPublisherVersions().size(), 1);

    conns = group.getByConnectId(add.connectId());
    Assert.assertEquals(conns.size(), 0);

    conns = group.getByConnectId(newer.connectId());
    Assert.assertEquals(conns.size(), 1);
    Assert.assertEquals(conns.get(newer.getRegisterId()), newer);
  }
}
