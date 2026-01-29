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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.common.model.metaserver.metrics.SystemLoad;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AdaptiveFlowOperationLimiterTest {

  @Test
  public void testNoConfigInDB() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      // Simulate the scenario where no configuration exists in the database
      Mockito.when(provideDataService.queryProvideData(Mockito.anyString()))
          .thenReturn(DBResponse.notfound().build());

      // Default throttling is disabled, so we can generate arbitrary session node data
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 2, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Verify that throttling remains disabled
      // Since the default task interval is 30 seconds, we check for 35 seconds (35 iterations)
      for (int i = 0; i < 35; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        // We only care about the enabled flag here, as disabled config skips all logic
        Assert.assertFalse(status.isEnabled());
        Thread.sleep(1000);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testFlowOperationThrottlingEnabled() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      // Enable the feature
      config.setEnabled(true);
      // Set task execution interval to 300ms for faster testing
      config.setIntervalMs(300);
      // Mark Session nodes with CPU > 60% as overloaded
      config.setCpuAverageThreshold(60);
      // Trigger partial throttling when >20% of nodes are overloaded
      config.setOverloadedNodePercentForPartialThrottle(20);
      // Trigger full throttling when >60% of nodes are overloaded
      config.setOverloadedNodePercentForFullThrottle(60);
      // Base throttling percentage for partial mode
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      // Mock DB to return the above configuration
      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Create 5 Session nodes: 3 healthy, 2 overloaded (40% overload ratio)
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 2, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Wait for the first task execution
      Thread.sleep(500);

      // Verify throttling status
      // Overload ratio = 2/5 = 40% > 20% → throttling should be enabled
      for (int i = 0; i < 5; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        Assert.assertTrue(status.isEnabled());
        // Linear interpolation:
        // progress = (40 - 20) / (60 - 20) = 0.5
        // throttlePercent = 50 + (100 - 50) * 0.5 = 75.0
        Assert.assertEquals(75, status.getThrottlePercent(), 0.001);
        Thread.sleep(300);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testFlowOperationThrottlingEnabledAndPercentIs100() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      // Enable the feature
      config.setEnabled(true);
      // Set task execution interval to 300ms
      config.setIntervalMs(300);
      // Mark Session nodes with CPU > 60% as overloaded
      config.setCpuAverageThreshold(60);
      // Trigger partial throttling when >20% of nodes are overloaded
      config.setOverloadedNodePercentForPartialThrottle(20);
      // Trigger full throttling when >60% of nodes are overloaded
      config.setOverloadedNodePercentForFullThrottle(60);
      // Base throttling percentage
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      // Mock DB to return the above configuration
      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Create 10 Session nodes: 3 healthy, 7 overloaded (70% overload ratio > 60%)
      // Expected: full throttling (100%)
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Wait for the first task execution
      Thread.sleep(500);

      // Verify throttling status is 100%
      for (int i = 0; i < 5; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        Assert.assertTrue(status.isEnabled());
        Assert.assertEquals(100, status.getThrottlePercent(), 0.001);
        Thread.sleep(300);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testFlowOperationThrottlingDisabled() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      // Enable the feature
      config.setEnabled(true);
      // Set task execution interval to 300ms
      config.setIntervalMs(300);
      // Mark Session nodes with CPU > 60% as overloaded
      config.setCpuAverageThreshold(60);
      // Trigger partial throttling when >20% of nodes are overloaded
      config.setOverloadedNodePercentForPartialThrottle(20);
      // Trigger full throttling when >60% of nodes are overloaded
      config.setOverloadedNodePercentForFullThrottle(60);
      // Base throttling percentage
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      // Mock DB to return the above configuration
      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Create 20 Session nodes: 18 healthy, 2 overloaded (10% overload ratio < 20%)
      // Expected: throttling disabled
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(18, 2, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Wait for the first task execution
      Thread.sleep(500);

      // Verify throttling is disabled
      for (int i = 0; i < 5; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        Assert.assertFalse(status.isEnabled());
        Thread.sleep(300);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testEmergencyOverrideDisabled() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      // Enable the feature
      config.setEnabled(true);
      // Set task execution interval to 300ms
      config.setIntervalMs(300);
      // Mark Session nodes with CPU > 60% as overloaded
      config.setCpuAverageThreshold(60);
      // Trigger partial throttling when >20% of nodes are overloaded
      config.setOverloadedNodePercentForPartialThrottle(20);
      // Trigger full throttling when >60% of nodes are overloaded
      config.setOverloadedNodePercentForFullThrottle(60);
      // Base throttling percentage
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      // Mock DB to return the above configuration
      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Create 10 Session nodes: 3 healthy, 7 overloaded (70% > 60%)
      // Normally, this would trigger 100% throttling.
      // However, emergency override is disabled, so throttling must be OFF.
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Disable emergency override (force disable all throttling)
      limiter.setEmergencyOverrideEnabled(false);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Wait for the first task execution
      Thread.sleep(500);

      // Verify throttling is disabled due to emergency override
      for (int i = 0; i < 5; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        Assert.assertFalse(status.isEnabled());
        Thread.sleep(300);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testSomeSessionNodeNotSupportSystemLoad() throws InterruptedException {
    // Test scenario where some Session nodes do not support reporting system load metrics
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      // Enable the feature
      config.setEnabled(true);
      // Set task execution interval to 300ms
      config.setIntervalMs(300);
      // Mark Session nodes with CPU > 60% as overloaded
      config.setCpuAverageThreshold(60);
      // Trigger partial throttling when >20% of nodes are overloaded
      config.setOverloadedNodePercentForPartialThrottle(20);
      // Trigger full throttling when >60% of nodes are overloaded
      config.setOverloadedNodePercentForFullThrottle(60);
      // Base throttling percentage
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      // Mock DB to return the above configuration
      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Total 20 nodes:
      // - 10 healthy (CPU <= 60%)
      // - 2 overloaded (CPU > 60%)
      // - 8 nodes without SystemLoad (treated as healthy)
      // Overload ratio = 2 / 20 = 10% < 20% → throttling should be disabled
      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(10, 2, 8);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      // Create limiter instance
      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      // Start the limiter
      this.safeStartLimiter(limiter);

      // Wait for the first task execution
      Thread.sleep(500);

      // Verify throttling is disabled
      for (int i = 0; i < 5; i++) {
        FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
        Assert.assertNotNull(status);
        Assert.assertFalse(status.isEnabled());
        Thread.sleep(300);
      }
    } finally {
      // Clean up resources
      this.safeStopLimiter(limiter);
    }
  }

  private VersionedList<SessionNode> createSessionNodes(
      int healthCount, int unHealthCount, int unSupportCount) {
    Random random = new Random();
    long timestamp = System.currentTimeMillis();

    List<SessionNode> sessionNodes = new ArrayList<>();
    for (int healthIndex = 0; healthIndex < healthCount; healthIndex++) {
      String ip = String.format("1.2.3.%d", healthIndex);
      double cpuAverage = random.nextDouble() * 100;
      if (cpuAverage > 60) {
        cpuAverage = 59;
      }
      double loadAverage = random.nextDouble() * 10;
      if (loadAverage > 3) {
        loadAverage = 3;
      }
      SessionNode healthSessionNode =
          new SessionNode(
              URL.valueOf(String.format("%s:9600", ip)),
              "RegionId",
              new ProcessId(ip, timestamp, 1, random.nextInt()),
              1,
              new SystemLoad(cpuAverage, loadAverage));
      sessionNodes.add(healthSessionNode);
    }

    for (int unHealthIndex = 0; unHealthIndex < unHealthCount; unHealthIndex++) {
      String ip = String.format("2.2.3.%d", unHealthIndex);
      double cpuAverage = random.nextDouble() * 100;
      if (cpuAverage <= 60) {
        cpuAverage = 61;
      }
      double loadAverage = random.nextDouble() * 10;
      if (loadAverage < 6) {
        loadAverage = 6;
      }
      SessionNode unHealthSessionNode =
          new SessionNode(
              URL.valueOf(String.format("%s:9600", ip)),
              "RegionId",
              new ProcessId(ip, timestamp, 2, random.nextInt()),
              1,
              new SystemLoad(cpuAverage, loadAverage));
      sessionNodes.add(unHealthSessionNode);
    }

    for (int unSupportIndex = 0; unSupportIndex < unSupportCount; unSupportIndex++) {
      String ip = String.format("3.2.3.%d", unSupportIndex);
      SessionNode unSupportSessionNode =
          new SessionNode(
              URL.valueOf(String.format("%s:9600", ip)),
              "RegionId",
              new ProcessId(ip, timestamp, 3, random.nextInt()),
              1);
      sessionNodes.add(unSupportSessionNode);
    }

    return new VersionedList<>(1L, sessionNodes);
  }

  private void safeStartLimiter(AdaptiveFlowOperationLimiter limiter) {
    try {
      if (limiter != null) {
        limiter.postConstruct();
      }
    } catch (Throwable throwable) {
    }
  }

  private void safeStopLimiter(AdaptiveFlowOperationLimiter limiter) {
    try {
      if (limiter != null) {
        limiter.preDestroy();
      }
    } catch (Throwable throwable) {
    }
  }
}
