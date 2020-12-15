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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class AppRevisionRegisterHandler extends AbstractServerHandler<AppRevisionRegister> {

    @Autowired
    private AppRevisionHandlerStrategy appRevisionHandlerStrategy;

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.CLIENT;
    }

    @Override
    public Object doHandle(Channel channel, AppRevisionRegister request) {
        RegisterResponse result = new RegisterResponse();
        appRevisionHandlerStrategy.handleAppRevisionRegister(request, result);
        return result;
    }

    @Override
    public Class interest() {
        return AppRevisionRegister.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

}
