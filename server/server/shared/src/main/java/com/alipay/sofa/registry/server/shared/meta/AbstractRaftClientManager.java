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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author shangyu.wh
 * @version $Id: RaftClientManager.java, v 0.1 2018-06-20 20:50 shangyu.wh Exp $
 */
public abstract class AbstractRaftClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRaftClientManager.class);

    private volatile RaftClient raftClient;

    public synchronized void startRaftClient() {
        if (raftClient != null) {
            throw new IllegalStateException("deps start raft client");
        }
        try {
            String serverConf = getServerConfig();
            RaftClient client = new RaftClient(getGroup(), serverConf);
            client.start();
            this.raftClient = client;
        } catch (Throwable e) {
            LOGGER.error("Start raft client error!", e);
            throw new RuntimeException("Start raft client error!", e);
        }
    }

    private String getServerConfig() {
        String ret = "";
        Set<String> ips = getConfigMetaIp();
        if (ips != null && !ips.isEmpty()) {
            ret = ips.stream().map(ip -> ip + ":" + ValueConstants.RAFT_SERVER_PORT).collect(Collectors.joining(","));
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("Init raft server config error!");
        }
        return ret;
    }

    public Set<String> getConfigMetaIp() {
        Set<String> set = Sets.newHashSet();
        Collection<String> metaDomains = getMetaNodeDomains();
        metaDomains.forEach(domain -> {
            String ip = NetUtil.getIPAddressFromDomain(domain);
            if (ip == null) {
                throw new RuntimeException("Node config convert domain {" + domain + "} error!");
            }
            set.add(ip);
        });
        return set;
    }

    private String getGroup() {
        return ValueConstants.RAFT_SERVER_GROUP + "_" + getLocalDataCenter();
    }

    public PeerId getLeader() {
        PeerId leader = raftClient.getLeader();
        if (leader == null) {
            LOGGER.error("[RaftClientManager] register MetaServer get no leader!");
            throw new RuntimeException("[RaftClientManager] register MetaServer get no leader!");
        }
        return leader;
    }

    public PeerId refreshLeader() {
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
    public boolean getClientStart() {
        return raftClient != null;
    }

    protected abstract String getLocalDataCenter();

    protected abstract Collection<String> getMetaNodeDomains();
}