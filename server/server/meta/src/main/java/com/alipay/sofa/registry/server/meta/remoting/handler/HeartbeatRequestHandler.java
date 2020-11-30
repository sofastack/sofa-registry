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
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesResult;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.DataHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.SessionHeartBeatResponse;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;

import java.util.Set;

/**
 * Handle session/data node's heartbeat request
 * @author shangyu.wh
 * @version $Id: RenewNodesRequestHandler.java, v 0.1 2018-03-30 19:58 shangyu.wh Exp $
 */
public class HeartbeatRequestHandler extends AbstractServerHandler<RenewNodesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatRequestHandler.class);

    @Autowired
    private CurrentDcMetaServer metaServer;

    @Override
    public Object reply(Channel channel, RenewNodesRequest renewNodesRequest) {
        Node renewNode = null;
        try {
            renewNode = renewNodesRequest.getNode();
            metaServer.renew(renewNode, renewNodesRequest.getDuration());

            BaseHeartBeatResponse response = null;
            switch (renewNode.getNodeType()) {
                case SESSION:
                    response = new SessionHeartBeatResponse(metaServer.getEpoch(),
                        metaServer.getSlotTable(), metaServer.getClusterMembers(),
                        metaServer.getSessionServers());
                case DATA:
                    response = new DataHeartBeatResponse(metaServer.getEpoch(),
                        metaServer.getSlotTable(), metaServer.getClusterMembers(),
                        metaServer.getSessionServers());
                case META:
                    response = new BaseHeartBeatResponse(metaServer.getEpoch(),
                        metaServer.getSlotTable(), metaServer.getClusterMembers());
                default:
                    break;
            }

            return new GenericResponse<BaseHeartBeatResponse>().fillSucceed(response);
        } catch (Throwable e) {
            LOGGER.error("Node " + renewNode + "renew error!", e);
            return new GenericResponse<BaseHeartBeatResponse>().fillFailed("Node " + renewNode
                                                                           + "renew error!");
        }
    }

    @Override
    public Class interest() {
        return RenewNodesRequest.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

}