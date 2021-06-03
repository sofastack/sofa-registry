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
package com.alipay.sofa.registry.server.session.remoting.console.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.sessionserver.ClientOffRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.google.common.collect.Sets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public final class ClientOffRequestHandler extends AbstractConsoleHandler<ClientOffRequest> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientOffRequestHandler.class);

  @Autowired protected ConnectionsService connectionsService;
  @Autowired protected SessionRegistry sessionRegistry;

  @Override
  public Object doHandle(Channel channel, ClientOffRequest request) {
    List<ConnectId> conIds = connectionsService.getIpConnects(Sets.newHashSet(request.getIps()));
    sessionRegistry.clientOff(conIds);
    LOGGER.info("clientOff conIds: {}", conIds);
    return CommonResponse.buildSuccessResponse();
  }

  @Override
  public Class interest() {
    return ClientOffRequest.class;
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }
}
