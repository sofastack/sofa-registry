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
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.server.session.store.engine.StoreEngine;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

/** */
public class WatcherStoreTest {

  @Test
  public void test() {
    WatcherStoreImpl watcherStore = new WatcherStoreImpl();

    String dataInfoId = "dataInfoId";
    String registerId = "registerId";
    Watcher watcher = watcherStore.get(dataInfoId, registerId);
    Assert.assertNull(watcher);

    long time = System.currentTimeMillis();
    watcher = new Watcher();
    watcher.setDataInfoId(dataInfoId);
    watcher.setRegisterId(registerId);
    watcher.setVersion(1L);
    watcher.setClientRegisterTimestamp(time);
    watcher.setSourceAddress(new URL("192.168.1.2", 9000));
    watcher.setTargetAddress(new URL("127.0.0.1", 34567));
    boolean success = watcherStore.add(watcher);
    Assert.assertTrue(success);

    watcher = watcherStore.get(dataInfoId, registerId);
    Assert.assertNotNull(watcher);

    Collection<Watcher> watchers = watcherStore.getAll();
    Assert.assertTrue(watchers.contains(watcher));

    watchers = watcherStore.getByDataInfoId(dataInfoId);
    Assert.assertTrue(watchers.contains(watcher));

    Collection<ConnectId> connectIds = watcherStore.getAllConnectId();
    Assert.assertEquals(1, connectIds.size());

    ConnectId connectId = connectIds.stream().findAny().get();
    Assert.assertNotNull(connectId);

    watchers = watcherStore.getByConnectId(connectId);
    Assert.assertTrue(watchers.contains(watcher));

    StoreEngine.StoreStat storeStat = watcherStore.stat();
    Assert.assertEquals(1, storeStat.nonEmptyDataIdSize());
    Assert.assertEquals(1, storeStat.size());

    watcherStore.delete(connectId);
    watcher = watcherStore.get(dataInfoId, registerId);
    Assert.assertNull(watcher);
    storeStat = watcherStore.stat();
    Assert.assertEquals(0, storeStat.nonEmptyDataIdSize());
    Assert.assertEquals(0, storeStat.size());

    Watcher watcher00 = new Watcher();
    watcher00.setDataInfoId(dataInfoId + "00");
    watcher00.setRegisterId(registerId + "00");
    watcher00.setVersion(1L);
    watcher00.setClientRegisterTimestamp(time);
    watcher00.setSourceAddress(new URL("192.168.1.2", 9000));
    watcher00.setTargetAddress(new URL("127.0.0.1", 34567));
    watcherStore.add(watcher00);

    storeStat = watcherStore.stat();
    Assert.assertEquals(1, storeStat.nonEmptyDataIdSize());
    Assert.assertEquals(1, storeStat.size());

    Watcher deleted = watcherStore.delete(dataInfoId + "00", registerId + "00");
    Assert.assertEquals(watcher00, deleted);

    storeStat = watcherStore.stat();
    Assert.assertEquals(0, storeStat.nonEmptyDataIdSize());
    Assert.assertEquals(0, storeStat.size());
  }
}
