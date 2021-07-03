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
package com.alipay.sofa.registry.remoting.jersey;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * @author shangyu.wh
 * @version $Id: JerseyClient.java, v 0.1 2018-01-30 11:13 shangyu.wh Exp $
 */
public class JerseyClient implements Client {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyClient.class);
  private static final JerseyClient instance = new JerseyClient();

  private final AtomicReference<javax.ws.rs.client.Client> client = new AtomicReference<>(null);

  private Map<String, Channel> channels = new HashMap<>();

  /** constructor */
  public JerseyClient() {
    setClient(getClient(null));
  }

  /**
   * get instance of jerseyClient
   *
   * @return
   */
  public static JerseyClient getInstance() {
    return instance;
  }

  @Override
  public Channel connect(URL url) {
    try {
      JerseyChannel channel = new JerseyChannel(getTarget(url), getClient());
      channels.put(url.buildAddressString(), channel);
      return channel;
    } catch (Exception e) {
      LOGGER.error("Create jersey connect:" + url + " error!", e);
      throw new RuntimeException("Create jersey connect:" + url + " error!", e);
    }
  }

  @Override
  public Object sendSync(URL url, Object message, int timeoutMillis) {
    return null;
  }

  @Override
  public Object sendSync(Channel channel, Object message, int timeoutMillis) {
    return null;
  }

  @Override
  public void sendCallback(
      URL url, Object message, CallbackHandler callbackHandler, int timeoutMillis) {}

  private WebTarget getTarget(URL targetUrl) {
    return getClient().target(getBaseUri(targetUrl));
  }

  private javax.ws.rs.client.Client getClient(ClientConfig clientConfig) {
    if (clientConfig == null) {
      clientConfig = new ClientConfig();
      clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 3000);
      clientConfig.property(ClientProperties.READ_TIMEOUT, 5000);
    }

    clientConfig.connectorProvider(new HttpUrlConnectorProvider());

    clientConfig.register(JacksonFeature.class);

    return ClientBuilder.newClient(clientConfig);
  }

  public javax.ws.rs.client.Client getClient() {
    return client.get();
  }

  public void setClient(final javax.ws.rs.client.Client clientIn) {
    client.getAndSet(clientIn);
  }

  public URI getBaseUri(URL targetUrl) {
    URI uri;
    try {
      uri =
          UriBuilder.fromUri("http://" + targetUrl.getIpAddress() + "/")
              .port(targetUrl.getPort())
              .build();
    } catch (Exception e) {
      LOGGER.error("get server URI error!", e);
      throw new RuntimeException("get server URI error!", e);
    }
    return uri;
  }

  @Override
  public Channel getChannel(URL url) {
    Channel c = channels.get(url.buildAddressString());
    if (c == null) {
      return null;
    } else {
      if (!c.isConnected()) {
        connect(url);
      }
    }
    return c;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return NetUtil.getLocalSocketAddress();
  }

  @Override
  public void close() {}

  @Override
  public boolean isClosed() {
    return false;
  }
}
