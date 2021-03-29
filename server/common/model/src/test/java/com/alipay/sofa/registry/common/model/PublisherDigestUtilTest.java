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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class PublisherDigestUtilTest {
  private static final Random RANDOM = new Random();
  //  public static void main(String[] args) {
  //    // treemap is faster than list.sort
  //    perf(100000, 200);
  //  }

  @Test
  public void testDigestConflict_sameId() {
    for (int i = 1; i < 100; i++) {
      int count = i * 10;
      Map<String, RegisterVersion> m1 = Maps.newHashMapWithExpectedSize(count);
      Map<String, RegisterVersion> m2 = Maps.newHashMapWithExpectedSize(count);
      for (int j = 0; j < count; j++) {
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long ver = 0;
        int r = RANDOM.nextInt(9);
        m1.put(id, RegisterVersion.of(ver, now));
        if (r % 3 == 0) {
          m2.put(id, RegisterVersion.of(ver, now));
        } else if (r % 3 == 1) {
          m2.put(id, RegisterVersion.of(ver + 1, now));
        } else {
          m2.put(id, RegisterVersion.of(ver + 1, System.currentTimeMillis()));
        }
      }
      DatumDigest d1 = PublisherDigestUtil.digest(new DatumSummary("testDataId", m1));
      DatumDigest d2 = PublisherDigestUtil.digest(new DatumSummary("testDataId", m2));
      Assert.assertFalse(StringFormatter.format("m1={},m2={}", m1, m2), d1.equals(d2));
    }
  }

  @Test
  public void testDigestConflict_diffId() {
    for (int i = 1; i < 100; i++) {
      int count = i * 10;
      Map<String, RegisterVersion> m1 = Maps.newHashMapWithExpectedSize(count);
      Map<String, RegisterVersion> m2 = Maps.newHashMapWithExpectedSize(count);
      for (int j = 0; j < count; j++) {
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long ver = 0;
        int r = RANDOM.nextInt(9);
        m1.put(id, RegisterVersion.of(ver, now));
        if (r % 3 == 0) {
          m2.put(id, RegisterVersion.of(ver, now));
        } else if (r % 3 == 1) {
          m2.put(id, RegisterVersion.of(ver + 1, now + 1));
        } else {
          m2.put(UUID.randomUUID().toString(), RegisterVersion.of(ver, now));
        }
      }
      DatumDigest d1 = PublisherDigestUtil.digest(new DatumSummary("testDataId", m1));
      DatumDigest d2 = PublisherDigestUtil.digest(new DatumSummary("testDataId", m2));
      Assert.assertFalse(StringFormatter.format("m1={},m2={}", m1, m2), d1.equals(d2));
    }
  }

  private static void perf(int count, int len) {
    Map<String, Object> m = Maps.newHashMapWithExpectedSize(len);

    byte[] bs = new byte[100];
    for (int i = 0; i < len; i++) {
      m.put(UUID.randomUUID().toString(), new Object());
    }
    System.out.println("begin warmup");
    for (int i = 0; i < 100000; i++) {
      perfTreeMap(m);
      perfSortList(m);
    }
    System.out.println("finish warmup");
    long start = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      perfTreeMap(m);
    }
    System.out.println("@" + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      perfSortList(m);
    }
    System.out.println("@" + (System.currentTimeMillis() - start));
  }

  private static int perfTreeMap(Map<String, Object> m) {
    TreeMap<String, Object> tree = new TreeMap<>(m);
    int count = 0;
    for (Map.Entry<String, Object> e : tree.entrySet()) {
      count += e.getKey().length();
    }
    return count;
  }

  private static int perfSortList(Map<String, Object> m) {
    String[] array = new String[m.size()];
    int i = 0;
    for (String str : m.keySet()) {
      array[i++] = str;
    }
    Arrays.sort(array);
    int count = 0;
    for (String str : array) {
      count += str.length();
    }
    return count;
  }
}
