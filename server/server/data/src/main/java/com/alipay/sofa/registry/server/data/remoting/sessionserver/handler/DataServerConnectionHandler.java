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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.shared.remoting.ListenServerChannelHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Connection handler for session connect data server
 *
 * fix shangyu.wh
 * @author xuanbei
 * @since 2019/2/15
 */
public class DataServerConnectionHandler extends ListenServerChannelHandler {
    @Autowired
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    @Override
    public void disconnected(Channel channel) {
        super.disconnected(channel);
        sessionServerConnectionFactory.sessionDisconnected(channel.getRemoteAddress());
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return NodeType.SESSION;
    }
}
