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
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: CancelHandler.java, v 0.1 2017-11-30 15:29 shangyu.wh Exp $
 */
public class CancelAddressRequestHandler extends AbstractServerHandler<CancelAddressRequest> {

  @Autowired private Registry sessionRegistry;

  @Override
  public Class interest() {
    return CancelAddressRequest.class;
  }

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.CLIENT;
  }

  @Override
  public void checkParam(CancelAddressRequest request) {
    ParaCheckUtil.checkNotEmpty(request.getConnectIds(), "request.connectIds");
  }

  @Override
  public Object doHandle(Channel channel, CancelAddressRequest request) {
    sessionRegistry.cancel(request.getConnectIds());
    return Result.success();
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return Result.failed(msg);
  }
}
