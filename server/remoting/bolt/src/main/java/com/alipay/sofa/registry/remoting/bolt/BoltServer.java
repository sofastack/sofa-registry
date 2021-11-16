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

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.ChannelHandler.HandlerType;
import com.alipay.sofa.registry.remoting.ChannelHandler.InvokeType;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shangyu.wh
 * @version $Id: BoltServer.java, v 0.1 2017-11-24 18:05 shangyu.wh Exp $
 */
public class BoltServer implements Server {
  private static final Logger LOGGER = LoggerFactory.getLogger(BoltServer.class);

  /** accoding server port can not be null */
  protected final URL url;

  private final List<ChannelHandler> channelHandlers;
  /** bolt server */
  private final RpcServer boltServer;
  /** started status */
  private final AtomicBoolean isStarted = new AtomicBoolean(false);

  private final AtomicBoolean initHandler = new AtomicBoolean(false);

  /**
   * constructor
   *
   * @param url
   * @param channelHandlers
   */
  public BoltServer(URL url, List<ChannelHandler> channelHandlers) {
    this.channelHandlers = channelHandlers;
    this.url = url;
    this.boltServer = createRpcServer();
  }

  public void configWaterMark(int low, int high) {
    boltServer.initWriteBufferWaterMark(low, high);
    LOGGER.info("config watermark, low={}, high={}", low, high);
  }

  /** start bolt server */
  public void startServer() {
    if (isStarted.compareAndSet(false, true)) {
      try {
        initHandler();
        boltServer.start();

      } catch (Exception e) {
        isStarted.set(false);
        LOGGER.error("Start bolt server error!", e);
        throw new RuntimeException("Start bolt server error!", e);
      }
    }
  }

  private void stopServer() {
    if (boltServer != null && isStarted.get()) {
      try {
        boltServer.stop();
      } catch (Exception e) {
        LOGGER.error("Stop bolt server error!", e);
        throw new RuntimeException("Stop bolt server error!", e);
      }
    }
  }

  /** just init cant start */
  public void initServer() {
    try {
      initHandler();
    } catch (Exception e) {
      LOGGER.error("Init bolt server error!", e);
      throw new RuntimeException("Init bolt server error!", e);
    }
  }

  protected RpcServer createRpcServer() {
    return new RpcServer(url.getPort(), true);
  }

  private void initHandler() {
    if (initHandler.compareAndSet(false, true)) {
      boltServer.addConnectionEventProcessor(
          ConnectionEventType.CONNECT, newConnectionEventProcessor(ConnectionEventType.CONNECT));
      boltServer.addConnectionEventProcessor(
          ConnectionEventType.CLOSE, newConnectionEventProcessor(ConnectionEventType.CLOSE));
      boltServer.addConnectionEventProcessor(
          ConnectionEventType.EXCEPTION,
          newConnectionEventProcessor(ConnectionEventType.EXCEPTION));

      registerUserProcessorHandler();
    }
  }

  protected ConnectionEventProcessor newConnectionEventProcessor(ConnectionEventType type) {
    return new ConnectionEventAdapter(type, BoltUtil.getListenerHandlers(channelHandlers));
  }

  private void registerUserProcessorHandler() {
    for (ChannelHandler channelHandler : channelHandlers) {
      if (HandlerType.PROCESSER.equals(channelHandler.getType())) {
        if (InvokeType.SYNC.equals(channelHandler.getInvokeType())) {
          boltServer.registerUserProcessor(newSyncUserProcessorAdapter(channelHandler));
        } else {
          boltServer.registerUserProcessor(newAsyncUserProcessorAdapter(channelHandler));
        }
      }
    }
  }

  protected SyncUserProcessor newSyncUserProcessorAdapter(ChannelHandler channelHandler) {
    return new SyncUserProcessorAdapter(channelHandler);
  }

  protected AsyncUserProcessor newAsyncUserProcessorAdapter(ChannelHandler channelHandler) {
    return new AsyncUserProcessorAdapter(channelHandler);
  }

