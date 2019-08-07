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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;

/**
 *
 * @author shangyu.wh
 * @version $Id: ClientHandler.java, v 0.1 2017-11-28 18:06 shangyu.wh Exp $
 */
public abstract class AbstractClientHandler<T> implements ChannelHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger("SESSION-CONNECT");

    @Override
    public void connected(Channel channel) throws RemotingException {
        if (channel != null && channel.isConnected()) {
            LOGGER
                .info(getConnectNodeType() + " node connected,remote address:"
                      + channel.getRemoteAddress() + " localAddress:" + channel.getLocalAddress());
        }
    }

    @Override
    public void disconnected(Channel channel) {
        if (channel != null && !channel.isConnected()) {
            LOGGER
                .info(getConnectNodeType() + " node disconnected,remote address:"
                      + channel.getRemoteAddress() + " localAddress:" + channel.getLocalAddress());
        }
    }

    protected abstract NodeType getConnectNodeType();

    @Override
    public void caught(Channel channel, T message, Throwable exception) {

    }

    @Override
    public void received(Channel channel, T message) {

    }

    @Override
    public Object reply(Channel channel, T message) {
        return null;
    }

    @Override
    public Class interest() {
        return null;
    }
}