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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.remoting.ServerManager;
import com.alipay.sofa.registry.client.remoting.ServerNode;
import com.alipay.sofa.registry.client.util.ServerNodeParser;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Direct server manager. Return server node RegistryClientConfig.getRegistryEndpoint()+
 * PORT 9600
 *
 * @author zhiqiang.li
 * @version $Id : DirectServerManager.java, v 0.1 2022-03-18 13:37 zhiqiang.li Exp $$
 */
public class DirectServerManager implements ServerManager {

  private final List<ServerNode> serverNodes;

  public DirectServerManager(RegistryClientConfig config) {
    this.serverNodes = new ArrayList<ServerNode>();
    int port = config.getRegistryEndpointPort();
    if (port == 0) {
      port = 9600;
    }
    this.serverNodes.add(
        ServerNodeParser.parse(String.format("%s:%s", config.getRegistryEndpoint(), port)));
  }

  @Override
  public List<ServerNode> getServerList() {
    return serverNodes;
  }

  @Override
  public ServerNode random() {
    return serverNodes.get(0);
  }
}
