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
package com.alipay.sofa.registry.concurrent;

import org.junit.Assert;
import org.junit.Test;

public class ThreadLocalStringBuilderTest {
  @Test
  public void test() {
    StringBuilder sb = ThreadLocalStringBuilder.get();
    for (int i = 0; i < 10000; i++) {
      sb.append("1");
    }
    ThreadLocalStringBuilder.get();
  }

  @Test
  public void testJoin() {
    Assert.assertEquals("12", ThreadLocalStringBuilder.join("1", "2"));
    Assert.assertEquals("123", ThreadLocalStringBuilder.join("1", "2", "3"));
    Assert.assertEquals("1234", ThreadLocalStringBuilder.join("1", "2", "3", "4"));
    Assert.assertEquals("12345", ThreadLocalStringBuilder.join("1", "2", "3", "4", "5"));
    Assert.assertEquals("123456", ThreadLocalStringBuilder.join("1", "2", "3", "4", "5", "6"));
  }
}
