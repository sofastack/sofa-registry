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
package com.alipay.sofa.registry.server.meta.node.impl;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NotifyProvideDataChange;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.DataNodeService;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.meta.store.StoreService;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNodeServiceImpl.java, v 0.1 2018-01-23 19:11 shangyu.wh Exp $
 */
public class DataNodeServiceImpl implements DataNodeService {

    private static final Logger   LOGGER = LoggerFactory.getLogger(DataNodeServiceImpl.class);

    @Autowired
    private NodeExchanger         dataNodeExchanger;

    @Autowired
    private StoreService          dataStoreService;

    @Autowired
    private AbstractServerHandler dataConnectionHandler;

    @Override
    public NodeType getNodeType() {
        return NodeType.DATA;
    }

    @Override
    public void notifyProvideDataChange(NotifyProvideDataChange notifyProvideDataChange) {

        NodeConnectManager nodeConnectManager = getNodeConnectManager();
        Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

        if (connections == null || connections.isEmpty()) {
            LOGGER.error("Push dataNode list error! No data node connected!");
            throw new RuntimeException("Push dataNode list error! No data node connected!");
        }

        // add register confirm
        StoreService storeService = ServiceFactory.getStoreService(NodeType.DATA);
        Map<String, DataNode> dataNodes = storeService.getNodes();

        if (dataNodes == null || dataNodes.isEmpty()) {
            LOGGER.error("Push dataNode list error! No data node registered!");
            throw new RuntimeException("Push dataNode list error! No data node registered!");
        }

        for (InetSocketAddress connection : connections) {

            if (!dataNodes.keySet().contains(connection.getAddress().getHostAddress())) {
                continue;
            }

            try {
                Request<NotifyProvideDataChange> request = new Request<NotifyProvideDataChange>() {

                    @Override
                    public NotifyProvideDataChange getRequestBody() {
                        return notifyProvideDataChange;
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(connection);
                    }
                };

                dataNodeExchanger.request(request);

            } catch (RequestException e) {
                throw new RuntimeException("Notify provide data change to dataServer error: "
                                           + e.getMessage(), e);
            }
        }
    }

    private NodeConnectManager getNodeConnectManager() {
        if (!(dataConnectionHandler instanceof NodeConnectManager)) {
            LOGGER.error("dataConnectionHandler inject is not NodeConnectManager instance!");
            throw new RuntimeException(
                "dataConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) dataConnectionHandler;
    }
}