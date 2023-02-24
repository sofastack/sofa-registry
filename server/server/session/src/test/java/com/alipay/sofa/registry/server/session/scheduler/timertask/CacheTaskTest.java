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
package com.alipay.sofa.registry.server.session.scheduler.timertask;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CacheTaskTest {
  private String app = "app";
  private String group = "group";
  private String instanceId = "instanceId";
  private String dataInfoId = "dataInfoId";

  private Watchers watchers;
  private Interests interests;
  private DataStore dataStore;

  private void init() {
    this.watchers = Mockito.mock(Watchers.class);
    Watcher watcher = new Watcher();
    watcher.setAppName(app);
    watcher.setGroup(group);
    watcher.setInstanceId(instanceId);
    watcher.setDataInfoId(dataInfoId);
    Mockito.when(watchers.count()).thenReturn(Tuple.of(1L, 1L));
    Mockito.when(watchers.getDataList()).thenReturn(Lists.newArrayList(watcher));

    this.interests = Mockito.mock(Interests.class);
    Subscriber subscriber = new Subscriber();
    subscriber.setAppName(app);
    subscriber.setGroup(group);
    subscriber.setInstanceId(instanceId);
    subscriber.setDataInfoId(dataInfoId);
    Mockito.when(interests.count()).thenReturn(Tuple.of(1L, 2L));
    Mockito.when(interests.getDataList()).thenReturn(Lists.newArrayList(subscriber));
    Mockito.when(interests.getDataInfoIds()).thenReturn(Lists.newArrayList(dataInfoId));
    Mockito.when(interests.getDatas(Mockito.anyString()))
        .thenReturn(Lists.newArrayList(subscriber));

    this.dataStore = Mockito.mock(DataStore.class);
    Publisher publisher = new Publisher();
    publisher.setAppName(app);
    publisher.setGroup(group);
    publisher.setInstanceId(instanceId);
    publisher.setDataInfoId(dataInfoId);
    Mockito.when(dataStore.count()).thenReturn(Tuple.of(1L, 3L));
    Mockito.when(dataStore.getDataList()).thenReturn(Lists.newArrayList(publisher));
    Mockito.when(dataStore.getDataInfoIds()).thenReturn(Lists.newArrayList(dataInfoId));
    Mockito.when(dataStore.getDatas(Mockito.anyString())).thenReturn(Lists.newArrayList(publisher));
  }

  @Test
  public void testCount() {
    init();
    CacheCountTask task = new CacheCountTask();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    task.sessionServerConfig = serverConfigBean;

    // npe
    Assert.assertFalse(task.syncCount());
    task.sessionWatchers = watchers;
    task.sessionDataStore = dataStore;
    task.sessionInterests = interests;

    serverConfigBean.setCacheCountIntervalSecs(0);
    Assert.assertFalse(task.init());
    // has item
    serverConfigBean.setCacheCountIntervalSecs(1);
    Assert.assertTrue(task.syncCount());
    Assert.assertTrue(task.init());
  }

  @Test
  public void testDigest() {
    init();
    SessionCacheDigestTask task = new SessionCacheDigestTask();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    task.sessionServerConfig = serverConfigBean;

    // npe
    Assert.assertFalse(task.dump());
    task.sessionDataStore = dataStore;
    task.sessionInterests = interests;

    serverConfigBean.setCacheDigestIntervalMinutes(0);
    Assert.assertFalse(task.init());
    // has item
    serverConfigBean.setCacheDigestIntervalMinutes(1);
    Assert.assertTrue(task.dump());
    Assert.assertTrue(task.init());
  }

  @Test
  public void testClient() {
    init();
    SyncClientsHeartbeatTask task = new SyncClientsHeartbeatTask();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    task.sessionServerConfig = serverConfigBean;
    task.sessionDataStore = dataStore;
    task.sessionInterests = interests;
    task.sessionWatchers = watchers;

    BoltExchange boltExchange = Mockito.mock(BoltExchange.class);
    task.boltExchange = boltExchange;
    task.executorManager = new ExecutorManager(serverConfigBean);
    task.syncCount();
  }

  @Test
  public void testSplitMultiSub() {
    List<Subscriber> subs = Lists.newArrayList();
    Tuple<List<Subscriber>, List<Subscriber>> tuple = CacheCountTask.splitMultiSub(subs);
    Assert.assertEquals(0, tuple.o1.size());
    Assert.assertEquals(0, tuple.o2.size());

    Subscriber subscriber = new Subscriber();
    subscriber.setAppName(app);
    subscriber.setGroup(group);
    subscriber.setInstanceId(instanceId);
    subscriber.setDataInfoId(dataInfoId);
    subs.add(subscriber);

    tuple = CacheCountTask.splitMultiSub(subs);
    Assert.assertEquals(1, tuple.o1.size());
    Assert.assertEquals(0, tuple.o2.size());

    Subscriber multiSubscriber = new Subscriber();
    subscriber.setAppName(app);
    subscriber.setGroup(group);
    subscriber.setInstanceId(instanceId);
    subscriber.setDataInfoId(dataInfoId);
    subscriber.setAcceptMulti(true);
    subs.add(multiSubscriber);

    tuple = CacheCountTask.splitMultiSub(subs);
    Assert.assertEquals(1, tuple.o1.size());
    Assert.assertEquals(1, tuple.o2.size());
  }
}
