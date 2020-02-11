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
package com.alipay.sofa.registry.server.data.remoting.metaserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent.FromType;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.MetaServerChangeEvent;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;

/**
 *
 * @author qian.lqlq
 * @version $Id: ServerChangeProcessor.java, v 0.1 2018-03-13 14:16 qian.lqlq Exp $
 */
public class ServerChangeHandler extends AbstractClientHandler<NodeChangeResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChangeHandler.class);

    @Autowired
    private EventCenter         eventCenter;

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Override
    public void checkParam(NodeChangeResult request) throws RuntimeException {

    }

    @Override
    public Object doHandle(Channel channel, NodeChangeResult request) {
        LOGGER.info("Received NodeChangeResult: {}", request);
        ExecutorFactory.getCommonExecutor().execute(() -> {
            if (request.getNodeType() == NodeType.DATA) {
                eventCenter.post(new DataServerChangeEvent(request.getNodes(),
                        request.getDataCenterListVersions(), FromType.META_NOTIFY));
            } else if (request.getNodeType() == NodeType.META) {
                Map<String, Map<String, MetaNode>> metaNodesMap = request.getNodes();
                if (metaNodesMap != null && !metaNodesMap.isEmpty()) {
                    Map<String, MetaNode> metaNodeMap = metaNodesMap.get(dataServerConfig.getLocalDataCenter());
                    if (metaNodeMap != null && !metaNodeMap.isEmpty()) {
                        HashMap<String, Set<String>> map = new HashMap<>();
                        map.put(dataServerConfig.getLocalDataCenter(), metaNodeMap.keySet());
                        eventCenter.post(new MetaServerChangeEvent(map));
                    }
                }
            }
        });
        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public Class interest() {
        return NodeChangeResult.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}