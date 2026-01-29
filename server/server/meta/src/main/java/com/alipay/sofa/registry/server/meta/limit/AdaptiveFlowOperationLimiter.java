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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.common.model.metaserver.metrics.SystemLoad;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adaptive flow operation limiter that dynamically adjusts throttling based on cluster-wide Session
 * node overload status.
 *
 * <p>The limiter:
 *
 * <ul>
 *   <li>Runs periodically (configured via {@link AdaptiveFlowOperationConfig#getIntervalMs()})
 *   <li>Only active when this node is stable leader
 *   <li>Calculates the percentage of overloaded Session nodes (CPU > threshold)
 *   <li>Applies linear throttling from {@code baseThrottlePercent} to 100% as overload ratio
 *       increases
 * </ul>
 */
@Component
public class AdaptiveFlowOperationLimiter extends AbstractLifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdaptiveFlowOperationLimiter.class);

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private SessionServerManager sessionServerManager;

  @Autowired private ProvideDataService provideDataService;

  private AtomicBoolean start;

  private WatchDog watchDog;

  /**
   * Emergency override switch, primarily used to forcibly disable cluster-wide throttling via Admin
   * API when the database is unavailable.
   */
  private AtomicBoolean emergencyOverrideEnabled;

  /**
   * Current throttling status, updated periodically by the watchdog thread. Initialized to disabled
   * state.
   */
  private final AtomicReference<FlowOperationThrottlingStatus> flowOperationThrottlingStatus;

  /** Latest valid configuration loaded from DB. */
  private volatile AdaptiveFlowOperationConfig config;

  public AdaptiveFlowOperationLimiter() {
    this.start = new AtomicBoolean(false);
    this.watchDog = new WatchDog();
    // Set to true to enable throttling by default
    this.emergencyOverrideEnabled = new AtomicBoolean(true);
    this.flowOperationThrottlingStatus =
        new AtomicReference<>(FlowOperationThrottlingStatus.disabled());
    this.config = new AdaptiveFlowOperationConfig();
  }

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
    LifecycleHelper.startIfPossible(this);
  }

  @PreDestroy
  public void preDestroy() throws Exception {
    LifecycleHelper.stopIfPossible(this);
    LifecycleHelper.disposeIfPossible(this);
  }

  @Override
  protected void doStart() throws StartException {
    if (this.start.compareAndSet(false, true)) {
      ConcurrentUtils.createDaemonThread("AdaptiveFlowOperationLimiterThread", this.watchDog)
          .start();
    }
  }

  @Override
  protected void doStop() throws StopException {
    if (this.start.compareAndSet(true, false)) {
      this.watchDog.close();
    }
  }

  private void safeProcess() {
    try {
      if (!emergencyOverrideEnabled.get()) {
        // The emergency override is disabled, meaning the Admin API has forcibly turned off
        // throttling.
        // Immediately set the status to disabled.
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // 1. Check the current node's role
      // We use amIStableAsLeader() to ensure that during leader transition,
      // we wait until most Session nodes have reconnected before deciding whether to enable
      // throttling.
      if (!metaLeaderService.amIStableAsLeader()) {
        // If this node is not the stable leader, skip throttling decision.
        // However, we explicitly disable throttling here to prevent stale throttling state from
        // persisting.
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // 2. Load configuration from the database.
      // Note: This involves JSON parsing and should not be performed too frequently.
      AdaptiveFlowOperationConfig config = this.queryOrCreateDefaultConfig();

      // Validate the loaded configuration
      if (!checkConfig(config)) {
        // If the configuration is invalid, fall back to disabling throttling.
        LOGGER.warn("[AdaptiveFlowOperationLimiter] Config is invalid, disabling throttling");
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // Cache the valid configuration for subsequent use (e.g., in isNodeOverloaded)
      this.config = config;

      // 3. Check whether the adaptive throttling feature is enabled in the configuration
      if (!config.isEnabled()) {
        // If the feature switch is off, explicitly disable throttling.
        // Since this is a single-threaded update, no CAS is needed.
        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] Flow operation throttling is disabled by config, turning off.");
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // 4. Retrieve the current load status of Session nodes.
      // Note: This also involves database access and JSON parsing, so frequency should be
      // controlled.
      VersionedList<SessionNode> sessionVersionedNodes =
          this.sessionServerManager.getSessionServerMetaInfo();
      if (null == sessionVersionedNodes) {
        // If no Session node metadata is available, disable throttling.
        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] No session nodes found (versioned list is null), disabling throttling.");
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // The retrieved list contains only healthy Session nodes (blacklisted nodes are already
      // excluded).
      List<SessionNode> sessionNodes = sessionVersionedNodes.getClusterMembers();
      if (CollectionUtils.isEmpty(sessionNodes)) {
        // If the Session node list is empty, disable throttling.
        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] Session node list is empty (size=0), disabling throttling.");
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
        return;
      }

      // 5. Determine whether to enable throttling based on the proportion of overloaded nodes.

      // First, get the total number of Session nodes.
      int totalSessionNum = sessionNodes.size();

      // Then, count the number of overloaded Session nodes.
      int overloadedSessionNum = 0;
      for (SessionNode sessionNode : sessionNodes) {
        SystemLoad systemLoad = sessionNode.getSystemLoad();
        if (null == systemLoad) {
          // If SystemLoad is null, it means this Session node does not support load reporting,
          // and its status cannot be determined. We conservatively treat it as a healthy node.
          continue;
        }
        if (this.isNodeOverloaded(systemLoad)) {
          // The node is overloaded; increment the counter.
          overloadedSessionNum++;
        }
      }

      // Calculate the percentage of overloaded nodes (range: 0.0 ~ 100.0)
      double overloadedPercent = (double) overloadedSessionNum * 100 / totalSessionNum;

      // Retrieve throttling thresholds from the configuration (values are percentages, e.g., 50.0
      // means 50%)
      double partialThrottleThreshold = config.getOverloadedNodePercentForPartialThrottle();
      double fullThrottleThreshold = config.getOverloadedNodePercentForFullThrottle();

      if (overloadedPercent >= fullThrottleThreshold) {
        // The proportion of overloaded nodes has reached the full throttling threshold; enable 100%
        // throttling.
        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] FULL throttling ENABLED (100%): {}/{} Session nodes overloaded, overload percent={}, [partial threshold={}, full threshold={}]",
            overloadedSessionNum,
            totalSessionNum,
            overloadedPercent,
            partialThrottleThreshold,
            fullThrottleThreshold);
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.enabled(100.0));
      } else if (overloadedPercent >= partialThrottleThreshold) {
        // The proportion of overloaded nodes is within the partial throttling range;
        // calculate the throttling percentage using linear interpolation.
        double baseThrottlePercent = config.getBaseThrottlePercent();
        double progress =
            (overloadedPercent - partialThrottleThreshold)
                / (fullThrottleThreshold - partialThrottleThreshold);
        double throttlePercent = baseThrottlePercent + (100.0 - baseThrottlePercent) * progress;

        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] PARTIAL throttling ENABLED: {}/{} Session nodes overloaded, overload percent={}, calculated throttle percent={}, [base={}, partial threshold={}, full threshold={}]",
            overloadedSessionNum,
            totalSessionNum,
            overloadedPercent,
            throttlePercent,
            baseThrottlePercent,
            partialThrottleThreshold,
            fullThrottleThreshold);

        this.flowOperationThrottlingStatus.set(
            FlowOperationThrottlingStatus.enabled(throttlePercent));
      } else {
        // The proportion of overloaded nodes is below the threshold; disable throttling.
        LOGGER.info(
            "[AdaptiveFlowOperationLimiter] Throttling DISABLED: {}/{} Session nodes overloaded, overload percent={}, [partial threshold={}, full threshold={}]",
            overloadedSessionNum,
            totalSessionNum,
            overloadedPercent,
            partialThrottleThreshold,
            fullThrottleThreshold);
        this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
      }
    } catch (Throwable throwable) {
      LOGGER.error("[AdaptiveFlowOperationLimiter] Process exception", throwable);
    }
  }

  private boolean checkConfig(AdaptiveFlowOperationConfig config) {
    if (config == null) {
      LOGGER.error("[AdaptiveFlowOperationLimiter] Config is null, using default settings");
      return false;
    }

    int intervalMs = config.getIntervalMs();
    if (intervalMs <= 0) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid intervalMs: {}, must be > 0", intervalMs);
      return false;
    }

    double cpuAverageThreshold = config.getCpuAverageThreshold();
    if (cpuAverageThreshold < 0 || cpuAverageThreshold > 100) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid cpuAverageThreshold: {}, must be in [0.0, 100.0]",
          cpuAverageThreshold);
      return false;
    }

    double baseThrottlePercent = config.getBaseThrottlePercent();
    if (baseThrottlePercent < 0 || baseThrottlePercent > 100) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid baseThrottlePercent: {}, must be in [0.0, 100.0]",
          baseThrottlePercent);
      return false;
    }

    double partial = config.getOverloadedNodePercentForPartialThrottle();
    if (partial < 0 || partial > 100) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid overloadedNodePercentForPartialThrottle: {}, must be in [0.0, 100.0]",
          partial);
      return false;
    }

    double full = config.getOverloadedNodePercentForFullThrottle();
    if (full < 0 || full > 100) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid overloadedNodePercentForFullThrottle: {}, must be in [0.0, 100.0]",
          full);
      return false;
    }

    if (partial >= full) {
      LOGGER.error(
          "[AdaptiveFlowOperationLimiter] Invalid thresholds: partial ({}) must be less than full ({})",
          partial,
          full);
      return false;
    }

    return true;
  }

  private AdaptiveFlowOperationConfig queryOrCreateDefaultConfig() {
    DBResponse<PersistenceData> queryConfigResp =
        this.provideDataService.queryProvideData(
            ValueConstants.ADAPTIVE_FLOW_OPERATION_CONFIG_DATA_ID);
    if (Objects.isNull(queryConfigResp)
        || OperationStatus.NOTFOUND == queryConfigResp.getOperationStatus()) {
      // If no configuration is found in the database, use the default configuration (which disables
      // throttling by default).
      return new AdaptiveFlowOperationConfig();
    } else {
      // Configuration found; parse it from JSON.
      // Exceptions during parsing will be thrown directly (not swallowed).
      PersistenceData persistenceData = queryConfigResp.getEntity();
      String configData = PersistenceDataBuilder.getEntityData(persistenceData);
      return JsonUtils.gsonRead(configData, AdaptiveFlowOperationConfig.class);
    }
  }

  private boolean isNodeOverloaded(SystemLoad systemLoad) {
    double cpuAverage = systemLoad.getCpuAverage();
    if (cpuAverage < 0 || cpuAverage > 100) {
      // The CPU metric reported by the Session node is abnormal; treat it as not overloaded.
      return false;
    }
    double cpuAverageThreshold = this.config.getCpuAverageThreshold();
    return cpuAverage > cpuAverageThreshold;
  }

  public void setEmergencyOverrideEnabled(boolean emergencyOverrideEnabled) {
    this.emergencyOverrideEnabled.set(emergencyOverrideEnabled);
    if (!emergencyOverrideEnabled) {
      // This method is called by the Admin API in emergency scenarios.
      // Forcefully override the throttling status immediately to prevent delays caused by slow DB
      // queries.
      // Even if the background thread overwrites this value later, the next cycle (typically
      // seconds)
      // will re-disable throttling due to the emergency flag being false.
      // We keep the logic simple without complex synchronization.
      this.flowOperationThrottlingStatus.set(FlowOperationThrottlingStatus.disabled());
    }
  }

  public boolean isEmergencyOverrideEnabled() {
    return this.emergencyOverrideEnabled.get();
  }

  /**
   * Returns the current throttling status.
   *
   * @return current {@link FlowOperationThrottlingStatus}, never null
   */
  public FlowOperationThrottlingStatus getFlowOperationThrottlingStatus() {
    return this.flowOperationThrottlingStatus.get();
  }

  protected final class WatchDog extends WakeUpLoopRunnable {

    @Override
    public int getWaitingMillis() {
      return AdaptiveFlowOperationLimiter.this.config.getIntervalMs();
    }

    @Override
    public void runUnthrowable() {
      AdaptiveFlowOperationLimiter.this.safeProcess();
    }
  }

  @VisibleForTesting
  public void setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @VisibleForTesting
  public void setSessionServerManager(SessionServerManager sessionServerManager) {
    this.sessionServerManager = sessionServerManager;
  }

  @VisibleForTesting
  public void setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
  }
}
