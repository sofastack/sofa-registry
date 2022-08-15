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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.push.TriggerPushContext;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id: DataChangeRequestHandler.java, v 0.1 2017-12-12 15:09 shangyu.wh Exp $
 */
public class DataChangeRequestHandler extends AbstractClientHandler<DataChangeRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeRequestHandler.class);
  /** store subscribers */
  @Autowired Interests sessionInterests;

  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired ExecutorManager executorManager;

  @Autowired FirePushService firePushService;

  @Autowired PushSwitchService pushSwitchService;

  @Autowired CacheService sessionCacheService;

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.DATA;
  }

  @Override
  public Executor getExecutor() {
    return executorManager.getDataChangeRequestExecutor();
  }

  @Override
  public void checkParam(DataChangeRequest request) {
    ParaCheckUtil.checkNotNull(request, "DataChangeRequest");
    ParaCheckUtil.checkNotBlank(request.getDataCenter(), "request.dataCenter");
    ParaCheckUtil.checkNotNull(request.getDataInfoIds(), "request.dataInfoIds");
  }

  @Override
  public Object doHandle(Channel channel, DataChangeRequest dataChangeRequest) {
    if (!pushSwitchService.canLocalDataCenterPush()) {
      return null;
    }
    final String dataNode = RemotingHelper.getRemoteHostAddress(channel);
    final String dataCenter = dataChangeRequest.getDataCenter();
    final long changeTimestamp = System.currentTimeMillis();
    for (Map.Entry<String, DatumVersion> e : dataChangeRequest.getDataInfoIds().entrySet()) {

      final String dataInfoId = e.getKey();
      if (!pushSwitchService.canPushMulti(Collections.singleton(dataCenter))) {
        continue;
      }
      final DatumVersion version = e.getValue();
      Interests.InterestVersionCheck check =
          sessionInterests.checkInterestVersion(dataCenter, dataInfoId, version.getValue());
      if (!check.interested) {
        if (check != Interests.InterestVersionCheck.NoSub) {
          // log exclude NoSub
          LOGGER.info("[SkipChange]{},{}, ver={}, {}", dataInfoId, dataCenter, version, check);
        }
        continue;
      }
      final TriggerPushContext changeCtx =
          new TriggerPushContext(
              dataCenter,
              version.getValue(),
              dataNode,
              changeTimestamp,
              dataChangeRequest.getTimes());
      firePushService.fireOnChange(dataInfoId, changeCtx);
    }
    return null;
  }

  @Override
  public Class interest() {
    return DataChangeRequest.class;
  }
}
