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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import com.alipay.remoting.Connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author qian.lqlq
 * @version $Id: MetaServerConnectionFactory.java, v 0.1 2018年03月13日 15:44 qian.lqlq Exp $
 */
public class MetaServerConnectionFactory {

    private final Map<String, Map<String, Connection>> MAP = new ConcurrentHashMap<>();

    /**
     *
     * @param dataCenter
     * @param ip
     * @param connection
     */
    public void register(String dataCenter, String ip, Connection connection) {

        Map<String, Connection> connectionMap = MAP.get(dataCenter);
        if (connectionMap == null) {
            Map<String, Connection> newConnectionMap = new ConcurrentHashMap<>();
            connectionMap = MAP.putIfAbsent(dataCenter, newConnectionMap);
            if (connectionMap == null) {
                connectionMap = newConnectionMap;
            }
        }

        connectionMap.put(ip, connection);
    }

    /**
     *
     * @param dataCenter
     * @param ip
     * @return
     */
    public Connection getConnection(String dataCenter, String ip) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, Connection> map = MAP.get(dataCenter);
            if (map.containsKey(ip)) {
                return map.get(ip);
            }
        }
        return null;
    }

    /**
     *
     * @param dataCenter
     * @return
     */
    public Map<String, Connection> getConnections(String dataCenter) {
        if (MAP.containsKey(dataCenter)) {
            return MAP.get(dataCenter);
        }
        return new HashMap<>();
    }

    /**
     *
     * @param dataCenter
     * @return
     */
    public Set<String> getIps(String dataCenter) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, Connection> map = MAP.get(dataCenter);
            if (map != null) {
                return map.keySet();
            }
        }
        return new HashSet<>();
    }

    /**
     *
     * @param dataCenter
     */
    public void remove(String dataCenter) {
        Map<String, Connection> map = getConnections(dataCenter);
        if (!map.isEmpty()) {
            for (Connection connection : map.values()) {
                if (connection.isFine()) {
                    connection.close();
                }
            }
        }
        MAP.remove(dataCenter);
    }

    /**
     *
     * @param dataCenter
     * @param ip
     */
    public void remove(String dataCenter, String ip) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, Connection> map = MAP.get(dataCenter);
            if (map != null) {
                map.remove(ip);
            }
        }
    }

    /**
     *
     * @return
     */
    public Set<String> getAllDataCenters() {
        return MAP.keySet();
    }
}