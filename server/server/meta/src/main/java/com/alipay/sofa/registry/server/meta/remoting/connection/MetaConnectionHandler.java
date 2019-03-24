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
package com.alipay.sofa.registry.server.meta.remoting.connection;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle meta node's connect request
 * @author shangyu.wh
 * @version $Id: MetaConnectionHandler.java, v 0.1 2018-02-12 15:01 shangyu.wh Exp $
 */
public class MetaConnectionHandler extends AbstractServerHandler implements NodeConnectManager {

    private static final Logger                                                      LOGGER      = LoggerFactory
                                                                                                     .getLogger(MetaConnectionHandler.class);

    @Autowired
    private NodeConfig                                                               nodeConfig;

    private Map<String/*dataCenter*/, Map<String/*connectId*/, InetSocketAddress>> connections = new ConcurrentHashMap<>();

    @Override
    public void connected(Channel channel) throws RemotingException {
        super.connected(channel);
        addConnection(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        super.disconnected(channel);
        removeConnection(channel);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.LISENTER;
    }

    @Override
    public void addConnection(Channel channel) {
        InetSocketAddress remoteAddress = channel.getRemoteAddress();
        String connectId = NetUtil.toAddressString(remoteAddress);
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        String dataCenter = nodeConfig.getMetaDataCenter(ipAddress);
        if (dataCenter != null) {
            Map<String/*connectId*/, InetSocketAddress> connectMap = connections.get(dataCenter);
            if (connectMap == null) {
                final Map<String, InetSocketAddress> newMap = new ConcurrentHashMap<>();
                connectMap = connections.putIfAbsent(dataCenter, newMap);
                if (connectMap == null) {
                    connectMap = newMap;
                }
            }
            connectMap.putIfAbsent(connectId, remoteAddress);
        }
    }

    @Override
    public boolean removeConnection(Channel channel) {
        InetSocketAddress remoteAddress = channel.getRemoteAddress();
        String connectId = NetUtil.toAddressString(remoteAddress);
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        String dataCenter = nodeConfig.getMetaDataCenter(ipAddress);
        if (dataCenter != null) {
            Map<String/*connectId*/, InetSocketAddress> connectMap = connections.get(dataCenter);
            if (connectMap != null) {
                connectMap.remove(connectId);
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<InetSocketAddress> getConnections(String dataCenter) {
        Map<String/*connectId*/, InetSocketAddress> connectMap = connections.get(dataCenter);
        if (connectMap == null || connectMap.isEmpty()) {
            LOGGER.error("Can not find connection of dataCenter {}", dataCenter);
            throw new RuntimeException("Can not find connection of dataCenter:" + dataCenter);
        }
        return connectMap.values();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.META;
    }
}