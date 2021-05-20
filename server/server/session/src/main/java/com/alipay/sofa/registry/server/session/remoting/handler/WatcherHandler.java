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

import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.strategy.WatcherHandlerStrategy;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: SubscriberHandler.java, v 0.1 2017-11-30 15:01 shangyu.wh Exp $
 */
public class WatcherHandler extends AbstractClientDataRequestHandler<ConfiguratorRegister> {

  @Autowired WatcherHandlerStrategy watcherHandlerStrategy;

  @Override
  public Object doHandle(Channel channel, ConfiguratorRegister message) {
    RegisterResponse result = new RegisterResponse();
    watcherHandlerStrategy.handleConfiguratorRegister(channel, message, result);
    return result;
  }

  @Override
  public Class interest() {
    return ConfiguratorRegister.class;
  }

  @Override
  public Executor getExecutor() {
    return executorManager.getAccessSubExecutor();
  }
}
