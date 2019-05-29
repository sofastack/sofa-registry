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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.ChannelHandler.HandlerType;
import com.alipay.sofa.registry.remoting.ChannelHandler.InvokeType;
import com.alipay.sofa.registry.remoting.Server;

/**
 *
 * @author shangyu.wh
 * @version $Id: BoltServer.java, v 0.1 2017-11-24 18:05 shangyu.wh Exp $
 */
public class BoltServer implements Server {

    private static final Logger        LOGGER      = LoggerFactory.getLogger(BoltServer.class);

    private static final Logger        PUSH_LOGGER = LoggerFactory.getLogger("SESSION-PUSH",
                                                       "[Server]");
    /**
     * accoding server port
     * can not be null
     */
    private final URL                  url;
    private final List<ChannelHandler> channelHandlers;
    /**
     * bolt server
     */
    private RpcServer                  boltServer;
    /**
     * started status
     */
    private AtomicBoolean              isStarted   = new AtomicBoolean(false);
    private Map<String, Channel>       channels    = new ConcurrentHashMap<>();

    private AtomicBoolean              initHandler = new AtomicBoolean(false);

    /**
     * constructor
     * @param url
     * @param channelHandlers
     */
    public BoltServer(URL url, List<ChannelHandler> channelHandlers) {
        this.channelHandlers = channelHandlers;
        this.url = url;
    }

    /**
     * start bolt server
     */
    public void startServer() {
        if (isStarted.compareAndSet(false, true)) {
            try {
                boltServer = new RpcServer(url.getPort(), true);
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

    /**
     * just init cant start
     */
    public void initServer() {
        try {
            boltServer = new RpcServer(url.getPort(), true);
            initHandler();
        } catch (Exception e) {
            LOGGER.error("Init bolt server error!", e);
            throw new RuntimeException("Init bolt server error!", e);
        }
    }

    private void initHandler() {
        if (initHandler.compareAndSet(false, true)) {
            boltServer.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                new ConnectionEventAdapter(ConnectionEventType.CONNECT,
                    getConnectionEventHandler(), this));
            boltServer.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                new ConnectionEventAdapter(ConnectionEventType.CLOSE, getConnectionEventHandler(),
                    this));
            boltServer.addConnectionEventProcessor(ConnectionEventType.EXCEPTION,
                new ConnectionEventAdapter(ConnectionEventType.EXCEPTION,
                    getConnectionEventHandler(), this));

            registerUserProcessorHandler();
        }
    }

    private ChannelHandler getConnectionEventHandler() {
        if (channelHandlers != null) {
            for (ChannelHandler channelHandler : channelHandlers) {
                if (HandlerType.LISENTER.equals(channelHandler.getType())) {
                    return channelHandler;
                }
            }
        }
        return null;
    }

    private void registerUserProcessorHandler() {
        if (channelHandlers != null) {
            for (ChannelHandler channelHandler : channelHandlers) {
                if (HandlerType.PROCESSER.equals(channelHandler.getType())) {
                    if (InvokeType.SYNC.equals(channelHandler.getInvokeType())) {
                        boltServer.registerUserProcessor(new SyncUserProcessorAdapter(
                            channelHandler));
                    } else {
                        boltServer.registerUserProcessor(new AsyncUserProcessorAdapter(
                            channelHandler));
                    }
                }
            }
        }
    }

    @Override
    public boolean isOpen() {
        return isStarted.get();
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<>();
        if (!this.channels.isEmpty()) {
            for (Iterator<Channel> it = this.channels.values().iterator(); it.hasNext();) {
                Channel channel = it.next();
                if (channel.isConnected()) {
                    chs.add(channel);
                } else {
                    it.remove();
                }
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        Channel channel = channels.get(NetUtil.toAddressString(remoteAddress));
        if (channel != null && channel.isConnected()) {
            return channel;
        }
        return null;
    }

    @Override
    public Channel getChannel(URL url) {
        Channel channel = channels.get(url.getAddressString());
        if (channel != null && channel.isConnected()) {
            return channel;
        }
        return null;
    }

    @Override
    public List<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(url.getPort());
    }

    @Override
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            stopServer();
        }
    }

