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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.SessionHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaServerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 20:05 yuzhi.lyz Exp $
 */
public final class MetaServerServiceImpl extends
                                        AbstractMetaServerService<SessionHeartBeatResponse> {

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private SlotTableCache      slotTableCache;

    @Autowired
    private DataNodeExchanger   dataNodeExchanger;

    @Override
    protected void handleRenewResult(SessionHeartBeatResponse result) {
        dataNodeExchanger.setServerIps(getDataServerList());
        dataNodeExchanger.notifyConnectServerAsync();

        slotTableCache.updateSlotTable(result.getSlotTable());
    }

    @Override
    protected Node createNode() {
        return new SessionNode(new URL(ServerEnv.IP), sessionServerConfig.getSessionServerRegion())
            .setProcessId(ServerEnv.PROCESS_ID);
    }

}
