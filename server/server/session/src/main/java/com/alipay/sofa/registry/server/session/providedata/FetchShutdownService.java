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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchShutdownService.ShutdownStorage;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchPersistenceSystemProperty;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : FetchShutdownService.java, v 0.1 2021年10月14日 17:11 xiaojian.xj Exp $
 */
public class FetchShutdownService
    extends AbstractFetchPersistenceSystemProperty<ShutdownStorage, ShutdownStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchShutdownService.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource private FetchStopPushService fetchStopPushService;

  @Autowired private SessionServerBootstrap sessionServerBootstrap;

  @Autowired private ProvideDataRepository provideDataRepository;

  private static final ShutdownStorage INIT =
      new ShutdownStorage(INIT_VERSION, ShutdownSwitch.defaultSwitch());

  public FetchShutdownService() {
    super(ValueConstants.SHUTDOWN_SWITCH_DATA_ID, INIT);
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected ShutdownStorage fetchFromPersistence() {
    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.SHUTDOWN_SWITCH_DATA_ID);
    if (persistenceData == null) {
      return INIT;
    }
    ShutdownSwitch shutdownSwitch = JsonUtils.read(persistenceData.getData(), ShutdownSwitch.class);
    ShutdownStorage update = new ShutdownStorage(persistenceData.getVersion(), shutdownSwitch);
    return update;
  }

  @Override
  protected boolean doProcess(ShutdownStorage expect, ShutdownStorage update) {
    try {
      if (running2stop(expect.shutdownSwitch, update.shutdownSwitch)) {
        if (!fetchStopPushService.isStopPushSwitch()) {
          LOGGER.error("forbid stop server when pushSwitch is open.");
          return false;
        }
        LOGGER.info(
            "stop server when pushSwitch is close, prev={}, current={}",
            expect.shutdownSwitch,
            update.shutdownSwitch);
        sessionServerBootstrap.destroy();
      }
      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch stop server switch error", e);
    }
    return false;
  }

  private boolean running2stop(ShutdownSwitch current, ShutdownSwitch update) {
    return !current.isShutdown() && update.isShutdown();
  }

  public boolean isShutdown() {
    return storage.get().shutdownSwitch.isShutdown();
  }

  protected static class ShutdownStorage extends SystemDataStorage {

    private ShutdownSwitch shutdownSwitch;

    public ShutdownStorage(long version, ShutdownSwitch shutdownSwitch) {
      super(version);
      this.shutdownSwitch = shutdownSwitch;
    }
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return FetchShutdownService
   */
  @VisibleForTesting
  public FetchShutdownService setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  /**
   * Setter method for property <tt>sessionServerBootstrap</tt>.
   *
   * @param sessionServerBootstrap value to be assigned to property sessionServerBootstrap
   * @return FetchShutdownService
   */
  @VisibleForTesting
  public FetchShutdownService setSessionServerBootstrap(
      SessionServerBootstrap sessionServerBootstrap) {
    this.sessionServerBootstrap = sessionServerBootstrap;
    return this;
  }

  /**
   * Setter method for property <tt>fetchStopPushService</tt>.
   *
   * @param fetchStopPushService value to be assigned to property fetchStopPushService
   * @return FetchShutdownService
   */
  @VisibleForTesting
  public FetchShutdownService setFetchStopPushService(FetchStopPushService fetchStopPushService) {
    this.fetchStopPushService = fetchStopPushService;
    return this;
  }
}
