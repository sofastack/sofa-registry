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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.lease.session.SessionManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.MetaServerHandler;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
public class SessionServerProvideDataNotifier extends AbstractProvideDataNotifier<SessionNode>
                                                                                              implements
                                                                                              ProvideDataNotifier {

    @Autowired
    private NodeExchanger         sessionNodeExchanger;

    @Autowired
    private AbstractServerHandler sessionConnectionHandler;

    @Autowired
    private SessionManager        sessionManager;

    @Override
    protected NodeExchanger getNodeExchanger() {
        return sessionNodeExchanger;
    }

    @Override
    protected List<SessionNode> getNodes() {
        return sessionManager.getClusterMembers();
    }

    @Override
    protected NodeConnectManager getNodeConnectManager() {
        if (!(sessionConnectionHandler instanceof NodeConnectManager)) {
            logger.error("sessionConnectionHandler inject is not NodeConnectManager instance!");
            throw new RuntimeException(
                "sessionConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) sessionConnectionHandler;
    }
}
