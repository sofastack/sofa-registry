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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.slot.SlotTableCacheImpl;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SessionDataStoreTest {
  @Test
  public void test() {
    SessionDataStore store = new SessionDataStore();
    store.slotTableCache = Mockito.mock(SlotTableCache.class);
    Publisher publisher0 = TestUtils.createTestPublishers(0, 1).get(0);
    Publisher publisher1 = TestUtils.createTestPublishers(1, 1).get(0);
    Mockito.when(store.slotTableCache.slotOf(publisher0.getDataInfoId())).thenReturn(0);
    Mockito.when(store.slotTableCache.slotOf(publisher1.getDataInfoId())).thenReturn(1);

    Assert.assertEquals(store.count().o2.longValue(), 0);
    store.add(publisher0);
    store.add(publisher1);
    Assert.assertEquals(store.count().o2.longValue(), 2);

    Map<String, Map<String, Publisher>> m = store.getDatas();
    Assert.assertEquals(m.size(), 2);
    assertPublisher(m, publisher0);
    assertPublisher(m, publisher1);

    Collection<Publisher> publishers = store.getDatas(publisher0.getDataInfoId());
    Assert.assertEquals(publishers.size(), 1);
    Assert.assertTrue(publishers.contains(publisher0));

    publishers = store.getDatas(publisher1.getDataInfoId());
    Assert.assertEquals(publishers.size(), 1);
    Assert.assertTrue(publishers.contains(publisher1));

    m = store.getDataInfoIdPublishers(0);
    Assert.assertEquals(m.size(), 1);
    assertPublisher(m, publisher0);

    m = store.getDataInfoIdPublishers(1);
    Assert.assertEquals(m.size(), 1);
    assertPublisher(m, publisher1);

    List<Publisher> list = store.getDataList();
    Assert.assertEquals(list.size(), 2);
    Assert.assertTrue(list.contains(publisher0));
    Assert.assertTrue(list.contains(publisher1));

    Collection<String> dataInfoIds = store.getDataInfoIds();
    Assert.assertEquals(dataInfoIds.size(), 2);
    Assert.assertTrue(dataInfoIds.contains(publisher0.getDataInfoId()));
    Assert.assertTrue(dataInfoIds.contains(publisher1.getDataInfoId()));

    Set<ConnectId> connectIds = store.getConnectIds();
    Assert.assertTrue(connectIds.size() <= 2);
    Assert.assertTrue(connectIds.contains(publisher0.connectId()));
    Assert.assertTrue(connectIds.contains(publisher1.connectId()));

    Publisher query = store.queryById("1", "2");
    Assert.assertNull(query);

    query = store.queryById(publisher0.getRegisterId(), "2");
    Assert.assertNull(query);

    query = store.queryById("1", publisher0.getDataInfoId());
    Assert.assertNull(query);

    query = store.queryById(publisher0.getDataInfoId(), publisher0.getRegisterId());
    Assert.assertNull(query);

    query = store.queryById(publisher0.getRegisterId(), publisher0.getDataInfoId());
    Assert.assertEquals(query, publisher0);

    query = store.queryById(publisher1.getRegisterId(), publisher1.getDataInfoId());
    Assert.assertEquals(query, publisher1);

    Map<String, Publisher> map = store.queryByConnectId(ConnectId.of("xx:100", "yy:200"));
    Assert.assertEquals(map.size(), 0);

    map = store.queryByConnectId(publisher0.connectId());
    Assert.assertEquals(map.get(publisher0.getRegisterId()), publisher0);

    map = store.queryByConnectId(publisher1.connectId());
    Assert.assertEquals(map.get(publisher1.getRegisterId()), publisher1);

    Assert.assertNull(store.deleteById("1", "2"));

    Assert.assertNotNull(store.deleteById(publisher0.getRegisterId(), publisher0.getDataInfoId()));
    query = store.queryById(publisher0.getRegisterId(), publisher0.getDataInfoId());
    Assert.assertNull(query);

    query = store.queryById(publisher1.getRegisterId(), publisher1.getDataInfoId());
    Assert.assertNotNull(query);

    map = store.deleteByConnectId(publisher1.connectId());
    Assert.assertEquals(map.size(), 1);
    Assert.assertEquals(map.get(publisher1.getRegisterId()), publisher1);
    Assert.assertEquals(store.getDatas().size(), 0);
  }

  private void assertPublisher(Map<String, Map<String, Publisher>> m, Publisher publisher) {
    Assert.assertEquals(m.get(publisher.getDataInfoId()).get(publisher.getRegisterId()), publisher);
    Assert.assertEquals(m.get(publisher.getDataInfoId()).size(), 1);
  }

  @Test
  public void testConcurrent() throws InterruptedException {
    SessionDataStore store = new SessionDataStore();
    store.slotTableCache = new SlotTableCacheImpl();
    int urlCount = 100;
    List<URL> candUrls = Lists.newArrayListWithExpectedSize(urlCount);
    URL targetAddress = new URL("192.168.0.2", 9600);
    for (int i = 0; i < urlCount; i++) {
      candUrls.add(i, new URL("192.168.0.1", 50000 + i));
    }
    int dataIdCount = 20;
    int ipPerDataId = 100;
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());
    List<Publisher> pubs = Lists.newArrayListWithCapacity(dataIdCount * ipPerDataId * 2);
    Set<URL> urlset = Sets.newHashSetWithExpectedSize(candUrls.size());
    for (int i = 0; i < dataIdCount; i++) {
      for (int j = 0; j < ipPerDataId; j++) {
        Publisher pub = TestUtils.createTestPublisher(StringFormatter.format("dataId-{}", i));
        URL u1 = candUrls.get(random.nextInt(urlCount));
        urlset.add(u1);
        pub.setSourceAddress(u1);
        pub.setTargetAddress(targetAddress);
        pubs.add(pub);
        Publisher newPub = PublisherUtils.clonePublisher(pub);
        newPub.setVersion(pub.getVersion() + 1);
        URL u2 = candUrls.get(random.nextInt(urlCount));
        urlset.add(u2);
        newPub.setSourceAddress(u2);
        newPub.setTargetAddress(targetAddress);
        pubs.add(newPub);
      }
    }
    List<URL> urls = new ArrayList<>(urlset);
    Collections.shuffle(pubs);
    List<Publisher> unpubs = pickSample(pubs, pubs.size() / 10, random);
    List<URL> clientOffUrls = pickSample(urls, urls.size() / 5, random);
    List<ConnectId> clientOffs = Lists.newArrayListWithExpectedSize(clientOffUrls.size());
    for (URL url : clientOffUrls) {
      clientOffs.add(
          new ConnectId(
              url.getIpAddress(), url.getPort(),
              targetAddress.getIpAddress(), targetAddress.getPort()));
    }

    CountDownLatch latch = new CountDownLatch(pubs.size() + unpubs.size() + clientOffs.size());
    ThreadPoolExecutor pubExecutor =
        new ThreadPoolExecutor(20, 20, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(pubs.size()));
    ThreadPoolExecutor unpubExecutor =
        new ThreadPoolExecutor(
            20, 20, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(unpubs.size()));
    ThreadPoolExecutor clientOffExecutor =
        new ThreadPoolExecutor(
            clientOffs.size(),
            clientOffs.size(),
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(clientOffs.size()));
    Timer refreshTimer = new Timer();

    for (Publisher pub : pubs) {
      pubExecutor.execute(
          () -> {
            store.addData(pub);
            latch.countDown();
          });
    }
    for (Publisher pub : unpubs) {
      unpubExecutor.execute(
          () -> {
            store.deleteById(pub.getRegisterId(), pub.getDataInfoId());
            latch.countDown();
          });
    }
    for (ConnectId connectId : clientOffs) {
      clientOffExecutor.execute(
          () -> {
            ConcurrentUtils.sleepUninterruptibly(random.nextInt(10), TimeUnit.MILLISECONDS);
            store.deleteByConnectId(connectId);
            latch.countDown();
          });
    }
    refreshTimer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            store.connectDataIndexer.triggerRefresh();
          }
        },
        0,
        50);
    latch.await();
    for (Publisher publisher : store.getDataList()) {
      Assert.assertTrue(
          store.queryByConnectId(publisher.connectId()).containsKey(publisher.getRegisterId()));
    }
    Assert.assertTrue(store.getConnectIds().size() >= urls.size() - clientOffs.size());
    for (ConnectId connectId : clientOffs) {
      store.deleteByConnectId(connectId);
    }
    for (Publisher publisher : store.getDataList()) {
      Assert.assertTrue(
          store.queryByConnectId(publisher.connectId()).containsKey(publisher.getRegisterId()));
    }
    Assert.assertTrue(store.getConnectIds().size() >= urls.size() - clientOffs.size());

    pubExecutor.shutdown();
    unpubExecutor.shutdown();
    clientOffExecutor.shutdown();
    refreshTimer.cancel();
  }

  public static <T> List<T> pickSample(List<T> population, int nSamplesNeeded, Random r) {
    ArrayList<T> ret = new ArrayList<>(nSamplesNeeded);
    int i = 0, nLeft = population.size();
    while (nSamplesNeeded > 0) {
      int rand = r.nextInt(nLeft);
      if (rand < nSamplesNeeded) {
        ret.add(population.get(i));
        nSamplesNeeded--;
      }
      nLeft--;
      i++;
    }
    Collections.shuffle(ret);
    return ret;
  }
}
