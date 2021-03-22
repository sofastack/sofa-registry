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
package com.alipay.sofa.registry.remoting.jersey.exchange;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.remoting.jersey.JerseyJettyServer;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author shangyu.wh
 * @version $Id: JerseyExchange.java, v 0.1 2018-01-29 19:49 shangyu.wh Exp $
 */
public class JerseyExchange implements Exchange<ResourceConfig> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyExchange.class);

  private ConcurrentHashMap<Integer, Server> serverMap = new ConcurrentHashMap<>();

  private Client client;

  @Override
  public Client connect(String serverType, URL serverUrl, ResourceConfig... channelHandlers) {
    JerseyClient jerseyClient = JerseyClient.getInstance();
    setClient(jerseyClient);
    return jerseyClient;
  }

  @Override
  public Client connect(
      String serverType, int connNum, URL serverUrl, ResourceConfig... channelHandlers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Server open(URL url, ResourceConfig... resources) {

    URI uri;
    try {
      uri = UriBuilder.fromUri("http://" + url.getIpAddress() + "/").port(url.getPort()).build();
    } catch (Exception e) {
      LOGGER.error("get server URI error!", e);
      throw new RuntimeException("get server URI error!", e);
    }
    JerseyJettyServer jerseyServer = new JerseyJettyServer(resources[0], uri);
    setServer(jerseyServer, url);
    jerseyServer.startServer();

    return jerseyServer;
  }

  @Override
  public Server open(
      URL url, int lowWaterMark, int highWaterMark, ResourceConfig... channelHandlers) {
    return open(url, channelHandlers);
  }

  @Override
  public Client getClient(String serverType) {
    if (null == client) {
      synchronized (JerseyExchange.class) {
        if (null == client) {
          JerseyClient jerseyClient = JerseyClient.getInstance();
          setClient(jerseyClient);
        }
      }
    }
    return client;
  }

  /**
   * Setter method for property <tt>client</tt>.
   *
   * @param client value to be assigned to property client
   */
  public void setClient(Client client) {
    this.client = client;
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
}
