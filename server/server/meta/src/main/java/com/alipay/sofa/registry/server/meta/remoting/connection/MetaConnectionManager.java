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
package com.alipay.sofa.registry.server.meta.remoting.connection;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle meta node's connect request
 *
 * @author shangyu.wh
 * @version $Id: MetaConnectionManager.java, v 0.1 2018-02-12 15:01 shangyu.wh Exp $
 */
public class MetaConnectionManager extends AbstractNodeConnectManager {

  @Autowired private NodeConfig nodeConfig;

  @Override
  public Collection<InetSocketAddress> getConnections(String dataCenter) {
    // get all address, the dataCenter arg is ignored
    Collection<InetSocketAddress> addresses = super.getConnections(dataCenter);
    if (addresses.isEmpty()) {
      return Collections.emptyList();
    }
    List<InetSocketAddress> ret = Lists.newArrayList();
    for (InetSocketAddress address : addresses) {
      String ipAddress = address.getAddress().getHostAddress();
      String dc = nodeConfig.getMetaDataCenter(ipAddress);
      if (dataCenter.equals(dc)) {
        ret.add(address);
      }
    }
    return ret;
  }

  @Override
  protected int getServerPort() {
    return metaServerConfig.getMetaServerPort();
  }

  @Override
  public NodeType getConnectNodeType() {
    return NodeType.META;
  }
}
