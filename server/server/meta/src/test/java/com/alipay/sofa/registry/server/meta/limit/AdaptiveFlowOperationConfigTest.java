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
package com.alipay.sofa.registry.server.meta.limit;

import org.junit.Assert;
import org.junit.Test;

public class AdaptiveFlowOperationConfigTest {

  @Test
  public void testDefaultValues() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();

    // Verify default values
    Assert.assertFalse(config.isEnabled());
    Assert.assertEquals(30_000, config.getIntervalMs());
    Assert.assertEquals(80.0, config.getCpuAverageThreshold(), 0.001);
    Assert.assertEquals(50.0, config.getOverloadedNodePercentForPartialThrottle(), 0.001);
    Assert.assertEquals(80.0, config.getOverloadedNodePercentForFullThrottle(), 0.001);
    Assert.assertEquals(50.0, config.getBaseThrottlePercent(), 0.001);
  }

  @Test
  public void testSetEnabled() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertFalse(config.isEnabled());

    config.setEnabled(true);
    Assert.assertTrue(config.isEnabled());

    config.setEnabled(false);
    Assert.assertFalse(config.isEnabled());
  }

  @Test
  public void testSetIntervalMs() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertEquals(30_000, config.getIntervalMs());

    config.setIntervalMs(1000);
    Assert.assertEquals(1000, config.getIntervalMs());

    config.setIntervalMs(60_000);
    Assert.assertEquals(60_000, config.getIntervalMs());
  }

  @Test
  public void testSetCpuAverageThreshold() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertEquals(80.0, config.getCpuAverageThreshold(), 0.001);

    config.setCpuAverageThreshold(70.0);
    Assert.assertEquals(70.0, config.getCpuAverageThreshold(), 0.001);

    config.setCpuAverageThreshold(90.0);
    Assert.assertEquals(90.0, config.getCpuAverageThreshold(), 0.001);
  }

  @Test
  public void testSetOverloadedNodePercentForPartialThrottle() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertEquals(50.0, config.getOverloadedNodePercentForPartialThrottle(), 0.001);

    config.setOverloadedNodePercentForPartialThrottle(30.0);
    Assert.assertEquals(30.0, config.getOverloadedNodePercentForPartialThrottle(), 0.001);

    config.setOverloadedNodePercentForPartialThrottle(60.0);
    Assert.assertEquals(60.0, config.getOverloadedNodePercentForPartialThrottle(), 0.001);
  }

  @Test
  public void testSetOverloadedNodePercentForFullThrottle() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertEquals(80.0, config.getOverloadedNodePercentForFullThrottle(), 0.001);

    config.setOverloadedNodePercentForFullThrottle(70.0);
    Assert.assertEquals(70.0, config.getOverloadedNodePercentForFullThrottle(), 0.001);

    config.setOverloadedNodePercentForFullThrottle(100.0);
    Assert.assertEquals(100.0, config.getOverloadedNodePercentForFullThrottle(), 0.001);
  }

  @Test
  public void testSetBaseThrottlePercent() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
    Assert.assertEquals(50.0, config.getBaseThrottlePercent(), 0.001);

    config.setBaseThrottlePercent(25.0);
    Assert.assertEquals(25.0, config.getBaseThrottlePercent(), 0.001);

    config.setBaseThrottlePercent(75.0);
    Assert.assertEquals(75.0, config.getBaseThrottlePercent(), 0.001);
  }

  @Test
  public void testCustomConfiguration() {
    AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();

    config.setEnabled(true);
    config.setIntervalMs(5000);
    config.setCpuAverageThreshold(60.0);
    config.setOverloadedNodePercentForPartialThrottle(20.0);
    config.setOverloadedNodePercentForFullThrottle(60.0);
    config.setBaseThrottlePercent(40.0);

    Assert.assertTrue(config.isEnabled());
    Assert.assertEquals(5000, config.getIntervalMs());
    Assert.assertEquals(60.0, config.getCpuAverageThreshold(), 0.001);
    Assert.assertEquals(20.0, config.getOverloadedNodePercentForPartialThrottle(), 0.001);
    Assert.assertEquals(60.0, config.getOverloadedNodePercentForFullThrottle(), 0.001);
    Assert.assertEquals(40.0, config.getBaseThrottlePercent(), 0.001);
  }
}
