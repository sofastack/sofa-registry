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
package com.alipay.sofa.registry.common.model.metaserver.metrics;

import org.junit.Assert;
import org.junit.Test;

public class SystemLoadTest {

  @Test
  public void testDefaultConstructor() {
    SystemLoad load = new SystemLoad();
    Assert.assertEquals(0.0, load.getCpuAverage(), 0.001);
    Assert.assertEquals(0.0, load.getLoadAverage(), 0.001);
  }

  @Test
  public void testConstructorWithParams() {
    SystemLoad load = new SystemLoad(75.5, 4.2);
    Assert.assertEquals(75.5, load.getCpuAverage(), 0.001);
    Assert.assertEquals(4.2, load.getLoadAverage(), 0.001);
  }

  @Test
  public void testSetCpuAverage() {
    SystemLoad load = new SystemLoad();
    load.setCpuAverage(80.0);
    Assert.assertEquals(80.0, load.getCpuAverage(), 0.001);
  }

  @Test
  public void testSetLoadAverage() {
    SystemLoad load = new SystemLoad();
    load.setLoadAverage(5.5);
    Assert.assertEquals(5.5, load.getLoadAverage(), 0.001);
  }

  @Test
  public void testStaticEqualsBothNull() {
    Assert.assertTrue(SystemLoad.equals(null, null));
  }

  @Test
  public void testStaticEqualsLeftNull() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertFalse(SystemLoad.equals(null, load));
  }

  @Test
  public void testStaticEqualsRightNull() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertFalse(SystemLoad.equals(load, null));
  }

  @Test
  public void testStaticEqualsSameInstance() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertTrue(SystemLoad.equals(load, load));
  }

  @Test
  public void testStaticEqualsEqualValues() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(50.0, 2.0);
    Assert.assertTrue(SystemLoad.equals(load1, load2));
  }

  @Test
  public void testStaticEqualsDifferentCpuAverage() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(60.0, 2.0);
    Assert.assertFalse(SystemLoad.equals(load1, load2));
  }

  @Test
  public void testStaticEqualsDifferentLoadAverage() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(50.0, 3.0);
    Assert.assertFalse(SystemLoad.equals(load1, load2));
  }

  @Test
  public void testInstanceEquals() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(50.0, 2.0);
    SystemLoad load3 = new SystemLoad(60.0, 2.0);

    Assert.assertEquals(load1, load2);
    Assert.assertNotEquals(load1, load3);
  }

  @Test
  public void testInstanceEqualsSameInstance() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertEquals(load, load);
  }

  @Test
  public void testInstanceEqualsNull() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertNotEquals(load, null);
  }

  @Test
  public void testInstanceEqualsDifferentClass() {
    SystemLoad load = new SystemLoad(50.0, 2.0);
    Assert.assertNotEquals(load, "not a SystemLoad");
  }

  @Test
  public void testHashCodeConsistency() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(50.0, 2.0);
    Assert.assertEquals(load1.hashCode(), load2.hashCode());
  }

  @Test
  public void testHashCodeDifferent() {
    SystemLoad load1 = new SystemLoad(50.0, 2.0);
    SystemLoad load2 = new SystemLoad(60.0, 3.0);
    Assert.assertNotEquals(load1.hashCode(), load2.hashCode());
  }

  @Test
  public void testToString() {
    SystemLoad load = new SystemLoad(75.5, 4.2);
    String str = load.toString();
    Assert.assertNotNull(str);
    Assert.assertTrue(str.contains("SystemLoad"));
  }
}
