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
package com.alipay.sofa.registry.server.data.change;

import static com.alipay.sofa.registry.server.data.change.ChangeMetrics.*;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataChangeEventCenterTest {
  private static final String DC = "testDc";
  private DataChangeEventCenter center;
  private DataServerConfig dataServerConfig;
  private DatumStorageDelegate datumStorageDelegate;
  private MultiClusterDataServerConfig multiClusterDataServerConfig;
  private DefaultCommonConfig defaultCommonConfig;

  private void setCenter() {
    this.center = new DataChangeEventCenter();
    this.dataServerConfig = TestBaseUtils.newDataConfig(DC);
    this.multiClusterDataServerConfig = TestBaseUtils.newMultiDataConfig();
    this.datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate(DC, true);
    this.defaultCommonConfig = TestBaseUtils.newDefaultCommonConfig(DC);
    center.setDataServerConfig(dataServerConfig);
    center.setDatumDelegate(datumStorageDelegate);
    center.setMultiClusterDataServerConfig(multiClusterDataServerConfig);
    center.setDefaultCommonConfig(defaultCommonConfig);
    dataServerConfig.setNotifyIntervalMillis(100);
  }

  @Test
  public void testHandleTempChangeNotInit() {
    setCenter();
    Assert.assertFalse(center.handleTempChanges(Collections.EMPTY_LIST));
    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    center.onTempPubChange(pub, DC);
    Assert.assertFalse(center.handleTempChanges(Collections.EMPTY_LIST));

    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    center.onTempPubChange(pub, DC);
    // npe
    Assert.assertTrue(center.handleTempChanges(Lists.newArrayList(channel)));

    // reject
    center.setNotifyTempExecutor(TestBaseUtils.rejectExecutor());
    center.onTempPubChange(pub, DC);
    double pre = ChangeMetrics.CHANGETEMP_SKIP_COUNTER.get();
    Assert.assertTrue(center.handleTempChanges(Lists.newArrayList(channel)));
    Assert.assertTrue(ChangeMetrics.CHANGETEMP_SKIP_COUNTER.get() == (pre + 1));
  }

  @Test
  public void testHandleChangeNotInit() {
    setCenter();

    Assert.assertFalse(
        center.handleChanges(
            center.transferChangeEvent(dataServerConfig.getNotifyMaxItems()),
            NodeType.SESSION,
            dataServerConfig.getNotifyPort(),
            true));

    Exchange exchange = Mockito.mock(Exchange.class);
    Server server = Mockito.mock(Server.class);
    Mockito.when(exchange.getServer(dataServerConfig.getNotifyPort())).thenReturn(server);
    center.setExchange(exchange);

    List<String> changes1 = Lists.newArrayList("1", "2");
    center.onChange(changes1, DataChangeType.PUT, DC);
    Assert.assertFalse(
        center.handleChanges(
            center.transferChangeEvent(dataServerConfig.getNotifyMaxItems()),
            NodeType.SESSION,
            dataServerConfig.getNotifyPort(),
            true));

    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    Mockito.when(server.selectAvailableChannelsForHostAddress())
        .thenReturn(
            Collections.singletonMap(
                channel.getRemoteAddress().getAddress().getHostAddress(), channel));

    Mockito.when(server.selectAllAvailableChannelsForHostAddress())
        .thenReturn(
            Collections.singletonMap(
                channel.getRemoteAddress().getAddress().getHostAddress(),
                Lists.newArrayList(channel)));

    center.onChange(changes1, DataChangeType.PUT, DC);
    Map<String, List<Channel>> channelsMap = Maps.newHashMap();
    channelsMap.put("localhost", Lists.newArrayList(channel));
    Assert.assertTrue(
        center.handleChanges(
            center.transferChangeEvent(dataServerConfig.getNotifyMaxItems()),
            NodeType.SESSION,
            dataServerConfig.getNotifyPort(),
            true));

    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DataChangeType.PUT, DC);
    datumStorageDelegate.getLocalDatumStorage().putPublisher(DC, pub);
    // npe
    Assert.assertTrue(
        center.handleChanges(
            center.transferChangeEvent(dataServerConfig.getNotifyMaxItems()),
            NodeType.SESSION,
            dataServerConfig.getNotifyPort(),
            true));

    // reject
    center.setNotifyExecutor(TestBaseUtils.rejectExecutor());
    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DataChangeType.PUT, DC);
    double pre = ChangeMetrics.CHANGE_SKIP_COUNTER.get();
    Assert.assertTrue(
        center.handleChanges(
            center.transferChangeEvent(dataServerConfig.getNotifyMaxItems()),
            NodeType.SESSION,
            dataServerConfig.getNotifyPort(),
            true));
    Assert.assertTrue(ChangeMetrics.CHANGE_SKIP_COUNTER.get() == (pre + 1));
  }

  @Test
  public void testNotify() {
    setCenter();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    channel.setActive(false);
    DataChangeEventCenter.ChangeNotifier notifier =
        center.newChangeNotifier(
            channel,
            dataServerConfig.getNotifyPort(),
            DC,
            Collections.singletonMap(String.valueOf(100), new DatumVersion(100)));

    double pre = CHANGE_FAIL_COUNTER.get();
    notifier.run();
    Assert.assertTrue(CHANGE_FAIL_COUNTER.get() == (pre + 1));

    channel.setActive(true);
    Exchange exchange = Mockito.mock(BoltExchange.class);
    Mockito.when(exchange.getServer(Mockito.anyInt())).thenReturn(Mockito.mock(Server.class));
    center.setExchange(exchange);

    pre = CHANGE_FAIL_COUNTER.get();
    double spre = CHANGE_SUCCESS_COUNTER.get();
    notifier.run();
    Assert.assertTrue(CHANGE_FAIL_COUNTER.get() == (pre));
    Assert.assertTrue(CHANGE_SUCCESS_COUNTER.get() == (spre + 1));
  }

  @Test
  public void testTempNotify() {
    setCenter();
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);

    DataChangeEventCenter.TempNotifier notifier = center.newTempNotifier(channel, null);

    // channel close
    channel.setActive(false);
    double pre = CHANGETEMP_FAIL_COUNTER.get();
    notifier.run();
    System.out.println(pre + ":" + CHANGETEMP_FAIL_COUNTER.get());
    Assert.assertTrue(CHANGETEMP_FAIL_COUNTER.get() == (pre + 1));

    channel.setActive(true);

    // npe
    pre = CHANGETEMP_FAIL_COUNTER.get();
    notifier.run();
    Assert.assertTrue(CHANGETEMP_FAIL_COUNTER.get() == (pre + 1));

    // success
    Datum datum = new Datum();
    datum.setDataCenter("testDc");
    datum.setDataInfoId("testDataInfoId");
    notifier = center.newTempNotifier(channel, datum);
    pre = CHANGETEMP_FAIL_COUNTER.get();
    double spre = CHANGETEMP_SUCCESS_COUNTER.get();
    notifier.run();
    Assert.assertTrue(CHANGETEMP_FAIL_COUNTER.get() == (pre));
    Assert.assertTrue(CHANGETEMP_SUCCESS_COUNTER.get() == (spre + 1));
  }

  @Test
  public void testHandleExpire_npe() {
    initHandleExpire();
    center.handleExpire();
  }

  @Test
  public void testHandleExpire_reject() {
    initHandleExpire();
    center.setNotifyExecutor(TestBaseUtils.rejectExecutor());
    center.handleExpire();
  }

  private void initHandleExpire() {
    setCenter();
    dataServerConfig.setNotifyRetryQueueSize(10);
    // not expire
    dataServerConfig.setNotifyRetryBackoffMillis(100000);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    List<DataChangeEventCenter.ChangeNotifier> list = Lists.newArrayList();
    for (int i = 0; i < dataServerConfig.getNotifyRetryQueueSize(); i++) {
      center.commitRetry(
          center.newChangeNotifier(
              channel,
              dataServerConfig.getNotifyPort(),
              DC,
              Collections.singletonMap(String.valueOf(i), new DatumVersion(100))));
      list.add(
          center.newChangeNotifier(
              channel,
              dataServerConfig.getNotifyPort(),
              DC,
              Collections.singletonMap(String.valueOf(i + 100), new DatumVersion(200))));
    }
    List<DataChangeEventCenter.ChangeNotifier> expires = center.getExpires();
    Assert.assertTrue(expires.isEmpty());
    // is full, make expire now
    dataServerConfig.setNotifyRetryBackoffMillis(0);
    for (DataChangeEventCenter.ChangeNotifier n : list) {
      center.commitRetry(n);
    }
    expires = center.getExpires();
    Assert.assertEquals(expires.size(), list.size());
    Assert.assertArrayEquals(expires.toArray(), list.toArray());
    for (DataChangeEventCenter.ChangeNotifier n : list) {
      center.commitRetry(n);
    }
  }

  @Test
  public void testOnTempChange() {
    setCenter();
    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    center.onTempPubChange(pub, DC);
    Datum datum = center.getOnTempPubChanges(DC).get(pub.getDataInfoId());
    Assert.assertEquals(datum.publisherSize(), 1);
    Assert.assertEquals(datum.getPubMap().get(pub.getRegisterId()), pub);

    center.onTempPubChange(pub, DC);
    Publisher older = TestBaseUtils.cloneBase(pub);
    older.setRegisterTimestamp(older.getRegisterTimestamp() - 1);
    center.onTempPubChange(older, DC);
    datum = center.getOnTempPubChanges(DC).get(pub.getDataInfoId());
    Assert.assertEquals(datum.publisherSize(), 1);
    Assert.assertEquals(datum.getPubMap().get(pub.getRegisterId()), pub);

    Publisher newer = TestBaseUtils.cloneBase(pub);
    newer.setRegisterTimestamp(newer.getRegisterTimestamp() + 1);
    center.onTempPubChange(newer, DC);
    datum = center.getOnTempPubChanges(DC).get(pub.getDataInfoId());
    Assert.assertEquals(datum.publisherSize(), 1);
    Assert.assertEquals(datum.getPubMap().get(pub.getRegisterId()), newer);

    datum = center.getOnTempPubChanges(DC + "1").get(pub.getDataInfoId());
    Assert.assertNull(datum);

    Publisher pub2 = TestBaseUtils.createTestPublisher("testDataId");
    center.onTempPubChange(pub2, DC);
    datum = center.getOnTempPubChanges(DC).get(pub.getDataInfoId());
    Assert.assertEquals(datum.publisherSize(), 2);
    Assert.assertEquals(datum.getPubMap().get(pub.getRegisterId()), newer);
    Assert.assertEquals(datum.getPubMap().get(pub2.getRegisterId()), pub2);
  }

  @Test
  public void testTransfer() {
    setCenter();
    List<String> changes1 = Lists.newArrayList("1", "2", "3");
    center.onChange(changes1, DataChangeType.PUT, DC);
    List<DataChangeEvent> events = center.transferChangeEvent(3);
    Assert.assertNotNull(events.get(0).toString());
    Assert.assertEquals(1, events.size());
    assertEvent(events, changes1);

    center.onChange(changes1, DataChangeType.PUT, DC);
    events = center.transferChangeEvent(2);
    Assert.assertEquals(2, events.size());
    assertEvent(events, changes1);
  }

  private void assertEvent(List<DataChangeEvent> events, List<String> changes) {
    List<String> elist = Lists.newArrayList();
    for (DataChangeEvent e : events) {
      elist.addAll(e.getDataInfoIds());
    }
    Assert.assertEquals(elist.size(), changes.size());
    Assert.assertEquals(Sets.newHashSet(elist), Sets.newHashSet(changes));
  }

  @Test
  public void testOnChange() {
    setCenter();
    List<String> changes1 = Lists.newArrayList("1", "2");
    center.onChange(changes1, DataChangeType.PUT, DC);

    List<String> changes2 = Lists.newArrayList("2", "3");
    center.onChange(changes2, DataChangeType.PUT, DC);

    List<String> changes3 = Lists.newArrayList("4", "5");
    center.onChange(changes3, DataChangeType.PUT, DC + "1");

    Set<String> s1 = Sets.newHashSet(changes1);
    s1.addAll(changes2);

    Assert.assertEquals(center.getOnChanges(DC), s1);
    Assert.assertEquals(center.getOnChanges(DC + "1"), Sets.newHashSet(changes3));
    Assert.assertEquals(center.getOnChanges(DC + "2"), Collections.emptySet());
  }

  @Test
  public void testInit() throws Exception {
    setCenter();
    // set center exchange
    Exchange exchange = Mockito.mock(Exchange.class);
    Server server = Mockito.mock(Server.class);
    Mockito.when(exchange.getServer(dataServerConfig.getNotifyPort())).thenReturn(server);
    Mockito.when(exchange.getServer(multiClusterDataServerConfig.getSyncRemoteSlotLeaderPort()))
        .thenReturn(server);

    center.setExchange(exchange);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    channel.setActive(false);
    Assert.assertFalse(channel.isConnected());

    Mockito.when(server.selectAvailableChannelsForHostAddress())
        .thenReturn(
            Collections.singletonMap(
                channel.getRemoteAddress().getAddress().getHostAddress(), channel));

    Mockito.when(server.selectAllAvailableChannelsForHostAddress())
        .thenReturn(
            Collections.singletonMap(
                channel.getRemoteAddress().getAddress().getHostAddress(),
                Lists.newArrayList(channel)));

    Mockito.when(server.sendSync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt()))
        .thenThrow(new UnsupportedOperationException());

    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    datumStorageDelegate.getLocalDatumStorage().putPublisher(DC, pub);

    center.init();
    this.dataServerConfig.setNotifyRetryBackoffMillis(1);
    this.dataServerConfig.setNotifyRetryTimes(1);

    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DataChangeType.PUT, DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(500);

    Mockito.verify(server, Mockito.times(0))
        .sendSync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());

    channel.setActive(true);
    Assert.assertTrue(channel.isConnected());

    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DataChangeType.PUT, DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(500);

    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DataChangeType.PUT, DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(500);
    Mockito.verify(server, Mockito.times(10))
        .sendSync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());
  }
}
