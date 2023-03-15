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

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.store.PublisherStore;
import com.alipay.sofa.registry.server.session.store.SubscriberStore;
import com.alipay.sofa.registry.server.session.store.WatcherStore;
import com.alipay.sofa.registry.server.session.store.engine.StoreEngine;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CacheTaskTest {

  private WatcherStore watcherStore;
  private SubscriberStore subscriberStore;
  private PublisherStore publisherStore;

  private void init() {
    final String app = "app";
    final String group = "group";
    final String instanceId = "instanceId";
    final String dataInfoId = "dataInfoId";

    this.watcherStore = Mockito.mock(WatcherStore.class);
    Watcher watcher = new Watcher();
    watcher.setAppName(app);
    watcher.setGroup(group);
    watcher.setInstanceId(instanceId);
    watcher.setDataInfoId(dataInfoId);
    Mockito.when(watcherStore.stat()).thenReturn(new StoreEngine.StoreStat(1, 1));
    Mockito.when(watcherStore.getAll()).thenReturn(Lists.newArrayList(watcher));

    this.subscriberStore = Mockito.mock(SubscriberStore.class);
    Subscriber subscriber = new Subscriber();
    subscriber.setAppName(app);
    subscriber.setGroup(group);
    subscriber.setInstanceId(instanceId);
    subscriber.setDataInfoId(dataInfoId);
    Mockito.when(subscriberStore.stat()).thenReturn(new StoreEngine.StoreStat(1, 2));
    Mockito.when(subscriberStore.getAll()).thenReturn(Lists.newArrayList(subscriber));
    Mockito.when(subscriberStore.getNonEmptyDataInfoId())
        .thenReturn(Lists.newArrayList(dataInfoId));
    Mockito.when(subscriberStore.getByDataInfoId(Mockito.anyString()))
        .thenReturn(Lists.newArrayList(subscriber));

    this.publisherStore = Mockito.mock(PublisherStore.class);
    Publisher publisher = new Publisher();
    publisher.setAppName(app);
    publisher.setGroup(group);
    publisher.setInstanceId(instanceId);
    publisher.setDataInfoId(dataInfoId);
    Mockito.when(publisherStore.stat()).thenReturn(new StoreEngine.StoreStat(1L, 3L));
    Mockito.when(publisherStore.getAll()).thenReturn(Lists.newArrayList(publisher));
    Mockito.when(publisherStore.getNonEmptyDataInfoId()).thenReturn(Lists.newArrayList(dataInfoId));
    Mockito.when(publisherStore.getByDataInfoId(Mockito.anyString()))
        .thenReturn(Lists.newArrayList(publisher));
  }

  @Test
  public void testCount() {
    init();
    CacheCountTask task = new CacheCountTask();
    SessionServerConfigBean serverConfigBean = TestUtils.newSessionConfig("testDc");
    task.sessionServerConfig = serverConfigBean;

    // npe
    Assert.assertFalse(task.syncCount());
    task.watcherStore = watcherStore;
    task.publisherStore = publisherStore;
    task.subscriberStore = subscriberStore;

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
    task.publisherStore = publisherStore;
    task.subscriberStore = subscriberStore;

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
    task.publisherStore = publisherStore;
    task.subscriberStore = subscriberStore;
    task.watcherStore = watcherStore;

    task.boltExchange = Mockito.mock(BoltExchange.class);
    task.executorManager = new ExecutorManager(serverConfigBean);
    task.syncCount();
  }
}
