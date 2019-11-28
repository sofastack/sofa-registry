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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.sofa.registry.common.model.dataserver.HeartbeatRequest;
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
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id : BoltClient.java, v 0.1 2017-11-27 14:46 shangyu.wh Exp $
 */
public class BoltClient implements Client {

    private static final Logger LOGGER                 = LoggerFactory.getLogger(BoltClient.class);

    private static final int    ENSURE_CONN_POOL_INTVL = 15000;

    /**
     * RpcTaskScanner: remove closed connections every 10 seconds
     * HealConnectionRunner: Ensure that the number of connections reaches connNum every 1 second
     */
    private RpcClient[]         rpcClients;

    private AtomicBoolean       closed                 = new AtomicBoolean(false);

    private AtomicInteger       roundRobinNextId       = new AtomicInteger(-1);

    private Set<Url>            connectedUrls          = ConcurrentHashMap.newKeySet();

    protected final int         connNum;
    protected int               connectTimeout         = 2000;

    /**
     * Instantiates a new Bolt client.
     */
    public BoltClient(int connNum) {
        rpcClients = new RpcClient[connNum];
        for (int i = 0; i < connNum; i++) {
            RpcClient rpcClient = new RpcClient();
            rpcClient.enableReconnectSwitch();
            rpcClient.enableConnectionMonitorSwitch();
            rpcClient.init();

            rpcClients[i] = rpcClient;
        }
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

        for (int i = 0; i < connNum; i++) {
            RpcClient rpcClient = rpcClients[i];
            rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                    newConnectionEventAdapter(connectionEventHandler, ConnectionEventType.CONNECT));
            rpcClient.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                    newConnectionEventAdapter(connectionEventHandler, ConnectionEventType.CLOSE));
            rpcClient.addConnectionEventProcessor(ConnectionEventType.EXCEPTION,
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

        // start a thread to ensure connection pool (connNum > 1): bolt does not maintain the number of connection
        // pools, we need to use heartbeat to make bolt maintain the number of connections
        if (connNum > 1) {
            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(ENSURE_CONN_POOL_INTVL);
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted in ConnPoolChecker", e);
                        break;
                    }

                    for (RpcClient rpcClient : rpcClients) {
                        try {
                            for (Url boltUrl : connectedUrls) {
                                // send something to make connection active
                                HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
                                rpcClient.invokeSync(boltUrl, heartbeatRequest, connectTimeout);
                            }
                        } catch (Throwable e) {
                            LOGGER.error("Error in ConnPoolChecker", e);
                        }
                    }

                }
            });
            t.setDaemon(true);
            t.setName("BoltClient-ConnPoolChecker");
            t.start();
        }
    }

    protected ConnectionEventProcessor newConnectionEventAdapter(ChannelHandler connectionEventHandler,
                                                                 ConnectionEventType connectEventType) {
        return new ConnectionEventAdapter(connectEventType, connectionEventHandler, null);
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
            Url boltUrl = getBoltUrl(url);
            connectedUrls.add(boltUrl);

            for (int i = 0; i < connNum; i++) {
                getBoltConnection(rpcClients[i], url);
            }
            RpcClient rpcClient = getNextRpcClient();
            Connection connection = getBoltConnection(rpcClient, url);
            BoltChannel channel = new BoltChannel();
            channel.setConnection(connection);
            return channel;

        } catch (RemotingException e) {
            LOGGER
                .error("Bolt client connect server got a RemotingException! target url:" + url, e);
            throw new RuntimeException("Bolt client connect server got a RemotingException!", e);
        }
    }

    protected Connection getBoltConnection(RpcClient rpcClient, URL url) throws RemotingException {
        Url boltUrl = getBoltUrl(url);
        try {
            Connection connection = rpcClient.getConnection(boltUrl, connectTimeout);
            if (connection == null || !connection.isFine()) {
                if (connection != null) {
                    connection.close();
                }
                throw new RemotingException("Get bolt connection failed for boltUrl: " + boltUrl);
            }
            return connection;
        } catch (InterruptedException e) {
            throw new RuntimeException(
                "BoltClient rpcClient.getConnection InterruptedException! target boltUrl:"
                        + boltUrl, e);
        }
    }

    private RpcClient getNextRpcClient() {
        int n = roundRobinNextId.incrementAndGet();
        if (n < 0) {
            roundRobinNextId.compareAndSet(n, 0);
            n = (n == Integer.MIN_VALUE) ? 0 : Math.abs(n);
        }
        n = n % rpcClients.length;
        return rpcClients[n];
    }

    protected Url getBoltUrl(URL url) {
        Url boltUrl = new Url(url.getIpAddress(), url.getPort());
        boltUrl.setProtocol(RpcProtocol.PROTOCOL_CODE);
        boltUrl.setVersion(RpcProtocolV2.PROTOCOL_VERSION_1);
        boltUrl.setConnNum(1);
        boltUrl.setConnWarmup(true);
        return boltUrl;
    }

    @Override
    public Channel getChannel(URL url) {
        try {
            RpcClient rpcClient = getNextRpcClient();
            Connection connection = getBoltConnection(rpcClient, url);
            BoltChannel channel = new BoltChannel();
            channel.setConnection(connection);
            return channel;
        } catch (RemotingException e) {
            LOGGER
                .error("Bolt client connect server got a RemotingException! target url:" + url, e);
            throw new RuntimeException("Bolt client connect server got a RemotingException!", e);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return NetUtil.getLocalSocketAddress();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            for (int i = 0; i < connNum; i++) {
                RpcClient rpcClient = rpcClients[i];
                rpcClient.shutdown();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public Object sendSync(URL url, Object message, int timeoutMillis) {
        try {
            RpcClient rpcClient = getNextRpcClient();
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
                RpcClient rpcClient = getNextRpcClient();
                return rpcClient.invokeSync(boltChannel.getConnection(), message, timeoutMillis);
            } catch (RemotingException e) {
                LOGGER.error("Bolt Client sendSync message RemotingException! target boltUrl:"
                             + boltChannel.getRemoteAddress(), e);
                throw new RuntimeException("Bolt Client sendSync message RemotingException!", e);
            } catch (InterruptedException e) {
                LOGGER.error("Bolt Client sendSync message InterruptedException! target boltUrl:"
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
            RpcClient rpcClient = getNextRpcClient();
            Connection connection = getBoltConnection(rpcClient, url);
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