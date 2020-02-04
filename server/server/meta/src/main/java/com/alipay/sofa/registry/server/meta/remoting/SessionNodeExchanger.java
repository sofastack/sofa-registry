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
package com.alipay.sofa.registry.server.meta.remoting;

import org.springframework.beans.factory.annotation.Autowired;

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
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionNodeExchanger.java, v 0.1 2018-01-15 21:21 shangyu.wh Exp $
 */
public class SessionNodeExchanger implements NodeExchanger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionNodeExchanger.class);

    @Autowired
    private Exchange            boltExchange;

    @Autowired
    private MetaServerConfig    metaServerConfig;

    @Override
    public Response request(Request request) throws RequestException {
        Response response = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SessionNodeExchanger request body:{} url:{}", request.getRequestBody(),
                    request.getRequestUrl());
        }

        Server sessionServer = boltExchange.getServer(metaServerConfig.getSessionServerPort());

        if (sessionServer != null) {

            Channel channel = sessionServer.getChannel(request.getRequestUrl());
            if (channel != null && channel.isConnected()) {
                final Object result = sessionServer.sendSync(channel, request.getRequestBody(),
                        request.getTimeout() != null ? request.getTimeout() : metaServerConfig.getSessionNodeExchangeTimeout());
                response = () -> result;
            } else {
                String errorMsg = "SessionNode Exchanger get channel error! channel with url:"
                        + channel == null ? "" : channel.getRemoteAddress() + " can not be null or disconnected!";
                LOGGER.error(errorMsg);
                throw new RequestException(errorMsg, request);
            }

        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SessionNodeExchanger response result:{} ", response.getResult());
        }
        return response;
    }

    @Override
    public Client connectServer() {
        return null;
    }
}