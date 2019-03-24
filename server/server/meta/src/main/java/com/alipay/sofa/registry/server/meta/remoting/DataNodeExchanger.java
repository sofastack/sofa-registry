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
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNodeExchanger.java, v 0.1 2018-01-23 19:18 shangyu.wh Exp $
 */
public class DataNodeExchanger implements NodeExchanger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataNodeExchanger.class);

    @Autowired
    private MetaServerConfig    metaServerConfig;

    @Autowired
    private Exchange            boltExchange;

    @Override
    public Response request(Request request) throws RequestException {
        Response response = null;
        try {
            Server dataServer = boltExchange.getServer(metaServerConfig.getDataServerPort());

            if (dataServer != null) {
                URL url = request.getRequestUrl();
                if (url != null) {

                    Channel channel = dataServer.getChannel(url);

                    if (channel != null && channel.isConnected()) {
                        final Object result = dataServer.sendSync(channel, request.getRequestBody(),
                                metaServerConfig.getDataNodeExchangeTimeout());
                        response = () -> result;
                    } else {
                        LOGGER.error("DataNode Exchanger get channel error! channel with url:" + url
                                + " can not be null or disconnected!");
                        throw new RequestException(
                                "DataNode Exchanger get channel error! channel with url:" + url
                                        + " can not be null or disconnected!",
                                request);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("DataNode Exchanger request data error!", e);
            throw new RequestException("DataNode Exchanger request data error!", request, e);
        }
        return response;
    }

    @Override
    public Client connectServer() {
        return null;
    }
}