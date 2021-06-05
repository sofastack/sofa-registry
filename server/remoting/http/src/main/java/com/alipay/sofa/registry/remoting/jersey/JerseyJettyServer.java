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
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.jersey.jetty.server.HttpConnectionCustomFactory;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.ProcessingException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

/**
 * @author shangyu.wh
 * @version $Id: Jersey.java, v 0.1 2018-01-29 17:57 shangyu.wh Exp $
 */
public class JerseyJettyServer implements Server {
  private static final String NCSA_FORMAT = "%{client}a \"%r\" %s %O";
  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyJettyServer.class);

  private final ResourceConfig resourceConfig;

  private final URI baseUri;

  private org.eclipse.jetty.server.Server server;
  /** started status */
  private final AtomicBoolean isStarted = new AtomicBoolean(false);

  /**
   * constructor
   *
   * @param resourceConfig
   * @param baseUri
   */
  public JerseyJettyServer(ResourceConfig resourceConfig, URI baseUri) {
    this.resourceConfig = resourceConfig;
    this.baseUri = baseUri;
  }

  /** start jersey server */
  public void startServer() {
    if (isStarted.compareAndSet(false, true)) {
      try {
        Log.setLog(new Slf4jLog());
        server = createServer(getBaseUri(), resourceConfig, true);
      } catch (Throwable e) {
        isStarted.set(false);
        LOGGER.error("Start Jetty jersey server error!", e);
        throw new RuntimeException("Start Jetty jersey server error!", e);
      }
    }
  }

  public static org.eclipse.jetty.server.Server createServer(
      final URI uri, final ResourceConfig resourceConfig, final boolean start) {
    if (uri == null) {
      throw new IllegalArgumentException(LocalizationMessages.URI_CANNOT_BE_NULL());
    }

    JettyHttpContainer handler =
        ContainerFactory.createContainer(JettyHttpContainer.class, resourceConfig);

    int defaultPort = Container.DEFAULT_HTTP_PORT;

    final int port = (uri.getPort() == -1) ? defaultPort : uri.getPort();

    final org.eclipse.jetty.server.Server server =
        new org.eclipse.jetty.server.Server(new JettyConnectorThreadPool());
    // init requestLog
    Slf4jRequestLogWriter writer = new Slf4jRequestLogWriter();
    CustomRequestLog log = new CustomRequestLog(writer, NCSA_FORMAT);
    server.setRequestLog(log);
    final ServerConnector http = new ServerConnector(server, new HttpConnectionCustomFactory());
    http.setAcceptQueueSize(512);
    http.setPort(port);
    server.setConnectors(new Connector[] {http});
    if (handler != null) {
      server.setHandler(handler);
    }

    if (start) {
      try {
        // Start the server.
        server.start();
      } catch (Throwable e) {
        throw new ProcessingException(LocalizationMessages.ERROR_WHEN_CREATING_SERVER(), e);
      }
    }
    return server;
  }

  private static final class JettyConnectorThreadPool extends QueuedThreadPool {
    private final ThreadFactory threadFactory =
        new ThreadFactoryBuilder()
            .setNameFormat("jetty-http-server-%d")
            .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
            .build();

    @Override
    protected Thread newThread(Runnable runnable) {
      return threadFactory.newThread(runnable);
    }
  }

  @Override
  public boolean isOpen() {
    if (server != null) {
      return server.isStarted();
    }
    return false;
  }

  @Override
  public List<Channel> getChannels() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, Channel> selectAvailableChannelsForHostAddress() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, List<Channel>> selectAllAvailableChannelsForHostAddress() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Channel getChannel(InetSocketAddress remoteAddress) {
    return null;
  }

  @Override
  public Channel getChannel(URL url) {
    return null;
  }

  @Override
  public void close(Channel channel) {
    throw new UnsupportedOperationException("Jersey Server don't support close Channel.");
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return new InetSocketAddress(getBaseUri().getPort());
  }

  @Override
  public void close() {
    if (server != null) {
      try {
        server.stop();
      } catch (Throwable e) {
        LOGGER.error("Jersey Jetty Server stop error!", e);
        throw new RuntimeException("Jersey Jetty Server stop error!", e);
      }
      return;
    }
    throw new RuntimeException("Jersey Server has not started!Server Channel has not created!");
  }

  @Override
  public boolean isClosed() {
    if (server != null) {
      return server.isStopped();
    }
    return true;
  }

  @Override
  public Object sendSync(Channel channel, Object message, int timeoutMillis) {
    return null;
  }

  @Override
  public void sendCallback(
      Channel channel, Object message, CallbackHandler callbackHandler, int timeoutMillis) {}

  /**
   * Getter method for property <tt>baseUri</tt>.
   *
   * @return property value of baseUri
   */
  public URI getBaseUri() {
    return baseUri;
  }

  @Override
  public int getChannelCount() {
    return 0;
  }
}
