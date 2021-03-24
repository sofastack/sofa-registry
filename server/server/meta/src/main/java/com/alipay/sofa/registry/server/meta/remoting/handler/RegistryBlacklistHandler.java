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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryBlacklistRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryBlacklistManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
@Component
public class RegistryBlacklistHandler extends BaseMetaServerHandler<RegistryBlacklistRequest> {

  @Autowired private RegistryBlacklistManager registryBlacklistManager;

  @Autowired private MetaLeaderService metaLeaderService;

  @Override
  public Object doHandle(Channel channel, RegistryBlacklistRequest request) {
    if (!metaLeaderService.amILeader()) {
      buildFailedResponse("not leader");
    }
    DataOperation operation = request.getOperation();
    String ip = request.getIp();
    switch (operation) {
      case ADD:
        registryBlacklistManager.addToBlacklist(ip);
        break;
      case REMOVE:
        registryBlacklistManager.removeFromBlacklist(ip);
        break;
      default:
        break;
    }
    return GenericResponse.buildSuccessResponse();
  }

  @Override
  public Class interest() {
    return RegistryBlacklistRequest.class;
  }
}
