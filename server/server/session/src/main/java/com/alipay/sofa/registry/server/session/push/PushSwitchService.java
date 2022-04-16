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
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class PushSwitchService {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource private FetchGrayPushSwitchService fetchGrayPushSwitchService;

  @Autowired private DataCenterMetadataCache dataCenterMetadataCache;

  public boolean canPushMulti(String dataInfoId, Set<String> dataCenters) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "push.dataInfoId");
    ParaCheckUtil.checkNotEmpty(dataCenters, "push.dataCenters");
    for (String dataCenter : dataCenters) {
      if (!dataCenterCanPush(dataCenter)) {
        return false;
      }
    }
    return true;
  }

  public boolean canLocalDataCenterPush() {
    return dataCenterCanPush(sessionServerConfig.getSessionServerDataCenter())
        || CollectionUtils.isNotEmpty(fetchGrayPushSwitchService.getOpenIps());
  }

  public boolean canIpPushMulti(String ip, String dataInfoId, Set<String> dataCenters) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "push.dataInfoId");
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

  private boolean dataCenterCanPush(String dataCenter) {
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
}
