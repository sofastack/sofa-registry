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

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle data node's register request
 * @author shangyu.wh
 * @version $Id: DataNodeHandler.java, v 0.1 2018-01-18 18:04 shangyu.wh Exp $
 */
public class DataNodeHandler extends AbstractServerHandler<DataNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataNodeHandler.class);

    @Autowired
    private Registry            metaServerRegistry;

    @Override
    public Object reply(Channel channel, DataNode dataNode) {
        NodeChangeResult nodeChangeResult;
        try {
            nodeChangeResult = metaServerRegistry.register(dataNode);
            LOGGER.info("Data node {} register success!result:{}", dataNode, nodeChangeResult);
        } catch (Exception e) {
            LOGGER.error("Data node register error!", e);
            throw new RuntimeException("Data node register error!", e);
        }
        return nodeChangeResult;
    }

    @Override
    public Class interest() {
        return DataNode.class;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

}