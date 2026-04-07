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
package com.alipay.sofa.registry.server.session.limit;

import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import org.junit.Assert;
import org.junit.Test;

public class FlowOperationThrottlingObserverTest {

  @Test
  public void testDefaultState() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Default state should be disabled for both local and cluster
    Assert.assertFalse(observer.getLocalThrottlingStatus().isEnabled());
    Assert.assertFalse(observer.getClusterThrottlingStatus().isEnabled());

    // Should not throttle when both are disabled
    Assert.assertFalse(observer.shouldThrottle());
  }

  @Test
  public void testLocalThrottlingHasPriority() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Enable local throttling
    observer.updateLocalThrottlingStatus(FlowOperationThrottlingStatus.enabled(100.0));

    // Local throttling should always return true regardless of cluster status
    Assert.assertTrue(observer.shouldThrottle());

    // Even if cluster throttling is disabled, local takes priority
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.disabled());
    Assert.assertTrue(observer.shouldThrottle());
  }

  @Test
  public void testClusterThrottlingFullThrottle() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Cluster throttling at 100% should always throttle
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(100.0));

    // Run multiple times to verify consistency
    for (int i = 0; i < 100; i++) {
      Assert.assertTrue(observer.shouldThrottle());
    }
  }

  @Test
  public void testClusterThrottlingNoThrottle() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Cluster throttling at 0% should never throttle
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(0.0));

    // Run multiple times to verify consistency
    for (int i = 0; i < 100; i++) {
      Assert.assertFalse(observer.shouldThrottle());
    }
  }

  @Test
  public void testClusterThrottlingProbabilistic() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Cluster throttling at 50% should throttle roughly half the time
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(50.0));

    int throttledCount = 0;
    int totalRuns = 10000;

    for (int i = 0; i < totalRuns; i++) {
      if (observer.shouldThrottle()) {
        throttledCount++;
      }
    }

    // Allow 10% margin for randomness
    double throttleRate = (double) throttledCount / totalRuns;
    Assert.assertTrue(
        "Throttle rate should be around 50%, but was " + (throttleRate * 100) + "%",
        throttleRate > 0.40 && throttleRate < 0.60);
  }

  @Test
  public void testUpdateWithNullIsIgnored() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Set initial state
    observer.updateLocalThrottlingStatus(FlowOperationThrottlingStatus.enabled(100.0));
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(50.0));

    // Update with null should be ignored
    observer.updateLocalThrottlingStatus(null);
    observer.updateClusterThrottlingStatus(null);

    // State should remain unchanged
    Assert.assertTrue(observer.getLocalThrottlingStatus().isEnabled());
    Assert.assertEquals(100.0, observer.getLocalThrottlingStatus().getThrottlePercent(), 0.01);
    Assert.assertTrue(observer.getClusterThrottlingStatus().isEnabled());
    Assert.assertEquals(50.0, observer.getClusterThrottlingStatus().getThrottlePercent(), 0.01);
  }

  @Test
  public void testDisabledClusterThrottling() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Even with high throttle percent, if disabled, should not throttle
    FlowOperationThrottlingStatus status = new FlowOperationThrottlingStatus(false, 100.0);
    observer.updateClusterThrottlingStatus(status);

    for (int i = 0; i < 100; i++) {
      Assert.assertFalse(observer.shouldThrottle());
    }
  }

  @Test
  public void testLocalDisabledClusterEnabled() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();

    // Local disabled, cluster enabled at 100%
    observer.updateLocalThrottlingStatus(FlowOperationThrottlingStatus.disabled());
    observer.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(100.0));

    // Should fall back to cluster throttling
    Assert.assertTrue(observer.shouldThrottle());
  }
}
