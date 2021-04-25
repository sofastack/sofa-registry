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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SlotSessionDataStoreTest {
  @Test
  public void test() {
    SlotSessionDataStore store = new SlotSessionDataStore();
    store.slotTableCache = Mockito.mock(SlotTableCache.class);
    Publisher publisher0 = TestUtils.createTestPublishers(0, 1).get(0);
    Publisher publisher1 = TestUtils.createTestPublishers(1, 1).get(0);
    Mockito.when(store.slotTableCache.slotOf(publisher0.getDataInfoId())).thenReturn(0);
    Mockito.when(store.slotTableCache.slotOf(publisher1.getDataInfoId())).thenReturn(1);

    Assert.assertEquals(store.count(), 0);
    store.add(publisher0);
    store.add(publisher1);
    Assert.assertEquals(store.count(), 2);

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

    Set<String> processIds = store.collectProcessIds();
    Assert.assertTrue(processIds.size() <= 2);
    Assert.assertTrue(processIds.contains(publisher0.getProcessId()));
    Assert.assertTrue(processIds.contains(publisher1.getProcessId()));

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

    Assert.assertFalse(store.deleteById("1", "2"));

    Assert.assertTrue(store.deleteById(publisher0.getRegisterId(), publisher0.getDataInfoId()));
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
}
