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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.push.PushEfficiencyConfigUpdater;
import com.alipay.sofa.registry.server.session.push.PushEfficiencyImproveConfig;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/2/21
 */
public class FetchPushEfficiencyConfigService
    extends AbstractFetchSystemPropertyService<FetchPushEfficiencyConfigService.SwitchStorage> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FetchPushEfficiencyConfigService.class);
  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater;

  /**
   * Creates a FetchPushEfficiencyConfigService configured to fetch the push-efficiency
   * improvement configuration from provider data.
   *
   * Initializes the underlying AbstractFetchSystemPropertyService with the data id
   * for push task delay config and an initial SwitchStorage containing the initial
   * version and a default PushEfficiencyImproveConfig instance.
   */
  public FetchPushEfficiencyConfigService() {
    super(
        ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
        new SwitchStorage(INIT_VERSION, new PushEfficiencyImproveConfig()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  /**
   * Processes provider data to update the PushEfficiencyImproveConfig if valid.
   *
   * <p>Deserializes provider {@code data} into a {@link PushEfficiencyImproveConfig}, validates it,
   * and, if applicable, atomically replaces the expected storage and applies the new configuration
   * via {@code pushEfficiencyConfigUpdater}.
   *
   * @param expect the current expected {@link SwitchStorage} used for compare-and-set
   * @param data provider data containing the config JSON
   * @return {@code true} if processing succeeded (including the no-op case when the provided config
   *     string is blank); {@code false} on deserialization error, validation failure, out-of-date
   *     storage (compare-and-set failed), or when the config indicates it should not be applied
   *     (e.g., {@code inIpZoneSBF()} is false)
   */
  @Override
  protected boolean doProcess(SwitchStorage expect, ProvideData data) {
    final String configString = ProvideData.toString(data);

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    if (StringUtils.isBlank(configString)) {
      return true;
    }
    try {
      pushEfficiencyImproveConfig = JsonUtils.read(configString, PushEfficiencyImproveConfig.class);
    } catch (Throwable e) {
      LOGGER.error("Decode PushEfficiencyImproveConfig failed", e);
      return false;
    }
    if (pushEfficiencyImproveConfig == null || !pushEfficiencyImproveConfig.validate()) {
      LOGGER.error(
          "Fetch PushEfficiencyImproveConfig invalid, value={}", pushEfficiencyImproveConfig);
      return false;
    }
    pushEfficiencyImproveConfig.setSessionServerConfig(sessionServerConfig);
    if (!pushEfficiencyImproveConfig.inIpZoneSBF()) {
      return false;
    }

    SwitchStorage update = new SwitchStorage(data.getVersion(), pushEfficiencyImproveConfig);
    if (!compareAndSet(expect, update)) {
      return false;
    }

    this.pushEfficiencyConfigUpdater.updateFromProviderData(pushEfficiencyImproveConfig);

    LOGGER.info(
        "Fetch PushEfficiencyImproveConfig success, prev={}, current={}",
        expect.pushEfficiencyImproveConfig,
        pushEfficiencyImproveConfig);
    return true;
  }

  /**
   * Injects a SessionServerConfig instance into this service (primarily for testing).
   *
   * <p>Returns the service instance to allow fluent/chainable setup in tests.
   *
   * @return this FetchPushEfficiencyConfigService instance
   */
  @VisibleForTesting
  public FetchPushEfficiencyConfigService setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  /**
   * Sets the PushEfficiencyConfigUpdater instance (typically used for testing) and returns this service for chaining.
   *
   * @return this FetchPushEfficiencyConfigService instance
   */
  @VisibleForTesting
  public FetchPushEfficiencyConfigService setPushEfficiencyConfigUpdater(
      PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater) {
    this.pushEfficiencyConfigUpdater = pushEfficiencyConfigUpdater;
    return this;
  }

  protected static class SwitchStorage extends SystemDataStorage {
    protected final PushEfficiencyImproveConfig pushEfficiencyImproveConfig;

    public SwitchStorage(long version, PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
      super(version);
      this.pushEfficiencyImproveConfig = pushEfficiencyImproveConfig;
    }
  }
}
