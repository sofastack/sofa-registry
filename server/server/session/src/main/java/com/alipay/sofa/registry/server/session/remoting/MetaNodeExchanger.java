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

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import com.alipay.sofa.registry.server.session.remoting.handler.AbstractClientHandler;

/**
 * The type Data node exchanger.
 * @author shangyu.wh
 * @version $Id : DataNodeExchanger.java, v 0.1 2017-12-01 11:51 shangyu.wh Exp $
 */
public class MetaNodeExchanger implements NodeExchanger {

    private static final Logger               LOGGER = LoggerFactory
                                                         .getLogger(MetaNodeExchanger.class);

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private SessionServerConfig               sessionServerConfig;

    @Resource(name = "metaClientHandlers")
    private Collection<AbstractClientHandler> metaClientHandlers;

    @Autowired
    private RaftClientManager                 raftClientManager;

    @Autowired
    private NodeManager                       metaNodeManager;

    /**
     * @see MetaNodeExchanger#request(Request)
     */
    @Override
    public Response request(Request request) throws RequestException {

        Response response;
        URL url = request.getRequestUrl();
        try {
            Client sessionClient = boltExchange.getClient(Exchange.META_SERVER_TYPE);

            if (sessionClient == null) {
                LOGGER.warn(
                        "MetaNode Exchanger get dataServer connection {} error! Connection can not be null or disconnected!",
                        url);
                sessionClient = boltExchange.connect(Exchange.META_SERVER_TYPE, url,
                        metaClientHandlers.toArray(new ChannelHandler[metaClientHandlers.size()]));
            }
            try {

                final Object result = sessionClient.sendSync(url, request.getRequestBody(),
                        request.getTimeout() != null ? request.getTimeout() : sessionServerConfig.getDataNodeExchangeTimeOut());
                response = () -> result;

            } catch (Exception e) {
                //retry
                url = new URL(raftClientManager.refreshLeader().getIp(),
                        sessionServerConfig.getMetaServerPort());
                LOGGER.warn("MetaNode Exchanger request send error!It will be retry once!Request url:{}", url);

                final Object result = sessionClient.sendSync(url, request.getRequestBody(),
                        request.getTimeout() != null ? request.getTimeout() : sessionServerConfig.getDataNodeExchangeTimeOut());
                response = () -> result;
            }
        } catch (Exception e) {
            LOGGER.error("MetaNode Exchanger request data error!Request url:" + url, e);
            throw new RequestException("MetaNode Exchanger request data error!Request url:" + url,
                    request, e);
        }

        return response;
    }

    @Override
    public synchronized Client connectServer() {

        Collection<MetaNode> nodes = metaNodeManager.getDataCenterNodes();
        if (nodes == null || nodes.isEmpty()) {
            metaNodeManager.getAllDataCenterNodes();
            nodes = metaNodeManager.getDataCenterNodes();
        }
        Client sessionClient = null;
        for (MetaNode node : nodes) {
            URL url = new URL(node.getIp(), sessionServerConfig.getMetaServerPort());
            try {
                sessionClient = boltExchange.getClient(Exchange.META_SERVER_TYPE);
                if (sessionClient == null) {
                    sessionClient = boltExchange.connect(Exchange.META_SERVER_TYPE, url,
                        metaClientHandlers.toArray(new ChannelHandler[metaClientHandlers.size()]));
                }
            } catch (Exception e) {
                LOGGER.error("MetaNode Exchanger connect MetaServer error!url:" + url, e);
                continue;
            }
            try {
                Channel channel = sessionClient.getChannel(url);
                if (channel == null) {
                    sessionClient.connect(url);
                }
            } catch (Exception e) {
                LOGGER.error("MetaNode Exchanger connect channel error!url:" + url, e);
            }
        }
        return sessionClient;
    }
}