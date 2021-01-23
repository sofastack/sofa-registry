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

import com.alipay.remoting.*;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
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

    protected static final Logger      PUSH_LOGGER = LoggerFactory.getLogger("SESSION-PUSH",
                                                       "[Server]");
    /**
     * accoding server port
     * can not be null
     */
    protected final URL                url;
    private final List<ChannelHandler> channelHandlers;
    /**
     * bolt server
     */
    private final RpcServer            boltServer;
    /**
     * started status
     */
    private final AtomicBoolean        isStarted   = new AtomicBoolean(false);
    private final Map<String, Channel> channels    = new ConcurrentHashMap<>();

    private final AtomicBoolean        initHandler = new AtomicBoolean(false);

    /**
     * constructor
     * @param url
     * @param channelHandlers
     */
    public BoltServer(URL url, List<ChannelHandler> channelHandlers) {
        this.channelHandlers = channelHandlers;
        this.url = url;
        this.boltServer = createRpcServer();
    }

    /**
     * start bolt server
     */
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

    /**
     * just init cant start
     */
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
            boltServer.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                newConnectionEventProcessor(ConnectionEventType.CONNECT));
            boltServer.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                newConnectionEventProcessor(ConnectionEventType.CLOSE));
            boltServer.addConnectionEventProcessor(ConnectionEventType.EXCEPTION,
                newConnectionEventProcessor(ConnectionEventType.EXCEPTION));

            registerUserProcessorHandler();
        }
    }

    protected ConnectionEventProcessor newConnectionEventProcessor(ConnectionEventType type) {
        return new ConnectionEventAdapter(type, getConnectionEventHandler(), this);
    }

    protected ChannelHandler getConnectionEventHandler() {
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
                        boltServer
                            .registerUserProcessor(newSyncUserProcessorAdapter(channelHandler));
                    } else {
                        boltServer
                            .registerUserProcessor(newAsyncUserProcessorAdapter(channelHandler));
                    }
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
    public Object sendSync(Channel channel, Object message, int timeoutMillis) {
        if (channel != null && channel.isConnected()) {
            Url boltUrl = null;
            try {
                boltUrl = new Url(channel.getRemoteAddress().getAddress().getHostAddress(), channel
                    .getRemoteAddress().getPort());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bolt Server sendSync message:{} , target url:{}", message,
                        boltUrl);
                }

                return boltServer.invokeSync(boltUrl, message, newInvokeContext(message),
                    timeoutMillis);
            } catch (RemotingException e) {
                LOGGER.error("Bolt Server sendSync message RemotingException! target url:"
                             + boltUrl, e);
                throw new RuntimeException("Bolt Server sendSync message RemotingException!", e);
            } catch (InterruptedException e) {
                LOGGER.error("Bolt Server sendSync message InterruptedException! target url:"
                             + boltUrl, e);
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
            Url boltUrl = null;
            try {
                boltUrl = new Url(channel.getRemoteAddress().getAddress().getHostAddress(), channel
                    .getRemoteAddress().getPort());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bolt Server sendSync message:{} , target url:{}", message,
                        boltUrl);
                }
                boltServer.invokeWithCallback(boltUrl, message, newInvokeContext(message),
                    new InvokeCallback() {
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
                            return callbackHandler.getExecutor();
                        }
                    }, timeoutMillis);
                return;
            } catch (RemotingException e) {
                throw new RuntimeException("Bolt Server invoke with callback RemotingException!", e);
            } catch (InterruptedException e) {
                PUSH_LOGGER.error(
                    "Bolt Server invoke with callback InterruptedException! target url:" + boltUrl,
                    e);
                throw new RuntimeException(
                    "Bolt Server invoke with callback InterruptedException!", e);
            }
        }
        throw new IllegalArgumentException(
            "Send message connection can not be null or connection not be connected!");
    }

    protected InvokeContext newInvokeContext(Object request) {
        return null;
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
    public int getChannelCount() {
        return channels.size();
    }
}