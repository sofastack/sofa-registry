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

import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractNodeConnectManager implements NodeConnectManager {
  @Autowired protected Exchange boltExchange;
  @Autowired protected MetaServerConfig metaServerConfig;

  @Override
  public Collection<InetSocketAddress> getConnections(String dataCenter) {
    Server server = boltExchange.getServer(getServerPort());
    if (server == null) {
      return Collections.emptyList();
    }
    List<Channel> channels = server.getChannels();
    List<InetSocketAddress> ret = Lists.newArrayListWithCapacity(channels.size());
    for (Channel channel : channels) {
      ret.add(channel.getRemoteAddress());
    }
    return ret;
  }

  protected abstract int getServerPort();
}
