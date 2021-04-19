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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.shared.remoting.ListenServerChannelHandler;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: ServerConnectionLisener.java, v 0.1 2017-11-30 15:04 shangyu.wh Exp $
 */
public class ClientNodeConnectionHandler extends ListenServerChannelHandler {

  @Autowired private Registry sessionRegistry;

  @Autowired private ExecutorManager executorManager;

  @Override
  public void disconnected(Channel channel) {
    super.disconnected(channel);
    fireCancelClient(channel);
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.CLIENT;
  }

  private void fireCancelClient(Channel channel) {
    // avoid block connect ConnectionEventExecutor thread pool
    executorManager
        .getConnectClientExecutor()
        .execute(
            () -> {
              ConnectId connectId =
                  ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress());
              sessionRegistry.cancel(Collections.singletonList(connectId));
            });
  }
}
