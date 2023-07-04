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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.bolt.BoltClient;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-29 12:08 yuzhi.lyz Exp $
 */
public abstract class ClientSideExchanger
    implements NodeExchanger, ApplicationListener<ContextRefreshedEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientSideExchanger.class);
  protected final String serverType;

  @Autowired protected Exchange boltExchange;

  protected volatile Set<String> serverIps = Sets.newHashSet();
  private final Connector connector;

  protected ClientSideExchanger(String serverType) {
    this.serverType = serverType;
    this.connector = new Connector();
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    init();
  }

  public void init() {
    ConcurrentUtils.createDaemonThread(serverType + "-async-connector", connector).start();
    LOGGER.info("{} init connector", serverType);
  }

  @Override
  public Response request(Request request) throws RequestException {
    final URL url = request.getRequestUrl();
    if (url == null) {
      throw new RequestException("null url", request);
    }
    Client client = boltExchange.getClient(serverType);
    final int timeout = request.getTimeout() != null ? request.getTimeout() : getRpcTimeoutMillis();
    try {
      if (client == null) {
        connect(url);
        client = boltExchange.getClient(serverType);
      }
      CallbackHandler callback = request.getCallBackHandler();
      if (callback == null) {
        final Object result = client.sendSync(url, request.getRequestBody(), timeout);
        return () -> result;
      } else {
        client.sendCallback(url, request.getRequestBody(), callback, timeout);
        return () -> Response.ResultStatus.SUCCESSFUL;
      }
    } catch (Throwable e) {
      throw new RequestException(
          serverType + "Exchanger request error! Request url:" + url, request, e);
    }
  }

  public Response requestRaw(String ip, Object raw) throws RequestException {
    Request req = new SimpleRequest(raw, new URL(ip, getServerPort()));
    return request(req);
  }

  @Override
  public Client connectServer() {
    Set<String> ips = serverIps;
    if (!ips.isEmpty()) {
      int count = tryConnectAllServer(ips);
      if (count == 0) {
        throw new RuntimeException("failed to connect any servers, " + ips);
      }
    }
    return getClient();
  }

  public Client getClient() {
    return boltExchange.getClient(serverType);
  }

  protected int tryConnectAllServer(Set<String> ips) {
    int connectCount = 0;
    for (String node : ips) {
      URL url = new URL(node, getServerPort());
      try {
        connect(url);
        connectCount++;
      } catch (Throwable e) {
        LOGGER.error("Exchanger connect server error!url:" + url, e);
      }
    }
    return connectCount;
  }

  public Channel connect(URL url) {
    Client client = getClient();
    if (client == null) {
      synchronized (this) {
        client = getClient();
        if (client == null) {
          client =
              boltExchange.connect(
                  serverType,
                  getConnNum(),
                  url,
                  getClientHandlers().toArray(new ChannelHandler[0]));
        }
      }
    }
    Channel channel = client.getChannel(url);
    if (channel == null) {
      synchronized (this) {
        channel = client.getChannel(url);
        if (channel == null) {
          channel = client.connect(url);
        }
      }
    }
    return channel;
  }

  public Map<String, List<Connection>> getConnections() {
    Client client = boltExchange.getClient(serverType);
    if (client == null) {
      return Collections.emptyMap();
    }
    return ((BoltClient) client).getConnections();
  }

  public void notifyConnectServerAsync() {
    connector.wakeup();
  }

  private final class Connector extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      Set<String> ips = serverIps;
      try {
        tryConnectAllServer(ips);
      } catch (Throwable e) {
        LOGGER.error("failded to connect {}", ips, e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return 3000;
    }
  }

  public abstract int getRpcTimeoutMillis();

  public abstract int getServerPort();

  public int getConnNum() {
    return 1;
  }

  protected abstract Collection<ChannelHandler> getClientHandlers();

  public Set<String> getServerIps() {
    return serverIps;
  }

  public synchronized void setServerIps(Collection<String> serverIps) {
    this.serverIps = Collections.unmodifiableSet(Sets.newHashSet(serverIps));
  }

  @VisibleForTesting
  public void setBoltExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
  }
}
