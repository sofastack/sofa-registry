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
package com.alipay.sofa.registry.server.shared.providedata;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.util.PersistenceDataParser;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseStopPushService
    extends AbstractFetchPersistenceSystemProperty<
        BaseStopPushService.StopPushStorage, BaseStopPushService.StopPushStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseStopPushService.class);

  @Autowired private ProvideDataRepository provideDataRepository;

  private static final StopPushStorage INIT = new StopPushStorage(INIT_VERSION, true);

  public BaseStopPushService() {
    // default value is stop.push
    super(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, INIT);
  }

  @Override
  protected StopPushStorage fetchFromPersistence() {
    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);

    if (persistenceData == null) {
      return INIT;
    }
    return new StopPushStorage(
        persistenceData.getVersion(),
        PersistenceDataParser.parse2BoolIgnoreCase(persistenceData, INIT.stopPushSwitch));
  }

  @Override
  protected boolean doProcess(StopPushStorage expect, StopPushStorage update) {
    try {

      if (!compareAndSet(expect, update)) {
        LOGGER.error(
            "[FetchStopPushService]compareAndSet fail, expect={}/{}, update={}/{}",
            expect.getVersion(),
            expect.stopPushSwitch,
            update.getVersion(),
            update.stopPushSwitch);
        return false;
      }

      afterProcess(this.storage.get());
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

    public boolean isStopPush() {
      return stopPushSwitch;
    }
  }

  protected void afterProcess(StopPushStorage storage) {}

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
}
