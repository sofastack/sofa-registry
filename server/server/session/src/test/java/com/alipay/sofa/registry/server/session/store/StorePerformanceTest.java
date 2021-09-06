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

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.slot.SlotTableCacheImpl;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.List;
import org.junit.Test;

public class StorePerformanceTest {

  private static final int DATA_ID_NUM = 10000;
  private static final int DATA_NUM = 80;

  @Test
  public void test() {
    // too slow
    //    warm();
    //    testSub0();
    //    testSub1();
  }

  public static void warm() {
    SessionInterests store = new SessionInterests();

    for (int i = 0; i < 5; i++) {
      int slotId = i % 256;
      List<Publisher> list = TestUtils.createTestPublishers(slotId, 5);
      for (Publisher p : list) {
        Subscriber sub = new Subscriber();
        sub.setRegisterId(p.getRegisterId());
        sub.setDataInfoId(p.getDataInfoId());
        sub.setClientVersion(BaseInfo.ClientVersion.StoreData);
        sub.setScope(ScopeEnum.zone);
        sub.setSourceAddress(new URL("192.168.0.1", 54321));
        sub.setTargetAddress(new URL("192.168.0.2", 9600));
        sub.needPushEmpty("testdc");
        store.add(sub);
      }
    }
    final long start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      final List<Subscriber> l = store.getDataList();
      for (Subscriber s : l) {
        s.needPushEmpty("testdc");
      }
      store.forEach(
          (d, map) -> {
            for (Subscriber s : map.values()) {
              s.needPushEmpty("testdc");
            }
          });
      if (i % 10000 == 0) {
        System.out.println(i + ":" + (System.currentTimeMillis() - start));
      }
    }
  }

  public static void testSub0() {
    SessionInterests store = new SessionInterests();

    for (int i = 0; i < DATA_ID_NUM; i++) {
      int slotId = i % 256;
      List<Publisher> list = TestUtils.createTestPublishers(slotId, DATA_NUM);
      for (Publisher p : list) {
        Subscriber sub = new Subscriber();
        sub.setRegisterId(p.getRegisterId());
        sub.setDataInfoId(p.getDataInfoId());
        sub.setClientVersion(BaseInfo.ClientVersion.StoreData);
        sub.setScope(ScopeEnum.zone);
        sub.needPushEmpty("testdc");
        store.add(sub);
      }
    }
    List<Subscriber> list = store.getDataList();
    System.out.println("start test");
    long size = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      final List<Subscriber> l = store.getDataList();
      for (Subscriber s : l) {
        s.needPushEmpty("testdc");
      }
      size += l.size();
      if (i % 100 == 0) {
        System.out.println(i + ":" + (System.currentTimeMillis() - start));
      }
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));
    if (true) {
      return;
    }
    size = 0;
    ConcurrentUtils.createDaemonThread("test-mofidier", new Modifier((List) list, store)).start();
    start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      final List<Subscriber> l = store.getDataList();
      for (Subscriber s : l) {
        s.needPushEmpty("testdc");
      }
      size += l.size();
      if (i % 100 == 0) {
        System.out.println(i + ":" + (System.currentTimeMillis() - start));
      }
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));
  }

  public static void testSub1() {
    SessionInterests store = new SessionInterests();

    for (int i = 0; i < DATA_ID_NUM; i++) {
      int slotId = i % 256;
      List<Publisher> list = TestUtils.createTestPublishers(slotId, DATA_NUM);
      for (Publisher p : list) {
        Subscriber sub = new Subscriber();
        sub.setRegisterId(p.getRegisterId());
        sub.setDataInfoId(p.getDataInfoId());
        sub.setClientVersion(BaseInfo.ClientVersion.StoreData);
        sub.setScope(ScopeEnum.zone);
        sub.needPushEmpty("testdc");
        store.add(sub);
      }
    }
    List<Subscriber> list = store.getDataList();
    System.out.println("start test");
    long size = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      store.forEach(
          (d, map) -> {
            for (Subscriber s : map.values()) {
              s.needPushEmpty("testdc");
            }
          });
      if (i % 100 == 0) {
        System.out.println(i + ":" + (System.currentTimeMillis() - start));
      }
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));

    if (true) {
      return;
    }
    size = 0;
    ConcurrentUtils.createDaemonThread("test-mofidier", new Modifier((List) list, store)).start();
    start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      store.forEach(
          (d, map) -> {
            for (Subscriber s : map.values()) {
              s.needPushEmpty("testdc");
            }
          });
      if (i % 100 == 0) {
        System.out.println(i + ":" + (System.currentTimeMillis() - start));
      }
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));
  }

  private static final class Modifier implements Runnable {
    final List<BaseInfo> infoList;
    final AbstractDataManager mgr;

    Modifier(List<BaseInfo> list, AbstractDataManager mgr) {
      this.infoList = list;
      this.mgr = mgr;
    }

    @Override
    public void run() {
      int i = 0;
      for (; ; ) {
        if (i < 0) {
          i = 0;
        }
        BaseInfo b = infoList.get(i % infoList.size());
        mgr.deleteById(b.getRegisterId(), b.getDataInfoId());
        mgr.add(b);
      }
    }
  }

  public static void testData() {
    SessionDataStore store = new SessionDataStore();
    store.slotTableCache = new SlotTableCacheImpl();

    for (int i = 0; i < DATA_ID_NUM; i++) {
      int slotId = i % 256;
      List<Publisher> list = TestUtils.createTestPublishers(slotId, DATA_NUM);
      for (Publisher p : list) {
        store.add(p);
      }
    }
    System.out.println("start warm");
    for (int i = 0; i < 10000; i++) {
      store.getDataList();
    }
    System.out.println("start test");
    long size = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      size += store.getDataList().size();
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));

    size = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      size += store.getDataList().size();
    }
    System.out.println(size + ":" + (System.currentTimeMillis() - start));
  }
}
