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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ReNewNodesRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle session/data node's heartbeat request
 * @author shangyu.wh
 * @version $Id: ReNewNodesRequestHandler.java, v 0.1 2018-03-30 19:58 shangyu.wh Exp $
 */
public class ReNewNodesRequestHandler extends AbstractServerHandler<ReNewNodesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReNewNodesRequestHandler.class);

    @Autowired
    private Registry            metaServerRegistry;

    @Override
    public Object reply(Channel channel, ReNewNodesRequest reNewNodesRequest) {
        Node reNewNode = null;
        try {
            reNewNode = reNewNodesRequest.getNode();
            metaServerRegistry.reNew(reNewNode, reNewNodesRequest.getDuration());
        } catch (Exception e) {
            LOGGER.error("Node " + reNewNode + "reNew error!", e);
            throw new RuntimeException("Node reNew error!", e);
        }
        return null;
    }

    @Override
    public Class interest() {
        return ReNewNodesRequest.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }
}