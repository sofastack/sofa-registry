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
import com.alipay.sofa.registry.common.model.sessionserver.ClientOnRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public final class ClientOnRequestHandler extends AbstractConsoleHandler<ClientOnRequest> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientOnRequestHandler.class);

  @Autowired protected ConnectionsService connectionsService;

  @Override
  public Object doHandle(Channel channel, ClientOnRequest request) {
    List<String> conIds = connectionsService.closeIpConnects(request.getIps());
    LOGGER.info("clientOn conIds: {}", conIds);
    return CommonResponse.buildSuccessResponse();
  }

  @Override
  public Class interest() {
    return ClientOnRequest.class;
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }
}
