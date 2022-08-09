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
package com.alipay.sofa.registry.remoting.bolt.exchange;

import com.alipay.remoting.config.Configs;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltClient;
import com.alipay.sofa.registry.remoting.bolt.BoltServer;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.util.OsUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;

/**
 * @author shangyu.wh
 * @version $Id: BoltExchange.java, v 0.1 2017-11-27 15:47 shangyu.wh Exp $
 */
public class BoltExchange implements Exchange<ChannelHandler> {

  private final Map<String, Client> clients = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Integer, Server> serverMap = new ConcurrentHashMap<>();

  static {
    // def size=400, it is too big, and queueSize is too small
    System.setProperty(Configs.TP_QUEUE_SIZE, String.valueOf(1000 * 10));
    System.setProperty(Configs.TP_MIN_SIZE, String.valueOf(OsUtils.getCpuCount() * 10));
    System.setProperty(Configs.TP_MAX_SIZE, String.valueOf(OsUtils.getCpuCount() * 10));

    // bolt.tcp.heartbeat.maxtimes * bolt.tcp.heartbeat.interval
    // If the client has not sent heartbeat for 45 seconds, it is determined as down
    // The default value of 90s is too long. Change it to 45s to reduce the impact of physical
    // machine downtime
    if (StringUtils.isEmpty(System.getProperty(Configs.TCP_SERVER_IDLE))) {
      System.setProperty(Configs.TCP_SERVER_IDLE, String.valueOf(45000));
    }
  }

  @Override
  public Client connect(String serverType, URL serverUrl, ChannelHandler... channelHandlers) {
    return this.connect(serverType, 1, serverUrl, channelHandlers);
  }

  @Override
  public Client connect(
      String serverType, int connNum, URL serverUrl, ChannelHandler... channelHandlers) {
    if (channelHandlers == null) {
      throw new IllegalArgumentException("channelHandlers cannot be null!");
    }
    Client client =
        clients.computeIfAbsent(serverType, key -> newBoltClient(connNum, channelHandlers));
    // if serverUrl is null, just init the client
    // the conn will auto create if absent
    if (serverUrl != null) {
      client.connect(serverUrl);
    }
    return client;
  }

  @Override
  public Server open(URL url, ChannelHandler... channelHandlers) {
    BoltServer server = createServer(url, channelHandlers);
    server.startServer();
    return server;
  }

  private BoltServer createServer(URL url, ChannelHandler... channelHandlers) {
    if (channelHandlers == null) {
      throw new IllegalArgumentException("channelHandlers cannot be null!");
    }
    BoltServer server = createBoltServer(url, channelHandlers);
    setServer(server, url);
    return server;
  }

  @Override
  public Server open(
      URL url, int lowWaterMark, int highWaterMark, ChannelHandler... channelHandlers) {
    BoltServer server = createServer(url, channelHandlers);
    server.configWaterMark(lowWaterMark, highWaterMark);
    server.startServer();
    return server;
  }

  @Override
  public Client getClient(String serverType) {
    return clients.get(serverType);
  }

  @Override
  public Server getServer(Integer port) {
    return serverMap.get(port);
  }

  /**
   * add server into serverMap
   *
   * @param server
   * @param url
   */
  public void setServer(Server server, URL url) {
    serverMap.putIfAbsent(url.getPort(), server);
  }

  private BoltClient newBoltClient(int connNum, ChannelHandler[] channelHandlers) {
    BoltClient boltClient = createBoltClient(connNum);
    boltClient.initHandlers(Arrays.asList(channelHandlers));
    return boltClient;
  }

  protected BoltClient createBoltClient(int connNum) {
    return new BoltClient(connNum);
  }

  protected BoltServer createBoltServer(URL url, ChannelHandler[] channelHandlers) {
    return new BoltServer(url, Arrays.asList(channelHandlers));
  }
}
