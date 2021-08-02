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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerQueryRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerResp;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;
import java.util.Set;
import javax.annotation.Resource;

/**
 * @author xiaojian.xj
 * @version : GetClientManagerRequestHandler.java, v 0.1 2021年07月31日 17:31 xiaojian.xj Exp $
 */
public class GetClientManagerRequestHandler
    extends AbstractConsoleHandler<ClientManagerQueryRequest> {

  @Resource private FetchClientOffAddressService fetchClientOffAddressService;

  @Override
  public GenericResponse<ClientManagerResp> doHandle(
      Channel channel, ClientManagerQueryRequest request) {
    Set<String> clientOffAddress = fetchClientOffAddressService.getClientOffAddress();
    return new GenericResponse<ClientManagerResp>()
        .fillSucceed(new ClientManagerResp(true, clientOffAddress));
  }

  @Override
  public Class interest() {
    return ClientManagerQueryRequest.class;
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse<ClientManagerResp>().fillFailed(msg);
  }
}
