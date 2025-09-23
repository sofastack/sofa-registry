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
package com.alipay.sofa.registry.server.session.push;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author huicha
 * @date 2025/7/24
 */
public class AutoPushEfficiencyConfig {

  private static final int DEFAULT_WINDOW_NUM = 6;

  private static final long DEFAULT_WINDOW_TIME_MILLIS = TimeUnit.SECONDS.toMillis(10L);

  private static final long DEFAULT_PUSH_COUNT_THRESHOLD = 170000L;

  private static final int DEFAULT_DEBOUNCING_TIME_MAX = 1000;

  private static final int DEFAULT_DEBOUNCING_TIME_MIN = 100;

  private static final int DEFAULT_DEBOUNCING_TIME_STEP = 100;

  private static final int DEFAULT_MAX_DEBOUNCING_TIME_MAX = 3000;

  private static final int DEFAULT_MAX_DEBOUNCING_TIME_MIN = 1000;

  private static final int DEFAULT_MAX_DEBOUNCING_TIME_STEP = 200;

  private static final double DEFAULT_LOAD_THRESHOLD = 6;

  private boolean enableAutoPushEfficiency = false;

  private int windowNum = DEFAULT_WINDOW_NUM;

  private long windowTimeMillis = DEFAULT_WINDOW_TIME_MILLIS;

  private long pushCountThreshold = DEFAULT_PUSH_COUNT_THRESHOLD;

  // == 攒批时长参数 ==
  // 启动攒批时长的自动化调整
  private boolean enableDebouncingTime = false;

  // 攒批时长的最大值
  private int debouncingTimeMax = DEFAULT_DEBOUNCING_TIME_MAX;

  // 攒批时长的最小值
  private int debouncingTimeMin = DEFAULT_DEBOUNCING_TIME_MIN;

  // 调整攒批时长的步长
  private int debouncingTimeStep = DEFAULT_DEBOUNCING_TIME_STEP;

  // 启动最大攒批时长的自动化调整
  // 可以看下下面这个方法，最大攒批时长，和攒批时长是两个不同的指标
  // @see com.alipay.sofa.registry.server.session.push.ChangeProcessor.Worker.setChangeTaskWorkDelay
  private boolean enableMaxDebouncingTime = false;

  // 最大攒批时长的最大值
  private int maxDebouncingTimeMax = DEFAULT_MAX_DEBOUNCING_TIME_MAX;

  // 最大攒批时长的最小值
  private int maxDebouncingTimeMin = DEFAULT_MAX_DEBOUNCING_TIME_MIN;

  // 最大调整攒批时长的步长
  private int maxDebouncingTimeStep = DEFAULT_MAX_DEBOUNCING_TIME_STEP;
  // == 攒批时长参数 ==

  // == 开关流限流参数 ==
  private boolean enableTrafficOperateLimitSwitch = false;

  private double loadThreshold = DEFAULT_LOAD_THRESHOLD;
  /**
   * Returns whether auto-push efficiency optimizations are enabled.
   *
   * <p>If true, the session push logic should apply the configured windowing, debouncing, and load
   * thresholds to adapt push behavior for improved efficiency.
   *
   * @return true when auto-push efficiency is enabled; false otherwise
   */

  public boolean isEnableAutoPushEfficiency() {
    return enableAutoPushEfficiency;
  }

  /**
   * Enables or disables automatic push-efficiency behavior.
   *
   * @param enableAutoPushEfficiency true to enable auto push efficiency; false to disable it
   */
  public void setEnableAutoPushEfficiency(boolean enableAutoPushEfficiency) {
    this.enableAutoPushEfficiency = enableAutoPushEfficiency;
  }

  /**
   * Returns the number of time windows used to aggregate push counts for auto-push efficiency.
   *
   * @return the configured window count (defaults to {@code 6})
   */
  public int getWindowNum() {
    return windowNum;
  }

