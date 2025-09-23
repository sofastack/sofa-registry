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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 推送流控配置
 *
 * @author huicha
 * @date 2025/7/23
 */
public class AutoPushEfficiencyRegulator extends LoopRunnable {

  private static final Logger LOGGER = LoggerFactory.getLogger("AUTO-PUSH-EFFICIENCY-REGULATOR");

  private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

  // 窗口的时长 (毫秒)
  private final long windowTime;

  // 窗口数量
  private final int windowNum;

  // 窗口; 用于统计每个窗口内的推送次数
  private final AtomicLong[] windows;

  // 当前窗口的索引
  private final AtomicInteger index;

  // 阈值; 推送次数高于这个阈值的时候会开始逐渐调整攒批配置
  private final long pushCountThreshold;

  // 阈值; 用于调整开关流限流开关的 Load 阈值。
  // 不影响推送攒批配置
  private final double loadThreshold;

  // 预热次数，等到所有的窗口都轮换过一遍之后才能开始统计
  // 这里因为 warmupTimes 的值总是单线程读写的，因此没有加 volatile 关键字
  private int warmupTimes;

  // 唯一 ID
  private final Long id;

  // 推送效率配置更新器
  private final PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater;

  // 采集系统负载指标
  private final OperatingSystemMXBean operatingSystemMXBean;

  // 攒批时长
  private final IntMetric debouncingTime;

  // 最大攒批时长
  private final IntMetric maxDebouncingTime;

  // 开关流限流
  // 因为命名为 enable traffic operate 在这里可能会有些歧义
  // 可能会有人理解成 load 过高时是否操作开关流限流，因此这里命名的比较长
  private final BooleanMetric trafficOperateLimitSwitch;

