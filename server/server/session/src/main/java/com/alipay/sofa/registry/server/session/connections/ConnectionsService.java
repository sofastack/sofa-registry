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
package com.alipay.sofa.registry.server.session.connections;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ConnectionsService {

  @Autowired Exchange boltExchange;

  @Autowired DataStore sessionDataStore;

  @Autowired Interests sessionInterests;

  @Autowired Watchers sessionWatchers;

  @Autowired SessionServerConfig sessionServerConfig;

  public List<String> getConnections() {
    Server server = boltExchange.getServer(sessionServerConfig.getServerPort());
    return server.getChannels().stream()
        .map(
            channel ->
                channel.getRemoteAddress().getAddress().getHostAddress()
                    + ":"
                    + channel.getRemoteAddress().getPort())
        .collect(Collectors.toList());
  }

  /**
   * get connectIds by ip set
   *
   * @param ipSet ip set
   * @return
   */
  public List<ConnectId> getIpConnects(Set<String> ipSet) {
    return markChannelAndGetIpConnects(ipSet, null, null);
  }

  /**
   * get connectIds by ip set
   *
   * @param ipSet ip set
   * @return
   */
  public List<ConnectId> markChannelAndGetIpConnects(Set<String> ipSet, String key, Object value) {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null || CollectionUtils.isEmpty(ipSet)) {
      return Collections.emptyList();
    }
    List<ConnectId> connections = Lists.newArrayList();
    for (BoltChannel channel : searchChannels(ipSet)) {
      if (StringUtils.isNotBlank(key)) {
        channel.setConnAttribute(key, value);
      }
      connections.add(ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress()));
    }
    return connections;
  }

  private Collection<BoltChannel> searchChannels(Set<String> ipSet) {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null || CollectionUtils.isEmpty(ipSet)) {
      return Collections.emptyList();
    }
    Collection<Channel> channels = sessionServer.getChannels();
    List<BoltChannel> ret = Lists.newArrayListWithExpectedSize(ipSet.size());
    for (Channel channel : channels) {
      String ip = channel.getClientIP();
      if (ipSet.contains(ip)) {
        ret.add((BoltChannel) channel);
      }
    }
    return ret;
  }

  public List<Channel> getAllChannel() {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null) {
      return Collections.emptyList();
    }

    return sessionServer.getChannels();
  }

  /**
   * close ip connects
   *
   * @param ipList ip list
   * @return
   */
  public List<ConnectId> closeIpConnects(List<String> ipList) {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null || CollectionUtils.isEmpty(ipList)) {
      return Collections.emptyList();
    }
    List<ConnectId> connections = Lists.newArrayListWithExpectedSize(ipList.size());
    for (BoltChannel channel : searchChannels(Sets.newHashSet(ipList))) {
      sessionServer.close(channel);
      ConnectId connectId = ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress());
      connections.add(connectId);
    }
    return connections;
  }
}
