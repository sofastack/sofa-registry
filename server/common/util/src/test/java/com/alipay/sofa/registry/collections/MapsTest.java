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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class MapsTest {
  @Test
  public void test() {
    Assert.assertTrue(Maps.trimMap(null).isEmpty());
    final Map m0 = Maps.trimMap(new HashMap<>());
    Assert.assertTrue(m0.isEmpty());
    TestUtils.assertException(UnsupportedOperationException.class, () -> m0.put(1, 2));

    final Map m1 = Maps.trimMap(Collections.singletonMap(1, 2));
    Assert.assertEquals(m1, Collections.singletonMap(1, 2));
    TestUtils.assertException(UnsupportedOperationException.class, () -> m1.put(1, 2));

    HashMap map = new HashMap();
    map.put(1, 10);
    map.put(2, 20);
    final Map m2 = Maps.trimMap(map);
    Assert.assertEquals(m2, map);
    TestUtils.assertException(UnsupportedOperationException.class, () -> m2.put(1, 2));

    map.put(3, 30);
    final Map m3 = Maps.trimMap(map);
    Assert.assertEquals(m3, map);
    TestUtils.assertException(UnsupportedOperationException.class, () -> m3.put(1, 2));

    map.put(4, 40);
    final Map m4 = Maps.trimMap(map);
    Assert.assertEquals(m4, map);
    TestUtils.assertException(UnsupportedOperationException.class, () -> m4.put(1, 2));

    map.put(5, 50);
    final Map m5 = Maps.trimMap(map);
    Assert.assertEquals(m5, map);
    m5.put(100, 200);
  }
}
