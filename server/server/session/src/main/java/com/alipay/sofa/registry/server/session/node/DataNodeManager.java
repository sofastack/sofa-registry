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

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNodeManager.java, v 0.1 2017-12-08 19:34 shangyu.wh Exp $
 */
public class DataNodeManager extends AbstractNodeManager<DataNode> {

    private static final Logger      LOGGER = LoggerFactory.getLogger(DataNodeManager.class,
                                                "[DataNodeManager]");

    @Autowired
    private SessionServerConfig      sessionServerConfig;

    private ConsistentHash<DataNode> consistentHash;

    @Override
    public DataNode getNode(String dataInfoId) {
        DataNode dataNode = consistentHash.getNodeFor(dataInfoId);
        if (dataNode == null) {
            LOGGER.error("calculate data node error!,dataInfoId={}", dataInfoId);
            throw new RuntimeException("DataNodeManager calculate data node error!,dataInfoId="
                                       + dataInfoId);
        }
        return dataNode;
    }

    @Override
    public void updateNodes(NodeChangeResult nodeChangeResult) {
        write.lock();
        try {
            super.updateNodes(nodeChangeResult);
            consistentHash = new ConsistentHash(sessionServerConfig.getNumberOfReplicas(),
                getDataCenterNodes());

        } finally {
            write.unlock();
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.DATA;
    }

    @Override
    public void renewNode() {

    }

}