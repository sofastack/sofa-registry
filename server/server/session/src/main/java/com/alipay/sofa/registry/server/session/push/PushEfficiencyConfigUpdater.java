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

  public PushEfficiencyConfigUpdater() {
    this.lock = new ReentrantLock();
    this.stop = false;
  }

  /**
   * ProviderData 中关于推送相关的配置发生了变化
   *
   * @param pushEfficiencyImproveConfig
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

  @Override
  public void start() {}

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

  @Override
  public boolean isRunning() {
    this.lock.lock();
    try {
      return this.stop;
    } finally {
      this.lock.unlock();
    }
  }

  @VisibleForTesting
  public AutoPushEfficiencyRegulator getAutoPushEfficiencyRegulator() {
    return autoPushEfficiencyRegulator;
  }

  @VisibleForTesting
  public void setChangeProcessor(ChangeProcessor changeProcessor) {
    this.changeProcessor = changeProcessor;
  }

  @VisibleForTesting
  public void setPushProcessor(PushProcessor pushProcessor) {
    this.pushProcessor = pushProcessor;
  }

  @VisibleForTesting
  public void setFirePushService(FirePushService firePushService) {
    this.firePushService = firePushService;
  }

  @VisibleForTesting
  public void setClientManagerResource(ClientManagerResource clientManagerResource) {
    this.clientManagerResource = clientManagerResource;
  }
}
