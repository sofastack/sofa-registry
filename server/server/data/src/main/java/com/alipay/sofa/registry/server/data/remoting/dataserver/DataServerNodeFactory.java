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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.util.TimeUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * the factory to hold other dataservers and connection connected to them
 *
 * @author qian.lqlq
 * @version $Id: DataServerNodeFactory.java, v 0.1 2018-03-13 18:45 qian.lqlq Exp $
 */
public class DataServerNodeFactory {
    private static final int                                      TRY_COUNT = 5;

    private static final Logger                                   LOGGER    = LoggerFactory
                                                                                .getLogger(DataServerNodeFactory.class);
    @Autowired
    private DataNodeExchanger                                     dataNodeExchanger;

    @Autowired
    private DataServerConfig                                      dataServerConfig;
    /**
     * row:     dataCenter
     * column:  ip
     * value    dataServerNode
     */
    private static final Map<String, Map<String, DataServerNode>> MAP       = new ConcurrentHashMap<>();

    /**
     * add a dataserver to cache
     *
     * @param dataServerNode
     */
    public static void register(DataServerNode dataServerNode, DataServerConfig dataServerConfig) {
        String dataCenter = dataServerNode.getDataCenter();
        Map<String, DataServerNode> dataMap = MAP.get(dataCenter);
        if (dataMap == null) {
            Map<String, DataServerNode> newMap = new ConcurrentHashMap<>();
            dataMap = MAP.putIfAbsent(dataCenter, newMap);
            if (dataMap == null) {
                dataMap = newMap;
            }
        }
        dataMap.put(dataServerNode.getIp(), dataServerNode);
    }

    /**
     * get dataserver by specific datacenter and ip
     *
     * @param dataCenter
     * @param ip
     * @return
     */
    public static DataServerNode getDataServerNode(String dataCenter, String ip) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, DataServerNode> map = MAP.get(dataCenter);
            if (map.containsKey(ip)) {
                return map.get(ip);
            }
        }
        return null;
    }

    /**
     * get dataservers by specific datacenter
     *
     * @param dataCenter
     * @return
     */
    public static Map<String, DataServerNode> getDataServerNodes(String dataCenter) {
        if (MAP.containsKey(dataCenter)) {
            return MAP.get(dataCenter);
        } else {
            return new HashMap<>();
        }
    }

    /**
     * get ip of dataservers by specific datacenter
     *
     * @param dataCenter
     * @return
     */
    public static Set<String> getIps(String dataCenter) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, DataServerNode> map = MAP.get(dataCenter);
            if (map != null) {
                return map.keySet();
            }
        }
        return new HashSet<>();
    }

    /**
     * remove dataserver by specific datacenter and ip
     *
     * @param dataCenter
     * @param ip
     */
    public static void remove(String dataCenter, String ip, DataServerConfig dataServerConfig) {
        if (MAP.containsKey(dataCenter)) {
            Map<String, DataServerNode> map = MAP.get(dataCenter);
            if (map != null) {
                DataServerNode dataServerNode = map.get(ip);
                Connection connection = dataServerNode.getConnection();
                if (connection != null && connection.isFine()) {
                    connection.close();
                }
                map.remove(ip);
            }
        }
    }

    /**
     * remove dataservers by specific datacenter
     *
     * @param dataCenter
     */
    public static void remove(String dataCenter) {
        getDataServerNodes(dataCenter).values().stream().map(DataServerNode::getConnection)
                .filter(connection -> connection != null && connection.isFine()).forEach(Connection::close);
        MAP.remove(dataCenter);
    }

    public void connectDataServer(String dataCenter, String ip) {
        Connection conn = null;
        for (int tryCount = 0; tryCount < TRY_COUNT; tryCount++) {
            try {
                conn = ((BoltChannel) dataNodeExchanger.connect(new URL(ip, dataServerConfig
                    .getSyncDataPort()))).getConnection();
                break;
            } catch (Exception e) {
                LOGGER.error("[DataServerChangeEventHandler] connect dataServer {} in {} error",
                    ip, dataCenter, e);
                TimeUtil.randomDelay(3000);
            }
        }
        if (conn == null || !conn.isFine()) {
            LOGGER.error(
                "[DataServerChangeEventHandler] connect dataServer {} in {} failed five times", ip,
                dataCenter);
            throw new RuntimeException(
                String
                    .format(
                        "[DataServerChangeEventHandler] connect dataServer %s in %s failed five times,dataServer will not work,please check connect!",
                        ip, dataCenter));
        }
    }
}