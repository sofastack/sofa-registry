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
package com.alipay.sofa.registry.server.session.remoting;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.exchange.message.Response.ResultStatus;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;

/**
 *
 * @author shangyu.wh
 * @version $Id: ClientNodeExchanger.java, v 0.1 2017-12-12 12:13 shangyu.wh Exp $
 */
public class ClientNodeExchanger implements NodeExchanger {
    private static final Logger LOGGER = LoggerFactory.getLogger("SESSION-PUSH", "[Exchange]");

    @Autowired
    private Exchange            boltExchange;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Override
    public Response request(Request request) throws RequestException {
        Response response = null;
        try {
            //sender who session node send message to client node
            Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

            if (sessionServer != null) {
                URL url = request.getRequestUrl();
                if (url != null) {
                    Channel channel = sessionServer.getChannel(url);

                    if (channel != null && channel.isConnected()) {
                        if (null != request.getCallBackHandler()) {
                            //TODO log ASYNC
                            sessionServer.sendCallback(channel, request.getRequestBody(),
                                    request.getCallBackHandler(),
                                    request.getTimeout() != null ? request.getTimeout() : sessionServerConfig.getClientNodeExchangeTimeOut());
                            response = () -> ResultStatus.SUCCESSFUL;
                        } else {
                            final Object result = sessionServer.sendSync(channel,
                                    request.getRequestBody(),
                                    request.getTimeout() != null ? request.getTimeout() : sessionServerConfig.getClientNodeExchangeTimeOut());
                            response = () -> result;
                        }
                    } else {
                        LOGGER.error(
                                "ClientNode Exchanger get channel {} error! Can't be null or disconnected!",
                                url);
                        throw new RequestException("ClientNode Exchanger get channel " + url
                                + "error! Can't be null or disconnected!",
                                request);
                    }
                }
            }
        } catch (Exception e) {
            throw new RequestException("ClientNode Exchanger request data error!", request, e);
        }
        return response;
    }

    @Override
    public Client connectServer() {
        return null;
    }
}