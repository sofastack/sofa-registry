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
import com.alipay.sofa.registry.server.session.push.ChangeProcessor;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushEfficiencyImproveConfig;
import com.alipay.sofa.registry.server.session.push.PushProcessor;
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

  @Autowired private ChangeProcessor changeProcessor;
  @Autowired private PushProcessor pushProcessor;

  @Autowired private FirePushService firePushService;

  public FetchPushEfficiencyConfigService() {
    super(
        ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
        new SwitchStorage(INIT_VERSION, new PushEfficiencyImproveConfig()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

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
    changeProcessor.setWorkDelayTime(pushEfficiencyImproveConfig);
    pushProcessor.setPushTaskDelayTime(pushEfficiencyImproveConfig);
    if (firePushService.getRegProcessor() != null) {
      firePushService.getRegProcessor().setWorkDelayTime(pushEfficiencyImproveConfig);
    }
    LOGGER.info(
        "Fetch PushEfficiencyImproveConfig success, prev={}, current={}",
        expect.pushEfficiencyImproveConfig,
        pushEfficiencyImproveConfig);
    return true;
  }

  @VisibleForTesting
  public FetchPushEfficiencyConfigService setChangeProcessor(ChangeProcessor changeProcessor) {
    this.changeProcessor = changeProcessor;
    return this;
  }

  @VisibleForTesting
  public FetchPushEfficiencyConfigService setPushProcessor(PushProcessor pushProcessor) {
    this.pushProcessor = pushProcessor;
    return this;
  }

  @VisibleForTesting
  public FetchPushEfficiencyConfigService setFirePushService(FirePushService firePushService) {
    this.firePushService = firePushService;
    return this;
  }

  @VisibleForTesting
  public FetchPushEfficiencyConfigService setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
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
