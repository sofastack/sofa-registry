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
package com.alipay.sofa.registry.server.session.store.engine;

import com.alipay.sofa.registry.common.model.store.Watcher;
import java.util.Collection;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

/** 测试基础的存储引擎 */
public class SimpleMemoryStoreEngineTest {

  @Test
  public void test() {
    String dataInfoId = "dataInfoId";
    String registerId = "registerId";
    SimpleMemoryStoreEngine<Watcher> storeEngine = new SimpleMemoryStoreEngine<>(128);
    Watcher watcher = storeEngine.get(dataInfoId, registerId);
    Assert.assertNull(watcher);

    long time = System.currentTimeMillis();

    watcher = new Watcher();
    watcher.setDataInfoId(dataInfoId);
    watcher.setRegisterId(registerId);
    watcher.setVersion(1L);
    watcher.setClientRegisterTimestamp(time);
    Pair<Boolean, Watcher> pair = storeEngine.putIfAbsent(watcher);
    Assert.assertTrue(pair.getKey());
    Assert.assertNull(pair.getValue());
    // 重复添加，且版本相同的情况则添加失败，返回的Watcher是已经存在的Watcher
    pair = storeEngine.putIfAbsent(watcher);
    Assert.assertFalse(pair.getKey());
    Assert.assertEquals(watcher, pair.getValue());
    // 重复添加，提升版本好，能添加成功
    Watcher watcher1 = new Watcher();
    watcher1.setDataInfoId(dataInfoId);
    watcher1.setRegisterId(registerId);
    watcher1.setVersion(2L);
    watcher1.setClientRegisterTimestamp(time);
    pair = storeEngine.putIfAbsent(watcher1);
    Assert.assertTrue(pair.getKey());
    Assert.assertEquals(watcher, pair.getValue());

    // get出来是最新添加的Watcher
    Watcher watcher2 = storeEngine.get(dataInfoId, registerId);
    Assert.assertEquals(watcher1, watcher2);

    // 批量获取的结果中包含了目标Watcher
    Collection<Watcher> watchers = storeEngine.get(dataInfoId);
    Assert.assertTrue(watchers.contains(watcher1));
    Collection<Watcher> allWatchers = storeEngine.getAll();
    Assert.assertTrue(allWatchers.contains(watcher1));

    // 验证删除元素
    Watcher deletedWatcher = storeEngine.delete(dataInfoId, registerId);
    Assert.assertEquals(watcher1, deletedWatcher);

    // 同一个DataInfoId增加两个元素，用于验证统计结果
    Watcher watcher00 = new Watcher();
    watcher00.setDataInfoId(dataInfoId + "00");
    watcher00.setRegisterId(registerId + "00");
    watcher00.setVersion(1L);
    watcher00.setClientRegisterTimestamp(time);
    storeEngine.putIfAbsent(watcher00);

    Watcher watcher01 = new Watcher();
    watcher01.setDataInfoId(dataInfoId + "00");
    watcher01.setRegisterId(registerId + "01");
    watcher01.setVersion(1L);
    watcher01.setClientRegisterTimestamp(time);
    storeEngine.putIfAbsent(watcher01);

    Collection<String> nonEmptyDataInfoIdCollection = storeEngine.getNonEmptyDataInfoId();
    Assert.assertEquals(1, nonEmptyDataInfoIdCollection.size());
    Assert.assertTrue(nonEmptyDataInfoIdCollection.contains(dataInfoId + "00"));

    StoreEngine.StoreStat storeStat = storeEngine.stat();
    Assert.assertEquals(1, storeStat.nonEmptyDataIdSize());
    Assert.assertEquals(2, storeStat.size());
  }
}
