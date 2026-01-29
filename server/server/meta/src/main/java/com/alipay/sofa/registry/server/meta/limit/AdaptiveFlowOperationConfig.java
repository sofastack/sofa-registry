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

/**
 * Configuration for adaptive flow control based on cluster-wide node overload status.
 *
 * <p>This configuration enables dynamic throttling of traffic when a certain percentage of nodes in
 * the cluster are overloaded (defined as CPU usage exceeding {@code cpuAverageThreshold}). The
 * throttling percentage increases linearly as the proportion of overloaded nodes grows:
 *
 * <ul>
 *   <li>If overloaded node percentage &lt; {@link #overloadedNodePercentForPartialThrottle}, no
 *       throttling is applied.
 *   <li>If overloaded node percentage is between {@link #overloadedNodePercentForPartialThrottle}
 *       and {@link #overloadedNodePercentForFullThrottle}, throttling percentage increases linearly
 *       from {@link #baseThrottlePercent} to 100.0.
 *   <li>If overloaded node percentage &ge; {@link #overloadedNodePercentForFullThrottle}, full
 *       throttling (100%) is applied.
 * </ul>
 *
 * <p>All percentage values in this configuration are represented as numbers in the range [0.0,
 * 100.0], where 50.0 means 50%, and 100.0 means 100%.
 */
public class AdaptiveFlowOperationConfig {

  /** Default value for whether adaptive flow control is enabled. */
  private static final boolean DEFAULT_ENABLED = false;

  /** Default interval (in milliseconds) between successive overload checks. */
  private static final int DEFAULT_INTERVAL_MS = 30_000;

  /**
   * Default CPU usage threshold as a percentage (0.0 ~ 100.0) above which a node is considered
   * overloaded. For example, 80.0 means a node is overloaded when its CPU usage exceeds 80%.
   */
  private static final double DEFAULT_CPU_AVERAGE_THRESHOLD = 80.0;

  /**
   * Default threshold of overloaded node percentage that triggers the start of partial throttling.
   * When the percentage of overloaded nodes reaches this value, throttling begins at {@link
   * #DEFAULT_BASE_THROTTLE_PERCENT}. For example, 50.0 means partial throttling starts when 50% of
   * nodes are overloaded.
   */
  private static final double DEFAULT_PARTIAL_THROTTLE_OVERLOADED_NODE_PERCENT = 50.0;

  /**
   * Default threshold of overloaded node percentage that triggers full throttling (100%). When the
   * percentage of overloaded nodes reaches or exceeds this value, all non-critical traffic is
   * blocked. For example, 80.0 means full throttling is applied when 80% or more nodes are
   * overloaded.
   */
  private static final double DEFAULT_FULL_THROTTLE_OVERLOADED_NODE_PERCENT = 80.0;

  /**
   * Default base throttling percentage applied when the overloaded node percentage just reaches
   * {@link #DEFAULT_PARTIAL_THROTTLE_OVERLOADED_NODE_PERCENT}. The actual throttling percentage
   * increases linearly from this value to 100.0 as the overloaded node percentage increases toward
   * {@link #DEFAULT_FULL_THROTTLE_OVERLOADED_NODE_PERCENT}. For example, 50.0 means 50% of traffic
   * is throttled initially.
   */
  private static final double DEFAULT_BASE_THROTTLE_PERCENT = 50.0;

  /** Whether adaptive flow control is enabled. */
  private boolean enabled;

  /** Interval in milliseconds between periodic checks of cluster node overload status. */
  private int intervalMs;

  /**
   * CPU usage threshold as a percentage (0.0 ~ 100.0). A node is considered overloaded if its CPU
   * usage exceeds this value. For example, 80.0 means 80% CPU usage.
   */
  private double cpuAverageThreshold;

  /**
   * The percentage of overloaded nodes (0.0 ~ 100.0) at which partial throttling begins. Must be
   * less than or equal to {@link #overloadedNodePercentForFullThrottle}.
   */
  private double overloadedNodePercentForPartialThrottle;

  /**
   * The percentage of overloaded nodes (0.0 ~ 100.0) at which full throttling (100%) is enforced.
   * Must be greater than or equal to {@link #overloadedNodePercentForPartialThrottle}.
   */
  private double overloadedNodePercentForFullThrottle;

  /**
   * The base throttling percentage (0.0 ~ 100.0) applied when the overloaded node percentage equals
   * {@link #overloadedNodePercentForPartialThrottle}. As the overloaded node percentage increases
   * toward {@link #overloadedNodePercentForFullThrottle}, the throttling percentage increases
   * linearly from this value to 100.0.
   */
  private double baseThrottlePercent;

