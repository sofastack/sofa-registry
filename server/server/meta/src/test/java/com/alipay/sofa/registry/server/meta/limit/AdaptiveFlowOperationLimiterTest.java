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

      // Disable emergency throttling (force disable all throttling)
      limiter.setEmergencyThrottlingEnabled(false);

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

  @Test
  public void testIsEmergencyThrottlingEnabled() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = new AdaptiveFlowOperationLimiter();
    // Default should be true
    Assert.assertTrue(limiter.isEmergencyThrottlingEnabled());

    limiter.setEmergencyThrottlingEnabled(false);
    Assert.assertFalse(limiter.isEmergencyThrottlingEnabled());

    limiter.setEmergencyThrottlingEnabled(true);
    Assert.assertTrue(limiter.isEmergencyThrottlingEnabled());
  }

  @Test
  public void testNullSessionVersionedNodes() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Return null for session nodes
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo()).thenReturn(null);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled when session nodes is null
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testEmptySessionNodesList() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Return empty list
      VersionedList<SessionNode> emptyNodes = new VersionedList<>(1L, new ArrayList<>());
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo()).thenReturn(emptyNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled when session nodes list is empty
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidConfigDisablesThrottling() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Create invalid config: partial threshold >= full threshold
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setOverloadedNodePercentForPartialThrottle(80);
      config.setOverloadedNodePercentForFullThrottle(60); // Invalid: partial >= full

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testNotStableLeaderDisablesThrottling() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      // Not stable as leader
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(false);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled when not stable leader
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidCpuAverageThreshold() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Invalid: cpuAverageThreshold > 100
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setCpuAverageThreshold(150); // Invalid

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidIntervalMs() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Invalid: intervalMs <= 0
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(-1); // Invalid

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidBaseThrottlePercent() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Invalid: baseThrottlePercent > 100
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setBaseThrottlePercent(150); // Invalid

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidPartialThrottlePercent() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Invalid: overloadedNodePercentForPartialThrottle > 100
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setOverloadedNodePercentForPartialThrottle(150); // Invalid

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testInvalidFullThrottlePercent() throws InterruptedException {
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Invalid: overloadedNodePercentForFullThrottle > 100
      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setOverloadedNodePercentForFullThrottle(150); // Invalid

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 7, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Throttling should be disabled due to invalid config
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testSessionNodeWithAbnormalCpuAverage() throws InterruptedException {
    // Test case where session node reports CPU > 100 or < 0
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      AdaptiveFlowOperationConfig config = new AdaptiveFlowOperationConfig();
      config.setEnabled(true);
      config.setIntervalMs(300);
      config.setCpuAverageThreshold(60);
      config.setOverloadedNodePercentForPartialThrottle(20);
      config.setOverloadedNodePercentForFullThrottle(60);
      config.setBaseThrottlePercent(50);

      String configJson = JsonUtils.writeValueAsString(config);
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, configJson);
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      // Create session nodes with abnormal CPU values (> 100 and < 0)
      Random random = new Random();
      long timestamp = System.currentTimeMillis();
      List<SessionNode> sessionNodes = new ArrayList<>();

      // Add node with CPU > 100 (should be treated as not overloaded)
      sessionNodes.add(
          new SessionNode(
              URL.valueOf("1.2.3.1:9600"),
              "RegionId",
              new ProcessId("1.2.3.1", timestamp, 1, random.nextInt()),
              1,
              new SystemLoad(150.0, 2.0))); // Invalid CPU

      // Add node with CPU < 0 (should be treated as not overloaded)
      sessionNodes.add(
          new SessionNode(
              URL.valueOf("1.2.3.2:9600"),
              "RegionId",
              new ProcessId("1.2.3.2", timestamp, 1, random.nextInt()),
              1,
              new SystemLoad(-10.0, 2.0))); // Invalid CPU

      // Add 3 normal healthy nodes
      for (int i = 3; i <= 5; i++) {
        sessionNodes.add(
            new SessionNode(
                URL.valueOf("1.2.3." + i + ":9600"),
                "RegionId",
                new ProcessId("1.2.3." + i, timestamp, 1, random.nextInt()),
                1,
                new SystemLoad(50.0, 2.0)));
      }

      VersionedList<SessionNode> sessionVersionedNodes = new VersionedList<>(1L, sessionNodes);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // All 5 nodes are healthy (abnormal CPU values treated as not overloaded)
      // 0% overloaded < 20% threshold -> disabled
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testSafeProcessCatchBlock() throws InterruptedException {
    // Cover L281-282: safeProcess() catch block by making provideDataService throw after leader
    // check
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      // Throw exception when querying config to trigger the catch block in safeProcess
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenThrow(new RuntimeException("Simulated DB failure"));

      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);

      // Wait for at least one safeProcess cycle to execute and hit the catch block
      Thread.sleep(500);

      // Limiter should remain in disabled state after the exception
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertNotNull(status);
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }

  @Test
  public void testCheckConfigWithNullConfig() throws InterruptedException {
    // Cover L287-289: checkConfig(null) by returning JSON "null" from DB
    AdaptiveFlowOperationLimiter limiter = null;
    try {
      MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
      Mockito.when(metaLeaderService.amIStableAsLeader()).thenReturn(true);

      // Create a PersistenceData with "null" JSON to make gsonRead return null
      PersistenceData persistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID, "null");
      DBResponse<PersistenceData> dbResponse = DBResponse.ok().entity(persistenceData).build();

      ProvideDataService provideDataService = Mockito.mock(ProvideDataService.class);
      Mockito.when(
              provideDataService.queryProvideData(
                  ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID))
          .thenReturn(dbResponse);

      VersionedList<SessionNode> sessionVersionedNodes = this.createSessionNodes(3, 2, 0);
      SessionServerManager sessionServerManager = Mockito.mock(SessionServerManager.class);
      Mockito.when(sessionServerManager.getSessionServerMetaInfo())
          .thenReturn(sessionVersionedNodes);

      limiter = new AdaptiveFlowOperationLimiter();
      limiter.setMetaLeaderService(metaLeaderService);
      limiter.setProvideDataService(provideDataService);
      limiter.setSessionServerManager(sessionServerManager);

      this.safeStartLimiter(limiter);
      Thread.sleep(500);

      // Config is null, so checkConfig returns false, throttling should be disabled
      FlowOperationThrottlingStatus status = limiter.getFlowOperationThrottlingStatus();
      Assert.assertNotNull(status);
      Assert.assertFalse(status.isEnabled());
    } finally {
      this.safeStopLimiter(limiter);
    }
  }
}