  @Override
  public boolean isOpen() {
    return isStarted.get();
  }

  @Override
  public List<Channel> getChannels() {
    Map<String, List<Connection>> conns = boltServer.getConnectionManager().getAll();
    if (conns.isEmpty()) {
      return Collections.emptyList();
    }
    List<Channel> ret = Lists.newArrayListWithCapacity(128);
    for (List<Connection> list : conns.values()) {
      for (Connection conn : list) {
        if (conn.isFine()) {
          BoltChannel boltChannel = new BoltChannel(conn);
          ret.add(boltChannel);
        }
      }
    }
    return ret;
  }

  @Override
  public Map<String, Channel> selectAvailableChannelsForHostAddress() {
    Map<String, List<Channel>> chns = selectAllAvailableChannelsForHostAddress();
    Map<String, Channel> ret = Maps.newHashMapWithExpectedSize(chns.size());
    for (Map.Entry<String, List<Channel>> e : chns.entrySet()) {
      List<Channel> list = e.getValue();
      ret.put(e.getKey(), CollectionUtils.getRandom(list));
    }
    return ret;
  }

  @Override
  public Map<String, List<Channel>> selectAllAvailableChannelsForHostAddress() {
    Collection<Channel> chnList = getChannels();
    if (chnList.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, List<Channel>> chns = Maps.newHashMapWithExpectedSize(128);
    for (Channel chn : chnList) {
      List<Channel> list =
          chns.computeIfAbsent(
              chn.getRemoteAddress().getAddress().getHostAddress(), k -> Lists.newArrayList());
      list.add(chn);
    }
    return chns;
  }

  @Override
  public Channel getChannel(InetSocketAddress remoteAddress) {
    URL url = new URL(remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
    return getChannel(url);
  }

  @Override
  public Channel getChannel(URL url) {
    Url key = new Url(url.getIpAddress(), url.getPort());
    Connection conn = boltServer.getConnectionManager().get(key.getUniqueKey());
    if (conn == null) {
      return null;
    }
    return new BoltChannel(conn);
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return new InetSocketAddress(url.getPort());
  }

  @Override
  public void close() {
    stopServer();
  }

  @Override
  public void close(Channel channel) {
    if (channel == null) {
      return;
    }
    BoltChannel boltChannel = (BoltChannel) channel;
    Connection connection = boltChannel.getConnection();
    if (connection.isFine()) {
      connection.close();
    }
  }

  @Override
  public boolean isClosed() {
    return !isStarted.get();
  }

  @Override
  public Object sendSync(Channel channel, Object message, int timeoutMillis) {
    BoltUtil.checkChannelConnected(channel);
    try {
      Url boltUrl = BoltUtil.createTargetUrl(channel);
      return boltServer.invokeSync(boltUrl, message, newInvokeContext(message), timeoutMillis);
    } catch (Throwable e) {
      throw BoltUtil.handleException("BoltServer", channel, e, "sendSync");
    }
  }

  @Override
  public void sendCallback(
      Channel channel, Object message, CallbackHandler callbackHandler, int timeoutMillis) {
    BoltUtil.checkChannelConnected(channel);
    try {
      Url boltUrl = BoltUtil.createTargetUrl(channel);
      boltServer.invokeWithCallback(
          boltUrl,
          message,
          newInvokeContext(message),
          new InvokeCallbackHandler(channel, callbackHandler),
          timeoutMillis);
    } catch (Throwable e) {
      throw BoltUtil.handleException("BoltServer", channel, e, "invokeCallback");
    }
  }

  protected InvokeContext newInvokeContext(Object request) {
    return null;
  }

  public RpcServer getRpcServer() {
    return boltServer;
  }

  @Override
  public int getChannelCount() {
    Map<String, List<Connection>> conns = boltServer.getConnectionManager().getAll();
    int count = 0;
    for (List<Connection> list : conns.values()) {
      for (Connection conn : list) {
        if (conn.isFine()) {
          count++;
        }
      }
    }
    return count;
  }
}
