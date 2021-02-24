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
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.DataHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.SessionHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.monitor.data.DataMessageListener;
import com.alipay.sofa.registry.server.meta.monitor.heartbeat.HeartbeatListener;
import com.alipay.sofa.registry.server.meta.monitor.session.SessionMessageListener;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * Handle session/data node's heartbeat request
 * @author shangyu.wh
 * @version $Id: RenewNodesRequestHandler.java, v 0.1 2018-03-30 19:58 shangyu.wh Exp $
 */
public class HeartbeatRequestHandler extends MetaServerHandler<HeartbeatRequest<Node>> {

    private static final Logger          logger = LoggerFactory
                                                    .getLogger(HeartbeatRequestHandler.class);

    @Autowired
    private DefaultCurrentDcMetaServer   currentDcMetaServer;

    @Autowired
    private DefaultSlotManager           defaultSlotManager;

    @Autowired
    private List<DataMessageListener>    dataMessageListeners;

    @Autowired
    private List<SessionMessageListener> sessionMessageListeners;

    @Autowired
    private NodeConfig                   nodeConfig;

    @Override
    public Object doHandle(Channel channel, HeartbeatRequest<Node> heartbeat) {
        Node renewNode = null;
        try {
            renewNode = heartbeat.getNode();
            onHeartbeat(heartbeat, channel);

            SlotTable slotTable = currentDcMetaServer.getSlotTable();
            if (!SlotTableUtils.isValidSlotTable(slotTable)) {
                return new GenericResponse<BaseHeartBeatResponse>()
                    .fillFailed("slot-table not valid, check meta-server log for detail");
            }
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
            logger.error("Node {} renew error!", renewNode, e);
            return new GenericResponse<BaseHeartBeatResponse>().fillFailed("Node " + renewNode
                                                                           + "renew error!");
        }
    }

    @SuppressWarnings("unchecked")
    private void onHeartbeat(HeartbeatRequest heartbeat, Channel channel) {
        new DefaultHeartbeatListener(nodeConfig.getLocalDataCenter(), channel)
            .onHeartbeat(heartbeat);
        Node node = heartbeat.getNode();
        switch (node.getNodeType()) {
            case SESSION:
                currentDcMetaServer.getSessionServerManager().renew((SessionNode) node,
                    heartbeat.getDuration());
                onSessionHeartbeat(heartbeat);
            case DATA:
                currentDcMetaServer.getDataServerManager().renew((DataNode) node,
                    heartbeat.getDuration());
                onDataHeartbeat(heartbeat);
            case META:
                throw new IllegalArgumentException("node type not correct: " + node.getNodeType());
            default:
                break;
        }
        throw new IllegalArgumentException("node type not correct: " + node.getNodeType());
    }

    private void onSessionHeartbeat(HeartbeatRequest<SessionNode> heartbeat) {
        sessionMessageListeners.forEach(listener -> {
            try {
                listener.onHeartbeat(heartbeat);
            } catch (Throwable th) {
                logger.error("[onDataHeartbeat]", th);
            }
        });
    }

    private void onDataHeartbeat(HeartbeatRequest<DataNode> heartbeat) {
        dataMessageListeners.forEach(listener -> {
            try {
                listener.onHeartbeat(heartbeat);
            } catch (Throwable th) {
                logger.error("[onDataHeartbeat]", th);
            }
        });
    }

    private SlotTable transferDataNodeSlotToSlotTable(DataNode node, SlotTable slotTable) {
        DataNodeSlot dataNodeSlot = defaultSlotManager.getDataNodeManagedSlot(node, false);
        Set<Slot> result = Sets.newHashSet();
        dataNodeSlot.getLeaders().forEach(leaderSlotId->result.add(slotTable.getSlot(leaderSlotId)));
        dataNodeSlot.getFollowers().forEach(followerSlotId->result.add(slotTable.getSlot(followerSlotId)));
        return new SlotTable(slotTable.getEpoch(), result);
    }

    @Override
    public Class interest() {
        return HeartbeatRequest.class;
    }

    public static class DefaultHeartbeatListener implements HeartbeatListener<Node> {

        private static final Logger logger                      = LoggerFactory
                                                                    .getLogger(DefaultHeartbeatListener.class);

        public static final String  KEY_TIMESTAMP_GAP_THRESHOLD = "timestamp.gap.threshold";

        private static final long   timeGapThreshold            = Long.getLong(
                                                                    KEY_TIMESTAMP_GAP_THRESHOLD,
                                                                    2000);

        private final String        dataCenter;

        private final Channel       channel;

        private volatile boolean    isValidChannel              = true;

        public DefaultHeartbeatListener(String dataCenter, Channel channel) {
            this.dataCenter = dataCenter;
            this.channel = channel;
        }

        @Override
        public void onHeartbeat(HeartbeatRequest<Node> heartbeat) {
            checkIfDataCenterMatched(heartbeat);
            checkIfTimeSynced(heartbeat);
            checkIfSlotBasicInfoMatched(heartbeat);
            closeIfChannelNotValid();
        }

        private void closeIfChannelNotValid() {
            if (!isValidChannel) {
                channel.close();
            }
        }

        private void checkIfTimeSynced(HeartbeatRequest<Node> heartbeat) {
            long timestamp = heartbeat.getTimestamp();
            if (System.currentTimeMillis() - timestamp > timeGapThreshold) {
                logger.error("[checkIfTimeSynced] {} timestamp[{}] is far behind mine[{}]",
                    heartbeat.getNode(), timestamp, System.currentTimeMillis());
            }
        }

        private void checkIfSlotBasicInfoMatched(HeartbeatRequest<Node> heartbeat) {

            SlotConfig.SlotBasicInfo slotBasicInfo = heartbeat.getSlotBasicInfo();
            if (SlotConfig.FUNC.equals(slotBasicInfo.getSlotFunc())) {
                logger
                    .error(
                        "[checkIfSlotBasicInfoMatched] {} slot function not match(meta-server: [{}], node: [{}]",
                        heartbeat.getNode(), SlotConfig.FUNC, slotBasicInfo.getSlotFunc());
                isValidChannel = false;
            }
            if (SlotConfig.SLOT_NUM != slotBasicInfo.getSlotNum()) {
                logger
                    .error(
                        "[checkIfSlotBasicInfoMatched] {} slot number not match(meta-server: [{}], node: [{}]",
                        heartbeat.getNode(), SlotConfig.SLOT_NUM, slotBasicInfo.getSlotNum());
                isValidChannel = false;
            }
            if (SlotConfig.SLOT_REPLICAS != slotBasicInfo.getSlotReplicas()) {
                logger
                    .error(
                        "[checkIfSlotBasicInfoMatched] {} slot replicas not match(meta-server: [{}], node: [{}]",
                        heartbeat.getNode(), SlotConfig.SLOT_REPLICAS, slotBasicInfo.getSlotReplicas());
                isValidChannel = false;
            }

        }

        private void checkIfDataCenterMatched(HeartbeatRequest<Node> heartbeat) {
            String dc = heartbeat.getDataCenter();
            if (!this.dataCenter.equalsIgnoreCase(dc)) {
                logger
                    .error(
                        "[checkIfDataCenterMatched] {} datacenter not match(meta-server: [{}], node: [{}]",
                        heartbeat.getNode(), this.dataCenter, dc);
                isValidChannel = false;
            }
        }

    }
}