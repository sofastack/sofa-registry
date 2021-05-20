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

import com.alipay.sofa.registry.core.model.SyncConfigRequest;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.strategy.SyncConfigHandlerStrategy;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigHandler.java, v 0.1 2018-03-14 23:15 zhuoyu.sjw Exp $$
 */
public class SyncConfigHandler extends AbstractClientDataRequestHandler<SyncConfigRequest> {
  @Autowired SyncConfigHandlerStrategy syncConfigHandlerStrategy;

  @Override
  public Object doHandle(Channel channel, SyncConfigRequest request) {
    SyncConfigResponse response = new SyncConfigResponse();
    response.setSuccess(true);
    syncConfigHandlerStrategy.handleSyncConfigResponse(response);
    return response;
  }

  @Override
  public Class interest() {
    return SyncConfigRequest.class;
  }

  @Override
  public Executor getExecutor() {
    return executorManager.getAccessSubExecutor();
  }
}
