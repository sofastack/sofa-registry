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
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.ChannelHandler.HandlerType;
import com.alipay.sofa.registry.remoting.ChannelHandler.InvokeType;
import com.alipay.sofa.registry.remoting.Client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Bolt client.
 * @author shangyu.wh
 * @version $Id : BoltClient.java, v 0.1 2017-11-27 14:46 shangyu.wh Exp $
 */
public class BoltClient implements Client {

    private static final Logger  LOGGER          = LoggerFactory.getLogger(BoltClient.class);
    private RpcClient            boltClient;
    private List<ChannelHandler> channelHandlers = new ArrayList<>();
    private Map<String, Channel> channels        = new HashMap<>();
    private AtomicBoolean        initHandler     = new AtomicBoolean(false);

    /**
     * Instantiates a new Bolt client.
     */
    public BoltClient() {
        boltClient = new RpcClient();
        boltClient.init();
    }

    @Override
    public Channel connect(URL targetUrl) {

        if (targetUrl == null) {
            throw new IllegalArgumentException("Create connection targetUrl can not be null!");
        }
        InetSocketAddress address = URL.toInetSocketAddress(targetUrl);
        Channel c = getChannel(address);
        if (c != null && c.isConnected()) {
            LOGGER.info("Target url:" + targetUrl + " has been connected!", targetUrl);
            return c;
        }

        initHandler();

        try {
            Connection connection = boltClient
                .getConnection(NetUtil.toAddressString(address), 1000);
            if (connection != null) {
                BoltChannel channel = new BoltChannel();
                channel.setConnection(connection);
                channels.put(connection.getUrl().getOriginUrl(), channel);
                return channel;
            } else {
                throw new RuntimeException("Bolt client connect server get none connection!");
            }

        } catch (RemotingException e) {
            LOGGER.error("Bolt client connect server got a RemotingException! target url:"
                         + targetUrl, e);
            throw new RuntimeException("Bolt client connect server got a RemotingException!", e);
        } catch (InterruptedException e) {
            LOGGER.error("Bolt client connect server has been Interrupted!", e);
            throw new RuntimeException("Bolt client connect server has been Interrupted!", e);
        }
    }

    private void initHandler() {

        if (initHandler.compareAndSet(false, true)) {
            boltClient.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                new ConnectionEventAdapter(ConnectionEventType.CONNECT,
                    getConnectionEventHandler(), null));
            boltClient.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                new ConnectionEventAdapter(ConnectionEventType.CLOSE, getConnectionEventHandler(),
                    null));
            boltClient.addConnectionEventProcessor(ConnectionEventType.EXCEPTION,
                new ConnectionEventAdapter(ConnectionEventType.EXCEPTION,
                    getConnectionEventHandler(), null));

