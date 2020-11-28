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

import java.util.*;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.*;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.SessionHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionNodeManager.java, v 0.1 2018-03-05 10:42 shangyu.wh Exp $
 */
public class SessionNodeManager extends AbstractNodeManager<SessionNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionNodeManager.class);

    @Autowired
    private NodeManager         metaNodeManager;

    @Autowired
    private NodeManager         dataNodeManager;

    @Override
    public SessionNode getNode(String dataInfoId) {
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.SESSION;
    }

    public List<String> getZoneServerList(String zonename) {
        List<String> serverList = new ArrayList<>();
        Collection<SessionNode> sessionNodes = getDataCenterNodes();
        if (sessionNodes != null && !sessionNodes.isEmpty()) {
            if (zonename == null || zonename.isEmpty()) {
                zonename = sessionServerConfig.getSessionServerRegion();
            }
            for (SessionNode sessionNode : sessionNodes) {
                if (zonename.equals(sessionNode.getRegionId())) {
                    URL url = sessionNode.getNodeUrl();
                    if (url != null) {
                        serverList.add(url.getIpAddress());
                    }
                }
            }

        }
        return serverList;
    }

    @Override
    public void renewNode() {
        final String leaderIp = raftClientManager.getLeader().getIp();
        try {
            Request<RenewNodesRequest> renewNodesRequestRequest = new Request<RenewNodesRequest>() {

                @Override
                public RenewNodesRequest getRequestBody() {
                    URL clientUrl = new URL(NetUtil.getLocalAddress().getHostAddress(), 0);
                    SessionNode sessionNode = new SessionNode(clientUrl,
                        sessionServerConfig.getSessionServerRegion());
                    RenewNodesRequest request = new RenewNodesRequest(sessionNode);
                    return request;
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(leaderIp, sessionServerConfig.getMetaServerPort());
                }
            };

            GenericResponse<SessionHeartBeatResponse> resp = (GenericResponse<SessionHeartBeatResponse>) metaNodeExchanger
                .request(renewNodesRequestRequest).getResult();
            if (resp != null && resp.isSuccess()) {
                handleRenewResult(resp.getData());
            } else {
                LOGGER.error("[RenewNodeTask] renew node to metaServer error : {}, {}", leaderIp,
                    resp);
                throw new RuntimeException("renew node failed, " + resp);
            }
        } catch (Throwable e) {
            throw new RuntimeException("SessionNodeManager renew node error! " + e.getMessage(), e);
        }
    }

    private void handleRenewResult(SessionHeartBeatResponse result) {
        // TODO
    }

    @Override
    public void updateNodes(NodeChangeResult nodeChangeResult) {
        write.lock();
        try {
            Long receiveVersion = nodeChangeResult.getVersion();
            boolean versionChange = checkAndUpdateListVersions(
                sessionServerConfig.getSessionServerDataCenter(), receiveVersion);
            if (!versionChange) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Current data type {} list version has not updated!",
                        getNodeType());
                }
            }
            nodes = nodeChangeResult.getNodes();
        } finally {
            write.unlock();
        }
    }
}
