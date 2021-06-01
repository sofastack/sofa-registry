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
package com.alipay.sofa.registry.server.session.provideData;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.provideData.FetchStopPushService.StopPushStorage;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushService.java, v 0.1 2021年05月16日 17:48 xiaojian.xj Exp $
 */
public class FetchStopPushService extends AbstractFetchSystemPropertyService<StopPushStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchStopPushService.class);

  @Autowired private Registry sessionRegistry;

  public FetchStopPushService() {
    super(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    storage.set(new StopPushStorage(INIT_VERSION, false));
  }

  @Override
  protected boolean doProcess(StopPushStorage expect, ProvideData data) {

    // push stop switch
    final Boolean stop = ProvideData.toBool(data);
    if (stop == null) {
      LOGGER.info("Fetch session stopPushSwitch content null");
      return false;
    }

    try {
      StopPushStorage update = new StopPushStorage(data.getVersion(), stop);

      if (!compareAndSet(expect, update)) {
        return false;
      }

      if (expect.stopPushSwitch && !update.stopPushSwitch) {
        // prev is stop, now close stop, trigger push
        sessionRegistry.fetchChangDataProcess();
      }
      LOGGER.info(
          "Fetch session stopPushSwitch={}, prev={}, current={}",
          stop,
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

  protected class StopPushStorage extends AbstractFetchSystemPropertyService.SystemDataStorage {
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
  @VisibleForTesting
  public void setStopPushSwitch(long version, boolean stopPushSwitch) {
    this.storage.set(new StopPushStorage(version, stopPushSwitch));
  }
}
