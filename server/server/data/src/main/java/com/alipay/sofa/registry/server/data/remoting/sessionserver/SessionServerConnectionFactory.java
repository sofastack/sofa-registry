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
package com.alipay.sofa.registry.server.data.remoting.sessionserver;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.ProcessIdCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * the factory to hold SessionServer connections
 *
 * @author qian.lqlq
 * @author kezhu.wukz
 * @version $Id: SessionServerConnectionFactory.java, v 0.1 2017-12-06 15:48 qian.lqlq Exp $
 */
public class SessionServerConnectionFactory {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(SessionServerConnectionFactory.class);

  private final Map<String, Channels> session2Connections = new ConcurrentHashMap<>();

  public boolean registerSession(ProcessId processId, Channel channel) {
    if (channel == null) {
      LOGGER.warn("registerSession with null channel, {}", processId);
      return false;
    }
    final InetSocketAddress remoteAddress = channel.getRemoteAddress();
    final Connection conn = ((BoltChannel) channel).getConnection();
    if (remoteAddress == null || conn == null) {
      LOGGER.warn("registerSession with null channel.connection, {}", processId);
      return false;
    }

    Channels channels =
        session2Connections.computeIfAbsent(
            remoteAddress.getAddress().getHostAddress(), k -> new Channels());
    Tuple<ProcessId, Connection> exist = channels.channels.get(remoteAddress);
    if (exist != null) {
      if (exist.o1.equals(processId)) {
        // the same
        return false;
      }
      LOGGER.warn(
          "registerSession channel {} has conflict processId, exist={}, register={}",
          channel,
          exist.o1,
          processId);
    }
    processId = ProcessIdCache.cache(processId);
    // maybe register happens after disconnect or parallely, at that time, connection is not fine
    // synchronized avoid the case: isFine=true->disconnect.remove->register.put
    synchronized (this) {
      if (!channel.isConnected()) {
        LOGGER.warn("registerSession with unconnected channel, {}, {}", processId, remoteAddress);
        return false;
      }
      channels.channels.put(remoteAddress, new Tuple<>(processId, conn));
    }

    LOGGER.info(
        "registerSession {}, processId={}, channelSize={}",
        channel,
        processId,
        channels.channels.size());
    return true;
  }

  /**
   * session disconnected, The SessionServerDisconnectEvent is triggered only when the last
   * connections is removed
   */
  public void sessionDisconnected(Channel channel) {
    final InetSocketAddress remoteAddress = channel.getRemoteAddress();
    if (remoteAddress == null) {
      LOGGER.warn("sessionDisconnected with null channel.connection");
      return;
    }
    final Channels channels = session2Connections.get(remoteAddress.getAddress().getHostAddress());
    if (channels == null) {
      LOGGER.warn("sessionDisconnected not found channels {}", remoteAddress);
      return;
    }
    Tuple<ProcessId, Connection> tuple = null;
    synchronized (this) {
      tuple = channels.channels.remove(remoteAddress);
    }
    LOGGER.info(
        "sessionDisconnected, removed={}, processId={}, channelSize={}",
        channel,
        tuple != null ? tuple.o1 : null,
        channels.channels.size());
  }

  /** get the map of sessionIp -> connection */
  public Map<String, Connection> getSessionConnectionMap() {
    Map<String, Connection> map = Maps.newHashMapWithExpectedSize(session2Connections.size());
    for (Map.Entry<String, Channels> e : session2Connections.entrySet()) {
      Channels channels = e.getValue();
      Connection conn = channels.chooseConnection();
      if (conn != null) {
        map.put(e.getKey(), conn);
      }
    }
    return map;
  }

  public Map<String, List<Connection>> getAllSessionConnections() {
    Map<String, List<Connection>> map = Maps.newHashMapWithExpectedSize(session2Connections.size());
    for (Map.Entry<String, Channels> e : session2Connections.entrySet()) {
      Channels channels = e.getValue();
      List<Connection> conns = new ArrayList<>(channels.channels.size());
      for (Tuple<ProcessId, Connection> t : channels.channels.values()) {
        conns.add(t.o2);
      }
      map.put(e.getKey(), conns);
    }
    return map;
  }

  public boolean containsConnection(ProcessId sessionProcessId) {
    for (Channels p : session2Connections.values()) {
      if (p.contains(sessionProcessId)) {
        return true;
      }
    }
    return false;
  }

  /** get connections of SessionServer ( Randomly select a connection for each session ) */
  public List<Connection> getSessionConnections() {
    return new ArrayList<>(getSessionConnectionMap().values());
  }

  public Set<ProcessId> getProcessIds() {
    Set<ProcessId> set = Sets.newHashSet();
    session2Connections.values().forEach(c -> set.addAll(c.getProcessIds()));
    return set;
  }

  /** convenient class to store sessionConnAddress and connection */
  private static final class Channels {
    final AtomicInteger roundRobin = new AtomicInteger(-1);
    // remoteAddress as key
    final Map<InetSocketAddress, Tuple<ProcessId, Connection>> channels = Maps.newConcurrentMap();

    Connection chooseConnection() {
      List<Tuple<ProcessId, Connection>> list = Lists.newArrayList(channels.values());
      if (list.isEmpty()) {
        return null;
      }
      int n = roundRobin.incrementAndGet();
      if (n < 0) {
        roundRobin.compareAndSet(n, 0);
        n = (n == Integer.MIN_VALUE) ? 0 : Math.abs(n);
      }
      n = n % list.size();
      return list.get(n).o2;
    }

    boolean contains(ProcessId processId) {
      for (Tuple<ProcessId, Connection> t : channels.values()) {
        if (t.o1.equals(processId)) {
          return true;
        }
      }
      return false;
    }

    Set<ProcessId> getProcessIds() {
      return channels.values().stream().map(t -> t.o1).collect(Collectors.toSet());
    }
  }
}
