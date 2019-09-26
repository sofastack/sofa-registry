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
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class MetaNodeExchanger implements NodeExchanger {
    private static final Logger               LOGGER = LoggerFactory
                                                         .getLogger(MetaNodeExchanger.class);

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private IMetaServerService                metaServerService;

    @Autowired
    private DataServerConfig                  dataServerConfig;

    @Resource(name = "metaClientHandlers")
    private Collection<AbstractClientHandler> metaClientHandlers;

    @Override
    public Response request(Request request) {
        Channel channel = connect(request.getRequestUrl());
        Client client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
        LOGGER.info("MetaNode Exchanger request={},url={},callbackHandler={}", request.getRequestBody(),
                request.getRequestUrl(), request.getCallBackHandler());

        try {
            final Object result = client.sendSync(channel, request.getRequestBody(),
                    dataServerConfig.getRpcTimeout());
            return () -> result;
        } catch (Exception e) {
            //retry
            URL url = new URL(metaServerService.refreshLeader().getIp(),
                    dataServerConfig.getMetaServerPort());
            channel = client.getChannel(url);
            if (channel == null) {
                channel = client.connect(url);
            }
            LOGGER.warn("MetaNode Exchanger request send error!It will be retry once!Request url:{}", url);

            final Object result = client.sendSync(channel, request.getRequestBody(),
                    dataServerConfig.getRpcTimeout());
            return () -> result;
        }
    }

    public Channel connect(URL url) {
        Client client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
        if (client == null) {
            synchronized (this) {
                client = boltExchange.getClient(Exchange.META_SERVER_TYPE);
                if (client == null) {
                    client = boltExchange.connect(Exchange.META_SERVER_TYPE, url,
                        metaClientHandlers.toArray(new ChannelHandler[metaClientHandlers.size()]));
                }
            }
        }
        //try to connect data
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
