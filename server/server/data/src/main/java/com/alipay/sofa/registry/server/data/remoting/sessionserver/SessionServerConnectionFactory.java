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
package com.alipay.sofa.registry.server.data.remoting.sessionserver;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the factory to hold SessionServer connections
 *
 * @author qian.lqlq
 * @author kezhu.wukz
 * @version $Id: SessionServerConnectionFactory.java, v 0.1 2017-12-06 15:48 qian.lqlq Exp $
 */
public class SessionServerConnectionFactory {
    private static final Logger     LOGGER              = LoggerFactory
                                                            .getLogger(SessionServerConnectionFactory.class);

    private final Map<String, Pair> session2Connections = new ConcurrentHashMap<>();

    @Autowired
    private DataServerConfig        dataServerConfig;

    /**
     * register connection
     *
     * @param processId
     * @param connection
     */
    public void registerSession(ProcessId processId, Connection connection) {
        String sessionConnAddress = NetUtil.toAddressString(connection.getRemoteAddress());
        // maybe register happens after disconnect or parallely, at that time, connection is not fine
        // synchronized avoid the case: isFine=true->disconnect.remove->register.put
        synchronized (this) {
            if (!connection.isFine()) {
                return;
            }
            Pair pair = session2Connections.computeIfAbsent(connection.getRemoteIP(), k -> new Pair(processId));
            pair.connections.put(sessionConnAddress, connection);
        }

        LOGGER.info("session({}, processId={}) registered", sessionConnAddress, processId);
    }

    /**
     * session disconnected, The SessionServerDisconnectEvent is triggered only when the last connections is removed
     */
    public void sessionDisconnected(InetSocketAddress sessionAddress) {
        final String sessionIpAddress = sessionAddress.getAddress().getHostAddress();
        final Pair pair = session2Connections.get(sessionIpAddress);
        if (pair == null) {
            LOGGER.warn("sessionDisconnected not found session {}", sessionIpAddress);
            return;
        }
        String sessionConnAddress = NetUtil.toAddressString(sessionAddress);
        boolean removed = false;
        synchronized (this) {
            removed = pair.connections.remove(sessionConnAddress) != null;
        }
        LOGGER.info("sessionDisconnected({}, processId={}) unregistered, removed={}, remains={}",
            sessionConnAddress, pair.processId, removed, pair.connections.size());
    }

    /**
     * get the map of sessionIp -> connection
     */
    public Map<String, Connection> getSessonConnectionMap() {
        Map<String, Connection> map = new HashMap<>(session2Connections.size());
        for (Map.Entry<String, Pair> e : session2Connections.entrySet()) {
            Pair pair = e.getValue();
            Object[] conns = pair.connections.values().toArray();
            if (conns.length > 0) {
                int n = pair.roundRobin.incrementAndGet();
                if (n < 0) {
                    pair.roundRobin.compareAndSet(n, 0);
                    n = (n == Integer.MIN_VALUE) ? 0 : Math.abs(n);
                }
                n = n % conns.length;
                map.put(e.getKey(), (Connection) conns[n]);
            }
        }
        return map;
    }

    public boolean containsConnection(ProcessId sessionProcessId) {
        for (Pair p : session2Connections.values()) {
            if (p.processId.equals(sessionProcessId) && !p.connections.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * get connections of SessionServer ( Randomly select a connection for each session )
     */
    public List<Connection> getSessionConnections() {
        return new ArrayList<>(getSessonConnectionMap().values());
    }

    /**
     * convenient class to store sessionConnAddress and connection
     */
    private static final class Pair {
        final AtomicInteger           roundRobin  = new AtomicInteger(-1);
        final ProcessId               processId;
        final Map<String, Connection> connections = Maps.newConcurrentMap();

        Pair(ProcessId processId) {
            this.processId = processId;
        }
    }
}
