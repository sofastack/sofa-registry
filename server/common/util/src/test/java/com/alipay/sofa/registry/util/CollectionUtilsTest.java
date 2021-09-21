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
package com.alipay.sofa.registry.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class CollectionUtilsTest {
  List<String> stringCollection =
      new ArrayList<>(Arrays.asList("zhangsan", "lisi", "wangwu", "zhaoliu", "sunqi", "zhouba"));

  @Test
  public void testGetRandom() {
    Object obj = CollectionUtils.getRandom(Collections.emptyList());
    Assert.assertNull(obj);
    boolean allValueSame = true;
    String firstValue = null;
    for (int i = 0; i < 10; i++) {
      String radomeValue = CollectionUtils.getRandom(stringCollection);
      Assert.assertTrue(stringCollection.contains(radomeValue));
      if (firstValue == null) {
        firstValue = radomeValue;
      } else if (!radomeValue.equals(firstValue)) {
        allValueSame = false;
      }
    }
    Assert.assertFalse(allValueSame);
  }

  @Test
  public void testSingleMap() {
    Map<String, String> m = Maps.newHashMap();
    Map<String, String> to = CollectionUtils.toSingletonMap(m);
    Assert.assertEquals(to, m);
    Assert.assertTrue(to == m);

    m.put("1", "a");
    m.put("2", "b");
    to = CollectionUtils.toSingletonMap(m);
    Assert.assertEquals(to, m);
    Assert.assertTrue(to == m);

    m.remove("1");
    to = CollectionUtils.toSingletonMap(m);
    Assert.assertEquals(to, m);
    Assert.assertTrue(to != m);

    try {
      to.clear();
      Assert.fail();
    } catch (UnsupportedOperationException exception) {
    }
  }

  @Test
  public void testFuzzyTotalSize() {
    List<String> items = Lists.newArrayList();
    for (int i = 0; i < 2000; i++) {
      items.add("1234567890");
    }
    Assert.assertEquals(20000, CollectionUtils.fuzzyTotalSize(items, String::length));
    List<String> items2 = Lists.newArrayList("12345");
    Assert.assertEquals(5, CollectionUtils.fuzzyTotalSize(items2, String::length));
    Assert.assertEquals(0, CollectionUtils.fuzzyTotalSize(items2, s -> 0));
    Assert.assertEquals(0, CollectionUtils.fuzzyTotalSize(Lists.newArrayList(), s -> 100));
  }
}
