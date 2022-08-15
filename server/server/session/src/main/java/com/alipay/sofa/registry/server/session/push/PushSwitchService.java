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
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.providedata.FetchGrayPushSwitchService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class PushSwitchService {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource private FetchGrayPushSwitchService fetchGrayPushSwitchService;

  @Autowired private DataCenterMetadataCache dataCenterMetadataCache;

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
      return switchCanPush(sessionServerConfig.getSessionServerDataCenter())
          || CollectionUtils.isNotEmpty(fetchGrayPushSwitchService.getOpenIps());
    }

    return switchCanPush(dataCenter);
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

  private boolean switchCanPush(String dataCenter) {
    Boolean stopPush = dataCenterMetadataCache.isStopPush(dataCenter);
    if (stopPush == null || stopPush) {
      return false;
    }
    return true;
  }

  private boolean dataCenterAndIpCanPush(String dataCenter, String ip) {
    Boolean stopPush = dataCenterMetadataCache.isStopPush(dataCenter);
    if (stopPush == null || stopPush) {
      return sessionServerConfig.isLocalDataCenter(dataCenter)
          && fetchGrayPushSwitchService.getOpenIps().contains(ip);
    }
    return true;
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
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
   */
  @VisibleForTesting
  public PushSwitchService setFetchGrayPushSwitchService(
      FetchGrayPushSwitchService fetchGrayPushSwitchService) {
    this.fetchGrayPushSwitchService = fetchGrayPushSwitchService;
    return this;
  }

  /**
   * Setter method for property <tt>dataCenterMetadataCache</tt>.
   *
   * @param dataCenterMetadataCache value to be assigned to property dataCenterMetadataCache
   */
  @VisibleForTesting
  public PushSwitchService setDataCenterMetadataCache(
      DataCenterMetadataCache dataCenterMetadataCache) {
    this.dataCenterMetadataCache = dataCenterMetadataCache;
    return this;
  }

  /**
   * Getter method for property <tt>dataCenterMetadataCache</tt>.
   *
   * @return property value of dataCenterMetadataCache
   */
  @VisibleForTesting
  public DataCenterMetadataCache getDataCenterMetadataCache() {
    return dataCenterMetadataCache;
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
