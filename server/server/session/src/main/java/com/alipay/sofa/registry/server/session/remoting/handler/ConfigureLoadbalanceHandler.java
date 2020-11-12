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
import com.alipay.sofa.registry.common.model.metaserver.ConfigureLoadbalanceRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiangxu
 * @version : ConfigureLoadbalanceHandler.java, v 0.1 2020年05月29日 11:51 上午 xiangxu Exp $
 */
public class ConfigureLoadbalanceHandler extends AbstractClientHandler<ConfigureLoadbalanceRequest> {

    @Autowired
    private ConnectionsService connectionsService;

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.META;
    }

    @Override
    public Class interest() {
        return ConfigureLoadbalanceRequest.class;
    }

    @Override
    public void checkParam(ConfigureLoadbalanceRequest request) throws RuntimeException {
        ParaCheckUtil.checkNonNegative(request.getMaxConnections(), "request.maxConnections");
    }


    @Override
    public Object doHandle(Channel channel, ConfigureLoadbalanceRequest request) {
        connectionsService.setMaxConnections(request.getMaxConnections());
        return null;
    }
}