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
package com.alipay.sofa.registry.server.shared.providedata;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class AbstractProvideDataWatcherTest {

  @Test
  public void test() {
    TestWatcher watcher = new TestWatcher();
    watcher.metaNodeService = mock(MetaServerService.class);

    Assert.assertEquals(watcher.watcherDog.getWaitingMillis(), watcher.fetchIntervalMillis());

    String dataId = "testDataId";
    Assert.assertNull(watcher.get(dataId));
    Assert.assertNull(watcher.getDataWatcher(dataId));

    Assert.assertTrue(watcher.watch(dataId));
    Assert.assertNull(watcher.get(dataId));
    AbstractProvideDataWatcher.DataWatcher dw = watcher.getDataWatcher(dataId);
    Assert.assertNotNull(dw);
    final long time = dw.watchTimestamp;
    ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.MILLISECONDS);
    Assert.assertTrue(watcher.watch(dataId));
    Assert.assertEquals(1, watcher.refreshWatch(Collections.singletonList(dataId)));
    Assert.assertTrue(dw.watchTimestamp > time);

    watcher.fetch();
    verify(watcher.metaNodeService, times(1)).fetchData(anyMap());

    Map<String, ProvideData> updateMap = Maps.newHashMap();
    updateMap.put("dataIdNotExist", null);
    ProvideData pd = new ProvideData(null, dataId, 100L);
    updateMap.put(dataId, pd);
    watcher.updateFetchData(updateMap);
    Assert.assertTrue(watcher.get(dataId) == pd);

    watcher.updateFetchData(updateMap);
    Assert.assertTrue(watcher.get(dataId) == pd);

    ProvideData pd2 = new ProvideData(null, dataId, 200L);
    updateMap.put(dataId, pd2);

    watcher.updateFetchData(updateMap);
    Assert.assertTrue(watcher.get(dataId) == pd2);

    Assert.assertTrue(TestWatcher.transferQuery(Collections.emptyList(), 2).isEmpty());

    List<AbstractProvideDataWatcher.DataWatcher> watcherList = Lists.newArrayList();
    watcherList.add(dw);
    watcherList.add(new AbstractProvideDataWatcher.DataWatcher("test1"));
    AbstractProvideDataWatcher.DataWatcher dw2 =
        new AbstractProvideDataWatcher.DataWatcher("test2");
    dw2.provideData = new ProvideData(null, "test2", 50L);
    watcherList.add(dw2);
    List<Map<String, Long>> result = TestWatcher.transferQuery(watcherList, 100);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(200, result.get(0).get(dataId).longValue());
    Assert.assertEquals(-1, result.get(0).get("test1").longValue());
    Assert.assertEquals(50, result.get(0).get("test2").longValue());

    result = TestWatcher.transferQuery(watcherList, 2);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(200, result.get(0).get(dataId).longValue());
    Assert.assertEquals(-1, result.get(0).get("test1").longValue());
    Assert.assertEquals(50, result.get(1).get("test2").longValue());

    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    // clean expire
    watcher.watcherDog.runUnthrowable();
    Assert.assertNull(watcher.getDataWatcher(dataId));
    watcher.lease = 1000;
    watcher.start();
  }

  private static final class TestWatcher extends AbstractProvideDataWatcher {
    int lease = 0;

    protected TestWatcher() {
      super("test");
    }

    @Override
    protected int fetchBatchSize() {
      return 10;
    }

    @Override
    protected int fetchIntervalMillis() {
      return 10;
    }

    @Override
    protected int watcherLeaseSecs() {
      return lease;
    }
  }
}
