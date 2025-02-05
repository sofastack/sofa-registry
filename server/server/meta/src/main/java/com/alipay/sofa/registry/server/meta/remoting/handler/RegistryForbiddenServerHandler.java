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
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryForbiddenServerRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryForbiddenServerManager;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
public class RegistryForbiddenServerHandler
    extends BaseMetaServerHandler<RegistryForbiddenServerRequest> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RegistryForbiddenServerHandler.class);

  @Autowired private RegistryForbiddenServerManager registryForbiddenServerManager;

  @Autowired private MetaLeaderService metaLeaderService;

  @Override
  public void checkParam(RegistryForbiddenServerRequest request) {
    ParaCheckUtil.checkNotBlank(request.getCell(), "cell");
    ParaCheckUtil.checkNotBlank(request.getIp(), "ip");
  }

  @Override
  public Object doHandle(Channel channel, RegistryForbiddenServerRequest request) {
    LOGGER.info(
        "[doHandle] from {}, request: {}", channel.getRemoteAddress().getHostName(), request);
    if (!metaLeaderService.amILeader()) {
      buildFailedResponse("not leader");
    }
    DataOperation operation = request.getOperation();
    String ip = request.getIp();

    boolean success = false;
    switch (operation) {
      case ADD:
        success = registryForbiddenServerManager.addToBlacklist(request);
        break;
      case REMOVE:
        success = registryForbiddenServerManager.removeFromBlacklist(request);
        break;
      default:
        break;
    }

    if (success) {
      return GenericResponse.buildSuccessResponse();
    }

    LOGGER.error("forbidden server: {} operation:{} fail.", ip, operation);
    return GenericResponse.buildFailedResponse("handle forbidden server fail.");
  }

  @Override
  public Class interest() {
    return RegistryForbiddenServerRequest.class;
  }
}
