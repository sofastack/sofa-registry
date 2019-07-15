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
package com.alipay.sofa.registry.server.data.event.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.cache.DataServerChangeItem;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.LocalDataServerChangeEvent;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;
import com.alipay.sofa.registry.server.data.util.TimeUtil;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataServerChangeEventHandler.java, v 0.1 2018-03-13 14:38 qian.lqlq Exp $
 */
public class DataServerChangeEventHandler extends AbstractEventHandler<DataServerChangeEvent> {

    private static final Logger LOGGER    = LoggerFactory
                                              .getLogger(DataServerChangeEventHandler.class);

    private static final int    TRY_COUNT = 5;

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Autowired
    private DataServerCache     dataServerCache;

    @Autowired
    private DataNodeExchanger   dataNodeExchanger;

    @Autowired
    private EventCenter         eventCenter;

    @Override
    public Class interest() {
        return DataServerChangeEvent.class;
    }

    @Override
    public void doHandle(DataServerChangeEvent event) {
        synchronized (this) {
            //register self first,execute once
            DataServerNodeFactory.initConsistent(dataServerConfig);

            DataServerChangeItem dataServerChangeItem = event.getDataServerChangeItem();
            Set<String> localDataServers = dataServerCache.getDataServers(
                dataServerConfig.getLocalDataCenter()).keySet();
            //get changed dataservers
            Map<String, Set<String>> changedMap = dataServerCache
                .compareAndSet(dataServerChangeItem);
            if(!changedMap.isEmpty()) {
                for (Entry<String, Set<String>> changeEntry : changedMap.entrySet()) {
                    String dataCenter = changeEntry.getKey();
                    Set<String> ips = changeEntry.getValue();
                    if (!CollectionUtils.isEmpty(ips)) {
                        for (String ip : ips) {
                            if (!StringUtils.equals(ip, DataServerConfig.IP)) {
                                DataServerNode dataServerNode = DataServerNodeFactory
                                        .getDataServerNode(dataCenter, ip);
                                if (dataServerNode == null || dataServerNode.getConnection() == null
                                        || !dataServerNode.getConnection().isFine()) {
                                    connectDataServer(dataCenter, ip);
                                }
                            }
                        }
                        //remove all old DataServerNode not in change map
                        Set<String> ipSet = DataServerNodeFactory.getIps(dataCenter);
                        for (String ip : ipSet) {
                            if (!ips.contains(ip)) {
                                DataServerNodeFactory.remove(dataCenter, ip, dataServerConfig);
                                LOGGER.info(
                                        "[DataServerChangeEventHandler] remove connection, datacenter:{}, ip:{}",
                                        dataCenter, ip);
                            }
                        }

                        Long newVersion = dataServerCache.getDataCenterNewVersion(dataCenter);
                        Map<String, DataNode> newDataNodes = dataServerCache.getNewDataServerMap(dataCenter);
                        //if the datacenter is self, post LocalDataServerChangeEvent
                        if (dataServerConfig.getLocalDataCenter().equals(dataCenter)) {
                            Set<String> newjoined = new HashSet<>(ips);
                            newjoined.removeAll(localDataServers);
                            //avoid input map reference operation DataServerNodeFactory MAP
                            Map<String, DataNode> map = new ConcurrentHashMap<>(newDataNodes);

                            LOGGER.info("Node list change fire LocalDataServerChangeEvent,current node list={},version={}",
                                    map.keySet(), newVersion);
                            eventCenter.post(new LocalDataServerChangeEvent(map, newjoined,
                                    dataServerChangeItem.getVersionMap()
                                            .get(dataServerConfig.getLocalDataCenter()),
                                    newVersion));
                        } else {
                            dataServerCache.updateItem(newDataNodes, newVersion, dataCenter);
                        }
                    } else {
                        //if the datacenter which has no dataservers is not self, remove it
                        if (!dataServerConfig.getLocalDataCenter().equals(dataCenter)) {
                            removeDataCenter(dataCenter);
                        }
                        Long newVersion = dataServerCache.getDataCenterNewVersion(dataCenter);
                        Map<String, DataNode> newDataNodes = dataServerCache.getNewDataServerMap(dataCenter);
                        dataServerCache.updateItem(newDataNodes, newVersion, dataCenter);
                    }
                }
            } else {
                //refresh for keep connect
                Set<String> allDataCenter = new HashSet<>(DataServerNodeFactory.getAllDataCenters());
                for (String dataCenter:allDataCenter) {
                    Map<String, DataServerNode> dataNodes = DataServerNodeFactory.getDataServerNodes(dataCenter);
                    if(dataNodes != null && !dataNodes.isEmpty()){

                        dataNodes.forEach((ip,dataServerNode)->{
                            if (!StringUtils.equals(ip, DataServerConfig.IP)) {
                                Connection connection = dataServerNode.getConnection();
                                if (connection != null && !connection.isFine()) {
                                    LOGGER.warn("[DataServerChangeEventHandler] dataServer connections is not fine,try to reconnect it,old connection={},dataCenter={}",
                                                    connection.getRemoteAddress(), dataCenter);
                                    connectDataServer(dataCenter, ip);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * connect specific dataserver
     *
     * @param dataCenter
     * @param ip
     */
    private void connectDataServer(String dataCenter, String ip) {
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
                "[DataServerChangeEventHandler] connect dataserver {} in {} failed five times", ip,
                dataCenter);
            throw new RuntimeException(
                String
                    .format(
                        "[DataServerChangeEventHandler] connect dataserver %s in %s failed five times,dataServer will not work,please check connect!",
                        ip, dataCenter));
        }
        //maybe get dataNode from metaServer,current has not start! register dataNode info to factory,wait for connect task next execute
        DataServerNodeFactory.register(new DataServerNode(ip, dataCenter, conn), dataServerConfig);
    }

    /**
     * remove datacenter, and close connections of dataservers in this datacenter
     *
     * @param dataCenter
     */
    private void removeDataCenter(String dataCenter) {
        DataServerNodeFactory.getDataServerNodes(dataCenter).values().stream().map(DataServerNode::getConnection)
                .filter(connection -> connection != null && connection.isFine()).forEach(Connection::close);
        DataServerNodeFactory.remove(dataCenter);
        LOGGER.info(
                "[DataServerChangeEventHandler] remove connections of datacenter : {}", dataCenter);
    }
}