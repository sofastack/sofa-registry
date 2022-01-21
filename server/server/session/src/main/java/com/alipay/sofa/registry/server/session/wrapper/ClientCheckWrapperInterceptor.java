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
package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * check connect already existed
 *
 * @author shangyu.wh
 * @version 1.0: ClientCheckWrapperInterceptor.java, v 0.1 2019-06-18 13:53 shangyu.wh Exp $
 */
public class ClientCheckWrapperInterceptor
    implements WrapperInterceptor<RegisterInvokeData, Boolean> {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private Exchange boltExchange;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation)
      throws Exception {

    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo baseInfo = (BaseInfo) registerInvokeData.getStoreData();

    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

    Channel channel = sessionServer.getChannel(baseInfo.getSourceAddress());

    if (channel == null) {
      throw new RequestChannelClosedException(
          String.format("Register address %s  channel closed", baseInfo.getSourceAddress()));
    }
    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return 100;
  }
}
