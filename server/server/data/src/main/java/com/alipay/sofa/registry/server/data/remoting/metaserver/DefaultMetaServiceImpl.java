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

import com.alipay.remoting.Connection;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.*;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.SessionServerCache;
import com.alipay.sofa.registry.server.data.cache.SessionServerChangeItem;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.util.TimeUtil;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * @author qian.lqlq
 * @version $Id: DefaultMetaServiceImpl.java, v 0.1 2018－03－07 20:41 qian.lqlq Exp $
 */
public class DefaultMetaServiceImpl implements IMetaServerService {

    private static final Logger         LOGGER      = LoggerFactory
                                                        .getLogger(DefaultMetaServiceImpl.class);

    private static final int            TRY_COUNT   = 3;

    @Autowired
    private DataServerConfig            dataServerConfig;

    @Autowired
    private MetaNodeExchanger           metaNodeExchanger;

    @Autowired
    private MetaServerConnectionFactory metaServerConnectionFactory;

    private RaftClient                  raftClient;

    private AtomicBoolean               clientStart = new AtomicBoolean(false);

    @Autowired
    private SessionServerCache          sessionServerCache;

    @Override
    public void renewNodeTask() {
        final String leaderIp = getLeader().getIp();
        try {
            RenewNodesRequest<DataNode> renewNodesRequest = new RenewNodesRequest<>(new DataNode(
                new URL(DataServerConfig.IP), dataServerConfig.getLocalDataCenter()));
            // datanode need the meta and session nodes info
            renewNodesRequest.setTargetNodeTypes(Sets.newHashSet(NodeType.META, NodeType.SESSION));
            GenericResponse<RenewNodesResult> resp = (GenericResponse<RenewNodesResult>) metaNodeExchanger
                .request(new Request() {
                    @Override
                    public Object getRequestBody() {
                        return renewNodesRequest;
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(leaderIp, dataServerConfig.getMetaServerPort());
                    }
                }).getResult();
            if (resp != null && resp.isSuccess()) {
                handleRenewResult(resp.getData());
            } else {
                LOGGER.error("[RenewNodeTask] renew data node to metaServer error : {}, {}",
                    leaderIp, resp);
            }
        } catch (Exception e) {
            LOGGER.error("[RenewNodeTask] renew data node to metaServer error : {}", leaderIp, e);
            String newip = refreshLeader().getIp();
            LOGGER.warn("[RenewNodeTask] renew data node to metaServer error,leader refresh: {}",
                newip);
        }

    }

    private void handleRenewResult(RenewNodesResult result) {
        Map<Node.NodeType, NodeChangeResult> resultMap = result.getResults();
        if (resultMap == null) {
            return;
        }
        NodeChangeResult<SessionNode> sessions = resultMap.get(NodeType.SESSION);
        Map<String, Long> versionMap = sessions.getDataCenterListVersions();
        versionMap.put(sessions.getLocalDataCenter(), sessions.getVersion());
        sessionServerCache.setSessionServerChangeItem(new SessionServerChangeItem(sessions.getNodes(), versionMap));

        NodeChangeResult<MetaNode> metas = resultMap.get(NodeType.META);
        metas.getNodes().forEach((dataCenter, metaNodes) -> {
            for (String metaNode : metaNodes.keySet()) {
                Connection connection = metaServerConnectionFactory.getConnection(dataCenter, metaNode);
                if (connection == null || !connection.isFine()) {
                    registerMetaServer(dataCenter, metaNode);
                    LOGGER.info("[Renew] adds meta connection, datacenter:{}, ip:{}", dataCenter, metaNode);
                }
            }
            Set<String> ipSet = metaServerConnectionFactory.getIps(dataCenter);
            for (String ip : ipSet) {
                if (!metaNodes.containsKey(ip)) {
                    metaServerConnectionFactory.remove(dataCenter, ip);
                    LOGGER.info("[Renew] remove meta connection, datacenter:{}, ip:{}", dataCenter, ip);
                }
            }
        });
    }

    private void registerMetaServer(String dataCenter, String ip) {
        PeerId leader = getLeader();

        for (int tryCount = 0; tryCount < TRY_COUNT; tryCount++) {
            try {
                Channel channel = metaNodeExchanger.connect(new URL(ip, dataServerConfig
                    .getMetaServerPort()));
                //connect all meta server
                if (channel != null && channel.isConnected()) {
                    metaServerConnectionFactory.register(dataCenter, ip,
                        ((BoltChannel) channel).getConnection());
                }
            } catch (Exception e) {
                LOGGER.error("[MetaServerChangeEventHandler] connect metaServer:{} error", ip, e);
                TimeUtil.randomDelay(1000);
            }
        }
    }

    @Override
    public ProvideData fetchData(String dataInfoId) {

        Map<String, Connection> connectionMap = metaServerConnectionFactory
            .getConnections(dataServerConfig.getLocalDataCenter());
        String leader = getLeader().getIp();
        if (connectionMap.containsKey(leader)) {
            Connection connection = connectionMap.get(leader);
            if (connection.isFine()) {
                try {
                    Request<FetchProvideDataRequest> request = new Request<FetchProvideDataRequest>() {

                        @Override
                        public FetchProvideDataRequest getRequestBody() {

                            return new FetchProvideDataRequest(dataInfoId);
                        }

                        @Override
                        public URL getRequestUrl() {
                            return new URL(connection.getRemoteIP(), connection.getRemotePort());
                        }
                    };

                    Response response = metaNodeExchanger.request(request);

                    Object result = response.getResult();
                    if (result instanceof ProvideData) {
                        return (ProvideData) result;
                    } else {
                        LOGGER.error("fetch null provider data!");
                        throw new RuntimeException("MetaNodeService fetch null provider data!");
                    }
                } catch (Exception e) {
                    LOGGER.error("fetch provider data error! " + e.getMessage(), e);
                    throw new RuntimeException("fetch provider data error! " + e.getMessage(), e);
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