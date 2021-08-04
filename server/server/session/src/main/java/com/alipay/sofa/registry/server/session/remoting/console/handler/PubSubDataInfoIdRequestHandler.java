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
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdRequest;
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdResp;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.store.FetchPubSubDataInfoIdService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : PubSubDataInfoIdRequestHandler.java, v 0.1 2021年08月04日 14:23 xiaojian.xj Exp $
 */
public class PubSubDataInfoIdRequestHandler
    extends AbstractConsoleHandler<PubSubDataInfoIdRequest> {

  @Autowired protected FetchPubSubDataInfoIdService fetchPubSubDataInfoIdService;

  @Override
  public Class interest() {
    return PubSubDataInfoIdRequest.class;
  }

  @Override
  public GenericResponse<PubSubDataInfoIdResp> doHandle(
      Channel channel, PubSubDataInfoIdRequest request) {
    PubSubDataInfoIdResp pubSubDataInfoIdResp =
        fetchPubSubDataInfoIdService.queryByIps(request.getIps());
    return new GenericResponse<PubSubDataInfoIdResp>().fillSucceed(pubSubDataInfoIdResp);
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse<PubSubDataInfoIdResp>().fillFailed(msg);
  }
}
