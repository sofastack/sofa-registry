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

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents the current throttling status for flow control operations.
 *
 * <p>This class carries two key pieces of information:
 *
 * <ul>
 *   <li>Whether throttling is logically enabled ({@link #enabled})
 *   <li>The actual throttling percentage ({@link #throttlePercent}), where:
 *       <ul>
 *         <li>{@code 0.0} means no traffic is throttled,
 *         <li>{@code 50.0} means 50% of traffic is throttled,
 *         <li>{@code 100.0} means all non-critical traffic is blocked.
 *       </ul>
 * </ul>
 *
 * <p>All percentage values are represented as numbers in the range [0.0, 100.0].
 */
public class FlowOperationThrottlingStatus implements Serializable {

  // --- Static factory methods ---

  /**
   * Returns a default instance representing disabled throttling. Equivalent to {@code new
   * FlowOperationThrottlingStatus(false, 0.0)}.
   *
   * @return a disabled throttling status
   */
  public static FlowOperationThrottlingStatus disabled() {
    return new FlowOperationThrottlingStatus(false, 0.0);
  }

  /**
   * Returns an enabled throttling status with the specified throttling percentage.
   *
   * @param throttlePercent the throttling percentage (0.0 ~ 100.0)
   * @return an enabled throttling status
   */
  public static FlowOperationThrottlingStatus enabled(double throttlePercent) {
    return new FlowOperationThrottlingStatus(true, throttlePercent);
  }

  // --- Instance fields ---

  /**
   * Indicates whether adaptive flow throttling is enabled. When {@code false}, {@link
   * #throttlePercent} should be interpreted as 0.0.
   */
  private boolean enabled;

  /**
   * The throttling percentage, ranging from 0.0 to 100.0. For example:
   *
   * <ul>
   *   <li>0.0 → no throttling
   *   <li>30.0 → 30% of requests are throttled
   *   <li>100.0 → full throttling (all non-critical flows blocked)
   * </ul>
   */
  private double throttlePercent;

  /** Default constructor for Hessian deserialization. */
  public FlowOperationThrottlingStatus() {
    this.enabled = false;
    this.throttlePercent = 0.0;
  }

  /**
   * Constructs a new throttling status.
   *
   * @param enabled whether throttling is enabled
   * @param throttlePercent throttling percentage (0.0 ~ 100.0)
   */
  public FlowOperationThrottlingStatus(boolean enabled, double throttlePercent) {
    this.enabled = enabled;
    this.throttlePercent = throttlePercent;
  }

  /**
   * Returns whether throttling is enabled.
   *
   * @return {@code true} if enabled, {@code false} otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the current throttling percentage. Even if {@link #isEnabled()} returns {@code false},
   * this value may still be 0.0.
   *
   * @return throttling percentage in the range [0.0, 100.0]
   */
  public double getThrottlePercent() {
    return throttlePercent;
  }

  /**
   * Sets whether throttling is enabled.
   *
   * @param enabled {@code true} to enable, {@code false} to disable
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Sets the throttling percentage.
   *
   * @param throttlePercent throttling percentage (0.0 ~ 100.0)
   */
  public void setThrottlePercent(double throttlePercent) {
    this.throttlePercent = throttlePercent;
  }

  /**
   * Creates a defensive copy of this status.
   *
   * @return a new instance with the same enabled and throttlePercent values
   */
  public FlowOperationThrottlingStatus copy() {
    return new FlowOperationThrottlingStatus(this.enabled, this.throttlePercent);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
