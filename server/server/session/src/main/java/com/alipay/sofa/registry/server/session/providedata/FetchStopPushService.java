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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService.StopPushStorage;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchPersistenceSystemProperty;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.server.shared.util.PersistenceDataParser;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushService.java, v 0.1 2021年05月16日 17:48 xiaojian.xj Exp $
 */
public class FetchStopPushService
    extends AbstractFetchPersistenceSystemProperty<StopPushStorage, StopPushStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchStopPushService.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private ProvideDataRepository provideDataRepository;

  private static final StopPushStorage INIT = new StopPushStorage(INIT_VERSION, true);

  public FetchStopPushService() {
    // default value is stop.push
    super(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, INIT);
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected StopPushStorage fetchFromPersistence() {
    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);

    if (persistenceData == null) {
      return INIT;
    }
    return new StopPushStorage(
        persistenceData.getVersion(), PersistenceDataParser.parse2BoolIgnoreCase(persistenceData, INIT.stopPushSwitch));
  }

  @Override
  protected boolean doProcess(StopPushStorage expect, StopPushStorage update) {

    try {

      if (!compareAndSet(expect, update)) {
        LOGGER.error("[FetchStopPushService]compareAndSet fail, expect={}/{}, update={}/{}",
                expect.getVersion(), expect.stopPushSwitch, update.getVersion(), update.stopPushSwitch);
        return false;
      }
      LOGGER.info(
          "Fetch session stopPushSwitch={}, prev={}, current={}",
          update,
          expect.stopPushSwitch,
          update.stopPushSwitch);

      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch session stopPushSwitch error.", e);
      return false;
    }
  }

  /**
   * Getter method for property <tt>stopPushSwitch</tt>.
   *
   * @return property value of stopPushSwitch
   */
  public boolean isStopPushSwitch() {
    return storage.get().stopPushSwitch;
  }

  protected static class StopPushStorage extends SystemDataStorage {
    protected final boolean stopPushSwitch;

    public StopPushStorage(long version, boolean stopPushSwitch) {
      super(version);
      this.stopPushSwitch = stopPushSwitch;
    }
  }

  /**
   * Setter method for property <tt>stopPushSwitch</tt>.
   *
   * @param stopPushSwitch value to be assigned to property stopPushSwitch
   */
  public void setStopPushSwitch(long version, boolean stopPushSwitch) {
    LOGGER.info(
        "set session stopPushSwitch, version={}, stopPushSwitch={}", version, stopPushSwitch);
    this.storage.set(new StopPushStorage(version, stopPushSwitch));
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   */
  @VisibleForTesting
  protected FetchStopPushService setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }
}
