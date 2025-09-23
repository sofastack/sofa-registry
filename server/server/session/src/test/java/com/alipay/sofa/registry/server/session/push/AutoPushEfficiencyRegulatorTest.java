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

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author huicha
 * @date 2025/7/24
 */
public class AutoPushEfficiencyRegulatorTest {

  @Test
  public void testUpdateDebouncingTimeAndMaxDebouncingTime() throws InterruptedException {
    // 开启自动化配置
    AutoPushEfficiencyConfig autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(true);

    // 总推送数超过 10 笔就触发增加攒批时长
    autoPushEfficiencyConfig.setPushCountThreshold(10);

    autoPushEfficiencyConfig.setWindowNum(10);
    autoPushEfficiencyConfig.setWindowTimeMillis(100);

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    MockPushEfficiencyConfigUpdater mockPushEfficiencyConfigUpdater =
        new MockPushEfficiencyConfigUpdater();
    PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater =
        Mockito.mock(PushEfficiencyConfigUpdater.class, mockPushEfficiencyConfigUpdater);

    AutoPushEfficiencyRegulator autoPushEfficiencyRegulator =
        new AutoPushEfficiencyRegulator(pushEfficiencyImproveConfig, pushEfficiencyConfigUpdater);

    try {
      for (int loop = 0; loop < 40; loop++) {
        for (int i = 0; i < 5; i++) {
          autoPushEfficiencyRegulator.safeIncrementPushCount();
        }
        Thread.sleep(50);
      }

      int debouncingTime = mockPushEfficiencyConfigUpdater.getDebouncingTime();
      int maxDebouncingTime = mockPushEfficiencyConfigUpdater.getMaxDebouncingTime();
      Assert.assertEquals(
          debouncingTime, 1000 /*AutoPushEfficiencyConfig.DEFAULT_DEBOUNCING_TIME_MAX*/);
      Assert.assertEquals(
          maxDebouncingTime, 3000 /*AutoPushEfficiencyConfig.DEFAULT_MAX_DEBOUNCING_TIME_MAX*/);

      Thread.sleep(2000);

      debouncingTime = mockPushEfficiencyConfigUpdater.getDebouncingTime();
      maxDebouncingTime = mockPushEfficiencyConfigUpdater.getMaxDebouncingTime();
      Assert.assertEquals(
          debouncingTime, 100 /*AutoPushEfficiencyConfig.DEFAULT_DEBOUNCING_TIME_MIN*/);
      Assert.assertEquals(
          maxDebouncingTime, 1000 /*AutoPushEfficiencyConfig.DEFAULT_MAX_DEBOUNCING_TIME_MIN*/);
    } finally {
      autoPushEfficiencyRegulator.close();
    }
  }

  @Test
  public void testOnlyUpdateDebouncingTime() throws InterruptedException {
    // 开启自动化配置
    AutoPushEfficiencyConfig autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);

    // 不自适应调整 max debouncing time，但是这里设置单独的初始配置
    // 预期这个初始配置不生效
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(false);
    autoPushEfficiencyConfig.setMaxDebouncingTimeMin(10);
    autoPushEfficiencyConfig.setMaxDebouncingTimeMax(100);
    autoPushEfficiencyConfig.setMaxDebouncingTimeStep(10);

    // 总推送数超过 10 笔就触发增加攒批时长
    autoPushEfficiencyConfig.setPushCountThreshold(10);

    autoPushEfficiencyConfig.setWindowNum(10);
    autoPushEfficiencyConfig.setWindowTimeMillis(100);

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setZoneSet(Collections.singleton("ALL_ZONE"));
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    // 设置一个特别的 max debouncing time，预期后面自适应调整攒批配置的时候，max debouncing time 始终为 15000
    pushEfficiencyImproveConfig.setChangeDebouncingMaxMillis(15000);

    MockPushEfficiencyConfigUpdater mockPushEfficiencyConfigUpdater =
        new MockPushEfficiencyConfigUpdater();
    PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater =
        Mockito.mock(PushEfficiencyConfigUpdater.class, mockPushEfficiencyConfigUpdater);

    AutoPushEfficiencyRegulator autoPushEfficiencyRegulator =
        new AutoPushEfficiencyRegulator(pushEfficiencyImproveConfig, pushEfficiencyConfigUpdater);

    try {
      for (int loop = 0; loop < 40; loop++) {
        for (int i = 0; i < 5; i++) {
          autoPushEfficiencyRegulator.safeIncrementPushCount();
        }
        Thread.sleep(50);
      }

      int debouncingTime = mockPushEfficiencyConfigUpdater.getDebouncingTime();
      int maxDebouncingTime = mockPushEfficiencyConfigUpdater.getMaxDebouncingTime();
      Assert.assertEquals(
          debouncingTime, 1000 /*AutoPushEfficiencyConfig.DEFAULT_DEBOUNCING_TIME_MAX*/);
      Assert.assertEquals(
          maxDebouncingTime, 15000 /*AutoPushEfficiencyConfig.DEFAULT_MAX_DEBOUNCING_TIME_MAX*/);

      Thread.sleep(2000);

      debouncingTime = mockPushEfficiencyConfigUpdater.getDebouncingTime();
      maxDebouncingTime = mockPushEfficiencyConfigUpdater.getMaxDebouncingTime();
      Assert.assertEquals(
          debouncingTime, 100 /*AutoPushEfficiencyConfig.DEFAULT_DEBOUNCING_TIME_MIN*/);
      Assert.assertEquals(
          maxDebouncingTime, 15000 /*AutoPushEfficiencyConfig.DEFAULT_MAX_DEBOUNCING_TIME_MIN*/);
    } finally {
      autoPushEfficiencyRegulator.close();
    }
  }
}

class MockPushEfficiencyConfigUpdater implements Answer<Void> {

  private int debouncingTime;

  private int maxDebouncingTime;

  @Override
  public Void answer(InvocationOnMock invocation) throws Throwable {
    this.debouncingTime = invocation.getArgumentAt(0, Integer.class);
    this.maxDebouncingTime = invocation.getArgumentAt(1, Integer.class);
    return null;
  }

  public int getDebouncingTime() {
    return debouncingTime;
  }

  public void setDebouncingTime(int debouncingTime) {
    this.debouncingTime = debouncingTime;
  }

  public int getMaxDebouncingTime() {
    return maxDebouncingTime;
  }

  public void setMaxDebouncingTime(int maxDebouncingTime) {
    this.maxDebouncingTime = maxDebouncingTime;
  }
}
