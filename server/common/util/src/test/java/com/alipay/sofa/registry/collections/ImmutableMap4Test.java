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
package com.alipay.sofa.registry.collections;

import com.alipay.sofa.registry.TestUtils;
import com.google.common.collect.Sets;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableMap4Test {

  @Test
  public void test() {
    Map<Integer, String> m = new HashMap<>();
    m.put(1, "a");
    ImmutableMap4 map4 = ImmutableMap4.newMap(m);
    Assert.assertTrue(map4.containsKey(1));
    Assert.assertFalse(map4.containsKey("a"));
    Assert.assertEquals(map4.keySet(), Sets.newHashSet(1));
    Assert.assertTrue(map4.containsValue("a"));
    Assert.assertFalse(map4.containsValue(1));
    Assert.assertEquals(map4.values().size(), 1);
    Assert.assertTrue(map4.values().contains("a"));
    Assert.assertEquals(map4.toMap(), m);
    Assert.assertEquals(map4, m);
    Assert.assertEquals(map4.hashCode(), m.hashCode());

    m.put(2, "b");
    map4 = ImmutableMap4.newMap(m);
    Assert.assertTrue(map4.containsKey(1));
    Assert.assertFalse(map4.containsKey("a"));
    Assert.assertTrue(map4.containsKey(2));
    Assert.assertFalse(map4.containsKey("b"));
    Assert.assertEquals(map4.keySet(), Sets.newHashSet(1, 2));
    Assert.assertTrue(map4.containsValue("a"));
    Assert.assertFalse(map4.containsValue(1));
    Assert.assertTrue(map4.containsValue("b"));
    Assert.assertFalse(map4.containsValue(2));
    Assert.assertEquals(map4.values().size(), 2);
    Assert.assertTrue(map4.values().contains("a"));
    Assert.assertTrue(map4.values().contains("b"));
    Assert.assertEquals(map4.toMap(), m);
    Assert.assertEquals(map4, m);
    Assert.assertEquals(map4.hashCode(), m.hashCode());

    m.put(3, "c");
    map4 = ImmutableMap4.newMap(m);
    Assert.assertTrue(map4.containsKey(1));
    Assert.assertFalse(map4.containsKey("a"));
    Assert.assertTrue(map4.containsKey(2));
    Assert.assertFalse(map4.containsKey("b"));
    Assert.assertTrue(map4.containsKey(3));
    Assert.assertFalse(map4.containsKey("c"));
    Assert.assertEquals(map4.keySet(), Sets.newHashSet(1, 2, 3));
    Assert.assertTrue(map4.containsValue("a"));
    Assert.assertFalse(map4.containsValue(1));
    Assert.assertTrue(map4.containsValue("b"));
    Assert.assertFalse(map4.containsValue(2));
    Assert.assertTrue(map4.containsValue("c"));
    Assert.assertFalse(map4.containsValue(3));
    Assert.assertEquals(map4.values().size(), 3);
    Assert.assertTrue(map4.values().contains("a"));
    Assert.assertTrue(map4.values().contains("b"));
    Assert.assertTrue(map4.values().contains("c"));
    Assert.assertEquals(map4.toMap(), m);
    Assert.assertEquals(map4, m);
    Assert.assertEquals(map4.hashCode(), m.hashCode());

    m.put(4, "d");
    map4 = ImmutableMap4.newMap(m);
    Assert.assertTrue(map4.containsKey(1));
    Assert.assertFalse(map4.containsKey("a"));
    Assert.assertTrue(map4.containsKey(2));
    Assert.assertFalse(map4.containsKey("b"));
    Assert.assertTrue(map4.containsKey(3));
    Assert.assertFalse(map4.containsKey("c"));
    Assert.assertTrue(map4.containsKey(4));
    Assert.assertFalse(map4.containsKey("d"));
    Assert.assertEquals(map4.keySet(), Sets.newHashSet(1, 2, 3, 4));
    Assert.assertTrue(map4.containsValue("a"));
    Assert.assertFalse(map4.containsValue(1));
    Assert.assertTrue(map4.containsValue("b"));
    Assert.assertFalse(map4.containsValue(2));
    Assert.assertTrue(map4.containsValue("c"));
    Assert.assertFalse(map4.containsValue(3));
    Assert.assertTrue(map4.containsValue("d"));
    Assert.assertFalse(map4.containsValue(4));
    Assert.assertEquals(map4.values().size(), 4);
    Assert.assertTrue(map4.values().contains("a"));
    Assert.assertTrue(map4.values().contains("b"));
    Assert.assertTrue(map4.values().contains("c"));
    Assert.assertTrue(map4.values().contains("d"));
    Assert.assertEquals(map4.toMap(), m);
    Assert.assertEquals(map4, m);
    Assert.assertEquals(map4.hashCode(), m.hashCode());

    Assert.assertTrue(map4.toString(), map4.toString().contains("a"));
  }

  @Test
  public void testNew() {
    ImmutableMap4 map4 = ImmutableMap4.newMap(Collections.emptyMap());
    Assert.assertEquals(map4.size(), 0);
    Assert.assertTrue(map4.isEmpty());

    map4 = ImmutableMap4.newMap(Collections.singletonMap(1, 1));
    Assert.assertEquals(map4.size(), 1);
    Assert.assertFalse(map4.isEmpty());

    Map<Integer, Integer> m = new HashMap<>();
    m.put(1, 1);
    m.put(2, 2);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertEquals(map4.size(), 2);
    Assert.assertFalse(map4.isEmpty());

    m.put(3, 3);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertEquals(map4.size(), 3);
    Assert.assertFalse(map4.isEmpty());

    m.put(4, 4);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertEquals(map4.size(), 4);
    Assert.assertFalse(map4.isEmpty());

    m.put(5, 5);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertNull(map4);

    m.put(6, 6);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertNull(map4);

    m = new HashMap<>();
    m.put(1, 1);
    m.put(null, 2);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertNull(map4);

    m = new HashMap<>();
    m.put(1, 1);
    m.put(2, null);
    map4 = ImmutableMap4.newMap(m);
    Assert.assertNull(map4);
  }

  @Test
  public void testUnsupported() {
    ImmutableMap4 map4 = ImmutableMap4.newMap(Collections.emptyMap());
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.put(1, 2));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.remove(1));
    TestUtils.assertException(
        UnsupportedOperationException.class, () -> map4.putAll(Collections.emptyMap()));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.clear());
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.replaceAll(null));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.putIfAbsent(1, 2));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.remove(1, 2));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.replace(1, 2, 3));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.replace(1, 2));
    TestUtils.assertException(
        UnsupportedOperationException.class, () -> map4.computeIfAbsent(1, null));
    TestUtils.assertException(
        UnsupportedOperationException.class, () -> map4.computeIfPresent(1, null));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.compute(1, null));
    TestUtils.assertException(UnsupportedOperationException.class, () -> map4.merge(1, 2, null));
  }

  @Test
  public void testPerf() {
    Map<String, String> m = new HashMap<>();
    for (int i = 0; i < 4; i++) {
      m.put(System.currentTimeMillis() + ":" + i, System.currentTimeMillis() + ":" + i * 2);
    }
    ImmutableMap4 map4 = ImmutableMap4.newMap(m);
    List<String> keys = new ArrayList<>(m.keySet());
    int num = 10000000;
    for (int i = 0; i < num * 1; i++) {
      for (String key : keys) {
        m.get(key);
        map4.get(key);
      }
    }

    long start = System.currentTimeMillis();
    for (int i = 0; i < num; i++) {
      for (String key : keys) {
        m.get(key);
      }
    }
    System.out.println(System.currentTimeMillis() - start);

    start = System.currentTimeMillis();
    for (int i = 0; i < num; i++) {
      for (String key : keys) {
        map4.get(key);
      }
    }
    System.out.println(System.currentTimeMillis() - start);
  }
}