  /**
   * Creates and starts an AutoPushEfficiencyRegulator that monitors push activity and adapts
   * debouncing and traffic-limit settings over rolling time windows.
   *
   * <p>The constructor reads configuration from the provided PushEfficiencyImproveConfig, initializes
   * the rolling windows and internal metrics (debouncingTime, maxDebouncingTime, trafficOperateLimitSwitch),
   * assigns an internal id, and starts a daemon thread to run the regulator loop.
   *
   * @param pushEfficiencyImproveConfig configuration source used to initialize window sizes,
   *        thresholds, and metric bounds/steps
   */
  public AutoPushEfficiencyRegulator(
      PushEfficiencyImproveConfig pushEfficiencyImproveConfig,
      PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater) {
    // 获取自适应攒批配置
    AutoPushEfficiencyConfig autoPushEfficiencyConfig =
        pushEfficiencyImproveConfig.getAutoPushEfficiencyConfig();

    // 初始化窗口相关配置
    this.windowTime = autoPushEfficiencyConfig.getWindowTimeMillis();
    this.windowNum = autoPushEfficiencyConfig.getWindowNum();
    this.windows = new AtomicLong[windowNum];
    for (int i = 0; i < windowNum; i++) {
      this.windows[i] = new AtomicLong(0);
    }
    this.index = new AtomicInteger(0);

    // 设置其他参数
    this.id = ID_GENERATOR.incrementAndGet();
    this.pushCountThreshold = autoPushEfficiencyConfig.getPushCountThreshold();
    this.loadThreshold = autoPushEfficiencyConfig.getLoadThreshold();
    this.warmupTimes = 0;
    this.pushEfficiencyConfigUpdater = pushEfficiencyConfigUpdater;
    this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    // 初始化可能需要调整的指标
    this.debouncingTime =
        new IntMetric(
            autoPushEfficiencyConfig.isEnableDebouncingTime(),
            pushEfficiencyImproveConfig.getChangeDebouncingMillis(),
            autoPushEfficiencyConfig.getDebouncingTimeMax(),
            autoPushEfficiencyConfig.getDebouncingTimeMin(),
            autoPushEfficiencyConfig.getDebouncingTimeStep());
    this.maxDebouncingTime =
        new IntMetric(
            autoPushEfficiencyConfig.isEnableMaxDebouncingTime(),
            pushEfficiencyImproveConfig.getChangeDebouncingMaxMillis(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeMax(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeMin(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeStep());
    this.trafficOperateLimitSwitch =
        new BooleanMetric(autoPushEfficiencyConfig.isEnableTrafficOperateLimitSwitch());

    // 启动定时任务
    ConcurrentUtils.createDaemonThread("AutoPushEfficiencyRegulator-" + this.id, this).start();
  }

  /**
   * Atomically increments the push counter for the currently active rolling window.
   *
   * This is a no-op when the regulator is closed. Errors thrown during the increment are caught
   * to prevent propagation.
   */
  public void safeIncrementPushCount() {
    try {
      if (this.isClosed()) {
        return;
      }
      int currentIndex = this.index.get();
      this.windows[currentIndex].incrementAndGet();
    } catch (Throwable throwable) {
      LOGGER.error(
          "[module=AutoPushEfficiencyRegulator][method=safeIncrementPushCount] increment push count exception",
          throwable);
    }
  }

  /**
   * Advance the rolling-window index to the next window, wrapping to zero when needed.
   *
   * The target window's counter is cleared to 0 before the active index is updated,
   * ensuring the newly activated window starts with an empty count.
   */
  private void rollWindow() {
    // 1. 获取当前窗口的索引
    int currentIndex = this.index.get();

    // 2. 计算出下一个窗口的索引
    int newIndex = currentIndex + 1;
    if (newIndex >= this.windowNum) {
      newIndex = 0;
    }

    // 3. 先清空下一个窗口的统计值，然后再更新索引
    this.windows[newIndex].set(0);
    this.index.set(newIndex);
  }

  /**
   * Sums and returns the total push count across all rolling windows.
   *
   * @return the aggregated push count from every window's AtomicLong counter
   */
  private long computeTotalPushCount() {
    long totalPushCount = 0;
    for (int forIndex = 0; forIndex < this.windows.length; forIndex++) {
      totalPushCount += this.windows[forIndex].get();
    }
    return totalPushCount;
  }

  /**
   * Determines whether the total push count across all rolling windows exceeds the configured threshold.
   *
   * @return true if the sum of all window push counters is greater than {@code pushCountThreshold}; false otherwise
   */
  private boolean checkPushCountIsHigh() {
    long totalPushCount = this.computeTotalPushCount();
    return totalPushCount > this.pushCountThreshold;
  }

  /**
   * Reads the current debouncing and max debouncing metrics and applies them to the
   * PushEfficiencyConfigUpdater, logging the values with the provided tag.
   *
   * @param tag short label indicating the reason or source of this update (used in logs)
   */
  private void updateDebouncingTime(String tag) {
    int debouncingTime = this.debouncingTime.load();
    int maxDebouncingTime = this.maxDebouncingTime.load();
    LOGGER.info(
        "[ID: {}][{}] debouncingTime: {} maxDebouncingTime: {}",
        this.id,
        tag,
        debouncingTime,
        maxDebouncingTime);
    this.pushEfficiencyConfigUpdater.updateDebouncingTime(debouncingTime, maxDebouncingTime);
  }

  /**
   * Read the current traffic-operate-limit switch state and apply it to the configuration updater.
   *
   * Retrieves the switch value from the internal metric and forwards it to
   * PushEfficiencyConfigUpdater.updateTrafficOperateLimitSwitch.
   */
  private void updateTrafficOperateLimitSwitch() {
    boolean trafficOperateLimitSwitch = this.trafficOperateLimitSwitch.load();
    LOGGER.info("[ID: {}] trafficOperateLimitSwitch: {}", this.id, trafficOperateLimitSwitch);
    this.pushEfficiencyConfigUpdater.updateTrafficOperateLimitSwitch(trafficOperateLimitSwitch);
  }

  /**
   * Returns the regulator's unique identifier.
   *
   * @return the unique id of this AutoPushEfficiencyRegulator
   */
  public Long getId() {
    return id;
  }

  /**
   * Main periodic loop executed by the regulator thread: performs warmup window rotation, evaluates
   * recent push activity, and adjusts debouncing and traffic-limit settings accordingly.
   *
   * <p>Behavior:
   * - If the regulator is closed, the method returns immediately.
   * - During warmup (fewer than configured windows have been rotated) it advances the rolling
   *   window and increments the warmup counter without evaluating push rates.
   * - After warmup completes, it computes whether the total push count across all windows exceeds
   *   the configured threshold, then attempts to update debouncing-related metrics and the
   *   traffic-operate-limit switch based on that result and current system load.
   * - Finally, it advances (rolls) the window. The final roll is intentionally performed after
   *   evaluations to avoid artificially lowering the observed push rate by clearing the newest
   *   window prematurely.
   */
  @Override
  public void runUnthrowable() {
    if (this.isClosed()) {
      // 如果任务已经被停止了，那么这个进程需要退出
      return;
    }

    // 1. 检查是否所有的窗口都轮换过了，即是否完成了预热
    if (this.warmupTimes < this.windowNum) {
      // 如果还没有，那么直接滚动窗口，不需要去检查推送频率
      this.rollWindow();
      this.warmupTimes++;
      return;
    }

    // 2. 已经完成预热了，检查推送频率是否过高
    boolean pushCountIsHigh = this.checkPushCountIsHigh();

    // 3. 根据推送频率调整推送配置
    this.tryUpdatePushConfig(pushCountIsHigh);

    // 4. 根据推送频率以及负载情况调整开关流限流配置
    this.tryUpdateTrafficOperateLimitSwitch(pushCountIsHigh);

    // 3. 滚动窗口
    // 这里放到最后滚动窗口是因为：
    // 滚动窗口时，会把最新的窗口计数清零，如果先滚动后检查推送频率，
    // 那么感知到的推送频率就会偏小一点
    this.rollWindow();
  }

  /**
   * Evaluate system load and push activity to toggle the traffic-operate-limit switch.
   *
   * <p>If the traffic-operate-limit feature is enabled, this method reads the system
   * 1-minute load average and:
   * - turns the switch on when both the recent push count is high and load > loadThreshold,
   * - turns the switch off otherwise.
   *
   * <p>If the switch state actually changes, updateTrafficOperateLimitSwitch() is invoked
   * to persist the change via the configured updater.
   *
   * @param pushCountIsHigh true when the aggregated push count across rolling windows exceeds the configured threshold
   */
  private void tryUpdateTrafficOperateLimitSwitch(boolean pushCountIsHigh) {
    if (!this.trafficOperateLimitSwitch.isEnable()) {
      // 如果没有开启支持操作开关流，那么就不执行后续的代码了，尽量尝试避免获取系统负载
      return;
    }

    // 这里获取到的是过去一分钟的负载平均值，这个值有可能小于 0，小于 0 时表示无法获取平均负载
    // 另外，这个方法的注释上写了这个方法设计上就会考虑可能较频繁调用，因此这里先不考虑做限制了
    double loadAverage = this.operatingSystemMXBean.getSystemLoadAverage();
    if (loadAverage < 0) {
      return;
    }

    boolean loadIsHigh = loadAverage > loadThreshold;

    if (pushCountIsHigh && loadIsHigh) {
      if (this.trafficOperateLimitSwitch.tryTurnOn()) {
        this.updateTrafficOperateLimitSwitch();
      }
    } else {
      if (this.trafficOperateLimitSwitch.tryTurnOff()) {
        this.updateTrafficOperateLimitSwitch();
      }
    }
  }

  /**
   * Adjusts debouncing-related push configuration based on current push rate.
   *
   * If `pushCountIsHigh` is true, attempts to increment debouncing metrics; if any metric
   * changes, calls {@code updateDebouncingTime("Increment")}. If false, attempts to
   * decrement metrics and, on change, calls {@code updateDebouncingTime("Decrement")}.
   *
   * @param pushCountIsHigh whether the recent aggregated push count exceeds the configured threshold
   */
  private void tryUpdatePushConfig(boolean pushCountIsHigh) {
    if (pushCountIsHigh) {
      // 推送频率过高，尝试更新攒批时长
      if (this.tryIncrementPushConfig()) {
        this.updateDebouncingTime("Increment");
      }
    } else {
      // 推送频率正常，此时尝试逐渐降低攒批时长
      if (this.tryDecrementPushConfig()) {
        this.updateDebouncingTime("Decrement");
      }
    }
  }

  /**
   * Attempt to increase push-related debounce metrics.
   *
   * Tries to increment the configured debouncingTime and maxDebouncingTime metrics
   * (if each metric is enabled and not already at its maximum). Returns true if
   * at least one metric was changed.
   *
   * @return true if either debouncingTime or maxDebouncingTime was successfully incremented
   */
  private boolean tryIncrementPushConfig() {
    boolean dataChange = false;

    if (debouncingTime.tryIncrement()) {
      dataChange = true;
    }

    if (maxDebouncingTime.tryIncrement()) {
      dataChange = true;
    }

    return dataChange;
  }

  /**
   * Attempt to decrease configurable push debouncing metrics.
   *
   * Tries to decrement the internal debouncingTime and maxDebouncingTime metrics (if enabled
   * and above their minimums). Returns true if at least one metric was changed.
   *
   * @return true if any metric value was decremented, false otherwise
   */
  private boolean tryDecrementPushConfig() {
    boolean dataChange = false;

    if (debouncingTime.tryDecrement()) {
      dataChange = true;
    }

    if (maxDebouncingTime.tryDecrement()) {
      dataChange = true;
    }

    return dataChange;
  }

  /**
   * Sleeps uninterruptibly for the regulator's configured window duration.
   *
   * Blocks the caller for {@code windowTime} milliseconds between regulation cycles.
   */
  @Override
  public void waitingUnthrowable() {
    ConcurrentUtils.sleepUninterruptibly(this.windowTime, TimeUnit.MILLISECONDS);
  }

  /**
   * Returns the configured number of rolling windows used by the regulator.
   *
   * @return the number of time windows in the rolling window array
   */
  @VisibleForTesting
  public int getWindowNum() {
    return this.windowNum;
  }

  /**
   * Returns the number of rolling window counters.
   *
   * Useful for testing/inspection to verify how many windows the regulator maintains.
   *
   * @return the configured number of windows (length of the internal windows array)
   */
  @VisibleForTesting
  public int getWindowsSize() {
    return this.windows.length;
  }

  /**
   * Returns the configured total-push threshold used to determine a high push rate.
   *
   * @return the push count threshold (sum of pushes across all rolling windows) 
   */
  @VisibleForTesting
  public long getPushCountThreshold() {
    return this.pushCountThreshold;
  }
}

class IntMetric {

  private final boolean enable;

  // 当不启用自适应攒批时，指标的默认值
  private final int defaultV;

  // 指标的最大值
  private final int max;

  // 指标的最小值
  private final int min;

  // 指标的步长
  private final int step;

  // 当前指标的值
  private int current;

  /**
   * Create an IntMetric controlling a bounded integer value that can be stepped up or down.
   *
   * @param enable   whether the metric is active (if false, load() will return {@code defaultV} and
   *                 tryIncrement/tryDecrement will be no-ops)
   * @param defaultV value to return from load() when the metric is not enabled
   * @param max      maximum allowed value for the metric (upper bound applied when incrementing)
   * @param min      minimum allowed value for the metric (lower bound applied when decrementing);
   *                 also used to initialize the current value
   * @param step     amount to change the current value on each increment/decrement operation
   */
  public IntMetric(boolean enable, int defaultV, int max, int min, int step) {
    this.enable = enable;
    this.defaultV = defaultV;
    this.max = max;
    this.min = min;
    this.step = step;
    this.current = min;
  }

  /**
   * Attempt to increase the metric by one step, up to the configured maximum.
   *
   * If the metric is disabled, this is a no-op and returns false. When enabled,
   * the current value is increased by `step` but never exceeds `max` (the value
   * is clamped to `max`). Returns true if the value changed.
   *
   * @return true if the metric was incremented, false if disabled or already at max
   */
  public boolean tryIncrement() {
    if (!this.enable) {
      return false;
    }

    if (this.current < this.max) {
      int newValue = this.current + this.step;
      if (newValue > this.max) {
        newValue = this.max;
      }
      this.current = newValue;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Attempt to decrease the metric by one step, bounded by the configured minimum.
   *
   * If the metric is disabled or already at its minimum value, this is a no-op and returns false.
   * When a decrement occurs, the current value is reduced by `step` but not below `min`.
   *
   * @return true if the current value was decreased; false otherwise
   */
  public boolean tryDecrement() {
    if (!this.enable) {
      return false;
    }
    if (this.current > this.min) {
      int newValue = this.current - this.step;
      if (newValue < this.min) {
        newValue = this.min;
      }
      this.current = newValue;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns whether this metric is enabled.
   *
   * @return true if the metric is active and can be changed; false otherwise
   */
  public boolean isEnable() {
    return this.enable;
  }

  /**
   * Returns the active value of this metric.
   *
   * If the metric is enabled, returns the current configured value; otherwise returns the defined default value.
   *
   * @return the metric value to use (current when enabled, default when disabled)
   */
  public int load() {
    if (this.enable) {
      return this.current;
    } else {
      return this.defaultV;
    }
  }

  /**
   * Returns the configured default value for this metric.
   *
   * This value is intended to be used when the metric is not enabled.
   *
   * @return the metric's default integer value
   */
  public int loadDefaultV() {
    return this.defaultV;
  }
}

class BooleanMetric {

  private final boolean enable;

  private boolean current;

  /**
   * Create a BooleanMetric.
   *
   * @param enable whether this boolean metric is active; when false, state changes will be ignored
   */
  public BooleanMetric(boolean enable) {
    this.enable = enable;
    this.current = false;
  }

  /**
   * Attempts to enable (turn on) this boolean metric.
   *
   * If the metric is not enabled or is already on, the method does nothing.
   *
   * @return true if the metric was off and was changed to on; false if it was already on or not enabled.
   */
  public boolean tryTurnOn() {
    if (!this.enable) {
      return false;
    }
    if (this.current) {
      return false;
    } else {
      this.current = true;
      return true;
    }
  }

  /**
   * Attempts to turn the metric off.
   *
   * If the metric is not enabled this is a no-op and returns false. If the metric is enabled and currently on,
   * it flips the state to off and returns true. If it is already off, returns false.
   *
   * @return true if the metric was on and was turned off; false if it was already off or not enabled
   */
  public boolean tryTurnOff() {
    if (!this.enable) {
      return false;
    }
    if (this.current) {
      this.current = false;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns whether this metric is enabled.
   *
   * @return true if the metric is active and can be changed; false otherwise
   */
  public boolean isEnable() {
    return this.enable;
  }

  /**
   * Returns the current boolean state of this metric.
   *
   * Callers may check isEnable() to determine whether the metric is active; this method always returns the last stored state.
   *
   * @return true if the metric is currently on, false otherwise
   */
  public boolean load() {
    return this.current;
  }
}
