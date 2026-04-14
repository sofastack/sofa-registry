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

import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.server.session.limit.FlowOperationThrottlingObserver;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2025/7/24
 */
public class PushEfficiencyConfigUpdaterTest {

  @Test
  public void testUpdateFromProviderData() {
    ChangeProcessor changeProcessor = Mockito.mock(ChangeProcessor.class);
    PushProcessor pushProcessor = Mockito.mock(PushProcessor.class);
    FirePushService firePushService = Mockito.mock(FirePushService.class);
    FlowOperationThrottlingObserver flowOperationThrottlingObserver =
        new FlowOperationThrottlingObserver();

    PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater = new PushEfficiencyConfigUpdater();
    pushEfficiencyConfigUpdater.setChangeProcessor(changeProcessor);
    pushEfficiencyConfigUpdater.setPushProcessor(pushProcessor);
    pushEfficiencyConfigUpdater.setFirePushService(firePushService);
    pushEfficiencyConfigUpdater.setFlowOperationThrottlingObserver(flowOperationThrottlingObserver);

    // 更新没有开启自动化配置，因此预期是 null
    pushEfficiencyConfigUpdater.updateFromProviderData(new PushEfficiencyImproveConfig());

    AutoPushEfficiencyRegulator autoPushEfficiencyRegulator =
        pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNull(autoPushEfficiencyRegulator);

    // 第二次开启自动化配置
    AutoPushEfficiencyConfig autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(true);
    autoPushEfficiencyConfig.setWindowTimeMillis(10);

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    pushEfficiencyConfigUpdater.updateFromProviderData(pushEfficiencyImproveConfig);

    autoPushEfficiencyRegulator = pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNotNull(autoPushEfficiencyRegulator);

    // 第三次仍然开启，但是我们修改一部分配置
    autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(true);
    autoPushEfficiencyConfig.setWindowTimeMillis(10);
    autoPushEfficiencyConfig.setWindowNum(3);
    autoPushEfficiencyConfig.setPushCountThreshold(10);

    pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    pushEfficiencyConfigUpdater.updateFromProviderData(pushEfficiencyImproveConfig);

    AutoPushEfficiencyRegulator newAutoPushEfficiencyRegulator =
        pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNotNull(newAutoPushEfficiencyRegulator);

    int windowNum = newAutoPushEfficiencyRegulator.getWindowNum();
    int windowSize = newAutoPushEfficiencyRegulator.getWindowsSize();
    long pushCountThreshold = newAutoPushEfficiencyRegulator.getPushCountThreshold();

    Assert.assertEquals(3, windowNum);
    Assert.assertEquals(3, windowSize);
    Assert.assertEquals(10, pushCountThreshold);

    // 此时之前的 AutoPushEfficiencyRegulator 应当为关闭状态
    Assert.assertTrue(autoPushEfficiencyRegulator.isClosed());

    // 清理释放线程资源
    pushEfficiencyConfigUpdater.stop();

    // 新的 AutoPushEfficiencyRegulator 也应当为关闭状态
    Assert.assertTrue(newAutoPushEfficiencyRegulator.isClosed());
  }

  @Test
  public void testUpdateFromProviderDataDisablesLocalThrottle() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    // Pre-enable local throttling to verify it gets disabled
    observer.updateLocalThrottlingStatus(FlowOperationThrottlingStatus.enabled(100.0));
    Assert.assertTrue(observer.getLocalThrottlingStatus().isEnabled());

    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    updater.updateFromProviderData(new PushEfficiencyImproveConfig());

    // After updateFromProviderData, local throttling should be disabled
    Assert.assertFalse(observer.getLocalThrottlingStatus().isEnabled());
  }

  @Test
  public void testUpdateTrafficOperateLimitSwitchEnabled() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    // Enable auto push efficiency mode first
    enableAutoPushEfficiency(updater);

    // Now update traffic operate limit switch to true (enable throttling)
    updater.updateTrafficOperateLimitSwitch(true);

    FlowOperationThrottlingStatus localStatus = observer.getLocalThrottlingStatus();
    Assert.assertTrue(localStatus.isEnabled());
    Assert.assertEquals(100.0, localStatus.getThrottlePercent(), 0.001);
  }

  @Test
  public void testUpdateTrafficOperateLimitSwitchDisabled() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    // Enable auto push efficiency mode first
    enableAutoPushEfficiency(updater);

    // Enable throttling first
    updater.updateTrafficOperateLimitSwitch(true);
    Assert.assertTrue(observer.getLocalThrottlingStatus().isEnabled());

    // Now disable it
    updater.updateTrafficOperateLimitSwitch(false);
    Assert.assertFalse(observer.getLocalThrottlingStatus().isEnabled());
  }

  @Test
  public void testUpdateTrafficOperateLimitSwitchSkipsWhenNotAutoMode() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    // Without enabling auto push efficiency, updateTrafficOperateLimitSwitch should be skipped
    updater.updateTrafficOperateLimitSwitch(true);

    // Local throttling should remain disabled
    Assert.assertFalse(observer.getLocalThrottlingStatus().isEnabled());
  }

  @Test
  public void testIsRunning() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    // Before stop, isRunning returns false (stop field is false)
    Assert.assertFalse(updater.isRunning());

    // After stop, isRunning returns true (stop field is true)
    updater.stop();
    Assert.assertTrue(updater.isRunning());
  }

  @Test
  public void testStopCleansLocalThrottlingStatus() {
    FlowOperationThrottlingObserver observer = new FlowOperationThrottlingObserver();
    PushEfficiencyConfigUpdater updater = createUpdater(observer);

    // Enable auto push efficiency and set local throttling
    enableAutoPushEfficiency(updater);
    updater.updateTrafficOperateLimitSwitch(true);
    Assert.assertTrue(observer.getLocalThrottlingStatus().isEnabled());

    // Stop should clean up local throttling status
    updater.stop();
    Assert.assertFalse(observer.getLocalThrottlingStatus().isEnabled());
  }

  private PushEfficiencyConfigUpdater createUpdater(FlowOperationThrottlingObserver observer) {
    ChangeProcessor changeProcessor = Mockito.mock(ChangeProcessor.class);
    PushProcessor pushProcessor = Mockito.mock(PushProcessor.class);
    FirePushService firePushService = Mockito.mock(FirePushService.class);

    PushEfficiencyConfigUpdater updater = new PushEfficiencyConfigUpdater();
    updater.setChangeProcessor(changeProcessor);
    updater.setPushProcessor(pushProcessor);
    updater.setFirePushService(firePushService);
    updater.setFlowOperationThrottlingObserver(observer);
    return updater;
  }

  private void enableAutoPushEfficiency(PushEfficiencyConfigUpdater updater) {
    AutoPushEfficiencyConfig autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setWindowTimeMillis(10);

    PushEfficiencyImproveConfig config = new PushEfficiencyImproveConfig();
    config.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    updater.updateFromProviderData(config);
  }
}
