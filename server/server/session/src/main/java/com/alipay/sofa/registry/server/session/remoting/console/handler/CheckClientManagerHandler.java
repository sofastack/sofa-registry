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
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerRequest;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.client.manager.CheckClientManagerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : CheckClientManagerHandler.java, v 0.1 2022年01月04日 20:58 xiaojian.xj Exp $
 */
public class CheckClientManagerHandler extends AbstractConsoleHandler<CheckClientManagerRequest> {

  @Autowired private CheckClientManagerService checkClientManagerService;

  @Override
  public Class interest() {
    return CheckClientManagerRequest.class;
  }

  @Override
  public GenericResponse<CheckClientManagerResponse> doHandle(
      Channel channel, CheckClientManagerRequest request) {
    Tuple<Boolean, Long> ret =
        checkClientManagerService.checkLocalCache(request.getExpectedVersion());
    GenericResponse<CheckClientManagerResponse> response = new GenericResponse();
    response.fillSucceed(new CheckClientManagerResponse(ret.o1, ret.o2));
    return response;
  }
}
