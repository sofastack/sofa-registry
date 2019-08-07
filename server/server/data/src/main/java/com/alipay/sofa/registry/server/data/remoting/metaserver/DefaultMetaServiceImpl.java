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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.remoting.Connection;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.GetNodesRequest;
import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DataServerChangeItem;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;

/**
 *
 * @author qian.lqlq
 * @version $Id: DefaultMetaServiceImpl.java, v 0.1 2018－03－07 20:41 qian.lqlq Exp $
 */
public class DefaultMetaServiceImpl implements IMetaServerService {

    private static final Logger         LOGGER      = LoggerFactory
                                                        .getLogger(DefaultMetaServiceImpl.class);

    @Autowired
    private DataServerConfig            dataServerConfig;

    @Autowired
    private MetaNodeExchanger           metaNodeExchanger;

    @Autowired
    private MetaServerConnectionFactory metaServerConnectionFactory;

    private RaftClient                  raftClient;

    private AtomicBoolean               clientStart = new AtomicBoolean(false);

    @Override
    public Map<String, Set<String>> getMetaServerMap() {
        HashMap<String, Set<String>> map = new HashMap<>();
        Set<String> set = dataServerConfig.getMetaServerIpAddresses();

        Map<String, Connection> connectionMap = metaServerConnectionFactory
            .getConnections(dataServerConfig.getLocalDataCenter());
        Connection connection = null;
        try {
            if (connectionMap.isEmpty()) {
                List<String> list = new ArrayList(set);
                Collections.shuffle(list);
                connection = ((BoltChannel) metaNodeExchanger.connect(new URL(list.iterator()
                    .next(), dataServerConfig.getMetaServerPort()))).getConnection();
            } else {
                List<Connection> connections = new ArrayList<>(connectionMap.values());
                Collections.shuffle(connections);
                connection = connections.iterator().next();
                if (!connection.isFine()) {
                    connection = ((BoltChannel) metaNodeExchanger.connect(new URL(connection
                        .getRemoteIP(), dataServerConfig.getMetaServerPort()))).getConnection();
                }
            }

            GetNodesRequest request = new GetNodesRequest(NodeType.META);
            final Connection finalConnection = connection;
            Object obj = metaNodeExchanger.request(new Request() {
                @Override
                public Object getRequestBody() {
                    return request;
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(finalConnection.getRemoteIP(), finalConnection.getRemotePort());
                }
            }).getResult();
            if (obj instanceof NodeChangeResult) {
                NodeChangeResult<MetaNode> result = (NodeChangeResult<MetaNode>) obj;

                Map<String, Map<String, MetaNode>> metaNodesMap = result.getNodes();
                if (metaNodesMap != null && !metaNodesMap.isEmpty()) {
                    Map<String, MetaNode> metaNodeMap = metaNodesMap.get(dataServerConfig
                        .getLocalDataCenter());
                    if (metaNodeMap != null && !metaNodeMap.isEmpty()) {
                        map.put(dataServerConfig.getLocalDataCenter(), metaNodeMap.keySet());
                    } else {
                        LOGGER
                            .error(
                                "[DefaultMetaServiceImpl] refresh connections from metaServer {} has no result!",
                                connection.getRemoteIP());
                    }
                }
            }
        } catch (Exception e) {
            String con = connection != null ? connection.getRemoteIP() : "null";
            LOGGER.error("[DefaultMetaServiceImpl] refresh connections from metaServer error : {}",
                con, e);
            LOGGER
                .warn(
                    "[DefaultMetaServiceImpl] refresh connections from metaServer error,refresh leader : {}",
                    con);
            throw new RuntimeException(
                "[DefaultMetaServiceImpl] refresh connections from metaServer error!", e);
        }
        return map;
    }

    @Override
    public DataServerNode getDataServer(String dataCenter, String dataInfoId) {
        return DataServerNodeFactory.computeDataServerNode(dataCenter, dataInfoId);
    }

    @Override
    public List<DataServerNode> getDataServers(String dataCenter, String dataInfoId) {
        return DataServerNodeFactory.computeDataServerNodes(dataCenter, dataInfoId,
            dataServerConfig.getStoreNodes());
    }

