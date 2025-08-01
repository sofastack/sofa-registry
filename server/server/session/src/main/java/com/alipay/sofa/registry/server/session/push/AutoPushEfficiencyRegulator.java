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

  // 预热次数，等到所有的窗口都轮换过一遍之后才能开始统计
  // 这里因为 warmupTimes 的值总是单线程读写的，因此没有加 volatile 关键字
  private int warmupTimes;

  // 唯一 ID
  private final Long id;

  // 推送效率配置更新器
  private final PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater;

  // 攒批时长
  private final IntMetric debouncingTime;

  // 最大攒批时长
  private final IntMetric maxDebouncingTime;

  public AutoPushEfficiencyRegulator(
      AutoPushEfficiencyConfig autoPushEfficiencyConfig,
      PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater) {
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
    this.warmupTimes = 0;
    this.pushEfficiencyConfigUpdater = pushEfficiencyConfigUpdater;

    // 初始化可能需要调整的指标
    this.debouncingTime =
        new IntMetric(
            autoPushEfficiencyConfig.isEnableDebouncingTime(),
            autoPushEfficiencyConfig.getDebouncingTimeMax(),
            autoPushEfficiencyConfig.getDebouncingTimeMin(),
            autoPushEfficiencyConfig.getDebouncingTimeStep());
    this.maxDebouncingTime =
        new IntMetric(
            autoPushEfficiencyConfig.isEnableMaxDebouncingTime(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeMax(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeMin(),
            autoPushEfficiencyConfig.getMaxDebouncingTimeStep());

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

  private boolean checkPushCountIsHigh() {
    long totalPushCount = 0;
    for (int forIndex = 0; forIndex < this.windows.length; forIndex++) {
      totalPushCount += this.windows[forIndex].get();
    }
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
    if (this.checkPushCountIsHigh()) {
      // 推送频率过高，尝试更新攒批时长
      boolean dataChange = false;

      if (debouncingTime.tryIncrement()) {
        dataChange = true;
      }

      if (maxDebouncingTime.tryIncrement()) {
        dataChange = true;
      }

      if (dataChange) {
        this.updateDebouncingTime("Increment");
      }
    } else {
      // 推送频率正常，此时尝试逐渐降低攒批时长
      boolean dataChange = false;

      if (debouncingTime.tryDecrement()) {
        dataChange = true;
      }

      if (maxDebouncingTime.tryDecrement()) {
        dataChange = true;
      }

      if (dataChange) {
        this.updateDebouncingTime("Decrement");
      }
    }

    // 3. 滚动窗口
    // 这里放到最后滚动窗口是因为：
    // 滚动窗口时，会把最新的窗口计数清零，如果先滚动后检查推送频率，
    // 那么感知到的推送频率就会偏小一点
    this.rollWindow();
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

  // 指标的最大值
  private final int max;

  // 指标的最小值
  private final int min;

  // 指标的步长
  private final int step;

  // 当前指标的值
  private int current;

  public IntMetric(boolean enable, int max, int min, int step) {
    this.enable = enable;
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
    return this.current;
  }
}