  /** Constructs a new {@code AdaptiveFlowOperationConfig} with default values. */
  public AdaptiveFlowOperationConfig() {
    this.enabled = DEFAULT_ENABLED;
    this.intervalMs = DEFAULT_INTERVAL_MS;
    this.cpuAverageThreshold = DEFAULT_CPU_AVERAGE_THRESHOLD;
    this.overloadedNodePercentForPartialThrottle = DEFAULT_PARTIAL_THROTTLE_OVERLOADED_NODE_PERCENT;
    this.overloadedNodePercentForFullThrottle = DEFAULT_FULL_THROTTLE_OVERLOADED_NODE_PERCENT;
    this.baseThrottlePercent = DEFAULT_BASE_THROTTLE_PERCENT;
  }

  /**
   * Returns whether adaptive flow control is enabled.
   *
   * @return {@code true} if enabled, {@code false} otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Enables or disables adaptive flow control.
   *
   * @param enabled {@code true} to enable, {@code false} to disable
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the interval (in milliseconds) between overload status checks.
   *
   * @return check interval in milliseconds
   */
  public int getIntervalMs() {
    return intervalMs;
  }

  /**
   * Sets the interval (in milliseconds) between overload status checks.
   *
   * @param intervalMs the check interval in milliseconds
   */
  public void setIntervalMs(int intervalMs) {
    this.intervalMs = intervalMs;
  }

  /**
   * Returns the CPU usage threshold as a percentage (0.0 ~ 100.0) above which a node is considered
   * overloaded. For example, 80.0 means 80% CPU usage.
   *
   * @return CPU overload threshold (percentage)
   */
  public double getCpuAverageThreshold() {
    return cpuAverageThreshold;
  }

  /**
   * Sets the CPU usage threshold for determining node overload. The value should be a percentage
   * between 0.0 and 100.0.
   *
   * @param cpuAverageThreshold CPU overload threshold as a percentage (e.g., 80.0 for 80%)
   */
  public void setCpuAverageThreshold(double cpuAverageThreshold) {
    this.cpuAverageThreshold = cpuAverageThreshold;
  }

  /**
   * Returns the overloaded node percentage threshold at which partial throttling begins. The value
   * is a percentage between 0.0 and 100.0.
   *
   * @return partial throttling threshold (percentage)
   */
  public double getOverloadedNodePercentForPartialThrottle() {
    return overloadedNodePercentForPartialThrottle;
  }

  /**
   * Sets the overloaded node percentage threshold for starting partial throttling. The value should
   * be between 0.0 and 100.0, and less than or equal to the full throttling threshold.
   *
   * @param overloadedNodePercentForPartialThrottle the threshold percentage (e.g., 50.0 for 50%)
   */
  public void setOverloadedNodePercentForPartialThrottle(
      double overloadedNodePercentForPartialThrottle) {
    this.overloadedNodePercentForPartialThrottle = overloadedNodePercentForPartialThrottle;
  }

  /**
   * Returns the overloaded node percentage threshold at which full throttling (100%) is applied.
   * The value is a percentage between 0.0 and 100.0.
   *
   * @return full throttling threshold (percentage)
   */
  public double getOverloadedNodePercentForFullThrottle() {
    return overloadedNodePercentForFullThrottle;
  }

  /**
   * Sets the overloaded node percentage threshold for enforcing full throttling. The value should
   * be between 0.0 and 100.0, and greater than or equal to the partial throttling threshold.
   *
   * @param overloadedNodePercentForFullThrottle the threshold percentage (e.g., 80.0 for 80%)
   */
  public void setOverloadedNodePercentForFullThrottle(double overloadedNodePercentForFullThrottle) {
    this.overloadedNodePercentForFullThrottle = overloadedNodePercentForFullThrottle;
  }

  /**
   * Returns the base throttling percentage applied when the overloaded node percentage reaches the
   * partial throttling threshold. The actual throttling percentage increases linearly from this
   * value to 100.0 as the overloaded node percentage approaches the full throttling threshold. The
   * value is between 0.0 and 100.0.
   *
   * @return base throttling percentage (e.g., 50.0 for 50%)
   */
  public double getBaseThrottlePercent() {
    return baseThrottlePercent;
  }

  /**
   * Sets the base throttling percentage used at the start of partial throttling. The value should
   * be between 0.0 and 100.0.
   *
   * @param baseThrottlePercent base throttling percentage (e.g., 50.0 for 50%)
   */
  public void setBaseThrottlePercent(double baseThrottlePercent) {
    this.baseThrottlePercent = baseThrottlePercent;
  }
}
