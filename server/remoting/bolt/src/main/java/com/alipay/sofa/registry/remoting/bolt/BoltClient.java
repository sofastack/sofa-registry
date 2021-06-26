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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.Url;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.*;
import com.alipay.sofa.registry.remoting.ChannelHandler.HandlerType;
import com.alipay.sofa.registry.remoting.ChannelHandler.InvokeType;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Bolt client.
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id : BoltClient.java, v 0.1 2017-11-27 14:46 shangyu.wh Exp $
 */
public class BoltClient implements Client {

  private static final Logger LOGGER = LoggerFactory.getLogger(BoltClient.class);

  private final RpcClient rpcClient;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private int connectTimeout = 2000;

  private final int connNum;

  /** Instantiates a new Bolt client. */
  public BoltClient(int connNum) {
    rpcClient = new RpcClient();
    configIO();
    rpcClient.init();

    this.connNum = connNum;
  }

  private void configIO() {
    final int low = Integer.getInteger(Configs.NETTY_BUFFER_LOW_WATERMARK, 1024 * 256);
    final int high = Integer.getInteger(Configs.NETTY_BUFFER_HIGH_WATERMARK, 1024 * 288);
    rpcClient.initWriteBufferWaterMark(low, high);
    LOGGER.info("config watermark, low={}, high={}", low, high);
  }

  public Map<String, List<Connection>> getConnections() {
    return rpcClient.getAllManagedConnections();
  }

  /**
   * Setter method for property <tt>channelHandlers</tt>.
   *
   * @param channelHandlers value to be assigned to property channelHandlers
   */
  public void initHandlers(List<ChannelHandler> channelHandlers) {
    final ChannelHandler connectionEventHandler = BoltUtil.getListenerHandlers(channelHandlers);

    rpcClient.addConnectionEventProcessor(
        ConnectionEventType.CONNECT,
        newConnectionEventAdapter(connectionEventHandler, ConnectionEventType.CONNECT));
    rpcClient.addConnectionEventProcessor(
        ConnectionEventType.CLOSE,
        newConnectionEventAdapter(connectionEventHandler, ConnectionEventType.CLOSE));
    rpcClient.addConnectionEventProcessor(
        ConnectionEventType.EXCEPTION,
        newConnectionEventAdapter(connectionEventHandler, ConnectionEventType.EXCEPTION));

    for (ChannelHandler channelHandler : channelHandlers) {
      if (HandlerType.PROCESSER.equals(channelHandler.getType())) {
        if (InvokeType.SYNC.equals(channelHandler.getInvokeType())) {
          rpcClient.registerUserProcessor(newSyncProcessor(channelHandler));
        } else {
          rpcClient.registerUserProcessor(newAsyncProcessor(channelHandler));
        }
      }
    }
  }

  protected ConnectionEventProcessor newConnectionEventAdapter(
      ChannelHandler connectionEventHandler, ConnectionEventType connectEventType) {
    return new ConnectionEventAdapter(connectEventType, connectionEventHandler);
  }

  protected AsyncUserProcessorAdapter newAsyncProcessor(ChannelHandler channelHandler) {
    return new AsyncUserProcessorAdapter(channelHandler);
  }

  protected SyncUserProcessorAdapter newSyncProcessor(ChannelHandler channelHandler) {
    return new SyncUserProcessorAdapter(channelHandler);
  }

  @Override
  public Channel connect(URL url) {
    if (url == null) {
      throw new IllegalArgumentException("Create connection url can not be null!");
    }
    try {
      Connection connection = getBoltConnection(rpcClient, url);
      return new BoltChannel(connection);
    } catch (RemotingException e) {
      throw BoltUtil.handleException("BoltClient", url, e, "connect");
    }
  }

  protected Connection getBoltConnection(RpcClient rpcClient, URL url) throws RemotingException {
    Url boltUrl = createBoltUrl(url);
    try {
      Connection connection = rpcClient.getConnection(boltUrl, connectTimeout);
      if (connection == null || !connection.isFine()) {
        if (connection != null) {
          connection.close();
        }
        throw new ChannelConnectException("Get bolt connection failed for boltUrl: " + boltUrl);
      }
      return connection;
    } catch (InterruptedException e) {
      throw BoltUtil.handleException("BoltClient", boltUrl, e, "getConnection");
    }
  }

  protected Url createBoltUrl(URL url) {
    Url boltUrl = new Url(url.getIpAddress(), url.getPort());
    boltUrl.setProtocol(RpcProtocol.PROTOCOL_CODE);
    boltUrl.setConnNum(connNum);
    boltUrl.setConnWarmup(true);
    return boltUrl;
  }

  @Override
  public Channel getChannel(URL url) {
    try {
      Connection connection = getBoltConnection(rpcClient, url);
      BoltChannel channel = new BoltChannel(connection);
      return channel;
    } catch (Throwable e) {
      throw BoltUtil.handleException("BoltClient", url, e, "getChannel");
    }
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return NetUtil.getLocalSocketAddress();
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      rpcClient.shutdown();
    }
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  @Override
  public Object sendSync(URL url, Object message, int timeoutMillis) {
    try {
      Url boltUrl = createBoltUrl(url);
      return rpcClient.invokeSync(boltUrl, message, timeoutMillis);
    } catch (Throwable e) {
      throw BoltUtil.handleException("BoltClient", url, e, "sendSync");
    }
  }

  @Override
  public Object sendSync(Channel channel, Object message, int timeoutMillis) {
    BoltUtil.checkChannelConnected(channel);
    try {
      return rpcClient.invokeSync(((BoltChannel) channel).getConnection(), message, timeoutMillis);
    } catch (Throwable e) {
      throw BoltUtil.handleException("BoltClient", channel, e, "sendSync");
    }
  }

  @Override
  public void sendCallback(
      URL url, Object message, CallbackHandler callbackHandler, int timeoutMillis) {
    try {
      Connection connection = getBoltConnection(rpcClient, url);
      rpcClient.invokeWithCallback(
          connection,
          message,
          new InvokeCallbackHandler(new BoltChannel(connection), callbackHandler),
          timeoutMillis);
      return;
    } catch (RemotingException e) {
      throw BoltUtil.handleException("BoltClient", url, e, "sendCallback");
    }
  }
}
