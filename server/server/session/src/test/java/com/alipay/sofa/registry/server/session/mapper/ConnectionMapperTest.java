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
package com.alipay.sofa.registry.server.session.mapper;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionMapperTest {
  @Test
  public void test() {
    final String key = "testKey";
    final String val = "testVal";
    ConnectionMapper mapper = new ConnectionMapper();
    mapper.add(key, val);
    Assert.assertTrue(mapper.contains(key));
    Assert.assertEquals(mapper.get(key), val);
    Assert.assertNotEquals(mapper.get(key + "1"), val);
    Assert.assertEquals(mapper.size(), 1);
    Assert.assertEquals(mapper.get(), Collections.singletonMap(key, val));
    mapper.remove(key + "1");
    Assert.assertEquals(mapper.size(), 1);
    mapper.remove(key);
    Assert.assertNull(mapper.get(key));
  }
}
