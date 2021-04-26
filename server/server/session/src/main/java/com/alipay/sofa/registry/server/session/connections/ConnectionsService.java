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
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public class ConnectionsService {

  @Autowired Exchange boltExchange;

  @Autowired DataStore sessionDataStore;

  @Autowired Interests sessionInterests;

  @Autowired Watchers sessionWatchers;

  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired ConnectionMapper connectionMapper;

  public List<String> getConnections() {
    Server server = boltExchange.getServer(sessionServerConfig.getServerPort());
    Set<String> boltConnectIds =
        server.getChannels().stream()
            .map(
                channel ->
                    channel.getRemoteAddress().getAddress().getHostAddress()
                        + ":"
                        + channel.getRemoteAddress().getPort())
            .collect(Collectors.toSet());
    Set<String> connectIds = new HashSet<>();
    connectIds.addAll(
        sessionDataStore.getConnectIds().stream()
            .map(connectId -> connectId.clientAddress())
            .collect(Collectors.toList()));
    connectIds.addAll(
        sessionInterests.getConnectIds().stream()
            .map(connectId -> connectId.clientAddress())
            .collect(Collectors.toList()));
    connectIds.addAll(
        sessionWatchers.getConnectIds().stream()
            .map(connectId -> connectId.clientAddress())
            .collect(Collectors.toList()));
    connectIds.retainAll(boltConnectIds);
    return new ArrayList<>(connectIds);
  }
  /**
   * get connectIds by ip list
   *
   * @param _ipList ip list
   * @return
   */
  public List<ConnectId> getIpConnects(List<String> _ipList) {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null) {
      return Collections.emptyList();
    }
    List<ConnectId> connections = Lists.newArrayList();
    Set<String> ipSet = Sets.newHashSet(_ipList);
    Collection<Channel> channels = sessionServer.getChannels();
    for (Channel channel : channels) {
      String key = NetUtil.toAddressString(channel.getRemoteAddress());
      String ip = getIpFromConnectId(key);
      if (ipSet.contains(ip)) {
        connections.add(ConnectId.of(key, NetUtil.toAddressString(channel.getLocalAddress())));
      }
    }

    return connections;
  }

  /**
   * close ip connects
   *
   * @param _ipList ip list
   * @return
   */
  public List<String> closeIpConnects(List<String> _ipList) {
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null) {
      return Collections.emptyList();
    }
    List<String> connections = new ArrayList<>();
    Collection<Channel> channels = sessionServer.getChannels();
    Set<String> ipSet = Sets.newHashSet(_ipList);
    for (Channel channel : channels) {
      String key = NetUtil.toAddressString(channel.getRemoteAddress());
      String ip = getIpFromConnectId(key);
      if (ipSet.contains(ip)) {
        sessionServer.close(channel);
        connections.add(
            key
                + ValueConstants.CONNECT_ID_SPLIT
                + NetUtil.toAddressString(channel.getLocalAddress()));
      }
    }
    return connections;
  }

  private String getIpFromConnectId(String connectId) {
    if (connectionMapper.contains(connectId)) {
      return connectionMapper.get(connectId);
    } else {
      return connectId.substring(0, connectId.indexOf(':'));
    }
  }
}