    @Override
    public void close(Channel channel) {
        if (null != channel) {
            channels.remove(NetUtil.toAddressString(channel.getRemoteAddress()));
            BoltChannel boltChannel = (BoltChannel) channel;
            Connection connection = boltChannel.getConnection();
            if (null != connection && connection.isFine()) {
                connection.close();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return !isStarted.get();
    }

    @Override
    public void sendOneway(Channel channel, Object message) {
        if (channel != null && channel.isConnected()) {
            try {
                Url url = new Url(channel.getRemoteAddress().getAddress().getHostAddress(), channel
                    .getRemoteAddress().getPort());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bolt Server one way message:{} , target url:{}", message, url);
                }
                boltServer.oneway(url, message);
            } catch (RemotingException e) {
                LOGGER.error("Bolt Server one way message RemotingException! target url:" + url, e);
                throw new RuntimeException("Bolt Server one way message RemotingException!", e);
            } catch (InterruptedException e) {
                LOGGER.error("Bolt Server one way message InterruptedException! target url:" + url,
                    e);
                throw new RuntimeException("Bolt Server one way message InterruptedException!", e);
            }
        }
        throw new IllegalArgumentException(
            "Send message connection can not be null or connection not be connected!");
    }

    @Override
    public Object sendSync(Channel channel, Object message, int timeoutMillis) {
        if (channel != null && channel.isConnected()) {
            try {
                Url url = new Url(channel.getRemoteAddress().getAddress().getHostAddress(), channel
                    .getRemoteAddress().getPort());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bolt Server sendSync message:{} , target url:{}", message, url);
                }

                return boltServer.invokeSync(url, message, timeoutMillis);
            } catch (RemotingException e) {
                LOGGER
                    .error("Bolt Server sendSync message RemotingException! target url:" + url, e);
                throw new RuntimeException("Bolt Server sendSync message RemotingException!", e);
            } catch (InterruptedException e) {
                LOGGER.error(
                    "Bolt Server sendSync message InterruptedException! target url:" + url, e);
                throw new RuntimeException("Bolt Server sendSync message InterruptedException!", e);
            }
        }
        throw new IllegalArgumentException(
            "Send message connection can not be null or connection not be connected!");
    }

    @Override
    public void sendCallback(Channel channel, Object message, CallbackHandler callbackHandler,
                             int timeoutMillis) {
        if (channel != null && channel.isConnected()) {
            try {
                Url url = new Url(channel.getRemoteAddress().getAddress().getHostAddress(), channel
                    .getRemoteAddress().getPort());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bolt Server sendSync message:{} , target url:{}", message, url);
                }
                boltServer.invokeWithCallback(url, message, new InvokeCallback() {
                    @Override
                    public void onResponse(Object result) {
                        callbackHandler.onCallback(channel, result);
                    }

                    @Override
                    public void onException(Throwable e) {
                        callbackHandler.onException(channel, e);
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                }, timeoutMillis);
                return;
            } catch (RemotingException e) {
                throw new RuntimeException("Bolt Server invoke with callback RemotingException!", e);
            } catch (InterruptedException e) {
                PUSH_LOGGER.error(
                    "Bolt Server invoke with callback InterruptedException! target url:" + url, e);
                throw new RuntimeException(
                    "Bolt Server invoke with callback InterruptedException!", e);
            }
        }
        throw new IllegalArgumentException(
            "Send message connection can not be null or connection not be connected!");
    }

    public void addChannel(Channel channel) {
        channels.putIfAbsent(NetUtil.toAddressString(channel.getRemoteAddress()), channel);
    }

    public void removeChannel(Channel channel) {
        channels.remove(NetUtil.toAddressString(channel.getRemoteAddress()));
    }

    public RpcServer getRpcServer() {
        return boltServer;
    }

    @Override
    public int getChannelCount(){
        return channels.size();
    }
}