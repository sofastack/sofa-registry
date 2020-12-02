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

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import org.springframework.beans.factory.annotation.Autowired;
import sun.nio.ch.ThreadPool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * @author shangyu.wh
 * @version $Id: RaftClientManager.java, v 0.1 2018-06-20 20:50 shangyu.wh Exp $
 */
public class RaftClientManager {

    private static final Logger LOGGER      = LoggerFactory.getLogger(RaftClientManager.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private CommonConfig        commonConfig;

    @Autowired
    private ExecutorManager     executorManager;

    private RaftClient          raftClient;

    private AtomicBoolean       clientStart = new AtomicBoolean(false);

    private Set<String>         metaIps;

    public void startRaftClient() {
        try {
            if (clientStart.compareAndSet(false, true)) {
                String serverConf = getServerConfig();
                raftClient = new RaftClient(getGroup(), serverConf,
                    executorManager.getDefaultRequestExecutor());
                raftClient.start();
            }
        } catch (Exception e) {
            clientStart.set(false);
            LOGGER.error("Start raft client error!", e);
            throw new RuntimeException("Start raft client error!", e);
        }
    }

    private String getServerConfig() {
        String ret = "";
        Set<String> ips = getMetaIp();
        if (ips != null && !ips.isEmpty()) {
            ret = ips.stream().map(ip -> ip + ":" + ValueConstants.RAFT_SERVER_PORT)
                .collect(Collectors.joining(","));
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("Init raft server config error!");
        }
        return ret;
    }

    public Set<String> getMetaIp() {
        if (metaIps != null && !metaIps.isEmpty()) {
            return metaIps;
        }
        metaIps = new HashSet<>();
        Map<String, Collection<String>> metaMap = commonConfig.getMetaNode();
        if (metaMap != null && !metaMap.isEmpty()) {
            String localDataCenter = sessionServerConfig.getSessionServerDataCenter();
            if (localDataCenter != null && !localDataCenter.isEmpty()) {
                Collection<String> metas = metaMap.get(localDataCenter);
                if (metas != null && !metas.isEmpty()) {
                    metas.forEach(domain -> {
                        String ip = NetUtil.getIPAddressFromDomain(domain);
                        if (ip == null) {
                            throw new RuntimeException(
                                "Node config convert domain {" + domain + "} error!");
                        }
                        metaIps.add(ip);
                    });
                }
            }
        }
        return metaIps;
    }

    private String getGroup() {
        return ValueConstants.RAFT_SERVER_GROUP + "_"
               + sessionServerConfig.getSessionServerDataCenter();
    }

    public PeerId getLeader() {
        if (raftClient == null) {
            startRaftClient();
        }
        PeerId leader = raftClient.getLeader();
        if (leader == null) {
            LOGGER.error("[RaftClientManager] register MetaServer get no leader!");
            throw new RuntimeException("[RaftClientManager] register MetaServer get no leader!");
        }
        return leader;
    }

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

    /**
     * Getter method for property <tt>clientStart</tt>.
     *
     * @return property value of clientStart
     */
    public AtomicBoolean getClientStart() {
        return clientStart;
    }
}