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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.store.StoreService;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaClientExchanger.java, v 0.1 2018-02-12 17:24 shangyu.wh Exp $
 */
public class MetaClientExchanger implements NodeExchanger {

    private static final Logger             LOGGER           = LoggerFactory
                                                                 .getLogger(MetaClientExchanger.class);

    @Autowired
    private MetaServerConfig                metaServerConfig;

    @Autowired
    private Exchange                        boltExchange;

    @Autowired
    private NodeConfig                      nodeConfig;

    private Map<String/*dataCenter*/, URL> dataCenterUrlMap = new ConcurrentHashMap<>();

    private static final int                RETRY_TIMES      = 3;

    @Override
    public Response request(Request request) throws RequestException {
        Response response;

        try {
            Client metaClient = boltExchange.getClient(Exchange.META_SERVER_TYPE);
            URL url = request.getRequestUrl();

            if (metaClient == null) {
                LOGGER.warn(
                        "MetaClient Exchanger get metaServer connection {} error! Connection can not be null or disconnected!",
                        url);
                metaClient = boltExchange.connect(Exchange.META_SERVER_TYPE, url, new ChannelHandler[0]);
            }

            final Object result = metaClient.sendSync(url, request.getRequestBody(),
                    metaServerConfig.getMetaNodeExchangeTimeout());
            response = () -> result;
        } catch (Exception e) {
            LOGGER.error("MetaClient Exchanger request data error!", e);
            throw new RequestException("MetaClient Exchanger request data error!", request, e);
        }

        return response;
    }

    @Override
    public Client connectServer() {
        connectOtherMetaServer();
        return null;
    }

    /**
     * connect other datacenter's metaServer
     */
    public void connectOtherMetaServer() {
        Map<String, Collection<String>> configMetaNodeIP = nodeConfig.getMetaNodeIP();

        StoreService storeService = ServiceFactory.getStoreService(NodeType.META);
        NodeChangeResult nodeChangeResult = storeService.getNodeChangeResult();
        Map<String, Map<String, MetaNode>> registerNodes = nodeChangeResult.getNodes();

        configMetaNodeIP.forEach((dataCenter, ips) -> {
            List<String> metaIps = new ArrayList<>();
            if (!nodeConfig.getLocalDataCenter().equalsIgnoreCase(dataCenter)) {
                if (registerNodes != null && registerNodes.get(dataCenter) != null) {
                    Map<String, MetaNode> metaNodeMap = registerNodes.get(dataCenter);
                    metaIps.addAll(metaNodeMap.keySet());
                } else {
                    //first not receive other register
                    metaIps.addAll(ips);
                }
                Collections.shuffle(metaIps);
                String ip = metaIps.iterator().next();
                try {
                    URL url = new URL(ip, metaServerConfig.getMetaServerPort());

                    Client metaClient = boltExchange.getClient(Exchange.META_SERVER_TYPE);
                    if (metaClient != null && metaClient.getChannel(url) != null) {
                        return;
                    }

                    boltExchange.connect(Exchange.META_SERVER_TYPE, url, new ChannelHandler[0]);
                    dataCenterUrlMap.putIfAbsent(dataCenter, url);
                    LOGGER.info("Connect other meta server success! url:" + url);
                } catch (Exception e) {
                    LOGGER.error("Connect other meta server error! IP:" + ip, e);
                }
            }
        });
    }

    /**
     * get datacenter's meta server url
     * @param dataCenter
     * @return
     */
    public URL getDataCenterUrl(String dataCenter) {
        try {
            URL url = dataCenterUrlMap.get(dataCenter);

            if (url != null) {
                Client metaClient = boltExchange.getClient(Exchange.META_SERVER_TYPE);
                if (metaClient != null && metaClient.getChannel(url) != null) {
                    return url;
                }
            }

            Map<String, Collection<String>> configMetaNodeIP = nodeConfig.getMetaNodeIP();
            StoreService storeService = ServiceFactory.getStoreService(NodeType.META);
            Map<String, Map<String, MetaNode>> registerNodes = storeService.getNodeChangeResult()
                .getNodes();

            List<String> metaIps = new ArrayList<>();
            if (registerNodes != null && registerNodes.get(dataCenter) != null) {
                metaIps.addAll((registerNodes.get(dataCenter)).keySet());
            } else {
                //first not receive other register
                metaIps.addAll(configMetaNodeIP.get(dataCenter));
            }
            Collections.shuffle(metaIps);
            String ip = metaIps.iterator().next();
            URL newUrl = new URL(ip, metaServerConfig.getMetaServerPort());
            for (int i = 0; i <= RETRY_TIMES; i++) {
                try {
                    boltExchange.connect(Exchange.META_SERVER_TYPE, newUrl, new ChannelHandler[0]);

                    dataCenterUrlMap.put(dataCenter, newUrl);

                    LOGGER.info("Connect other meta server success! url:" + newUrl);
                } catch (Exception e) {
                    int remain = RETRY_TIMES - i;
                    if (remain > 0) {
                        LOGGER.error("Connect other meta server error!retry time remain {}",
                            remain, e);
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } else {
                        LOGGER.error("Connect other meta server error!retry all error!", e);
                        throw new RuntimeException("Connect other meta server error!", e);
                    }
                }
            }
            return newUrl;
        } catch (Exception e) {
            LOGGER.error("Get other meta server url error!", e);
            throw new RuntimeException("Get other meta server url error!", e);
        }
    }
}