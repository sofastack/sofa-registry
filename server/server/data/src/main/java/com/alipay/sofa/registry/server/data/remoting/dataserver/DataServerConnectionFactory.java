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
package com.alipay.sofa.registry.server.data.remoting.dataserver;

import com.alipay.remoting.Connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the factory to hold connections that other dataservers connected to local server
 *
 * @author qian.lqlq
 * @version $Id: DataServerConnectionFactory.java, v 0.1 2018-03-07 17:52 qian.lqlq Exp $
 */
public class DataServerConnectionFactory {

    /**
     * collection of connections
     * key:connectId ip:port
     */
    private final Map<String, Connection> MAP = new ConcurrentHashMap<>();

    /**
     * register connection
     *
     * @param connection
     */
    public void register(Connection connection) {
        MAP.put(getConnectId(connection), connection);
    }

    /**
     * remove connection by specific ip+port
     *
     * @param connection
     */
    public void remove(Connection connection) {
        MAP.remove(getConnectId(connection));
    }

    /**
     * get connection by ip
     *
     * @param ip
     * @return
     */
    public Connection getConnection(String ip) {
        return MAP.values().stream().filter(connection -> ip.equals(connection.getRemoteIP()) && connection.isFine()).findFirst().orElse(null);
    }

    private String getConnectId(Connection connection) {
        return connection.getRemoteIP() + ":" + connection.getRemotePort();
    }
}