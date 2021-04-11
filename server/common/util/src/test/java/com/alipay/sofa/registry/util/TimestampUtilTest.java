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

import java.sql.Timestamp;
import org.junit.Assert;
import org.junit.Test;

public class TimestampUtilTest {
  @Test
  public void test() {
    long now = System.currentTimeMillis();
    Timestamp ts = new Timestamp(now);
    Assert.assertEquals(String.valueOf(now), TimestampUtil.getNanosLong(ts), now * 1000000);
  }

  @Test
  public void testNanos() {
    long now = System.currentTimeMillis();
    long nanos = now / 1000 * 1000000000;
    Timestamp ts = new Timestamp(now);
    for (int i = 0; i <= 999999999; i++) {
      ts.setNanos(i);
      Assert.assertEquals(TimestampUtil.getNanosLong(ts), nanos + i);
    }
  }
}
