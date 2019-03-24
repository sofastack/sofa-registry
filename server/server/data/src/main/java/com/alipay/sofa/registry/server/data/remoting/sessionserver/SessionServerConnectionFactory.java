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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.SessionServerDisconnectEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * the factory to hold sesseionserver connections
 *
 * @author qian.lqlq
 * @version $Id: SessionServerConnectionFactory.java, v 0.1 2017-12-06 15:48 qian.lqlq Exp $
 */
public class SessionServerConnectionFactory {
    private static final Logger            LOGGER               = LoggerFactory
                                                                    .getLogger(SessionServerConnectionFactory.class);

    private static final int               DELAY                = 30 * 1000;

    /**
     * collection of connections
     * key      :   processId
     * value    :   connection
     */
    private final Map<String, Pair>        MAP                  = new ConcurrentHashMap<>();

    /**
     * key  :   sessionserver host
     * value:   sesseionserver processId
     */
    private final Map<String, String>      PROCESSID_MAP        = new ConcurrentHashMap<>();

    /**
     * key  :   sessionserver processId
     * value:   ip:port of clients
     */
    private final Map<String, Set<String>> PROCESSID_CLIENT_MAP = new ConcurrentHashMap<>();

    @Autowired
    private DisconnectEventHandler         disconnectEventHandler;

    /**
     * register connection
     *
     * @param processId
     * @param clientHosts
     * @param connection
     */
    public void register(String processId, Set<String> clientHosts, Connection connection) {
        String serverHost = NetUtil.toAddressString(connection.getRemoteAddress());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("session({}, processId={}) registered", serverHost, processId);
        }
        MAP.put(processId, new Pair(serverHost, connection));
        Set<String> ret = PROCESSID_CLIENT_MAP.getOrDefault(processId, null);
        if (ret == null) {
            PROCESSID_CLIENT_MAP.putIfAbsent(processId, new HashSet<>());
        }
        PROCESSID_CLIENT_MAP.get(processId).addAll(clientHosts);
        PROCESSID_MAP.put(serverHost, processId);
    }

    /**
     *
     * @param processId
     * @param clientAddress
     */
    public void registerClient(String processId, String clientAddress) {
        Set<String> ret = PROCESSID_CLIENT_MAP.getOrDefault(processId, null);
        if (ret == null) {
            PROCESSID_CLIENT_MAP.putIfAbsent(processId, new HashSet<>());
        }
        PROCESSID_CLIENT_MAP.get(processId).add(clientAddress);
    }

    /**
     * remove connection by specific host
     */
    public void removeProcess(String sessionServerHost) {
        String processId = PROCESSID_MAP.remove(sessionServerHost);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("session({}, processId={}) unregistered", sessionServerHost, processId);
        }
        if (processId != null) {
            disconnectEventHandler.receive(new SessionServerDisconnectEvent(processId,
                sessionServerHost, DELAY));
        }
    }

    /**
     *
     * @param processId
     */
    public Set<String> removeClients(String processId) {
        return PROCESSID_CLIENT_MAP.remove(processId);
    }

    /**
     *
     * @param processId
     * @return
     */
    public boolean removeProcessIfMatch(String processId, String sessionServerHost) {
        return MAP.remove(processId, new Pair(sessionServerHost, null));
    }

    /**
     * get all connections
     *
     * @return
     */
    public List<Connection> getConnections() {
        return MAP.size() <= 0 ?
                Collections.EMPTY_LIST :
                MAP.values().stream().map(Pair::getConnection).collect(Collectors.toList());
    }

    /**
     * convenient class to store sessionServerHost and connection
     */
    private static class Pair {
        private String     sessionServerHost;
        private Connection connection;

        private Pair(String sessionServerHost, Connection connection) {
            this.sessionServerHost = sessionServerHost;
            this.connection = connection;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair pair = (Pair) o;

            return sessionServerHost.equals(pair.sessionServerHost);
        }

        @Override
        public int hashCode() {
            return sessionServerHost.hashCode();
        }

        /**
         * Getter method for property <tt>connection</tt>.
         *
         * @return property value of connection
         */
        private Connection getConnection() {
            return connection;
        }

    }
}
