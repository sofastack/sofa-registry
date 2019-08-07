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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.StatusConfirmRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.DataNodeService;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.meta.store.StoreService;

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
    public void pushDataNodes(NodeChangeResult nodeChangeResult, Map<String, DataNode> targetNodes,
                              boolean confirm, String confirmNodeIp) {

        if (nodeChangeResult != null) {

            List<Throwable> exceptions = new ArrayList<>();

            NodeConnectManager nodeConnectManager = getNodeConnectManager();

            Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

            // add register confirm
            StoreService storeService = ServiceFactory.getStoreService(NodeType.DATA);
            DataCenterNodes dataCenterNodes = storeService.getDataCenterNodes();
            Map<String, DataNode> registeredNodes = dataCenterNodes.getNodes();

            if (registeredNodes == null || registeredNodes.isEmpty()) {
                LOGGER.error("Push dataNode list error! No data node registered!");
                throw new RuntimeException("Push dataNode list error! No data node registered!");
            }

            for (InetSocketAddress address : connections) {
                try {
                    if (targetNodes != null && !targetNodes.isEmpty()) {
                        if (!targetNodes.keySet().contains(address.getAddress().getHostAddress())) {
                            continue;
                        }
                    } else {
                        if (!registeredNodes.keySet().contains(
                            address.getAddress().getHostAddress())) {
                            continue;
                        }
                    }

                    Request<NodeChangeResult> nodeChangeRequestRequest = new Request<NodeChangeResult>() {
                        @Override
                        public NodeChangeResult getRequestBody() {
                            return nodeChangeResult;
                        }

                        @Override
                        public URL getRequestUrl() {
                            return new URL(address);
                        }
                    };
                    LOGGER.info("pushDataNodes sent url {},node type {}", address,
                        nodeChangeResult.getNodeType());
                    Response response = dataNodeExchanger.request(nodeChangeRequestRequest);

                    if (confirm) {
                        Object result = response.getResult();
                        if (result instanceof CommonResponse) {
                            CommonResponse genericResponse = (CommonResponse) result;
                            if (genericResponse.isSuccess()) {
                                confirmStatus(address, confirmNodeIp);
                            } else {
                                LOGGER.error("NodeChange notify get response fail!");
                                throw new RuntimeException("NodeChange notify get response fail!");
                            }
                        } else {
                            LOGGER
                                .error("NodeChange notify has not get response or response type illegal!");
                            throw new RuntimeException(
                                "NodeChange notify has not get response or response type illegal!");
                        }
                    }

                } catch (RequestException e) {
                    LOGGER.error("Push dataNode list error! " + e.getMessage(), e);
                    exceptions.add(e);
                } catch (RuntimeException e) {
                    LOGGER.error("Push dataNode list runtime error! ", e);
                    exceptions.add(e);
                }
            }
            if (!exceptions.isEmpty()) {
                throw new RuntimeException(
                    "DataNodeService push dataNode list error! errors count:" + exceptions.size());
            }
        }
    }

    @Override
    public void notifyStatusConfirm(StatusConfirmRequest statusConfirmRequest) {
        try {

            NodeConnectManager nodeConnectManager = getNodeConnectManager();
            Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

            for (InetSocketAddress address : connections) {
                if (address.getAddress().getHostAddress()
                    .equals(statusConfirmRequest.getNode().getNodeUrl().getIpAddress())) {

                    Request<StatusConfirmRequest> statusConfirmRequestRequest = new Request<StatusConfirmRequest>() {
                        @Override
                        public StatusConfirmRequest getRequestBody() {
                            return statusConfirmRequest;
                        }

                        @Override
                        public URL getRequestUrl() {
                            return new URL(address);
                        }
                    };

                    dataNodeExchanger.request(statusConfirmRequestRequest);

                    break;
                }
            }

        } catch (RequestException e) {
            throw new RuntimeException("Notify status confirm error: " + e.getMessage(), e);
        }
    }

    private void confirmStatus(InetSocketAddress address, String confirmNodeIp) {
        String ipAddress = address.getAddress().getHostAddress();
        dataStoreService.confirmNodeStatus(ipAddress, confirmNodeIp);
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