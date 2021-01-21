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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesRequest;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.DataHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.SessionHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Handle session/data node's heartbeat request
 * @author shangyu.wh
 * @version $Id: RenewNodesRequestHandler.java, v 0.1 2018-03-30 19:58 shangyu.wh Exp $
 */
public class HeartbeatRequestHandler extends MetaServerHandler<RenewNodesRequest<Node>> {

    private static final Logger        LOGGER = LoggerFactory
                                                  .getLogger(HeartbeatRequestHandler.class);

    @Autowired
    private DefaultCurrentDcMetaServer currentDcMetaServer;

    @Autowired
    private DefaultSlotManager         defaultSlotManager;

    @Override
    public Object doHandle(Channel channel, RenewNodesRequest<Node> renewNodesRequest) {
        Node renewNode = null;
        try {
            renewNode = renewNodesRequest.getNode();
            getLeaseManager(renewNode).renew(renewNode, renewNodesRequest.getDuration());
            SlotTable slotTable = currentDcMetaServer.getSlotTable();
            BaseHeartBeatResponse response = null;
            // TODO the epoch and the nodes is not atomic
            final long metaEpoch = currentDcMetaServer.getEpoch();
            final List<MetaNode> metaNodes = currentDcMetaServer.getClusterMembers();

            final long sessionEpoch = currentDcMetaServer.getSessionServerManager().getEpoch();
            final List<SessionNode> sessionNodes = currentDcMetaServer.getSessionServerManager()
                .getClusterMembers();

            switch (renewNode.getNodeType()) {
                case SESSION:
                    response = new SessionHeartBeatResponse(metaEpoch, slotTable, metaNodes,
                        sessionEpoch, sessionNodes);
                    break;
                case DATA:
                    slotTable = transferDataNodeSlotToSlotTable((DataNode) renewNode, slotTable);
                    response = new DataHeartBeatResponse(metaEpoch, slotTable, metaNodes,
                        sessionEpoch, sessionNodes);
                    break;
                case META:
                    response = new BaseHeartBeatResponse(metaEpoch, slotTable, metaNodes);
                    break;
                default:
                    break;
            }

            return new GenericResponse<BaseHeartBeatResponse>().fillSucceed(response);
        } catch (Throwable e) {
            LOGGER.error("Node {} renew error!", renewNode, e);
            return new GenericResponse<BaseHeartBeatResponse>().fillFailed("Node " + renewNode
                                                                           + "renew error!");
        }
    }

    private LeaseManager getLeaseManager(Node node) {
        switch (node.getNodeType()) {
            case SESSION:
                return currentDcMetaServer.getSessionServerManager();
            case DATA:
                return currentDcMetaServer.getDataServerManager();
            case META:
                throw new IllegalArgumentException("node type not correct: " + node.getNodeType());
            default:
                break;
        }
        throw new IllegalArgumentException("node type not correct: " + node.getNodeType());
    }

    private SlotTable transferDataNodeSlotToSlotTable(DataNode node, SlotTable slotTable) {
        DataNodeSlot dataNodeSlot = defaultSlotManager.getDataNodeManagedSlot(node, false);
        Map<Integer, Slot> result = Maps.newHashMap();
        dataNodeSlot.getLeaders().forEach(leaderSlotId->result.put(leaderSlotId, slotTable.getSlot(leaderSlotId)));
        dataNodeSlot.getFollowers().forEach(followerSlotId->result.put(followerSlotId, slotTable.getSlot(followerSlotId)));
        return new SlotTable(slotTable.getEpoch(), result);
    }

    @Override
    public Class interest() {
        return RenewNodesRequest.class;
    }
}