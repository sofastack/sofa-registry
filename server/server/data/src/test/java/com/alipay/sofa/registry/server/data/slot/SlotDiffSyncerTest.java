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
package com.alipay.sofa.registry.server.data.slot;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherResult;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.pubiterator.DatumBiConsumer;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

public class SlotDiffSyncerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlotDiffSyncerTest.class);

  private static final String DATACENTER = "testdc";
  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  private static final SyncContinues TRUE =
      new SyncContinues() {
        @Override
        public boolean continues() {
          return true;
        }
      };

  private static final SyncContinues FALSE =
      new SyncContinues() {
        @Override
        public boolean continues() {
          return false;
        }
      };

  @Test
  public void testPick() {
    Map<String, DatumSummary> summaryMap = Maps.newLinkedHashMap();
    for (int i = 0; i < 10; i++) {
      DatumSummary summary0 = TestBaseUtils.newDatumSummary(0);
      summaryMap.put(summary0.getDataInfoId(), summary0);
    }
    DatumSummary summary1 = TestBaseUtils.newDatumSummary(2);
    summaryMap.put(summary1.getDataInfoId(), summary1);
    Map<String, DatumSummary> picks = SlotDiffSyncer.pickSummaries(summaryMap, 5);
    // empty consume 1 publisher budget
    Assert.assertEquals(picks.size(), 5);
    for (DatumSummary summary : picks.values()) {
      Assert.assertEquals(summary.size(), 0);
    }

    picks = SlotDiffSyncer.pickSummaries(summaryMap, 11);
    Assert.assertEquals(picks.size(), 11);
    // get all publishers in datum
    Assert.assertEquals(picks.get(summary1.getDataInfoId()).size(), 2);
  }

  @Test
  public void testSyncDigestResp() {
    SlotDiffSyncer syncer = newSyncer();
    Assert.assertNull(
        syncer.processSyncDigestResp(
            true, DATACENTER, 10, failDigestResp(), null, Collections.emptyMap()));
    Assert.assertNull(
        syncer.processSyncDigestResp(true, DATACENTER, 10, null, null, Collections.emptyMap()));
    Assert.assertTrue(
        syncer
            .processSyncDigestResp(
                true, DATACENTER, 10, emptyDigestResp(), null, Collections.emptyMap())
            .isEmpty());

    DatumStorage storage = syncer.getDatumStorageDelegate();
    Publisher publisher1 = TestBaseUtils.createTestPublisher("dataId1");
    storage.putPublisher(DATACENTER, publisher1);
    Publisher publisher2 = TestBaseUtils.createTestPublisher("dataId2");
    storage.putPublisher(DATACENTER, publisher2);
    Publisher publisher3 = TestBaseUtils.createTestPublisher("dataId3");
    storage.putPublisher(DATACENTER, publisher3);

    GenericResponse resp =
        newDigestResp(
            Lists.newArrayList(publisher1.getDataInfoId()),
            Lists.newArrayList(publisher2.getDataInfoId()),
            Lists.newArrayList(publisher3.getDataInfoId()));
    Map<String, DatumSummary> summaryMap = Maps.newHashMap();
    summaryMap.put(
        publisher1.getDataInfoId(), TestBaseUtils.newDatumSummary(3, publisher1.getDataInfoId()));
    summaryMap.put(
        publisher3.getDataInfoId(), TestBaseUtils.newDatumSummary(2, publisher3.getDataInfoId()));
    // try remove publisher3, but not match register.version
    Assert.assertFalse(
        syncer.processSyncDigestResp(true, DATACENTER, 10, resp, null, summaryMap).isEmpty());
    Datum datum1 = storage.get(DATACENTER, publisher3.getDataInfoId());
    Assert.assertTrue(datum1.publisherSize() != 0);

    // remove publisher3
    summaryMap.put(
        publisher3.getDataInfoId(),
        new DatumSummary(
            publisher3.getDataInfoId(),
            Collections.singletonMap(publisher3.getRegisterId(), publisher3.registerVersion())));
    DataSlotDiffDigestResult result =
        syncer.processSyncDigestResp(true, DATACENTER, 10, resp, null, summaryMap);
    Assert.assertFalse(result.isEmpty());
    Datum datum2 = storage.get(DATACENTER, publisher3.getDataInfoId());
    Assert.assertTrue(datum2.publisherSize() == 0);
    Assert.assertTrue(datum2.getVersion() > datum1.getVersion());
  }

  @Test
  public void testSyncPublisherResp() {
    SlotDiffSyncer syncer = newSyncer();
    Assert.assertNull(
        syncer.processSyncPublisherResp(
            true, DATACENTER, 10, failPublisherResp(), null, Collections.emptyMap()));
    Assert.assertNull(
        syncer.processSyncPublisherResp(true, DATACENTER, 10, null, null, Collections.emptyMap()));
    Assert.assertTrue(
        syncer
            .processSyncPublisherResp(
                true, DATACENTER, 10, emptyPublisherResp(), null, Collections.emptyMap())
            .isEmpty());

    DatumStorage storage = syncer.getDatumStorageDelegate();
    Publisher publisher1 = TestBaseUtils.createTestPublisher("dataId1");
    storage.putPublisher(DATACENTER, publisher1);
    Publisher publisher2 = TestBaseUtils.createTestPublisher("dataId2");
    storage.putPublisher(DATACENTER, publisher2);
    Publisher publisher3 = TestBaseUtils.createTestPublisher("dataId3");
    storage.putPublisher(DATACENTER, publisher3);

    Map<String, DatumSummary> summaryMap = Maps.newHashMap();
    summaryMap.put(
        publisher1.getDataInfoId(),
        new DatumSummary(
            publisher1.getDataInfoId(),
            Collections.singletonMap(publisher1.getRegisterId(), publisher1.registerVersion())));
    summaryMap.put(
        publisher2.getDataInfoId(), TestBaseUtils.newDatumSummary(3, publisher2.getDataInfoId()));
    summaryMap.put(
        publisher3.getDataInfoId(),
        new DatumSummary(
            publisher3.getDataInfoId(),
            Collections.singletonMap(publisher3.getRegisterId(), publisher3.registerVersion())));

    publisher1 = TestBaseUtils.cloneBase(publisher1);
    publisher1.setVersion(publisher1.getVersion() + 1);
    GenericResponse resp =
        newPublishResp(
            false,
            Collections.singletonMap(
                publisher1.getDataInfoId(), Collections.singletonList(publisher1)),
            Collections.singletonMap(
                publisher3.getDataInfoId(), Collections.singletonList(publisher3.getRegisterId())));

    Datum datum1 = storage.get(DATACENTER, publisher1.getDataInfoId());
    Datum datum3 = storage.get(DATACENTER, publisher3.getDataInfoId());
    Assert.assertTrue(datum1.publisherSize() == 1);
    Assert.assertTrue(datum3.publisherSize() == 1);

    DataSlotDiffPublisherResult result =
        syncer.processSyncPublisherResp(true, DATACENTER, 10, resp, null, summaryMap);
    Assert.assertFalse(result.isEmpty());

    Datum datum1_1 = storage.get(DATACENTER, publisher1.getDataInfoId());
    Assert.assertTrue(datum1_1.publisherSize() == 1);
    Assert.assertEquals(datum1_1.getPubMap().get(publisher1.getRegisterId()), publisher1);
    Assert.assertTrue(datum1_1.getVersion() == datum1.getVersion());

    Datum datum3_3 = storage.get(DATACENTER, publisher3.getDataInfoId());
    Assert.assertTrue(datum3_3.publisherSize() == 0);
    Assert.assertTrue(datum3_3.getVersion() > datum3.getVersion());
  }

  @Test
  public void testSyncSession() {

    int slotId = 10;
    MockSync mockSync = mockSync(slotId, DATACENTER);
    SlotDiffSyncer syncer = mockSync.syncer;
    DatumStorageDelegate delegate = syncer.getDatumStorageDelegate();
    List<Publisher> p1 = mockSync.p1;
    List<Publisher> p2 = mockSync.p2;
    List<Publisher> p3 = mockSync.p3;
    List<Publisher> p4 = mockSync.p4;

    Map<String, Map<String, DatumSummary>> sessionDatumSummary = Maps.newHashMap();
    delegate.foreach(
        DATACENTER,
        -1,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionDatumSummary, Sets.newHashSet(ServerEnv.IP), ACCEPT_ALL));

    Assert.assertEquals(sessionDatumSummary.size(), 1);
    Assert.assertTrue(sessionDatumSummary.get(ServerEnv.IP) != null);

    sessionDatumSummary = Maps.newHashMap();
    delegate.foreach(
        DATACENTER,
        slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionDatumSummary, Sets.newHashSet(ServerEnv.IP), ACCEPT_ALL));

    Map<String, DatumSummary> summary = sessionDatumSummary.get(ServerEnv.IP);

    // sync failed or empty
    SessionNodeExchanger exchanger =
        mockExchange(SessionNodeExchanger.class, null, DataSlotDiffDigestRequest.class, null, null);
    Assert.assertFalse(
        syncer.syncSession(DATACENTER, 10, ServerEnv.IP, 10, exchanger, 10, TRUE, summary));

    exchanger =
        mockExchange(
            SessionNodeExchanger.class,
            failDigestResp(),
            DataSlotDiffDigestRequest.class,
            null,
            null);
    Assert.assertFalse(
        syncer.syncSession(DATACENTER, 10, ServerEnv.IP, 10, exchanger, 10, TRUE, summary));

    exchanger =
        mockExchange(
            SessionNodeExchanger.class,
            emptyDigestResp(),
            DataSlotDiffDigestRequest.class,
            null,
            null);
    Assert.assertTrue(
        syncer.syncSession(DATACENTER, 10, ServerEnv.IP, 10, exchanger, 10, TRUE, summary));

    // update p1.0, remove p2.0, remove p3.all, add p4
    GenericResponse digestResp =
        newDigestResp(
            Lists.newArrayList(p1.get(0).getDataInfoId(), p2.get(0).getDataInfoId()),
            Collections.singletonList(p4.get(0).getDataInfoId()),
            Collections.singletonList(p3.get(0).getDataInfoId()));

    Publisher p1Update = TestBaseUtils.cloneBase(p1.get(0));
    p1Update.setVersion(p1Update.getVersion() + 1);
    Map<String, List<Publisher>> update = Maps.newHashMap();
    update.put(p1Update.getDataInfoId(), Collections.singletonList(p1Update));
    update.put(p4.get(0).getDataInfoId(), p4);
    Map<String, List<String>> remove = Maps.newHashMap();
    remove.put(p2.get(0).getDataInfoId(), Collections.singletonList(p2.get(0).getRegisterId()));

    // sync publisher,
    GenericResponse publisherResp = newPublishResp(false, update, remove);
    exchanger =
        mockExchange(
            SessionNodeExchanger.class,
            digestResp,
            DataSlotDiffDigestRequest.class,
            publisherResp,
            DataSlotDiffPublisherRequest.class);
    boolean v = syncer.syncSession(DATACENTER, 10, ServerEnv.IP, 10, exchanger, 10, TRUE, summary);
    Assert.assertTrue(v);

    // p1 update
    Datum datum1 = delegate.get(DATACENTER, p1Update.getDataInfoId());
    Assert.assertEquals(datum1.publisherSize(), 3);
    Assert.assertEquals(datum1.getPubMap().get(p1Update.getRegisterId()), p1Update);
    Assert.assertEquals(datum1.getPubMap().get(p1.get(1).getRegisterId()), p1.get(1));
    Assert.assertEquals(datum1.getPubMap().get(p1.get(2).getRegisterId()), p1.get(2));

    // p2 remains 1
    Datum datum2 = delegate.get(DATACENTER, p2.get(0).getDataInfoId());
    Assert.assertEquals(datum2.publisherSize(), 1);
    Assert.assertEquals(datum2.getPubMap().get(p2.get(1).getRegisterId()), p2.get(1));

    // p3 remove all
    Datum datum3 = delegate.get(DATACENTER, p3.get(0).getDataInfoId());
    Assert.assertEquals(datum3.publisherSize(), 0);

    // p4 add all
    Datum datum4 = delegate.get(DATACENTER, p4.get(0).getDataInfoId());
    Assert.assertEquals(datum4.publisherSize(), 2);
    Assert.assertEquals(datum4.getPubMap().get(p4.get(0).getRegisterId()), p4.get(0));
    Assert.assertEquals(datum4.getPubMap().get(p4.get(1).getRegisterId()), p4.get(1));
  }

  @Test
  public void testSyncBreak() {
    int slotId = 10;
    MockSync mockSync = mockSync(slotId, DATACENTER);
    SlotDiffSyncer syncer = mockSync.syncer;
    DatumStorageDelegate delegate = syncer.getDatumStorageDelegate();
    List<Publisher> p1 = mockSync.p1;
    List<Publisher> p2 = mockSync.p2;
    List<Publisher> p3 = mockSync.p3;
    List<Publisher> p4 = mockSync.p4;

    // update p1.0, remove p2.0, remove p3.all, add p4
    GenericResponse digestResp =
        newDigestResp(
            Lists.newArrayList(p1.get(0).getDataInfoId(), p2.get(0).getDataInfoId()),
            Collections.singletonList(p4.get(0).getDataInfoId()),
            Collections.singletonList(p3.get(0).getDataInfoId()));

    Publisher p1Update = TestBaseUtils.cloneBase(p1.get(0));
    p1Update.setVersion(p1Update.getVersion() + 1);
    Map<String, List<Publisher>> update = Maps.newHashMap();
    update.put(p1Update.getDataInfoId(), Collections.singletonList(p1Update));
    update.put(p4.get(0).getDataInfoId(), p4);
    Map<String, List<String>> remove = Maps.newHashMap();
    remove.put(p2.get(0).getDataInfoId(), Collections.singletonList(p2.get(0).getRegisterId()));

    // sync publisher,
    GenericResponse publisherResp = newPublishResp(false, update, remove);
    SessionNodeExchanger exchanger =
        mockExchange(
            SessionNodeExchanger.class,
            digestResp,
            DataSlotDiffDigestRequest.class,
            publisherResp,
            DataSlotDiffPublisherRequest.class);

    Map<String, Map<String, DatumSummary>> sessionDatumSummary = Maps.newHashMap();
    delegate.foreach(
        DATACENTER,
        slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(
            sessionDatumSummary, Sets.newHashSet(ServerEnv.IP), ACCEPT_ALL));

    Map<String, DatumSummary> summary = sessionDatumSummary.get(ServerEnv.IP);

    boolean v = syncer.syncSession(DATACENTER, 10, ServerEnv.IP, 10, exchanger, 10, FALSE, summary);
    Assert.assertTrue(v);

    // sync break, only remove dataInfoIds
    Datum datum1 = delegate.get(DATACENTER, p1Update.getDataInfoId());
    Assert.assertEquals(datum1.publisherSize(), 3);
    Assert.assertEquals(datum1.getPubMap().get(p1.get(0).getRegisterId()), p1.get(0));
    Assert.assertEquals(datum1.getPubMap().get(p1.get(1).getRegisterId()), p1.get(1));
    Assert.assertEquals(datum1.getPubMap().get(p1.get(2).getRegisterId()), p1.get(2));

    // p2 remains 2
    Datum datum2 = delegate.get(DATACENTER, p2.get(0).getDataInfoId());
    Assert.assertEquals(datum2.publisherSize(), 2);
    Assert.assertEquals(datum2.getPubMap().get(p2.get(0).getRegisterId()), p2.get(0));
    Assert.assertEquals(datum2.getPubMap().get(p2.get(1).getRegisterId()), p2.get(1));

    // p3 remove all
    Datum datum3 = delegate.get(DATACENTER, p3.get(0).getDataInfoId());
    Assert.assertEquals(datum3.publisherSize(), 0);

    // p4 is empty
    Datum datum4 = delegate.get(DATACENTER, p4.get(0).getDataInfoId());
    Assert.assertEquals(datum4.publisherSize(), 0);
  }

  @Test
  public void testSyncLeader() {
    MockSync mockSync = mockSync(10, DATACENTER);
    SlotDiffSyncer syncer = mockSync.syncer;
    DatumStorageDelegate delegate = syncer.getDatumStorageDelegate();
    List<Publisher> p1 = mockSync.p1;
    List<Publisher> p2 = mockSync.p2;
    List<Publisher> p3 = mockSync.p3;
    List<Publisher> p4 = mockSync.p4;

    // sync failed or empty
    DataNodeExchanger exchanger =
        mockExchange(DataNodeExchanger.class, null, DataSlotDiffDigestRequest.class, null, null);
    Assert.assertFalse(
        syncer.syncSlotLeader(
            DATACENTER, DATACENTER, true, 10, ServerEnv.IP, 10, exchanger, 10, TRUE));

    exchanger =
        mockExchange(
            DataNodeExchanger.class, failDigestResp(), DataSlotDiffDigestRequest.class, null, null);
    Assert.assertFalse(
        syncer.syncSlotLeader(
            DATACENTER, DATACENTER, true, 10, ServerEnv.IP, 10, exchanger, 10, TRUE));

    exchanger =
        mockExchange(
            DataNodeExchanger.class,
            emptyDigestResp(),
            DataSlotDiffDigestRequest.class,
            null,
            null);
    Assert.assertTrue(
        syncer.syncSlotLeader(
            DATACENTER, DATACENTER, true, 10, ServerEnv.IP, 10, exchanger, 10, TRUE));

    // sync success
    // update p1.0, remove p2.0, remove p3.all, add p4
    GenericResponse digestResp =
        newDigestResp(
            Lists.newArrayList(p1.get(0).getDataInfoId(), p2.get(0).getDataInfoId()),
            Collections.singletonList(p4.get(0).getDataInfoId()),
            Collections.singletonList(p3.get(0).getDataInfoId()));

    Publisher p1Update = TestBaseUtils.cloneBase(p1.get(0));
    p1Update.setVersion(p1Update.getVersion() + 1);
    Map<String, List<Publisher>> update = Maps.newHashMap();
    update.put(p1Update.getDataInfoId(), Collections.singletonList(p1Update));
    update.put(p4.get(0).getDataInfoId(), p4);
    Map<String, List<String>> remove = Maps.newHashMap();
    remove.put(p2.get(0).getDataInfoId(), Collections.singletonList(p2.get(0).getRegisterId()));

    // sync publisher,
    GenericResponse publisherResp = newPublishResp(false, update, remove);
    exchanger =
        mockExchange(
            DataNodeExchanger.class,
            digestResp,
            DataSlotDiffDigestRequest.class,
            publisherResp,
            DataSlotDiffPublisherRequest.class);
    boolean v =
        syncer.syncSlotLeader(
            DATACENTER, DATACENTER, true, 10, ServerEnv.IP, 10, exchanger, 10, TRUE);
    Assert.assertTrue(v);

    // p1 update
    Datum datum1 = delegate.get(DATACENTER, p1Update.getDataInfoId());
    Assert.assertEquals(datum1.publisherSize(), 3);
    Assert.assertEquals(datum1.getPubMap().get(p1Update.getRegisterId()), p1Update);
    Assert.assertEquals(datum1.getPubMap().get(p1.get(1).getRegisterId()), p1.get(1));
    Assert.assertEquals(datum1.getPubMap().get(p1.get(2).getRegisterId()), p1.get(2));

    // p2 remains 1
    Datum datum2 = delegate.get(DATACENTER, p2.get(0).getDataInfoId());
    Assert.assertEquals(datum2.publisherSize(), 1);
    Assert.assertEquals(datum2.getPubMap().get(p2.get(1).getRegisterId()), p2.get(1));

    // p3 remove all
    Datum datum3 = delegate.get(DATACENTER, p3.get(0).getDataInfoId());
    Assert.assertEquals(datum3.publisherSize(), 0);

    // p4 add all
    Datum datum4 = delegate.get(DATACENTER, p4.get(0).getDataInfoId());
    Assert.assertEquals(datum4.publisherSize(), 2);
    Assert.assertEquals(datum4.getPubMap().get(p4.get(0).getRegisterId()), p4.get(0));
    Assert.assertEquals(datum4.getPubMap().get(p4.get(1).getRegisterId()), p4.get(1));
  }

  private static <T extends ClientSideExchanger> T mockExchange(
      Class<T> c, Object result1, Class reqClazz1, Object result2, Class reqClazz2) {
    T exchanger = mock(c);
    when(exchanger.requestRaw(anyString(), Matchers.isA(reqClazz1)))
        .thenReturn(
            new Response() {
              @Override
              public Object getResult() {
                return result1;
              }
            });
    if (reqClazz2 != null) {
      when(exchanger.requestRaw(anyString(), Matchers.isA(reqClazz2)))
          .thenReturn(
              new Response() {
                @Override
                public Object getResult() {
                  return result2;
                }
              });
    }
    return exchanger;
  }

  static SlotDiffSyncer newSyncer() {
    return newSyncer(true);
  }

  static SlotDiffSyncer newSyncer(boolean init) {
    DatumStorageDelegate delegate = TestBaseUtils.newLocalDatumDelegate(DATACENTER, init);
    DataChangeEventCenter eventCenter = new DataChangeEventCenter();
    SessionLeaseManager sessionLeaseManager = new SessionLeaseManager();
    SlotDiffSyncer syncer =
        new SlotDiffSyncer(
            TestBaseUtils.newDataConfig(DATACENTER),
            delegate,
            eventCenter,
            sessionLeaseManager,
            ACCEPT_ALL,
            LOGGER);
    return syncer;
  }

  private static GenericResponse failPublisherResp() {
    GenericResponse<DataSlotDiffPublisherResult> resp = new GenericResponse<>();
    resp.fillFailed("fail");
    return resp;
  }

  private static GenericResponse emptyPublisherResp() {
    GenericResponse<DataSlotDiffPublisherResult> resp = new GenericResponse<>();
    DataSlotDiffPublisherResult empty =
        new DataSlotDiffPublisherResult(false, Collections.emptyMap(), Collections.emptyMap());
    empty.setSessionProcessId(ServerEnv.PROCESS_ID);
    resp.fillSucceed(empty);
    return resp;
  }

  private static GenericResponse newPublishResp(
      boolean hasRemain,
      Map<String, List<Publisher>> updatedPublishers,
      Map<String, List<String>> removedPublishers) {
    GenericResponse<DataSlotDiffPublisherResult> resp = new GenericResponse<>();
    DataSlotDiffPublisherResult result =
        new DataSlotDiffPublisherResult(hasRemain, updatedPublishers, removedPublishers);
    result.setSessionProcessId(ServerEnv.PROCESS_ID);
    resp.fillSucceed(result);
    return resp;
  }

  private static GenericResponse failDigestResp() {
    GenericResponse<DataSlotDiffDigestResult> resp = new GenericResponse<>();
    resp.fillFailed("fail");
    return resp;
  }

  private static GenericResponse emptyDigestResp() {
    GenericResponse<DataSlotDiffDigestResult> resp = new GenericResponse<>();
    DataSlotDiffDigestResult empty =
        new DataSlotDiffDigestResult(
            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    empty.setSessionProcessId(ServerEnv.PROCESS_ID);
    resp.fillSucceed(empty);
    return resp;
  }

  private static GenericResponse newDigestResp(
      List<String> updatedDataInfoIds,
      List<String> addedDataInfoIds,
      List<String> removedDataInfoIds) {
    GenericResponse<DataSlotDiffDigestResult> resp = new GenericResponse<>();
    DataSlotDiffDigestResult result =
        new DataSlotDiffDigestResult(updatedDataInfoIds, addedDataInfoIds, removedDataInfoIds);
    result.setSessionProcessId(ServerEnv.PROCESS_ID);
    resp.fillSucceed(result);
    return resp;
  }

  public static final class MockSync {
    public SlotDiffSyncer syncer;
    public List<Publisher> p1;
    public List<Publisher> p2;
    public List<Publisher> p3;
    public List<Publisher> p4;
  }

  public static MockSync mockSync(int slotId, String dc, boolean init) {
    SlotDiffSyncer syncer = newSyncer(init);
    MockSync ms = new MockSync();
    if (init) {
      DatumStorageDelegate delegate = syncer.getDatumStorageDelegate();
      List<Publisher> p1 = TestBaseUtils.createTestPublishers(slotId, 3);
      List<Publisher> p2 = TestBaseUtils.createTestPublishers(slotId, 2);
      List<Publisher> p3 = TestBaseUtils.createTestPublishers(slotId, 2);
      List<Publisher> p4 = TestBaseUtils.createTestPublishers(slotId, 2);
      delegate.putPublisher(DATACENTER, p1.get(0).getDataInfoId(), p1);
      delegate.putPublisher(DATACENTER, p2.get(0).getDataInfoId(), p2);
      delegate.putPublisher(DATACENTER, p3.get(0).getDataInfoId(), p3);
      // empty d4
      delegate.createEmptyDatumIfAbsent(dc, p4.get(0).getDataInfoId());
      ms.p1 = p1;
      ms.p2 = p2;
      ms.p3 = p3;
      ms.p4 = p4;
    }
    ms.syncer = syncer;
    return ms;
  }

  static MockSync mockSync(int slotId, String dc) {
    return mockSync(slotId, dc, true);
  }
}
