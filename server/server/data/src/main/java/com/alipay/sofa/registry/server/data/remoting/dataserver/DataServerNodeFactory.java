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
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.google.common.collect.Lists;

/**
 * the factory to hold other dataservers and connection connected to them
 *
 * @author qian.lqlq
 * @version $Id: DataServerNodeFactory.java, v 0.1 2018-03-13 18:45 qian.lqlq Exp $
 */
public class DataServerNodeFactory {

    /**
     * row:     dataCenter
     * column:  ip
     * value    dataServerNode
     */
    private static final Map<String, Map<String, DataServerNode>>    MAP                 = new ConcurrentHashMap<>();

    /**
     * key:     dataCenter
     * value:   consistentHash
     */
    private static final Map<String, ConsistentHash<DataServerNode>> CONSISTENT_HASH_MAP = new ConcurrentHashMap<>();

    private static AtomicBoolean                                     init                = new AtomicBoolean(
                                                                                             false);

    /**
     * add a dataserver to cache
     *
     * @param dataServerNode
     */
    public static void register(DataServerNode dataServerNode, DataServerConfig dataServerConfig) {
        String dataCenter = dataServerNode.getDataCenter();
        if (!MAP.containsKey(dataCenter)) {
            MAP.put(dataCenter, new ConcurrentHashMap<>());
        }
        MAP.get(dataCenter).put(dataServerNode.getIp(), dataServerNode);
        refreshConsistent(dataCenter, dataServerConfig);
    }

    /**
     * refresh instance of consistentHash
     *
     * @param dataCenter
     */
    public static void refreshConsistent(String dataCenter, DataServerConfig dataServerConfig) {
        List<DataServerNode> dataServerNodes = Lists.newArrayList(MAP.get(dataCenter).values());
        if (dataServerConfig.getLocalDataCenter().equals(dataCenter)) {
            if (!MAP.get(dataCenter).keySet().contains(DataServerConfig.IP)) {
                dataServerNodes.add(new DataServerNode(DataServerConfig.IP, dataServerConfig
                    .getLocalDataCenter(), null));
            }
        }
        CONSISTENT_HASH_MAP.put(dataCenter,
            new ConsistentHash<>(dataServerConfig.getNumberOfReplicas(), dataServerNodes));
    }

    /**
     * for single node consistentHash
     * @param dataServerConfig
     */
    public static void initConsistent(DataServerConfig dataServerConfig) {
        if (init.compareAndSet(false, true)) {
            List<DataServerNode> dataServerNodes = Lists.newArrayList();
            dataServerNodes.add(new DataServerNode(DataServerConfig.IP, dataServerConfig
                .getLocalDataCenter(), null));
            CONSISTENT_HASH_MAP.put(dataServerConfig.getLocalDataCenter(), new ConsistentHash<>(
                dataServerConfig.getNumberOfReplicas(), dataServerNodes));
        }
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
        refreshConsistent(dataCenter, dataServerConfig);
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
        CONSISTENT_HASH_MAP.remove(dataCenter);
    }

    /**
     * get dataserver by specific datacenter and dataInfoId
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public static DataServerNode computeDataServerNode(String dataCenter, String dataInfoId) {
        ConsistentHash<DataServerNode> consistentHash = CONSISTENT_HASH_MAP.get(dataCenter);
        if (consistentHash != null) {
            return CONSISTENT_HASH_MAP.get(dataCenter).getNodeFor(dataInfoId);
        }
        return null;
    }

    public static List<DataServerNode> computeDataServerNodes(String dataCenter, String dataInfoId,
                                                              int backupNodes) {
        ConsistentHash<DataServerNode> consistentHash = CONSISTENT_HASH_MAP.get(dataCenter);
        if (consistentHash != null) {
            return CONSISTENT_HASH_MAP.get(dataCenter).getNUniqueNodesFor(dataInfoId, backupNodes);
        }
        return null;
    }

    /**
     * get all datacenters
     *
     * @return
     */
    public static Set<String> getAllDataCenters() {
        return MAP.keySet();
    }

}