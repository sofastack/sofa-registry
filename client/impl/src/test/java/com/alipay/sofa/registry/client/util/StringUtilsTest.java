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
package com.alipay.sofa.registry.client.util;

import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class StringUtilsTest {

  @Test
  public void isEmpty() {
    Assert.assertTrue(StringUtils.isEmpty(null));
    Assert.assertTrue(StringUtils.isEmpty(""));
    Assert.assertFalse(StringUtils.isEmpty(" "));
  }

  @Test
  public void isNotEmpty() {
    Assert.assertFalse(StringUtils.isNotEmpty(null));
    Assert.assertFalse(StringUtils.isNotEmpty(""));
    Assert.assertTrue(StringUtils.isNotEmpty(" "));
  }

  @Test
  public void isBlank() {
    Assert.assertTrue(StringUtils.isBlank(null));
    Assert.assertTrue(StringUtils.isBlank(""));
    Assert.assertTrue(StringUtils.isBlank(" "));
    Assert.assertFalse(StringUtils.isBlank(" 1"));
  }

  @Test
  public void isNotBlank() {
    Assert.assertFalse(StringUtils.isNotBlank(null));
    Assert.assertFalse(StringUtils.isNotBlank(""));
    Assert.assertFalse(StringUtils.isNotBlank(" "));
    Assert.assertTrue(StringUtils.isNotBlank(" 1"));
  }

  @Test
  public void defaultString() {
    Assert.assertEquals("", StringUtils.defaultString(null));
    Assert.assertEquals("x", StringUtils.defaultString("x"));
  }

  @Test
  public void testToString() {
    Assert.assertEquals(null, StringUtils.toString(null));
    Assert.assertEquals("Bean:11", StringUtils.toString(new Bean("11")));

    Assert.assertEquals(null, StringUtils.toString((Object) null, null));
    Assert.assertEquals("1", StringUtils.toString((Object) null, "1"));
    Assert.assertEquals("Bean:11", StringUtils.toString(new Bean("11"), null));
  }

  class Bean {
    private String s;

    public Bean() {}

    public Bean(String s) {
      this.s = s;
    }

    @Override
    public String toString() {
      return "Bean:" + s;
    }
  }
}
