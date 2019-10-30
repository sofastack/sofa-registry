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

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.WatcherHandlerStrategy;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberHandler.java, v 0.1 2017-11-30 15:01 shangyu.wh Exp $
 */
public class WatcherHandler extends AbstractServerHandler {
    @Autowired
    private ExecutorManager        executorManager;

    @Autowired
    private WatcherHandlerStrategy watcherHandlerStrategy;

    @Override
    public Object reply(Channel channel, Object message) {
        RegisterResponse result = new RegisterResponse();
        ConfiguratorRegister configuratorRegister = (ConfiguratorRegister) message;
        watcherHandlerStrategy.handleConfiguratorRegister(channel, configuratorRegister, result);
        return result;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return ConfiguratorRegister.class;
    }

    @Override
    public Executor getExecutor() {
        return executorManager.getAccessDataExecutor();
    }

}