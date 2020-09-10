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

import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.registry.Registry;

import java.util.concurrent.Executor;

/**
 * Handle session/data node's heartbeat request
 * @author shangyu.wh
 * @version $Id: RenewNodesRequestHandler.java, v 0.1 2018-03-30 19:58 shangyu.wh Exp $
 */
public class RenewNodesRequestHandler extends AbstractServerHandler<RenewNodesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenewNodesRequestHandler.class);

    @Autowired
    private Registry            metaServerRegistry;

    @Autowired
    private ExecutorManager     executorManager;

    @Override
    public Object reply(Channel channel, RenewNodesRequest renewNodesRequest) {
        Node renewNode = null;
        try {
            renewNode = renewNodesRequest.getNode();
            metaServerRegistry.renew(renewNode, renewNodesRequest.getDuration());
        } catch (Exception e) {
            LOGGER.error("Node " + renewNode + "renew error!", e);
            throw new RuntimeException("Node renew error!", e);
        }
        return null;
    }

    @Override
    public Class interest() {
        return RenewNodesRequest.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Executor getExecutor() {
        return executorManager.getRequestExecutor();
    }
}