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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModified;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.ArrangeTaskDispatcher;
import com.alipay.sofa.registry.server.meta.slot.SlotArranger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */

public class DefaultSlotArranger implements SlotArranger, Observer {

    private static final Logger             logger = LoggerFactory
                                                       .getLogger(DefaultSlotArranger.class);

    @Autowired
    private DataServerManager               dataServerManager;

    @Autowired
    private ArrangeTaskDispatcher<DataNode> arrangeTaskDispatcher;

    @PostConstruct
    public void postConstruct() {
        dataServerManager.addObserver(this);
    }

    @PreDestroy
    public void preDestroy() {
        dataServerManager.removeObserver(this);
    }

    @Override
    public void update(Observable source, Object message) {
        if (logger.isInfoEnabled()) {
            logger.info("[update] source: {}, message: {}", source, message);
        }
        if (message instanceof NodeAdded) {
            onServerAdded(((NodeAdded<DataNode>) message).getNode());
        }
        if (message instanceof NodeRemoved) {
            onServerRemoved(((NodeRemoved<DataNode>) message).getNode());
        }
        if (message instanceof NodeModified) {
            NodeModified<DataNode> nodeModified = (NodeModified<DataNode>) message;
            onServerChanged(nodeModified.getOldNode(), nodeModified.getNewNode());
        }
    }

    @Override
    public void onServerAdded(DataNode newNode) {
        arrangeTaskDispatcher.serverAlive(newNode);
    }

    @Override
    public void onServerRemoved(DataNode node) {
        arrangeTaskDispatcher.serverDead(node);
    }

    @Override
    public void onServerChanged(DataNode oldNode, DataNode newNode) {

    }
}