  /**
   * Set the number of sliding windows used to aggregate push metrics for auto-push efficiency.
   *
   * @param windowNum the number of windows (must be a positive integer; typical default: {@code 6})
   */
  public void setWindowNum(int windowNum) {
    this.windowNum = windowNum;
  }

  /**
   * Returns the configured window duration in milliseconds used for auto-push efficiency counting.
   *
   * @return window duration in milliseconds
   */
  public long getWindowTimeMillis() {
    return windowTimeMillis;
  }

  /**
   * Sets the duration of a single monitoring window used by the auto-push efficiency logic.
   *
   * @param windowTimeMillis duration of the window in milliseconds
   */
  public void setWindowTimeMillis(long windowTimeMillis) {
    this.windowTimeMillis = windowTimeMillis;
  }

  /**
   * Returns the configured push count threshold used to trigger auto-push efficiency logic.
   *
   * @return the number of pushes (within the configured window) that will trigger efficiency measures
   */
  public long getPushCountThreshold() {
    return pushCountThreshold;
  }

  /**
   * Sets the push count threshold used to trigger auto-push efficiency behavior.
   *
   * @param pushCountThreshold the minimum number of pushes within the configured window
   *                           that will activate auto-push efficiency measures
   */
  public void setPushCountThreshold(long pushCountThreshold) {
    this.pushCountThreshold = pushCountThreshold;
  }

  /**
   * Returns whether debouncing time is enabled.
   *
   * @return true if debouncing time is enabled, false otherwise
   */
  public boolean isEnableDebouncingTime() {
    return enableDebouncingTime;
  }

  /**
   * Enable or disable debouncing time behavior.
   *
   * When enabled, the configured debouncingTimeMin, debouncingTimeMax and debouncingTimeStep
   * values are used to adjust debouncing intervals for auto-push efficiency.
   *
   * @param enableDebouncingTime true to enable debouncing time adjustments, false to disable
   */
  public void setEnableDebouncingTime(boolean enableDebouncingTime) {
    this.enableDebouncingTime = enableDebouncingTime;
  }

  /**
   * Returns the maximum debouncing time in milliseconds.
   *
   * This value is the upper bound used when debouncing is enabled to limit how long pushes
   * may be delayed.
   *
   * @return maximum debouncing time (milliseconds)
   */
  public int getDebouncingTimeMax() {
    return debouncingTimeMax;
  }

  /**
   * Sets the maximum debouncing time (in milliseconds) used when debouncing is enabled.
   *
   * @param debouncingTimeMax maximum debouncing delay in milliseconds
   */
  public void setDebouncingTimeMax(int debouncingTimeMax) {
    this.debouncingTimeMax = debouncingTimeMax;
  }

  /**
   * Returns the configured minimum debouncing time in milliseconds.
   *
   * This value is used as the lower bound when debouncing is enabled to delay push operations.
   *
   * @return minimum debouncing time in milliseconds
   */
  public int getDebouncingTimeMin() {
    return debouncingTimeMin;
  }

  /**
   * Sets the minimum debouncing time used when debouncing push operations.
   *
   * @param debouncingTimeMin minimum debouncing interval in milliseconds
   */
  public void setDebouncingTimeMin(int debouncingTimeMin) {
    this.debouncingTimeMin = debouncingTimeMin;
  }

  /**
   * Returns the configured step increment used when adjusting the debouncing time.
   *
   * @return the debouncing time step in milliseconds
   */
  public int getDebouncingTimeStep() {
    return debouncingTimeStep;
  }

  /**
   * Set the step size (in milliseconds) used when adjusting the debouncing time.
   *
   * @param debouncingTimeStep step increment in milliseconds for debouncing time adjustments
   */
  public void setDebouncingTimeStep(int debouncingTimeStep) {
    this.debouncingTimeStep = debouncingTimeStep;
  }

