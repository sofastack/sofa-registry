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

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataChangeEventCenterTest {
  private static final String DC = "testDc";
  private DataChangeEventCenter center;
  private SessionServerConnectionFactory sessionServerConnectionFactory;
  private DataServerConfig dataServerConfig;
  private DatumCache datumCache;

  private void setCenter() {
    this.center = new DataChangeEventCenter();
    this.sessionServerConnectionFactory = new SessionServerConnectionFactory();
    this.dataServerConfig = TestBaseUtils.newDataConfig(DC);
    this.datumCache = TestBaseUtils.newLocalDatumCache(DC, true);
    center.setSessionServerConnectionFactory(sessionServerConnectionFactory);
    center.setDataServerConfig(dataServerConfig);
    center.setDatumCache(datumCache);
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
    Assert.assertTrue(center.handleTempChanges(Lists.newArrayList(channel.getConnection())));
  }

  @Test
  public void testHandleChangeNotInit() {
    setCenter();
    Assert.assertFalse(center.handleChanges(Collections.EMPTY_LIST));
    List<String> changes1 = Lists.newArrayList("1", "2");
    center.onChange(changes1, DC);
    Assert.assertFalse(center.handleChanges(Collections.EMPTY_LIST));

    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    center.onChange(changes1, DC);
    Assert.assertTrue(center.handleChanges(Lists.newArrayList(channel.getConnection())));
  }

  @Test
  public void testHandleExpire() {
    setCenter();
    dataServerConfig.setNotifyRetryQueueSize(10);
    // not expire
    dataServerConfig.setNotifyRetryBackoffMillis(100000);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    Connection conn = channel.conn;
    List<DataChangeEventCenter.ChangeNotifier> list = Lists.newArrayList();
    for (int i = 0; i < dataServerConfig.getNotifyRetryQueueSize(); i++) {
      center.commitRetry(
          center.newChangeNotifier(
              conn, DC, Collections.singletonMap(String.valueOf(i), new DatumVersion(100))));
      list.add(
          center.newChangeNotifier(
              conn, DC, Collections.singletonMap(String.valueOf(i + 100), new DatumVersion(200))));
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
    center.handleExpire();
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
    center.onChange(changes1, DC);
    List<DataChangeEvent> events = center.transferChangeEvent(3);
    Assert.assertNotNull(events.get(0).toString());
    Assert.assertEquals(1, events.size());
    assertEvent(events, changes1);

    center.onChange(changes1, DC);
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
    center.onChange(changes1, DC);

    List<String> changes2 = Lists.newArrayList("2", "3");
    center.onChange(changes2, DC);

    List<String> changes3 = Lists.newArrayList("4", "5");
    center.onChange(changes3, DC + "1");

    Set<String> s1 = Sets.newHashSet(changes1);
    s1.addAll(changes2);

    Assert.assertEquals(center.getOnChanges(DC), s1);
    Assert.assertEquals(center.getOnChanges(DC + "1"), Sets.newHashSet(changes3));
    Assert.assertEquals(center.getOnChanges(DC + "2"), Collections.emptySet());
  }

  @Test
  public void testInit() throws Exception {
    setCenter();
    sessionServerConnectionFactory = Mockito.mock(SessionServerConnectionFactory.class);
    center.setSessionServerConnectionFactory(sessionServerConnectionFactory);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 1000);
    channel.setActive(false);
    Assert.assertFalse(channel.conn.isFine());
    Mockito.when(sessionServerConnectionFactory.getSessionConnections())
        .thenReturn(Lists.newArrayList(channel.getConnection()));

    Publisher pub = TestBaseUtils.createTestPublisher("testDataId");
    datumCache.getLocalDatumStorage().put(pub);

    center.init();
    this.dataServerConfig.setNotifyRetryBackoffMillis(1);
    this.dataServerConfig.setNotifyRetryTimes(1);

    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(2000);

    channel.setActive(true);
    Assert.assertTrue(channel.conn.isFine());

    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(2000);

    // set center exchange
    Exchange exchange = Mockito.mock(Exchange.class);
    Server server = Mockito.mock(Server.class);
    Mockito.when(exchange.getServer(dataServerConfig.getPort())).thenReturn(server);
    center.setBoltExchange(exchange);
    Thread.sleep(100);
    center.onChange(Lists.newArrayList(pub.getDataInfoId()), DC);
    center.onTempPubChange(pub, DC);
    Thread.sleep(2000);
    Mockito.verify(exchange, Mockito.times(2)).getServer(dataServerConfig.getPort());
  }
}
