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
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
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

/**
 * The type Bolt client.
 * @author shangyu.wh
 * @version $Id : BoltClient.java, v 0.1 2017-11-27 14:46 shangyu.wh Exp $
 */
public class BoltClient implements Client {

    private static final Logger LOGGER         = LoggerFactory.getLogger(BoltClient.class);

    /**
     * RpcTaskScanner: remove closed connections every 10 seconds
     * HealConnectionRunner: Ensure that the number of connections reaches connNum every 1 second
     */
    private RpcClient           rpcClient;

    private AtomicBoolean       closed         = new AtomicBoolean(false);

    protected final int         connNum;
    protected int               connectTimeout = 2000;

    /**
     * Instantiates a new Bolt client.
     */
    public BoltClient(int connNum) {
        rpcClient = new RpcClient();
        rpcClient.enableReconnectSwitch();
        rpcClient.enableConnectionMonitorSwitch();
        rpcClient.init();
        this.connNum = connNum;
    }

    /**
     * Setter method for property <tt>channelHandlers</tt>.
     *
     * @param channelHandlers value to be assigned to property channelHandlers
     */
    public void initHandlers(List<ChannelHandler> channelHandlers) {
        ChannelHandler connectionEventHandler = null;
        for (ChannelHandler channelHandler : channelHandlers) {
            if (HandlerType.LISENTER.equals(channelHandler.getType())) {
                connectionEventHandler = channelHandler;
                break;
            }
        }

        rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT,
            new ConnectionEventAdapter(ConnectionEventType.CONNECT, connectionEventHandler, null));
        rpcClient.addConnectionEventProcessor(ConnectionEventType.CLOSE,
            new ConnectionEventAdapter(ConnectionEventType.CLOSE, connectionEventHandler, null));
        rpcClient
            .addConnectionEventProcessor(ConnectionEventType.EXCEPTION, new ConnectionEventAdapter(
                ConnectionEventType.EXCEPTION, connectionEventHandler, null));

        for (ChannelHandler channelHandler : channelHandlers) {
            if (HandlerType.PROCESSER.equals(channelHandler.getType())) {
                if (InvokeType.SYNC.equals(channelHandler.getInvokeType())) {
                    rpcClient.registerUserProcessor(getSyncProcessor(channelHandler));
                } else {
                    rpcClient.registerUserProcessor(getAsyncProcessor(channelHandler));
                }
            }
        }
    }

    protected AsyncUserProcessorAdapter getAsyncProcessor(ChannelHandler channelHandler) {
        return new AsyncUserProcessorAdapter(channelHandler);
    }

    protected SyncUserProcessorAdapter getSyncProcessor(ChannelHandler channelHandler) {
        return new SyncUserProcessorAdapter(channelHandler);
    }

    @Override
    public Channel connect(URL targetUrl) {
        if (targetUrl == null) {
            throw new IllegalArgumentException("Create connection targetUrl can not be null!");
        }
        try {
            Connection connection = getBoltConnection(targetUrl);
            BoltChannel channel = new BoltChannel();
            channel.setConnection(connection);
            return channel;

        } catch (RemotingException e) {
            LOGGER.error("Bolt client connect server got a RemotingException! target url:"
                         + targetUrl, e);
            throw new RuntimeException("Bolt client connect server got a RemotingException!", e);
        }
    }

    protected Connection getBoltConnection(URL targetUrl) throws RemotingException {
        Url url = getBoltUrl(targetUrl);
        try {
            Connection connection = rpcClient.getConnection(url, connectTimeout);
            if (connection == null || !connection.isFine()) {
                if (connection != null) {
                    connection.close();
                }
                throw new RemotingException("Get bolt connection failed for url: " + url);
            }
            return connection;
        } catch (InterruptedException e) {
            throw new RuntimeException(
                "BoltClient rpcClient.getConnection InterruptedException! target url:" + url, e);
        }
    }

    protected Url getBoltUrl(URL targetUrl) {
        Url url = new Url(targetUrl.getIpAddress(), targetUrl.getPort());
        url.setProtocol(RpcProtocol.PROTOCOL_CODE);
        url.setVersion(RpcProtocolV2.PROTOCOL_VERSION_1);
        url.setConnNum(connNum);
        url.setConnWarmup(true);
        return url;
    }

    @Override
    public Channel getChannel(URL url) {
        return this.connect(url);
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
            return rpcClient.invokeSync(getBoltUrl(url), message, timeoutMillis);
        } catch (RemotingException e) {
            String msg = "Bolt Client sendSync message RemotingException! target url:" + url;
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (InterruptedException e) {
            throw new RuntimeException(
                "Bolt Client sendSync message InterruptedException! target url:" + url, e);
        }
    }

    @Override
    public Object sendSync(Channel channel, Object message, int timeoutMillis) {
        if (channel != null && channel.isConnected()) {
            BoltChannel boltChannel = (BoltChannel) channel;
            try {
                return rpcClient.invokeSync(boltChannel.getConnection(), message, timeoutMillis);
            } catch (RemotingException e) {
                LOGGER.error("Bolt Client sendSync message RemotingException! target url:"
                             + boltChannel.getRemoteAddress(), e);
                throw new RuntimeException("Bolt Client sendSync message RemotingException!", e);
            } catch (InterruptedException e) {
                LOGGER.error("Bolt Client sendSync message InterruptedException! target url:"
                             + boltChannel.getRemoteAddress(), e);
                throw new RuntimeException("Bolt Client sendSync message InterruptedException!", e);
            }
        }
        throw new IllegalArgumentException(
            "Input channel: " + channel
                    + " error! channel cannot be null,or channel must be connected!");
    }

    @Override
    public void sendCallback(URL url, Object message, CallbackHandler callbackHandler,
                             int timeoutMillis) {
        try {
            Connection connection = getBoltConnection(url);
            BoltChannel channel = new BoltChannel();
            channel.setConnection(connection);
            rpcClient.invokeWithCallback(connection, message, new InvokeCallback() {

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
            String msg = "Bolt Client sendSync message RemotingException! target url:" + url;
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

}