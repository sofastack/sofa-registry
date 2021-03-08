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
package com.alipay.sofa.registry.server.meta.remoting.data;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.notifier.AbstractNotifier;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
@Component
public class DefaultDataServerService extends AbstractNotifier<DataNode> implements
                                                                        DataServerService {

    @Autowired
    private DataNodeExchanger dataNodeExchanger;

    @Autowired
    private AbstractServerHandler dataConnectionHandler;

    @Autowired
    private DataServerManager     dataServerManager;

    @Override
    protected NodeExchanger getNodeExchanger() {
        return dataNodeExchanger;
    }

    @Override
    protected List<DataNode> getNodes() {
        return dataServerManager.getDataServerMetaInfo().getClusterMembers();
    }

    @Override
    protected NodeConnectManager getNodeConnectManager() {
        if (!(dataConnectionHandler instanceof NodeConnectManager)) {
            logger.error("dataConnectionHandler inject is not NodeConnectManager instance!");
            throw new SofaRegistryRuntimeException(
                "dataConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) dataConnectionHandler;
    }

    @VisibleForTesting
    DefaultDataServerService setDataNodeExchanger(DataNodeExchanger dataNodeExchanger) {
        this.dataNodeExchanger = dataNodeExchanger;
        return this;
    }

    @VisibleForTesting
    DefaultDataServerService setDataConnectionHandler(AbstractServerHandler dataConnectionHandler) {
        this.dataConnectionHandler = dataConnectionHandler;
        return this;
    }

    @VisibleForTesting
    DefaultDataServerService setDataServerManager(DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }
}
