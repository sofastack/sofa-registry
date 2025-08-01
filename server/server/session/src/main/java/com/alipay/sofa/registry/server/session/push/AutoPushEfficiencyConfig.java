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

  private boolean enableAutoPushEfficiency = false;

  private int windowNum = DEFAULT_WINDOW_NUM;

  private long windowTimeMillis = DEFAULT_WINDOW_TIME_MILLIS;

  private long pushCountThreshold = DEFAULT_PUSH_COUNT_THRESHOLD;

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

  public boolean isEnableAutoPushEfficiency() {
    return enableAutoPushEfficiency;
  }

  public void setEnableAutoPushEfficiency(boolean enableAutoPushEfficiency) {
    this.enableAutoPushEfficiency = enableAutoPushEfficiency;
  }

  public int getWindowNum() {
    return windowNum;
  }

  public void setWindowNum(int windowNum) {
    this.windowNum = windowNum;
  }

  public long getWindowTimeMillis() {
    return windowTimeMillis;
  }

  public void setWindowTimeMillis(long windowTimeMillis) {
    this.windowTimeMillis = windowTimeMillis;
  }

  public long getPushCountThreshold() {
    return pushCountThreshold;
  }

  public void setPushCountThreshold(long pushCountThreshold) {
    this.pushCountThreshold = pushCountThreshold;
  }

  public boolean isEnableDebouncingTime() {
    return enableDebouncingTime;
  }

  public void setEnableDebouncingTime(boolean enableDebouncingTime) {
    this.enableDebouncingTime = enableDebouncingTime;
  }

  public int getDebouncingTimeMax() {
    return debouncingTimeMax;
  }

  public void setDebouncingTimeMax(int debouncingTimeMax) {
    this.debouncingTimeMax = debouncingTimeMax;
  }

  public int getDebouncingTimeMin() {
    return debouncingTimeMin;
  }

  public void setDebouncingTimeMin(int debouncingTimeMin) {
    this.debouncingTimeMin = debouncingTimeMin;
  }

  public int getDebouncingTimeStep() {
    return debouncingTimeStep;
  }

  public void setDebouncingTimeStep(int debouncingTimeStep) {
    this.debouncingTimeStep = debouncingTimeStep;
  }

  public boolean isEnableMaxDebouncingTime() {
    return enableMaxDebouncingTime;
  }

  public void setEnableMaxDebouncingTime(boolean enableMaxDebouncingTime) {
    this.enableMaxDebouncingTime = enableMaxDebouncingTime;
  }

  public int getMaxDebouncingTimeMax() {
    return maxDebouncingTimeMax;
  }

  public void setMaxDebouncingTimeMax(int maxDebouncingTimeMax) {
    this.maxDebouncingTimeMax = maxDebouncingTimeMax;
  }

  public int getMaxDebouncingTimeMin() {
    return maxDebouncingTimeMin;
  }

  public void setMaxDebouncingTimeMin(int maxDebouncingTimeMin) {
    this.maxDebouncingTimeMin = maxDebouncingTimeMin;
  }

  public int getMaxDebouncingTimeStep() {
    return maxDebouncingTimeStep;
  }

  public void setMaxDebouncingTimeStep(int maxDebouncingTimeStep) {
    this.maxDebouncingTimeStep = maxDebouncingTimeStep;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
