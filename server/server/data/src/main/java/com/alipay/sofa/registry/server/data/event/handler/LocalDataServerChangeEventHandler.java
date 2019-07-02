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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.NotifyFetchDatumRequest;
import com.alipay.sofa.registry.common.model.dataserver.NotifyOnlineRequest;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.BackupTriad;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.renew.LocalDataServerCleanHandler;
import com.alipay.sofa.registry.server.data.event.LocalDataServerChangeEvent;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;
import com.alipay.sofa.registry.server.data.util.LocalServerStatusEnum;
import com.alipay.sofa.registry.server.data.util.TimeUtil;
import com.google.common.collect.Lists;

/**
 *
 *
 * @author qian.lqlq
 * @version $Id: LocalDataServerChangeEventHandler.java, v 0.1 2018-04-28 23:55 qian.lqlq Exp $
 */
public class LocalDataServerChangeEventHandler extends
                                              AbstractEventHandler<LocalDataServerChangeEvent> {

    private static final Logger                       LOGGER    = LoggerFactory
                                                                    .getLogger(LocalDataServerChangeEventHandler.class);

    @Autowired
    private DataServerConfig                          dataServerConfig;

    @Autowired
    private LocalDataServerCleanHandler               localDataServerCleanHandler;

    @Autowired
    private DataServerCache                           dataServerCache;

    @Autowired
    private DataNodeExchanger                         dataNodeExchanger;

    @Autowired
    private DataNodeStatus                            dataNodeStatus;

    @Autowired
    private DatumCache                                datumCache;

    private BlockingQueue<LocalDataServerChangeEvent> events    = new LinkedBlockingDeque<>();

    private AtomicBoolean                             isChanged = new AtomicBoolean(false);

    @Override
    public Class interest() {
        return LocalDataServerChangeEvent.class;
    }

    @Override
    public void doHandle(LocalDataServerChangeEvent localDataServerChangeEvent) {
        isChanged.set(true);
        localDataServerCleanHandler.reset();
        events.offer(localDataServerChangeEvent);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        start();
    }

    /**
     *
     */
    public void start() {
        Executor executor = ExecutorFactory
            .newSingleThreadExecutor(LocalDataServerChangeEventHandler.class.getSimpleName());
        executor.execute(new LocalClusterDataSyncer());
    }

    private class LocalClusterDataSyncer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    LocalDataServerChangeEvent event = events.take();
                    //if the new joined servers contains self, set status as INITIAL
                    Set<String> newJoined = event.getNewJoined();
                    if (newJoined.contains(DataServerConfig.IP)
                        && dataNodeStatus.getStatus() != LocalServerStatusEnum.INITIAL) {
                        dataNodeStatus.setStatus(LocalServerStatusEnum.INITIAL);
                    }
                    //if size of events is greater than 0, not handle and continue, only handle the last one in the queue
                    if (events.size() > 0) {
                        continue;
                    }
                    long changeVersion = event.getVersion();

                    LOGGER.info("begin handle dataserver change, version={},localDataServer={}",
                        changeVersion, event.getLocalDataServerMap().keySet());
                    isChanged.set(false);
                    if (LocalServerStatusEnum.WORKING == dataNodeStatus.getStatus()) {
                        //if local server is working, compare sync data
                        notifyToFetch(event, changeVersion);
                    } else {
                        dataServerCache.checkAndUpdateStatus(changeVersion);
                        //if local server is not working, notify others that i am newer
                        notifyOnline(changeVersion);

                        dataServerCache.updateItem(event.getLocalDataServerMap(),
                            event.getLocalDataCenterversion(),
                            dataServerConfig.getLocalDataCenter());
                    }
                } catch (Throwable t) {
                    LOGGER.error("sync local data error", t);
                }
            }
        }

        /**
         * notify onlined newly dataservers to fetch datum
         *
         * @param event
         * @param changeVersion
         */
        private void notifyToFetch(LocalDataServerChangeEvent event, long changeVersion) {

            Map<String, DataNode> dataServerMapIn = event.getLocalDataServerMap();
            List<DataNode> dataServerNodeList = Lists.newArrayList(dataServerMapIn.values());
            ConsistentHash<DataNode> consistentHash = new ConsistentHash<>(
                dataServerConfig.getNumberOfReplicas(), dataServerNodeList);
            Map<String, DataNode> dataServerMap = new ConcurrentHashMap<>(dataServerMapIn);

            Map<String, Map<String, Map<String, BackupTriad>>> toBeSyncMap = getToBeSyncMap(consistentHash);
            if (!isChanged.get()) {
                if (!toBeSyncMap.isEmpty()) {
                    for (Entry<String, Map<String, Map<String, BackupTriad>>> toBeSyncEntry : toBeSyncMap
                        .entrySet()) {
                        String ip = toBeSyncEntry.getKey();
                        Map<String, Map<String, Long>> allVersionMap = new HashMap<>();
                        Map<String, Map<String, BackupTriad>> dataInfoMap = toBeSyncEntry
                            .getValue();
                        for (Entry<String, Map<String, BackupTriad>> dataCenterEntry : dataInfoMap
                            .entrySet()) {
                            String dataCenter = dataCenterEntry.getKey();
                            Map<String, Long> versionMap = new HashMap<>();
                            Map<String, BackupTriad> dataTriadMap = dataCenterEntry.getValue();
                            for (Entry<String, BackupTriad> dataTriadEntry : dataTriadMap
                                .entrySet()) {
                                String dataInfoId = dataTriadEntry.getKey();
                                Datum datum = datumCache.get(dataCenter, dataInfoId);
                                if (datum != null) {
                                    versionMap.put(dataInfoId, datum.getVersion());
                                }
                            }
                            if (!versionMap.isEmpty()) {
                                allVersionMap.put(dataCenter, versionMap);
                            }
                        }
                        if (!allVersionMap.isEmpty()) {
                            dataServerMap.remove(ip);
                            if (doNotify(ip, allVersionMap, changeVersion)) {
                                //remove new status node,avoid duplicate notify sync data
                                dataServerCache.removeNotifyNewStatusNode(ip);
                            }
                        }
                    }
                }
                //if no datum to notify, notify empty map
                if (!dataServerMap.isEmpty()) {
                    for (String targetIp : dataServerMap.keySet()) {
                        if (doNotify(targetIp, new HashMap<>(), changeVersion)) {
                            //remove new status node,avoid duplicate notify sync data
                            dataServerCache.removeNotifyNewStatusNode(targetIp);
                        }
                    }
                }
                if (!isChanged.get()) {
                    //update server list
                    dataServerCache.updateItem(dataServerMapIn, event.getLocalDataCenterversion(),
                        dataServerConfig.getLocalDataCenter());
                }
            }
        }

        /**
         * get map of datum to be synced
         *
         * @param consistentHash
         * @return
         */
        private Map<String/*ip*/, Map<String/*datacenter*/, Map<String/*datainfoId*/, BackupTriad>>> getToBeSyncMap(ConsistentHash<DataNode> consistentHash) {

            Map<String, Map<String, Map<String, BackupTriad>>> toBeSyncMap = new HashMap<>();
            Map<String, List<DataNode>> triadCache = new HashMap<>();

            ConsistentHash<DataNode> consistentHashOld = dataServerCache
                .calculateOldConsistentHash(dataServerConfig.getLocalDataCenter());
            if (consistentHash == null) {
                LOGGER.error("Calculate Old ConsistentHash error!");
                throw new RuntimeException("Calculate Old ConsistentHash error!");
            }

            //compute new triad for every datum in cache
            Map<String, Map<String, Datum>> allMap = datumCache.getAll();
            for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
                String dataCenter = dataCenterEntry.getKey();
                Map<String, Datum> datumMap = dataCenterEntry.getValue();
                for (String dataInfoId : datumMap.keySet()) {
                    //if dataservers are changed, no longer to handle
                    if (isChanged.get()) {
                        return new HashMap<>();
                    }
                    //compute new triad by new dataservers
                    List<DataNode> backupNodes;
                    if (triadCache.containsKey(dataInfoId)) {
                        backupNodes = triadCache.get(dataInfoId);
                    } else {
                        backupNodes = consistentHash.getNUniqueNodesFor(dataInfoId,
                            dataServerConfig.getStoreNodes());
                        triadCache.put(dataInfoId, backupNodes);
                    }
                    BackupTriad backupTriad = new BackupTriad(dataInfoId,
                        consistentHashOld.getNUniqueNodesFor(dataInfoId,
                            dataServerConfig.getStoreNodes()));
                    if (backupTriad != null) {
                        List<DataNode> newJoinedNodes = backupTriad.getNewJoined(backupNodes,
                            dataServerCache.getNotWorking());
                        //all data node send notify to new join,the same data maybe send twice,receiver check same data duplicate!
                        LOGGER
                            .info(
                                "DataInfoId {} has got newJoinedNodes={}  for backupNodes={},now backupTriad is {}",
                                dataInfoId, newJoinedNodes, backupNodes, backupTriad);
                        if (!newJoinedNodes.isEmpty()) {
                            for (DataNode node : newJoinedNodes) {
                                String ip = node.getIp();
                                if (!toBeSyncMap.containsKey(ip)) {
                                    toBeSyncMap.put(ip, new HashMap<>());
                                }
                                Map<String, Map<String, BackupTriad>> dataInfoMap = toBeSyncMap
                                    .get(ip);
                                if (!dataInfoMap.containsKey(dataCenter)) {
                                    dataInfoMap.put(dataCenter, new HashMap<>());
                                }
                                dataInfoMap.get(dataCenter).put(dataInfoId, backupTriad);
                            }
                        }
                    }
                }
            }
            LOGGER.info("Get to Be SyncMap {}", toBeSyncMap);
            return toBeSyncMap;
        }

        /**
         * do notify
         *
         * @param targetIp
         * @param notifyVersionMap
         */
        private boolean doNotify(String targetIp, Map<String, Map<String, Long>> notifyVersionMap,
                                 long version) {
            while (!isChanged.get()) {
                DataServerNode targetNode = DataServerNodeFactory.getDataServerNode(
                    dataServerConfig.getLocalDataCenter(), targetIp);
                if (targetNode == null || targetNode.getConnection() == null) {
                    LOGGER.info(
                        "notify version change to sync has not connect,targetNode={}, map={}",
                        targetIp, notifyVersionMap);
                    return false;
                }
                try {
                    CommonResponse response = (CommonResponse) dataNodeExchanger.request(
                        new Request() {
                            @Override
                            public Object getRequestBody() {
                                return new NotifyFetchDatumRequest(notifyVersionMap,
                                    DataServerConfig.IP, version);
                            }

                            @Override
                            public URL getRequestUrl() {
                                return new URL(targetNode.getConnection().getRemoteIP(), targetNode
                                    .getConnection().getRemotePort());
                            }
                        }).getResult();

                    if (response.isSuccess()) {
                        LOGGER
                            .info(
                                "notify {} version change to sync,current node list version={}, map={}",
                                targetNode.getIp(), version, notifyVersionMap);
                        return true;
                    } else {
                        throw new RuntimeException(response.getMessage());
                    }
                } catch (Throwable e) {
                    LOGGER.error("notify {} to fetch datum error", targetIp, e);
                    TimeUtil.randomDelay(500);
                }
            }
            return false;
        }

        /**
         * notify other dataservers that this server is online newly
         *
         * @param changeVersion
         */
        private void notifyOnline(long changeVersion) {
            Map<String, DataServerNode> dataServerNodeMap = DataServerNodeFactory
                .getDataServerNodes(dataServerConfig.getLocalDataCenter());
            for (Entry<String, DataServerNode> serverEntry : dataServerNodeMap.entrySet()) {
                while (true) {
                    String ip = serverEntry.getKey();
                    DataServerNode dataServerNode = serverEntry.getValue();
                    if (dataServerNode == null) {
                        break;
                    }
                    try {
                        if (dataServerNode.getConnection() == null
                            || !dataServerNode.getConnection().isFine()) {
                            //maybe get dataNode from metaServer,current has not connected!wait for connect task execute
                            TimeUtil.randomDelay(1000);
                            continue;
                        }
                        CommonResponse response = (CommonResponse) dataNodeExchanger.request(
                            new Request() {

                                @Override
                                public Object getRequestBody() {
                                    return new NotifyOnlineRequest(DataServerConfig.IP,
                                        changeVersion);
                                }

                                @Override
                                public URL getRequestUrl() {
                                    return new URL(dataServerNode.getConnection().getRemoteIP(),
                                        dataServerNode.getConnection().getRemotePort());
                                }
                            }).getResult();
                        if (response.isSuccess()) {
                            LOGGER.info("notify {} that i am newer success,version={}", ip,
                                changeVersion);
                            break;
                        } else {
                            throw new RuntimeException(response.getMessage());
                        }
                    } catch (Exception e) {
                        LOGGER.info("notify {} that i am newer failed", ip);
                        LOGGER.error("notify {} that i am newer error", ip, e);
                        TimeUtil.randomDelay(500);
                    }
                }
            }
        }

    }
}