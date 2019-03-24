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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;

/**
 *
 * @author shangyu.wh
 * @version $Id: NodeChangeRequestHandler.java, v 0.1 2018-03-03 11:21 shangyu.wh Exp $
 */
public class NodeChangeResultHandler extends AbstractClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeChangeResultHandler.class);

    @Override
    public Object reply(Channel channel, Object message) {

        if (!(message instanceof NodeChangeResult)) {
            LOGGER.error("Request message type {} is not mach the require data type!", message
                .getClass().getName());
            return null;
        }
        NodeChangeResult nodeChangeResult = (NodeChangeResult) message;

        NodeManager nodeManager = NodeManagerFactory.getNodeManager(nodeChangeResult.getNodeType());
        nodeManager.updateNodes(nodeChangeResult);
        LOGGER.info("Update {} node list success!info:{}", nodeChangeResult.getNodeType(),
            nodeChangeResult);
        return null;
    }

    @Override
    protected NodeType getConnectNodeType() {
        return NodeType.META;
    }

    @Override
    public void received(Channel channel, Object message) {

    }

    @Override
    public Class interest() {
        return NodeChangeResult.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }
}