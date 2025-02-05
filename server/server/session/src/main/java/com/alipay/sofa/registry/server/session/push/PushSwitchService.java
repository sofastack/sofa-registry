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

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.server.session.providedata.FetchGrayPushSwitchService;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class PushSwitchService {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource private FetchGrayPushSwitchService fetchGrayPushSwitchService;

  @Resource private FetchStopPushService fetchStopPushService;

  @Autowired private MetadataCacheRegistry metadataCacheRegistry;

  public boolean canPushMulti(Set<String> dataCenters) {
    ParaCheckUtil.checkNotEmpty(dataCenters, "push.dataCenters");
    for (String dataCenter : dataCenters) {
      if (!dataCenterCanPush(dataCenter)) {
        return false;
      }
    }
    return true;
  }

  private boolean dataCenterCanPush(String dataCenter) {
    if (sessionServerConfig.isLocalDataCenter(dataCenter)) {
      return pushEnable(sessionServerConfig.getSessionServerDataCenter())
          || CollectionUtils.isNotEmpty(fetchGrayPushSwitchService.getOpenIps());
    }

    return pushEnable(dataCenter);
  }

  public boolean canLocalDataCenterPush() {
    return dataCenterCanPush(sessionServerConfig.getSessionServerDataCenter());
  }

  public boolean canIpPushMulti(String ip, Set<String> dataCenters) {
    ParaCheckUtil.checkNotBlank(ip, "push.ip");
    ParaCheckUtil.checkNotEmpty(dataCenters, "push.dataCenters");

    for (String dataCenter : dataCenters) {
      if (!dataCenterAndIpCanPush(dataCenter, ip)) {
        return false;
      }
    }
    return true;
  }

  public boolean canIpPushLocal(String ip) {
    ParaCheckUtil.checkNotBlank(ip, "push.ip");
    return dataCenterAndIpCanPush(sessionServerConfig.getSessionServerDataCenter(), ip);
  }

  private boolean pushEnable(String dataCenter) {
    if (sessionServerConfig.isLocalDataCenter(dataCenter)) {
      return !fetchStopPushService.isStopPushSwitch()
          || CollectionUtils.isNotEmpty(fetchGrayPushSwitchService.getOpenIps());
    }

    return metadataCacheRegistry.getPushEnableDataCenters().contains(dataCenter);
  }

  private boolean dataCenterAndIpCanPush(String dataCenter, String ip) {

    if (sessionServerConfig.isLocalDataCenter(dataCenter)) {
      return !fetchStopPushService.isStopPushSwitch()
          || fetchGrayPushSwitchService.getOpenIps().contains(ip);
    }

    return metadataCacheRegistry.getPushEnableDataCenters().contains(dataCenter);
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return PushSwitchService
   */
  @VisibleForTesting
  public PushSwitchService setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  /**
   * Setter method for property <tt>fetchGrayPushSwitchService</tt>.
   *
   * @param fetchGrayPushSwitchService value to be assigned to property fetchGrayPushSwitchService
   * @return PushSwitchService
   */
  @VisibleForTesting
  public PushSwitchService setFetchGrayPushSwitchService(
      FetchGrayPushSwitchService fetchGrayPushSwitchService) {
    this.fetchGrayPushSwitchService = fetchGrayPushSwitchService;
    return this;
  }

  /**
   * Setter method for property <tt>fetchStopPushService</tt>.
   *
   * @param fetchStopPushService value to be assigned to property fetchStopPushService
   * @return PushSwitchService
   */
  @VisibleForTesting
  public PushSwitchService setFetchStopPushService(FetchStopPushService fetchStopPushService) {
    this.fetchStopPushService = fetchStopPushService;
    return this;
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
   * Setter method for property <tt>metadataCacheRegistry</tt>.
   *
   * @param metadataCacheRegistry value to be assigned to property metadataCacheRegistry
   * @return PushSwitchService
   */
  @VisibleForTesting
  public PushSwitchService setMetadataCacheRegistry(MetadataCacheRegistry metadataCacheRegistry) {
    this.metadataCacheRegistry = metadataCacheRegistry;
    return this;
  }
}
