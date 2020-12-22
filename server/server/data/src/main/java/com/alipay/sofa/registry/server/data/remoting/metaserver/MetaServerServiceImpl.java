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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.DataHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaServerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author qian.lqlq
 * @version $Id: MetaServiceImpl.java, v 0.1 2018－03－07 20:41 qian.lqlq Exp $
 */
public class MetaServerServiceImpl extends AbstractMetaServerService<DataHeartBeatResponse> {

    @Autowired
    private SlotManager          slotManager;

    @Autowired
    private DataNodeExchanger    dataNodeExchanger;

    @Autowired
    private SessionNodeExchanger sessionNodeExchanger;

    @Override
    protected void handleRenewResult(DataHeartBeatResponse result) {
        metaNodeExchanger.setServerIps(result.getMetaNodesMap().keySet());
        metaNodeExchanger.notifyConnectServerAsync();

        dataNodeExchanger.setServerIps(getDataServerList());
        dataNodeExchanger.notifyConnectServerAsync();

        sessionNodeExchanger.setServerIps(getSessionNodes().keySet());
        sessionNodeExchanger.notifyConnectServerAsync();

        slotManager.updateSlotTable(result.getSlotTable());
    }

    @Override
    protected Node createNode() {
        return new DataNode(new URL(ServerEnv.IP), metaNodeExchanger.getLocalDataCenter());
    }
}