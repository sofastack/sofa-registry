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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DataSyncServerConnectionHandler extends AbstractServerHandler {
    @Autowired
    private DataServerConnectionFactory dataServerConnectionFactory;

    @Override
    public ChannelHandler.HandlerType getType() {
        return ChannelHandler.HandlerType.LISENTER;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        super.connected(channel);
        dataServerConnectionFactory.register(((BoltChannel) channel).getConnection());
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        super.disconnected(channel);
        dataServerConnectionFactory.remove(((BoltChannel) channel).getConnection());
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

    @Override
    public void checkParam(Object request) throws RuntimeException {

    }

    @Override
    public Object doHandle(Channel channel, Object request) {
        return null;
    }

    @Override
    public Object buildFailedResponse(String msg) {
        return null;
    }
}
