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
package com.alipay.sofa.registry.server.meta.remoting.session;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionManager;
import com.alipay.sofa.registry.server.meta.remoting.notifier.AbstractNotifier;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Dec 03, 2020
 */
@Component
public class DefaultSessionServerService extends AbstractNotifier<SessionNode>
    implements SessionServerService {

  @Autowired private SessionNodeExchanger sessionNodeExchanger;

  @Autowired private SessionConnectionManager sessionConnectionManager;

  @Autowired private SessionServerManager sessionServerManager;

  @Override
  protected NodeExchanger getNodeExchanger() {
    return sessionNodeExchanger;
  }

  @Override
  protected List<SessionNode> getNodes() {
    return sessionServerManager.getSessionServerMetaInfo().getClusterMembers();
  }

  @Override
  protected NodeConnectManager getNodeConnectManager() {
    return sessionConnectionManager;
  }

  @VisibleForTesting
  DefaultSessionServerService setSessionNodeExchanger(SessionNodeExchanger sessionNodeExchanger) {
    this.sessionNodeExchanger = sessionNodeExchanger;
    return this;
  }

  @VisibleForTesting
  DefaultSessionServerService setSessionConnectionHandler(
      SessionConnectionManager sessionConnectionManager) {
    this.sessionConnectionManager = sessionConnectionManager;
    return this;
  }

  @VisibleForTesting
  DefaultSessionServerService setSessionServerManager(SessionServerManager sessionServerManager) {
    this.sessionServerManager = sessionServerManager;
    return this;
  }
}
