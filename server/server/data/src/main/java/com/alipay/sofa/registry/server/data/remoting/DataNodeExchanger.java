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
package com.alipay.sofa.registry.server.data.remoting;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DataNodeExchanger implements NodeExchanger {
    private static final Logger               LOGGER = LoggerFactory
                                                         .getLogger(DataNodeExchanger.class);

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private DataServerConfig                  dataServerConfig;

    @Resource(name = "dataClientHandlers")
    private Collection<AbstractClientHandler> dataClientHandlers;

    @Override
    public Response request(Request request) {
        Client client = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);
        LOGGER.info("DataNode Exchanger request={},url={},callbackHandler={}", request.getRequestBody(),
                request.getRequestUrl(), request.getCallBackHandler());

        if (null != request.getCallBackHandler()) {
            client.sendCallback(request.getRequestUrl(), request.getRequestBody(),
                    request.getCallBackHandler(),
                    dataServerConfig.getRpcTimeout());
            return () -> Response.ResultStatus.SUCCESSFUL;
        } else {
            final Object result = client.sendSync(request.getRequestUrl(), request.getRequestBody(),
                    dataServerConfig.getRpcTimeout());
            return () -> result;
        }
    }

    public Channel connect(URL url) {
        Client client = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);
        if (client == null) {
            synchronized (this) {
                client = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);
                if (client == null) {
                    client = boltExchange.connect(Exchange.DATA_SERVER_TYPE, url,
                        dataClientHandlers.toArray(new ChannelHandler[dataClientHandlers.size()]));
                }
            }
        }

        Channel channel = client.getChannel(url);
        if (channel == null) {
            synchronized (this) {
                channel = client.getChannel(url);
                if (channel == null) {
                    channel = client.connect(url);
                }
            }
        }

        return channel;
    }

    @Override
    public Client connectServer() {
        throw new UnsupportedOperationException();
    }
}