            registerUserProcessorHandler();
        }
    }

    private void registerUserProcessorHandler() {
        if (channelHandlers != null) {
            for (ChannelHandler channelHandler : channelHandlers) {
                if (HandlerType.PROCESSER.equals(channelHandler.getType())) {
                    if (InvokeType.SYNC.equals(channelHandler.getInvokeType())) {
                        boltClient.registerUserProcessor(new SyncUserProcessorAdapter(
                            channelHandler));
                    } else {
                        boltClient.registerUserProcessor(new AsyncUserProcessorAdapter(
                            channelHandler));
                    }
                }
            }
        }
    }

    @Override
    public Collection<Channel> getChannels() {

        Collection<Channel> chs = new HashSet<>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtil.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        Channel c = channels.get(NetUtil.toAddressString(remoteAddress));
        if (c == null || !c.isConnected()) {
            return null;
        }
        return c;
    }

    @Override
    public Channel getChannel(URL url) {
        Channel c = channels.get(url.getAddressString());
        if (c == null || !c.isConnected()) {
            return null;
        }
        return c;
    }

    @Override
    public List<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    /**
     * Setter method for property <tt>channelHandlers</tt>.
     *
     * @param channelHandlers value to be assigned to property channelHandlers
     */
    public void setChannelHandlers(List<ChannelHandler> channelHandlers) {
        this.channelHandlers = channelHandlers;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return NetUtil.getLocalSocketAddress();
    }

    /**
     * Gets connection event handler.
     *
     * @return the connection event handler
     */
    public ChannelHandler getConnectionEventHandler() {
        if (channelHandlers != null) {
            for (ChannelHandler channelHandler : channelHandlers) {
                if (HandlerType.LISENTER.equals(channelHandler.getType())) {
                    return channelHandler;
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
        Collection<Channel> chs = getChannels();
        if (chs != null && chs.size() > 0) {
            for (Channel ch : chs) {
                if (ch != null) {
                    boltClient.closeStandaloneConnection(((BoltChannel) ch).getConnection());
                }
            }
        }
    }

    @Override
    public void close(Channel channel) {
        if (channel != null) {
            Connection connection = ((BoltChannel) channel).getConnection();
            if (null != connection.getUrl() && null != connection.getUrl().getOriginUrl()) {
                channels.remove(connection.getUrl().getOriginUrl());
            }
            boltClient.closeStandaloneConnection(connection);
        }
    }

    @Override
    public boolean isClosed() {
        boolean ret = false;
        Collection<Channel> chs = getChannels();
        if (chs != null && chs.size() > 0) {
            for (Channel ch : chs) {
                if (ch != null && !ch.isConnected()) {
                    ret = true;
                    break;
                }
            }
        } else {
            //has no channels
            return true;
        }
        return ret;
    }

    @Override
    public void sendOneway(Channel channel, Object message) {
        if (channel != null && channel.isConnected()) {
            if (channel instanceof BoltChannel) {
                BoltChannel boltChannel = (BoltChannel) channel;
                try {
                    boltClient.oneway(boltChannel.getConnection(), message);
                } catch (RemotingException e) {
                    LOGGER.error("Bolt Client oneway request RemotingException! target url: {}",
                        boltChannel.getRemoteAddress(), e);
                }
            }
        }
    }

    @Override
    public Object sendSync(Channel channel, Object message, int timeoutMillis) {

        if (channel != null && channel.isConnected()) {
            if (channel instanceof BoltChannel) {
                BoltChannel boltChannel = (BoltChannel) channel;
                try {
                    return boltClient.invokeSync(boltChannel.getConnection(), message,
                        timeoutMillis);
                } catch (RemotingException e) {
                    LOGGER.error("Bolt Client sendSync message RemotingException! target url:"
                                 + boltChannel.getRemoteAddress(), e);
                    throw new RuntimeException("Bolt Client sendSync message RemotingException!", e);
                } catch (InterruptedException e) {
                    LOGGER.error("Bolt Client sendSync message InterruptedException! target url:"
                                 + boltChannel.getRemoteAddress(), e);
                    throw new RuntimeException(
                        "Bolt Client sendSync message InterruptedException!", e);
                }
            } else {
                throw new IllegalArgumentException("Input channel instance error! instance class:"
                                                   + channel.getClass().getName());
            }
        }
        throw new IllegalArgumentException(
            "Input channel: " + channel
                    + " error! channel cannot be null,or channel must be connected!");
    }

    @Override
    public void sendCallback(Channel channel, Object message, CallbackHandler callbackHandler,
                             int timeoutMillis) {
        if (channel != null && channel.isConnected()) {
            if (channel instanceof BoltChannel) {
                BoltChannel boltChannel = (BoltChannel) channel;
                try {
                    boltClient.invokeWithCallback(boltChannel.getConnection(), message,
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
                    LOGGER.error("Bolt Client sendSync message RemotingException! target url:"
                                 + boltChannel.getRemoteAddress(), e);
                    throw new RuntimeException("Bolt Client sendSync message RemotingException!", e);
                }
            } else {
                throw new IllegalArgumentException("Input channel instance error! instance class:"
                                                   + channel.getClass().getName());
            }
        }
        throw new IllegalArgumentException(
            "Input channel: " + channel
                    + " error! channel cannot be null,or channel must be connected!");
    }

}