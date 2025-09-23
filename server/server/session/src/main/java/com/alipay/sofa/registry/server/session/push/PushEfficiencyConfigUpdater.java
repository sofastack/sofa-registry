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

import com.alipay.sofa.registry.server.session.resource.ClientManagerResource;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huicha
 * @date 2025/7/24
 */
@Component
public class PushEfficiencyConfigUpdater implements SmartLifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(PushEfficiencyConfigUpdater.class);

  @Autowired private ChangeProcessor changeProcessor;

  @Autowired private PushProcessor pushProcessor;

  @Autowired private FirePushService firePushService;

  @Autowired private ClientManagerResource clientManagerResource;

  private Lock lock;

  private boolean stop;

  private boolean useAutoPushEfficiency = false;

  private AutoPushEfficiencyRegulator autoPushEfficiencyRegulator;

  /**
   * Constructs a PushEfficiencyConfigUpdater, initializing internal synchronization and run-state.
   *
   * Initializes a new ReentrantLock used to guard configuration updates and sets the component as not stopped.
   */
  public PushEfficiencyConfigUpdater() {
    this.lock = new ReentrantLock();
    this.stop = false;
  }

  /**
   * Apply push-efficiency configuration received from provider data.
   *
   * <p>Enables or disables automatic push-efficiency regulation according to the provided
   * PushEfficiencyImproveConfig. When auto mode is enabled this method will close any existing
   * AutoPushEfficiencyRegulator, initialize debouncing values to their configured minimums when
   * applicable, and create a new AutoPushEfficiencyRegulator. When auto mode is disabled it will
   * close and clear any existing regulator. In all cases the updated regulator (or null) is pushed
   * into the PushProcessor, and work delay settings are applied to ChangeProcessor, PushProcessor,
   * and the FirePushService registration processor (if present). Finally, traffic limiting is turned
   * off via the ClientManagerResource.
   *
   * <p>If the component has been stopped, the call is a no-op.
   *
   * @param pushEfficiencyImproveConfig configuration object from provider describing push-efficiency
   *     settings (including optional AutoPushEfficiencyConfig and debouncing/max-debouncing values)
   */
  public void updateFromProviderData(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
    this.lock.lock();
    try {
      LOGGER.info(
          "[PushEfficiencyConfigUpdater] update config from provider data: {}",
          pushEfficiencyImproveConfig);

      if (this.stop) {
        // 已销毁
        return;
      }

      AutoPushEfficiencyConfig autoPushEfficiencyConfig =
          pushEfficiencyImproveConfig.getAutoPushEfficiencyConfig();
      if (null != autoPushEfficiencyConfig
          && autoPushEfficiencyConfig.isEnableAutoPushEfficiency()) {
        // 新的配置中，开启了自动化配置
        this.useAutoPushEfficiency = true;

        if (null != this.autoPushEfficiencyRegulator) {
          // 此时还存在正在运行的 AutoPushEfficiencyRegulator 则需要关掉
          LOGGER.info(
              "[PushEfficiencyConfigUpdater] close old auto push efficiency regulator, id: {}, will create new one",
              this.autoPushEfficiencyRegulator.getId());
          this.autoPushEfficiencyRegulator.close();
        }

        // 这里需要调整下初始配置的值
        if (autoPushEfficiencyConfig.isEnableDebouncingTime()) {
          // 当自适应攒批需要调整 debouncing time 的时候，需要将 debouncing time 的初始值设置为 min
          pushEfficiencyImproveConfig.setChangeDebouncingMillis(
              autoPushEfficiencyConfig.getDebouncingTimeMin());
        }

        if (autoPushEfficiencyConfig.isEnableMaxDebouncingTime()) {
          // 当自适应攒批需要调整 max debouncing time 的时候，需要将 debouncing time 的初始值设置为 min
          pushEfficiencyImproveConfig.setChangeDebouncingMaxMillis(
              autoPushEfficiencyConfig.getMaxDebouncingTimeMin());
        }

        this.autoPushEfficiencyRegulator =
            new AutoPushEfficiencyRegulator(pushEfficiencyImproveConfig, this);
      } else {
        // 新的配置中，关闭了自动化配置，此时如果还存在正在运行的 AutoPushEfficiencyRegulator 则需要关掉
        this.useAutoPushEfficiency = false;

        if (null != this.autoPushEfficiencyRegulator) {
          LOGGER.info(
              "[PushEfficiencyConfigUpdater] close old auto push efficiency regulator, id: {}, will not create new one",
              this.autoPushEfficiencyRegulator.getId());
          this.autoPushEfficiencyRegulator.close();
        }

        this.autoPushEfficiencyRegulator = null;
      }

      // 更新一下 PushProcessor 中的 AutoPushEfficiencyRegulator，以便于统计推送次数
      this.pushProcessor.setAutoPushEfficiencyRegulator(this.autoPushEfficiencyRegulator);

      // 更新配置
      this.changeProcessor.setWorkDelayTime(pushEfficiencyImproveConfig);
      this.pushProcessor.setPushTaskDelayTime(pushEfficiencyImproveConfig);
      if (this.firePushService.getRegProcessor() != null) {
        this.firePushService.getRegProcessor().setWorkDelayTime(pushEfficiencyImproveConfig);
      }

      // 无论如何，先关闭掉限流
      this.clientManagerResource.setEnableTrafficOperate(true);
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Update the change debouncing window used by automatic push-efficiency regulation.
   *
   * When auto push efficiency is not enabled this method is a no-op. Otherwise it
   * updates the minimum and maximum debouncing delays (in milliseconds) applied
   * by the ChangeProcessor.
   *
   * @param debouncingTime minimum debouncing delay in milliseconds
   * @param maxDebouncingTime maximum debouncing delay in milliseconds
   */
  public void updateDebouncingTime(int debouncingTime, int maxDebouncingTime) {
    this.lock.lock();
    try {
      if (!this.useAutoPushEfficiency) {
        // 如果已经停止使用自动化配置了，那么这里就跳过更新，以防止最终实际使用的配置不是 ProvideData 中的配置
        return;
      }
      this.changeProcessor.setChangeDebouncingMillis(debouncingTime, maxDebouncingTime);
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Update the traffic operation limit switch when automatic push-efficiency regulation is enabled.
   *
   * <p>If automatic mode is not active, this method is a no-op. The provided flag represents whether
   * the traffic limit should be enabled; the updater inverts this flag before applying it because
   * enabling the limit disables normal traffic operations.
   *
   * @param trafficOperateLimitSwitch true to enable traffic limiting, false to disable it
   */
  public void updateTrafficOperateLimitSwitch(boolean trafficOperateLimitSwitch) {
    this.lock.lock();
    try {
      if (!this.useAutoPushEfficiency) {
        // 如果已经停止使用自动化配置了，那么这里就跳过更新，以防止最终实际使用的配置不是 ProvideData 中的配置
        return;
      }

      // 打开限制开关，意味着开启了限流，也就是不允许操作开关流，因此这里是反的
      this.clientManagerResource.setEnableTrafficOperate(!trafficOperateLimitSwitch);
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * No-op implementation of SmartLifecycle.start().
   *
   * This component does not perform any action when the lifecycle is started; lifecycle
   * state is managed via {@link #stop()} and {@link #isRunning()}.
   */
  @Override
  public void start() {}

  /**
   * Stops the component and releases associated resources.
   *
   * Marks the updater as stopped and, if an AutoPushEfficiencyRegulator exists, closes it.
   * This method is thread-safe; it acquires an internal lock while mutating state.
   * It is intended to be called when the Spring bean is being destroyed.
   */
  @Override
  public void stop() {
    // Bean 被销毁的时候需要清理释放线程资源
    this.lock.lock();
    try {
      if (!this.stop) {
        this.stop = true;
        if (null != this.autoPushEfficiencyRegulator) {
          this.autoPushEfficiencyRegulator.close();
        }
      }
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Returns the current stop state of this component.
   *
   * <p>Thread-safe: reads the internal stop flag under a lock. The method returns true when the
   * component has been stopped (the internal {@code stop} flag is set), and false otherwise.
   *
   * @return {@code true} if the component is stopped; {@code false} if it is not stopped
   */
  @Override
  public boolean isRunning() {
    this.lock.lock();
    try {
      return this.stop;
    } finally {
      this.lock.unlock();
    }
  }

  /**
   * Returns the current AutoPushEfficiencyRegulator instance.
   *
   * Intended for testing; may return null if automatic push efficiency is not active
   * or the regulator has been closed.
   *
   * @return the active AutoPushEfficiencyRegulator, or {@code null} if none is present
   */
  @VisibleForTesting
  public AutoPushEfficiencyRegulator getAutoPushEfficiencyRegulator() {
    return autoPushEfficiencyRegulator;
  }

  /**
   * Test-only setter that replaces the ChangeProcessor instance used by this updater.
   */
  @VisibleForTesting
  public void setChangeProcessor(ChangeProcessor changeProcessor) {
    this.changeProcessor = changeProcessor;
  }

  /**
   * Replaces the current PushProcessor instance. Intended for testing to inject a mock or stub.
   */
  @VisibleForTesting
  public void setPushProcessor(PushProcessor pushProcessor) {
    this.pushProcessor = pushProcessor;
  }

  /**
   * Test hook to replace the FirePushService instance used by this component.
   *
   * <p>Intended for use in unit tests to inject a mock or alternative implementation.
   */
  @VisibleForTesting
  public void setFirePushService(FirePushService firePushService) {
    this.firePushService = firePushService;
  }

  /**
   * Test hook to replace the ClientManagerResource used by this updater.
   *
   * <p>Intended for use in tests to inject a mock or stub implementation.
   */
  @VisibleForTesting
  public void setClientManagerResource(ClientManagerResource clientManagerResource) {
    this.clientManagerResource = clientManagerResource;
  }
}
