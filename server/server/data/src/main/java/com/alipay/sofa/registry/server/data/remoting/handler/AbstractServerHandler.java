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
package com.alipay.sofa.registry.server.data.remoting.handler;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author shangyu.wh
 * @version $Id: ServerHandler.java, v 0.1 2017-11-28 18:06 shangyu.wh Exp $
 */
public abstract class AbstractServerHandler<T> implements ChannelHandler<T> {

    @Autowired
    private ThreadPoolExecutor  defaultRequestExecutor;

    private static final Logger LOGGER          = LoggerFactory
                                                    .getLogger(AbstractServerHandler.class);

    private static final Logger LOGGER_CONNECT  = LoggerFactory.getLogger("DATA-CONNECT");

    private static final Logger LOGGER_EXCHANGE = LoggerFactory.getLogger("DATA-EXCHANGE");

    @Override
    public void connected(Channel channel) throws RemotingException {
        if (channel != null && channel.isConnected()) {
            LOGGER_CONNECT.info(getConnectNodeType() + " node connected,remote address:"
                                + channel.getRemoteAddress() + " localAddress:"
                                + channel.getLocalAddress());
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        if (channel != null && !channel.isConnected()) {
            LOGGER_CONNECT.info(getConnectNodeType() + " node disconnected,remote address:"
                                + channel.getRemoteAddress() + " localAddress:"
                                + channel.getLocalAddress());
        }
    }

    protected NodeType getConnectNodeType() {
        return NodeType.DATA;
    }

    @Override
    public void caught(Channel channel, T message, Throwable exception) {

    }

    @Override
    public void received(Channel channel, T message) {

    }

    @Override
    public Object reply(Channel channel, T request) {
        try {
            logRequest(channel, request);
            checkParam(request);

            return doHandle(channel, request);
        } catch (Exception e) {
            LOGGER.error("[{}] handle request failed", getClassName(), e);
            return buildFailedResponse(e.getMessage());
        }
    }

    @Override
    public Class interest() {
        return null;
    }

    /**
     * check params if valid
     *
     * @param request
     * @throws RuntimeException
     */
    public abstract void checkParam(T request) throws RuntimeException;

    /**
     * execute
     *
     * @param request
     * @return
     */
    public abstract Object doHandle(Channel channel, T request);

    /**
     * build failed response
     *
     * @param msg
     * @return
     */
    public abstract Object buildFailedResponse(String msg);

    /**
     * print request
     *
     * @param request
     */
    protected void logRequest(Channel channel, T request) {
        if (channel != null) {
            log(new StringBuilder("Remote:").append(channel.getRemoteAddress()).append(" Request:")
                .append(request).toString());
        } else {
            log(request.toString());
        }
    }

    /**
     * print info log
     *
     * @param log
     */
    protected void log(String log) {
        LOGGER_EXCHANGE.info(new StringBuilder("[").append(getClassName()).append("] ").append(log)
            .toString());
    }

    /**
     * get simple name of this class
     *
     * @return
     */
    private String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Executor getExecutor() {
        return defaultRequestExecutor;
    }
}