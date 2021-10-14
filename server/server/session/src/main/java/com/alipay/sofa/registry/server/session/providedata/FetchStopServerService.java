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

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchStopServerService.StopServerStorage;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : FetchStopServerService.java, v 0.1 2021年10月14日 17:11 xiaojian.xj Exp $
 */
public class FetchStopServerService extends AbstractFetchSystemPropertyService<StopServerStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchStopServerService.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private FetchStopPushService fetchStopPushService;

  @Autowired private SessionServerBootstrap sessionServerBootstrap;

  public FetchStopServerService() {
    super(
        ValueConstants.STOP_SERVER_SWITCH_DATA_ID,
        new StopServerStorage(INIT_VERSION, StopServerSwitch.defaultSwitch()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(StopServerStorage expect, ProvideData data) {
    final String value = ProvideData.toString(data);
    if (StringUtil.isBlank(value)) {
      LOGGER.info("Fetch stop server switch content empty");
      return true;
    }
    try {
      StopServerSwitch stopServerSwitch = JsonUtils.read(value, StopServerSwitch.class);
      StopServerStorage update = new StopServerStorage(data.getVersion(), stopServerSwitch);

      if (running2stop(expect.stopServerSwitch, update.stopServerSwitch)) {
        if (!fetchStopPushService.isStopPushSwitch()) {
          LOGGER.error("forbid stop server when pushSwitch is open.");
          return false;
        }
        LOGGER.info(
            "stop server when pushSwitch is close, prev={}, current={}",
            expect.stopServerSwitch,
            value);
        sessionServerBootstrap.destroy();
      }
      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch stop server switch error", e);
    }
    return false;
  }

  private boolean running2stop(StopServerSwitch current, StopServerSwitch update) {
    return !current.isStopServer() && update.isStopServer();
  }

  public boolean isStopServer() {
    return storage.get().stopServerSwitch.isStopServer();
  }

  protected static class StopServerStorage extends SystemDataStorage {

    private StopServerSwitch stopServerSwitch;

    public StopServerStorage(long version, StopServerSwitch stopServerSwitch) {
      super(version);
      this.stopServerSwitch = stopServerSwitch;
    }
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   */
  @VisibleForTesting
  public FetchStopServerService setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  /**
   * Setter method for property <tt>sessionServerBootstrap</tt>.
   *
   * @param sessionServerBootstrap value to be assigned to property sessionServerBootstrap
   */
  @VisibleForTesting
  public FetchStopServerService setSessionServerBootstrap(
      SessionServerBootstrap sessionServerBootstrap) {
    this.sessionServerBootstrap = sessionServerBootstrap;
    return this;
  }

  /**
   * Setter method for property <tt>fetchStopPushService</tt>.
   *
   * @param fetchStopPushService value to be assigned to property fetchStopPushService
   */
  @VisibleForTesting
  public FetchStopServerService setFetchStopPushService(FetchStopPushService fetchStopPushService) {
    this.fetchStopPushService = fetchStopPushService;
    return this;
  }
}
