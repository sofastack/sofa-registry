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
package com.alipay.sofa.registry.server.session.node;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.GetNodesRequest;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.VersionsMapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author shangyu.wh
 * @version $Id: AbstractNodeManager.java, v 0.1 2018-03-05 10:44 shangyu.wh Exp $
 */
public abstract class AbstractNodeManager<T extends Node> implements NodeManager<T> {

    private static final Logger                                            LOGGER                  = LoggerFactory
                                                                                                       .getLogger(AbstractNodeManager.class);

    private static final Logger                                            EXCHANGE_LOGGER         = LoggerFactory
                                                                                                       .getLogger("SESSION-EXCHANGE");
    protected final ReentrantReadWriteLock                                 readWriteLock           = new ReentrantReadWriteLock();
    protected final Lock                                                   read                    = readWriteLock
                                                                                                       .readLock();
    protected final Lock                                                   write                   = readWriteLock
                                                                                                       .writeLock();
    protected Map<String/*dataCenter id*/, Map<String /*ipAddress*/, T>> nodes                   = new ConcurrentHashMap<>();
    @Autowired
    protected SessionServerConfig                                          sessionServerConfig;

    @Autowired
    protected NodeExchanger                                                metaNodeExchanger;

    @Autowired
    protected RaftClientManager                                            raftClientManager;
    /**
     * store other dataCenter data list version
     */
    private ConcurrentHashMap<String/*dataCenter*/, Long /*version*/>    dataCenterNodesVersions = new ConcurrentHashMap<>();

    @Override
    public Collection<T> getDataCenterNodes() {
        read.lock();
        try {

            Map<String /*ipAddress*/, T> dataMap = nodes.get(sessionServerConfig
                .getSessionServerDataCenter());
            if (dataMap != null) {
                return dataMap.values();
            }
        } finally {
            read.unlock();
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<String> getDataCenters() {
        read.lock();
        try {
            return nodes.keySet();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void updateNodes(NodeChangeResult nodeChangeResult) {
        write.lock();
        try {
            nodes = nodeChangeResult.getNodes();
            dataCenterNodesVersions.putIfAbsent(nodeChangeResult.getLocalDataCenter(),
                nodeChangeResult.getVersion());
        } finally {
            write.unlock();
        }
    }

    public boolean checkAndUpdateListVersions(String dataCenterId, Long version) {
        return VersionsMapUtils.checkAndUpdateVersions(dataCenterNodesVersions, dataCenterId,
            version);
    }

    @Override
    public NodeChangeResult getAllDataCenterNodes() {
        NodeChangeResult nodeChangeResult;
        try {

            Request<GetNodesRequest> getNodesRequestRequest = new Request<GetNodesRequest>() {

                @Override
                public GetNodesRequest getRequestBody() {
                    return new GetNodesRequest(getNodeType());
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(raftClientManager.getLeader().getIp(),
                        sessionServerConfig.getMetaServerPort());
                }
            };

            Response<NodeChangeResult> response = metaNodeExchanger.request(getNodesRequestRequest);

            if (response != null && response.getResult() != null) {

                nodeChangeResult = response.getResult();
                updateNodes(nodeChangeResult);

                EXCHANGE_LOGGER.info("Update node type {} success!info:{}", getNodeType(),
                    nodeChangeResult.getNodes());
            } else {
                LOGGER.error(
                    "NodeManager get all dataCenter nodes type {} error!No response receive!",
                    getNodeType());
                throw new RuntimeException(
                    "NodeManager get all dataCenter nodes error!No response receive!");
            }

        } catch (RequestException e) {
            LOGGER.error("NodeManager get all dataCenter nodes error! " + e.getRequestMessage(), e);
            throw new RuntimeException("NodeManager get all dataCenter nodes error! "
                                       + e.getRequestMessage(), e);
        }

        return nodeChangeResult;
    }

    /**
     * Getter method for property <tt>dataCenterNodesVersions</tt>.
     *
     * @return property value of dataCenterNodesVersions
     */
    @Override
    public ConcurrentHashMap<String, Long> getDataCenterNodesVersions() {
        return dataCenterNodesVersions;
    }
}