    @Override
    public DataServerChangeItem getDateServers() {
        Map<String, Connection> connectionMap = metaServerConnectionFactory
            .getConnections(dataServerConfig.getLocalDataCenter());
        String leader = getLeader().getIp();
        if (connectionMap.containsKey(leader)) {
            Connection connection = connectionMap.get(leader);
            if (connection.isFine()) {
                try {
                    GetNodesRequest request = new GetNodesRequest(NodeType.DATA);
                    Object obj = metaNodeExchanger.request(new Request() {
                        @Override
                        public Object getRequestBody() {
                            return request;
                        }

                        @Override
                        public URL getRequestUrl() {
                            return new URL(connection.getRemoteIP(), connection.getRemotePort());
                        }
                    }).getResult();
                    if (obj instanceof NodeChangeResult) {
                        NodeChangeResult<DataNode> result = (NodeChangeResult<DataNode>) obj;
                        Map<String, Long> versionMap = result.getDataCenterListVersions();
                        versionMap.put(result.getLocalDataCenter(), result.getVersion());
                        return new DataServerChangeItem(result.getNodes(), versionMap);
                    }
                } catch (Exception e) {
                    LOGGER.error(
                        "[ConnectionRefreshTask] refresh connections from metaServer error : {}",
                        leader, e);
                    String newip = refreshLeader().getIp();
                    LOGGER
                        .warn(
                            "[ConnectionRefreshTask] refresh connections from metaServer error,refresh leader : {}",
                            newip);

                }
            }
        }
        String newip = refreshLeader().getIp();
        LOGGER.warn(
            "[ConnectionRefreshTask] refresh connections metaServer not fine,refresh leader : {}",
            newip);
        return null;
    }

    @Override
    public List<String> getOtherDataCenters() {
        Set<String> all = new HashSet<>(DataServerNodeFactory.getAllDataCenters());
        all.remove(dataServerConfig.getLocalDataCenter());
        return new ArrayList<>(all);
    }

    @Override
    public void renewNodeTask() {
        Map<String, Connection> connectionMap = metaServerConnectionFactory
            .getConnections(dataServerConfig.getLocalDataCenter());
        for (Entry<String, Connection> connectEntry : connectionMap.entrySet()) {
            String ip = connectEntry.getKey();
            //just send to leader
            if (ip.equals(getLeader().getIp())) {
                Connection connection = connectEntry.getValue();
                if (connection.isFine()) {
                    try {
                        RenewNodesRequest<DataNode> renewNodesRequest = new RenewNodesRequest<>(
                            new DataNode(new URL(DataServerConfig.IP),
                                dataServerConfig.getLocalDataCenter()));
                        metaNodeExchanger.request(new Request() {
                            @Override
                            public Object getRequestBody() {
                                return renewNodesRequest;
                            }

                            @Override
                            public URL getRequestUrl() {
                                return new URL(connection.getRemoteIP(), connection.getRemotePort());
                            }
                        }).getResult();
                    } catch (Exception e) {
                        LOGGER.error("[RenewNodeTask] renew data node to metaServer error : {}",
                            ip, e);
                        String newip = refreshLeader().getIp();
                        LOGGER
                            .warn(
                                "[RenewNodeTask] renew data node to metaServer error,leader refresh: {}",
                                newip);
                    }
                } else {
                    String newip = refreshLeader().getIp();
                    LOGGER
                        .warn(
                            "[RenewNodeTask] renew data node to metaServer not fine,leader refresh: {}",
                            newip);
                }
            }
        }
    }

    @Override
    public void startRaftClient() {
        try {
            if (clientStart.compareAndSet(false, true)) {
                String serverConf = getServerConfig();
                raftClient = new RaftClient(getGroup(), serverConf);
                raftClient.start();
            }
        } catch (Exception e) {
            LOGGER.error("Start raft client error!", e);
            throw new RuntimeException("Start raft client error!", e);
        }
    }

    private String getServerConfig() {
        String ret = "";
        Set<String> ips = dataServerConfig.getMetaServerIpAddresses();
        if (ips != null && !ips.isEmpty()) {
            ret = ips.stream().map(ip -> ip + ":" + ValueConstants.RAFT_SERVER_PORT)
                    .collect(Collectors.joining(","));
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("Init raft server config error!");
        }
        return ret;
    }

    private String getGroup() {
        return ValueConstants.RAFT_SERVER_GROUP + "_" + dataServerConfig.getLocalDataCenter();
    }

    @Override
    public PeerId getLeader() {
        if (raftClient == null) {
            startRaftClient();
        }
        PeerId leader = raftClient.getLeader();
        if (leader == null) {
            LOGGER.error("[DefaultMetaServiceImpl] register MetaServer get no leader!");
            throw new RuntimeException(
                "[DefaultMetaServiceImpl] register MetaServer get no leader!");
        }
        return leader;
    }

    @Override
    public PeerId refreshLeader() {
        if (raftClient == null) {
            startRaftClient();
        }
        PeerId leader = raftClient.refreshLeader();
        if (leader == null) {
            LOGGER.error("[RaftClientManager] refresh MetaServer get no leader!");
            throw new RuntimeException("[RaftClientManager] refresh MetaServer get no leader!");
        }
        return leader;
    }
}