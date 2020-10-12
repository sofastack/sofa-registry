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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.SessionServerDisconnectEvent;

/**
 * the factory to hold SessionServer connections
 *
 * @author qian.lqlq
 * @author kezhu.wukz
 * @version $Id: SessionServerConnectionFactory.java, v 0.1 2017-12-06 15:48 qian.lqlq Exp $
 */
public class SessionServerConnectionFactory {
    private static final Logger            LOGGER                      = LoggerFactory
                                                                           .getLogger(SessionServerConnectionFactory.class);

    private static final Map               EMPTY_MAP                   = new HashMap(0);

    /**
     * key  :   SessionServer address
     * value:   SessionServer processId
     */
    private final Map<String, String>      SESSION_CONN_PROCESS_ID_MAP = new ConcurrentHashMap<>();

    /**
     * key  :   SessionServer processId
     * value:   ip:port of clients
     */
    private final Map<String, Set<String>> PROCESS_ID_CONNECT_ID_MAP   = new ConcurrentHashMap<>();

    /**
     * key  :   SessionServer processId
     * value:   pair(SessionServer address, SessionServer connection)
     */
    private final Map<String, Pair>        PROCESS_ID_SESSION_CONN_MAP = new ConcurrentHashMap<>();

    @Autowired
    private DisconnectEventHandler         disconnectEventHandler;

    @Autowired
    private DataServerConfig               dataServerConfig;

    /**
     * register connection
     *
     * @param processId
     * @param connectIds
     * @param connection
     */
    public void registerSession(String processId, Set<String> connectIds, Connection connection) {
        if (!connection.isFine()) {
            return;
        }
        String sessionConnAddress = NetUtil.toAddressString(connection.getRemoteAddress());
        LOGGER.info("session({}, processId={}) registered", sessionConnAddress, processId);

        SESSION_CONN_PROCESS_ID_MAP.put(sessionConnAddress, processId);

        Set<String> connectIdSet = PROCESS_ID_CONNECT_ID_MAP
                .computeIfAbsent(processId, k -> ConcurrentHashMap.newKeySet());
        connectIdSet.addAll(connectIds);

        Pair pair = PROCESS_ID_SESSION_CONN_MAP.computeIfAbsent(processId, k -> new Pair(new ConcurrentHashMap<>()));
        pair.getConnections().put(sessionConnAddress, connection);

    }

    /**
     *
     * @param processId
     * @param connectId
     */
    public void registerConnectId(String processId, String connectId) {
        Set<String> connectIdSet = PROCESS_ID_CONNECT_ID_MAP
                .computeIfAbsent(processId, k -> ConcurrentHashMap.newKeySet());
        connectIdSet.add(connectId);
    }

    /**
     * session disconnected, The SessionServerDisconnectEvent is triggered only when the last connections is removed
     */
    public void sessionDisconnected(String sessionConnAddress) {
        String processId = SESSION_CONN_PROCESS_ID_MAP.remove(sessionConnAddress);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("session({}, processId={}) unregistered", sessionConnAddress, processId);
        }
        if (processId != null) {
            Pair pair = PROCESS_ID_SESSION_CONN_MAP.get(processId);

            // remove connection
            if (pair != null) {
                pair.getConnections().remove(sessionConnAddress);
                pair.lastDisconnectedSession = sessionConnAddress;
            }

            // The SessionServerDisconnectEvent is triggered only when the last connection is broken
            if (pair == null || pair.getConnections().isEmpty()) {
                disconnectEventHandler.receive(new SessionServerDisconnectEvent(processId,
                    sessionConnAddress, dataServerConfig.getSessionDisconnectDelayMs()));
            }
        }
    }

    /**
     *
     * @param processId
     */
    public Set<String> removeConnectIds(String processId) {
        return PROCESS_ID_CONNECT_ID_MAP.remove(processId);
    }

    /**
     * If the number of connections is 0, and lastDisconnectedSession matched, he ProcessId can be deleted
     */
    public boolean removeProcessIfMatch(String processId, String sessionConnAddress) {
        Pair emptyPair = new Pair(EMPTY_MAP);
        emptyPair.lastDisconnectedSession = sessionConnAddress;
        return PROCESS_ID_SESSION_CONN_MAP.remove(processId, emptyPair);
    }

    /**
     * get connections of SessionServer ( Randomly select a connection for each session )
     */
    public List<Connection> getSessionConnections() {
        List<Connection> list = new ArrayList<>(PROCESS_ID_SESSION_CONN_MAP.size());
        Collection<Pair> pairs = PROCESS_ID_SESSION_CONN_MAP.values();
        if (pairs != null) {
            for (Pair pair : pairs) {
                Object[] conns = pair.getConnections().values().toArray();
                if (conns.length > 0) {
                    int n = pair.roundRobin.incrementAndGet();
                    if (n < 0) {
                        pair.roundRobin.compareAndSet(n, 0);
                        n = (n == Integer.MIN_VALUE) ? 0 : Math.abs(n);
                    }
                    n = n % conns.length;
                    list.add((Connection) conns[n]);
                }
            }
        }
        return list;
    }

    /**
     * convenient class to store sessionConnAddress and connection
     */
    private static class Pair {
        private AtomicInteger           roundRobin = new AtomicInteger(-1);
        private Map<String, Connection> connections;
        private String                  lastDisconnectedSession;

        private Pair(Map<String, Connection> connections) {
            this.connections = connections;
        }

        @Override
        public boolean equals(Object o) {
            return connections.equals(((Pair) o).getConnections())
                   && (((Pair) o).lastDisconnectedSession.equals(lastDisconnectedSession));
        }

        /**
         * Getter method for property <tt>connections</tt>.
         *
         * @return property value of connections
         */
        private Map<String, Connection> getConnections() {
            return connections;
        }

    }
}
