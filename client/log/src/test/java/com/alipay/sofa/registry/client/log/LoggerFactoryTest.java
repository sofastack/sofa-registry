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
package com.alipay.sofa.registry.client.log;

import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class LoggerFactoryTest {

  @Test
  public void getLogger() {
    Assert.assertNull(LoggerFactory.getLogger((String) null));
    Assert.assertNull(LoggerFactory.getLogger((Class) null));
    Assert.assertEquals(
        LoggerFactory.getLogger(LoggerFactoryTest.class),
        LoggerFactory.getLogger(LoggerFactoryTest.class.getCanonicalName()));
  }

  @Test
  public void getLogSpace() {
    Assert.assertEquals("com.alipay.sofa.registry.client", LoggerFactory.getLogSpace());
    String key = "registry.client.log.space";
    String old = System.getProperty(key); //
    try {
      System.setProperty(key, "xxx");
      Assert.assertEquals("xxx", LoggerFactory.getLogSpace());
    } finally {
      if (old == null) {
        System.clearProperty(key);
      } else {
        System.setProperty(key, old);
      }
    }
    Assert.assertEquals("com.alipay.sofa.registry.client", LoggerFactory.getLogSpace());
  }

  @Test
  public void testIsBlank() {
    Assert.assertFalse(LoggerFactory.isBlank("123"));
    Assert.assertTrue(LoggerFactory.isBlank(" "));
    Assert.assertTrue(LoggerFactory.isBlank(""));
    Assert.assertTrue(LoggerFactory.isBlank(null));
    Assert.assertTrue(LoggerFactory.isBlank("   "));
    Assert.assertTrue(LoggerFactory.isBlank("\r\t"));
  }
}
