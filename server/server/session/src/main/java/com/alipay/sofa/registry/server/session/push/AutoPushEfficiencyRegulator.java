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

  /** 滚动窗口 */
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

  private long computeTotalPushCount() {
    long totalPushCount = 0;
    for (int forIndex = 0; forIndex < this.windows.length; forIndex++) {
      totalPushCount += this.windows[forIndex].get();
    }
    return totalPushCount;
  }

  private boolean checkPushCountIsHigh() {
    long totalPushCount = this.computeTotalPushCount();
    return totalPushCount > this.pushCountThreshold;
  }

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

  private void updateTrafficOperateLimitSwitch() {
    boolean trafficOperateLimitSwitch = this.trafficOperateLimitSwitch.load();
    LOGGER.info("[ID: {}] trafficOperateLimitSwitch: {}", this.id, trafficOperateLimitSwitch);
    this.pushEfficiencyConfigUpdater.updateTrafficOperateLimitSwitch(trafficOperateLimitSwitch);
  }

  public Long getId() {
    return id;
  }

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

  @Override
  public void waitingUnthrowable() {
    ConcurrentUtils.sleepUninterruptibly(this.windowTime, TimeUnit.MILLISECONDS);
  }

  @VisibleForTesting
  public int getWindowNum() {
    return this.windowNum;
  }

  @VisibleForTesting
  public int getWindowsSize() {
    return this.windows.length;
  }

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

  public IntMetric(boolean enable, int defaultV, int max, int min, int step) {
    this.enable = enable;
    this.defaultV = defaultV;
    this.max = max;
    this.min = min;
    this.step = step;
    this.current = min;
  }

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

  public boolean isEnable() {
    return this.enable;
  }

  public int load() {
    if (this.enable) {
      return this.current;
    } else {
      return this.defaultV;
    }
  }

  public int loadDefaultV() {
    return this.defaultV;
  }
}

class BooleanMetric {

  private final boolean enable;

  private boolean current;

  public BooleanMetric(boolean enable) {
    this.enable = enable;
    this.current = false;
  }

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

  public boolean isEnable() {
    return this.enable;
  }

  public boolean load() {
    return this.current;
  }
}