  /**
   * Returns whether the maximum debouncing-time feature is enabled.
   *
   * When enabled, the configured max debouncing time range/step settings are applied to bound
   * debouncing durations.
   *
   * @return true if max debouncing-time enforcement is enabled, false otherwise
   */
  public boolean isEnableMaxDebouncingTime() {
    return enableMaxDebouncingTime;
  }

  /**
   * Enables or disables the use of a configurable maximum debouncing time.
   *
   * @param enableMaxDebouncingTime true to enable applying the configured maximum debouncing time; false to disable
   */
  public void setEnableMaxDebouncingTime(boolean enableMaxDebouncingTime) {
    this.enableMaxDebouncingTime = enableMaxDebouncingTime;
  }

  /**
   * Returns the configured upper bound (milliseconds) for the maximum debouncing time used by auto-push efficiency.
   *
   * When the max-debouncing-time feature is enabled, this value caps the computed maximum debouncing delay.
   *
   * @return upper bound in milliseconds for max debouncing time
   */
  public int getMaxDebouncingTimeMax() {
    return maxDebouncingTimeMax;
  }

  /**
   * Sets the upper bound for the maximum debouncing time.
   *
   * @param maxDebouncingTimeMax the maximum debouncing time, in milliseconds
   */
  public void setMaxDebouncingTimeMax(int maxDebouncingTimeMax) {
    this.maxDebouncingTimeMax = maxDebouncingTimeMax;
  }

  /**
   * Returns the minimum allowed max-debouncing time.
   *
   * @return the minimum max-debouncing time in milliseconds
   */
  public int getMaxDebouncingTimeMin() {
    return maxDebouncingTimeMin;
  }

  /**
   * Sets the minimum allowed value for the maximum debouncing time.
   *
   * This value is in milliseconds and defines the lower bound used when computing
   * the maximum debouncing interval applied by the auto-push efficiency logic.
   *
   * @param maxDebouncingTimeMin minimum max-debouncing time in milliseconds
   */
  public void setMaxDebouncingTimeMin(int maxDebouncingTimeMin) {
    this.maxDebouncingTimeMin = maxDebouncingTimeMin;
  }

  /**
   * Returns the step size (in milliseconds) used when adjusting the maximum debouncing time.
   *
   * @return the max debouncing time step in milliseconds
   */
  public int getMaxDebouncingTimeStep() {
    return maxDebouncingTimeStep;
  }

  /**
   * Set the step size, in milliseconds, used when adjusting the maximum debouncing time.
   *
   * @param maxDebouncingTimeStep step size in milliseconds for each adjustment increment
   */
  public void setMaxDebouncingTimeStep(int maxDebouncingTimeStep) {
    this.maxDebouncingTimeStep = maxDebouncingTimeStep;
  }

  /**
   * Returns whether the traffic operate limit switch is enabled.
   *
   * When enabled, traffic-based operation limits are applied (see {@link #getLoadThreshold()}).
   *
   * @return true if the traffic operate limit switch is enabled; false otherwise
   */
  public boolean isEnableTrafficOperateLimitSwitch() {
    return enableTrafficOperateLimitSwitch;
  }

  /**
   * Returns the configured load threshold used by the traffic operate limit switch.
   *
   * This value (default 6.0) is the load boundary at which traffic operation limits may be applied.
   *
   * @return the current load threshold
   */
  public double getLoadThreshold() {
    return loadThreshold;
  }

  /**
   * Sets the load threshold used by the traffic operate limit switch.
   *
   * This value determines the load level at which traffic operation limits are applied
   * when the traffic operate limit switch is enabled.
   *
   * @param loadThreshold the load threshold value
   */
  public void setLoadThreshold(double loadThreshold) {
    this.loadThreshold = loadThreshold;
  }

  /**
   * Returns a string representation of this configuration.
   *
   * <p>The returned value is generated via reflection and includes the values of this object's fields.
   *
   * @return a human-readable string containing the names and values of the fields of this instance
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
