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

import com.alipay.sofa.registry.server.session.providedata.FetchGrayPushSwitchService;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;

public class PushSwitchService {

  @Resource FetchStopPushService fetchStopPushService;

  @Resource FetchGrayPushSwitchService fetchGrayPushSwitchService;

  public PushSwitchService() {}

  public boolean isGlobalPushSwitchStopped() {
    return fetchStopPushService.isStopPushSwitch();
  }

  public boolean canPush() {
    return !fetchStopPushService.isStopPushSwitch()
        || CollectionUtils.isNotEmpty(fetchGrayPushSwitchService.getOpenIps());
  }

  public boolean canIpPush(String ip) {
    return !fetchStopPushService.isStopPushSwitch()
        || fetchGrayPushSwitchService.getOpenIps().contains(ip);
  }

  /**
   * Setter method for property <tt>fetchStopPushService</tt>.
   *
   * @param fetchStopPushService value to be assigned to property fetchStopPushService
   */
  @VisibleForTesting
  public void setFetchStopPushService(FetchStopPushService fetchStopPushService) {
    this.fetchStopPushService = fetchStopPushService;
  }

  /**
   * Setter method for property <tt>fetchGrayPushSwitchService</tt>.
   *
   * @param fetchGrayPushSwitchService value to be assigned to property fetchGrayPushSwitchService
   */
  @VisibleForTesting
  public void setFetchGrayPushSwitchService(FetchGrayPushSwitchService fetchGrayPushSwitchService) {
    this.fetchGrayPushSwitchService = fetchGrayPushSwitchService;
  }

  /**
   * Getter method for property <tt>fetchStopPushService</tt>.
   *
   * @return property value of fetchStopPushService
   */
  @VisibleForTesting
  public FetchStopPushService getFetchStopPushService() {
    return fetchStopPushService;
  }

  /**
   * Getter method for property <tt>fetchGrayPushSwitchService</tt>.
   *
   * @return property value of fetchGrayPushSwitchService
   */
  @VisibleForTesting
  public FetchGrayPushSwitchService getFetchGrayPushSwitchService() {
    return fetchGrayPushSwitchService;
  }
}
