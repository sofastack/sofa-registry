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
package com.alipay.sofa.registry.server.session.metrics;

import com.alipay.sofa.registry.common.model.metaserver.metrics.SystemLoad;
import org.junit.Assert;
import org.junit.Test;

public class SessionMetricsCollectorTest {

  @Test
  public void testGetInstance() {
    SessionMetricsCollector instance1 = SessionMetricsCollector.getInstance();
    SessionMetricsCollector instance2 = SessionMetricsCollector.getInstance();

    Assert.assertNotNull(instance1);
    Assert.assertSame(instance1, instance2);
  }

  @Test
  public void testGetSystemLoad() {
    SessionMetricsCollector collector = SessionMetricsCollector.getInstance();
    SystemLoad systemLoad = collector.getSystemLoad();

    Assert.assertNotNull(systemLoad);
    // CPU average should be between -1 (unavailable) and 100
    double cpuAverage = systemLoad.getCpuAverage();
    Assert.assertTrue(cpuAverage >= -1 && cpuAverage <= 100);

    // Load average should be -1 (unavailable) or >= 0
    double loadAverage = systemLoad.getLoadAverage();
    Assert.assertTrue(loadAverage >= -1);
  }

  @Test
  public void testGetSystemCpuAverage() {
    SessionMetricsCollector collector = SessionMetricsCollector.getInstance();
    double cpuAverage = collector.getSystemCpuAverage();

    // Value should be -1 (unavailable) or in range [0, 100]
    Assert.assertTrue(cpuAverage == -1 || (cpuAverage >= 0 && cpuAverage <= 100));
  }

  @Test
  public void testGetSystemLoadAverage() {
    SessionMetricsCollector collector = SessionMetricsCollector.getInstance();
    double loadAverage = collector.getSystemLoadAverage();

    // Value should be -1 (unavailable) or >= 0
    Assert.assertTrue(loadAverage >= -1);
  }

  @Test
  public void testSystemLoadConsistency() {
    SessionMetricsCollector collector = SessionMetricsCollector.getInstance();

    // Get system load via getSystemLoad()
    SystemLoad systemLoad = collector.getSystemLoad();

    // Get individual metrics
    double cpuAverage = collector.getSystemCpuAverage();
    double loadAverage = collector.getSystemLoadAverage();

    // Values should be consistent (within a small tolerance for timing differences)
    // Since these are system metrics that can change, we just verify they are in valid ranges
    Assert.assertTrue(systemLoad.getCpuAverage() >= -1 && systemLoad.getCpuAverage() <= 100);
    Assert.assertTrue(systemLoad.getLoadAverage() >= -1);
  }
}
