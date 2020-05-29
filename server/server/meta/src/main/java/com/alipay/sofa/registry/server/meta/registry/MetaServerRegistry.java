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
package com.alipay.sofa.registry.server.meta.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.store.SessionStoreService;
import com.alipay.sofa.registry.server.meta.store.StoreService;

/**
 * factory func to operate StoreService
 *
 * @author shangyu.wh
 * @version $Id: MetaServerRegistry.java, v 0.1 2018-01-11 21:38 shangyu.wh Exp $
 */
public class MetaServerRegistry implements Registry<Node> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaServerRegistry.class);

    @Override
    public NodeChangeResult setNodes(List<Node> nodes) {
        if (nodes == null || nodes.size() == 0) {
            throw new IllegalArgumentException("Nodes is empty: " + nodes);
        }
        StoreService storeService = ServiceFactory.getStoreService(nodes.get(0).getNodeType());
        return storeService.setNodes(nodes);
    }

    @Override
    public NodeChangeResult register(Node node) {
        StoreService storeService = ServiceFactory.getStoreService(node.getNodeType());
        return storeService.addNode(node);
    }

    @Override
    public void cancel(String connectId, NodeType nodeType) {
        StoreService storeService = ServiceFactory.getStoreService(nodeType);
        storeService.removeNode(connectId);
    }

    @Override
    public void evict() {
        for (NodeType nodeType : NodeType.values()) {
            StoreService storeService = ServiceFactory.getStoreService(nodeType);
            if (storeService != null) {
                Collection<Node> expiredNodes = storeService.getExpired();
                if (expiredNodes != null && !expiredNodes.isEmpty()) {
                    storeService.removeNodes(expiredNodes);
                    LOGGER.info("Expired Nodes {} be evicted!", expiredNodes);
                }
            }
        }
    }

    @Override
    public void renew(Node node, int duration) {
        StoreService storeService = ServiceFactory.getStoreService(node.getNodeType());
        storeService.renew(node, duration);
    }

    @Override
    public void getOtherDataCenterNodeAndUpdate(NodeType nodeType) {
        StoreService storeService = ServiceFactory.getStoreService(nodeType);
        storeService.getOtherDataCenterNodeAndUpdate();
    }

    @Override
    public DataCenterNodes getDataCenterNodes(NodeType nodeType) {
        StoreService storeService = ServiceFactory.getStoreService(nodeType);
        return storeService.getDataCenterNodes();
    }

    @Override
    public NodeChangeResult getAllNodes(NodeType nodeType) {
        StoreService storeService = ServiceFactory.getStoreService(nodeType);
        return storeService.getNodeChangeResult();
    }

    @Override
    public void pushNodeListChange(NodeType nodeType) {
        StoreService storeService = ServiceFactory.getStoreService(nodeType);
        if (storeService != null) {
            storeService.pushNodeListChange();
        }
    }

    @Override
    public Map<String, Map<String, Map<String, Integer>>> sessionLoadbalance(int maxDisconnect) {
        SessionStoreService storeService = (SessionStoreService) ServiceFactory
            .getStoreService(NodeType.SESSION);
        return storeService.sessionLoadbalance(maxDisconnect);
    }
}