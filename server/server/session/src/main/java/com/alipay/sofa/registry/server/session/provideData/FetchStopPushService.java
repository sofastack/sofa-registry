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
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushService.java, v 0.1 2021年05月16日 17:48 xiaojian.xj Exp $
 */
public class FetchStopPushService extends AbstractFetchSystemPropertyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchStopPushService.class);

  private volatile boolean stopPushSwitch = false;

  @Autowired private Registry sessionRegistry;

  public FetchStopPushService() {
    super(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
  }

  @Override
  protected boolean doProcess(ProvideData data) {

    // push stop switch
    final Boolean stop = ProvideData.toBool(data);
    if (stop == null) {
      LOGGER.info("Fetch session stopPushSwitch content null");
      return false;
    }

    writeLock.lock();
    try {
      boolean prev = stopPushSwitch;
      stopPushSwitch = stop;
      version.set(data.getVersion());

      if (prev && !stop) {
        // prev is stop, now close stop, trigger push
        sessionRegistry.fetchChangDataProcess();
      }
      LOGGER.info(
          "Fetch session stopPushSwitch={}, prev={}, current={}", stop, prev, stopPushSwitch);

      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch session stopPushSwitch error.", e);
      return false;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Getter method for property <tt>stopPushSwitch</tt>.
   *
   * @return property value of stopPushSwitch
   */
  public boolean isStopPushSwitch() {
    readLock.lock();
    try {
      return stopPushSwitch;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Setter method for property <tt>stopPushSwitch</tt>.
   *
   * @param stopPushSwitch value to be assigned to property stopPushSwitch
   */
  @VisibleForTesting
  public void setStopPushSwitch(boolean stopPushSwitch) {
    this.stopPushSwitch = stopPushSwitch;
  }
}
