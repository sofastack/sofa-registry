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

import com.alipay.sofa.registry.common.model.metaserver.GetNodesRequest;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle session/data node's query request, such as getAllNodes request
 * and current this is no use for meta node, it's instead by RAFT
 * @author shangyu.wh
 * @version $Id: GetNodesRequestHandler.java, v 0.1 2018-03-02 15:12 shangyu.wh Exp $
 */
public class GetNodesRequestHandler extends AbstractServerHandler<GetNodesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger("META-CONNECT");

    @Autowired
    private Registry            metaServerRegistry;

    @Override
    public Object reply(Channel channel, GetNodesRequest getNodesRequest) {
        NodeChangeResult nodeChangeResult;
        try {
            nodeChangeResult = metaServerRegistry.getAllNodes(getNodesRequest.getNodeType());
            LOGGER.info("Get {} change node list {} success!from {}",
                getNodesRequest.getNodeType(), nodeChangeResult, channel.getRemoteAddress());
        } catch (Exception e) {
            LOGGER.error("Get node list error!", e);
            throw new RuntimeException("Get node list error!", e);
        }
        return nodeChangeResult;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return GetNodesRequest.class;
    }
}