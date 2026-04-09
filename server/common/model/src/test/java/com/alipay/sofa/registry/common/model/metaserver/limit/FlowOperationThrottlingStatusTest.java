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
package com.alipay.sofa.registry.common.model.metaserver.limit;

import org.junit.Assert;
import org.junit.Test;

public class FlowOperationThrottlingStatusTest {

  @Test
  public void testDefaultConstructor() {
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus();
    Assert.assertFalse(status.isEnabled());
    Assert.assertEquals(0.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testConstructorWithParams() {
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus(true, 50.0);
    Assert.assertTrue(status.isEnabled());
    Assert.assertEquals(50.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testDisabledFactory() {
    FlowOperationThrottlingStatus status = FlowOperationThrottlingStatus.disabled();
    Assert.assertFalse(status.isEnabled());
    Assert.assertEquals(0.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testEnabledFactory() {
    FlowOperationThrottlingStatus status = FlowOperationThrottlingStatus.enabled(75.0);
    Assert.assertTrue(status.isEnabled());
    Assert.assertEquals(75.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testEnabledFactoryWithZeroPercent() {
    FlowOperationThrottlingStatus status = FlowOperationThrottlingStatus.enabled(0.0);
    Assert.assertTrue(status.isEnabled());
    Assert.assertEquals(0.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testEnabledFactoryWithFullPercent() {
    FlowOperationThrottlingStatus status = FlowOperationThrottlingStatus.enabled(100.0);
    Assert.assertTrue(status.isEnabled());
    Assert.assertEquals(100.0, status.getThrottlePercent(), 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnabledFactoryWithNegativePercent() {
    FlowOperationThrottlingStatus.enabled(-1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnabledFactoryWithOverHundredPercent() {
    FlowOperationThrottlingStatus.enabled(100.1);
  }

  @Test
  public void testSetEnabled() {
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus();
    Assert.assertFalse(status.isEnabled());
    status.setEnabled(true);
    Assert.assertTrue(status.isEnabled());
    status.setEnabled(false);
    Assert.assertFalse(status.isEnabled());
  }

  @Test
  public void testSetThrottlePercent() {
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus();
    Assert.assertEquals(0.0, status.getThrottlePercent(), 0.001);
    status.setThrottlePercent(50.0);
    Assert.assertEquals(50.0, status.getThrottlePercent(), 0.001);
    status.setThrottlePercent(100.0);
    Assert.assertEquals(100.0, status.getThrottlePercent(), 0.001);
  }

  @Test
  public void testCopy() {
    FlowOperationThrottlingStatus original = new FlowOperationThrottlingStatus(true, 75.0);
    FlowOperationThrottlingStatus copy = original.copy();

    Assert.assertNotSame(original, copy);
    Assert.assertEquals(original.isEnabled(), copy.isEnabled());
    Assert.assertEquals(original.getThrottlePercent(), copy.getThrottlePercent(), 0.001);

    // Verify copy is independent
    copy.setEnabled(false);
    copy.setThrottlePercent(25.0);
    Assert.assertTrue(original.isEnabled());
    Assert.assertEquals(75.0, original.getThrottlePercent(), 0.001);
  }

  @Test
  public void testToString() {
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus(true, 50.0);
    String str = status.toString();
    Assert.assertNotNull(str);
    Assert.assertTrue(str.contains("FlowOperationThrottlingStatus"));
  }
}
