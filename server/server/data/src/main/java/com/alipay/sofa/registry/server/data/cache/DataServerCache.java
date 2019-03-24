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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.util.LocalServerStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * cache of dataservers
 *
 * @author qian.lqlq
 * @version $Id: DataServerCache.java, v 0.1 2018-05-05 17:00 qian.lqlq Exp $
 */
public class DataServerCache {

    private static final Logger                           LOGGER                  = LoggerFactory
                                                                                      .getLogger(DataServerCache.class);

    @Autowired
    private DataNodeStatus                                dataNodeStatus;

    @Autowired
    private DataServerConfig                              dataServerConfig;

    private volatile DataServerChangeItem                 dataServerChangeItem    = new DataServerChangeItem();

    private volatile DataServerChangeItem                 newDataServerChangeItem = new DataServerChangeItem();

    private final AtomicBoolean                           HAS_NOTIFY_ALL          = new AtomicBoolean(
                                                                                      false);

    private AtomicLong                                    curVersion              = new AtomicLong(
                                                                                      -1L);

    /** version -> Map(serverIp, serverStatus) */
    private Map<Long, Map<String, LocalServerStatusEnum>> nodeStatusMap           = new ConcurrentHashMap<>();

    /**
     * compare new infos and cached infos, and return these changed
     *
     * @param newItem
     * @return changedMap(datacenter, serverIp)
     */
    public Map<String, Set<String>> compareAndSet(DataServerChangeItem newItem) {
        synchronized (DataServerCache.class) {
            // versionMap: datacenter -> version
            Map<String, Long> newVersionMap = newItem.getVersionMap();
            //check current run newItem same
            Map<String, Long> currentNewVersionMap = newDataServerChangeItem.getVersionMap();
            if (!currentNewVersionMap.isEmpty()) {
                AtomicBoolean isTheSame = new AtomicBoolean(true);
                if (newVersionMap.size() == currentNewVersionMap.size()) {
                    for (Entry<String, Long> entry : newVersionMap.entrySet()) {
                        String dataCenter = entry.getKey();
                        Long version = entry.getValue();
                        Long currentVersion = currentNewVersionMap.get(dataCenter);
                        if (currentVersion != null && version != null) {
                            if (currentVersion.longValue() != version.longValue()) {
                                isTheSame.set(false);
                                break;
                            }
                        } else {
                            if (currentVersion != null || version != null) {
                                isTheSame.set(false);
                                break;
                            }
                        }
                    }
                } else {
                    isTheSame.set(false);
                }
                if (isTheSame.get()) {
                    LOGGER
                        .info(
                            "current process map has a same version as change map,this change will be ignored!process version={},get version={}",
                            currentNewVersionMap, newVersionMap);
                    return new HashMap<>();
                }
            }

            Map<String, Set<String>> changedMap = new HashMap<>();
            Map<String, Long> oldVersionMap = new HashMap<>(dataServerChangeItem.getVersionMap());
            Map<String, Map<String, DataNode>> newServerMap = newItem.getServerMap();

            for (Entry<String, Long> dataCenterEntry : newVersionMap.entrySet()) {
                String dataCenter = dataCenterEntry.getKey();
                if (oldVersionMap.containsKey(dataCenter)) {
                    Long oldVersion = oldVersionMap.remove(dataCenter);
                    if (oldVersion >= dataCenterEntry.getValue()) {
                        continue;
                    }
                }
                changedMap.put(dataCenter, newServerMap.get(dataCenter).keySet());
            }
            if (!oldVersionMap.isEmpty()) {
                for (String dataCenter : oldVersionMap.keySet()) {
                    changedMap.put(dataCenter, new HashSet<>());
                }
            }
            if (changedMap.containsKey(dataServerConfig.getLocalDataCenter())) {
                init(newVersionMap.get(dataServerConfig.getLocalDataCenter()));
            }
            if (!changedMap.isEmpty()) {
                LOGGER.info("old server map = {}", dataServerChangeItem.getServerMap());
                LOGGER.info("new server map = {}", newServerMap);
                LOGGER.info("new server version map = {}", newVersionMap);
                LOGGER.info("status map = {}", nodeStatusMap);
                LOGGER.info("changed map = {}", changedMap);
                newDataServerChangeItem = newItem;
            }
            return changedMap;
        }
    }

    private void init(long version) {
        if (curVersion.compareAndSet(-1, version)) {
            Map<String, LocalServerStatusEnum> map = new ConcurrentHashMap<>();
            map.put(DataServerConfig.IP, LocalServerStatusEnum.INITIAL);
            nodeStatusMap.put(version, map);
        } else {
            //first get dataChange after other data send init message
            Map<String, LocalServerStatusEnum> map = nodeStatusMap.get(curVersion.get());
            if (map != null) {
                map.putIfAbsent(DataServerConfig.IP, LocalServerStatusEnum.INITIAL);
            }
        }
    }

    private void addStatus(long version, String ip, LocalServerStatusEnum localServerStatusEnum) {
        long lastVersion = curVersion.getAndSet(version);
        Map<String, LocalServerStatusEnum> map = nodeStatusMap.remove(lastVersion);
        if (map != null) {
            map.put(ip, localServerStatusEnum);
            nodeStatusMap.put(curVersion.get(), map);
        } else {
            Map newMap = new ConcurrentHashMap<>();
            map = nodeStatusMap.putIfAbsent(curVersion.get(), newMap);
            if (map == null) {
                map = newMap;
            }
            map.put(ip, localServerStatusEnum);
        }
    }

    private void resetStatusMapToWorking() {
        Map<String, LocalServerStatusEnum> map = nodeStatusMap.get(curVersion.get());
        if (map != null) {
            map.clear();
            map.put(DataServerConfig.IP, LocalServerStatusEnum.WORKING);
        }
        LOGGER.info("nodeStatusMap has been reset!Result {}", nodeStatusMap);
    }

    public boolean removeNotifyNewStatusNode(String ip) {
        synchronized (DataServerCache.class) {
            Map<String, LocalServerStatusEnum> map = nodeStatusMap.get(curVersion.get());
            if (map != null) {
                LocalServerStatusEnum statusEnum = map.get(ip);
                if (statusEnum != null && statusEnum == LocalServerStatusEnum.INITIAL) {
                    if (map.remove(ip) != null) {
                        LOGGER.info("nodeStatusMap remove init status node {}!Result {}", ip,
                            nodeStatusMap);
                    }
                }
            }
            return false;
        }
    }

    public void synced(long version, String ip) {
        synchronized (DataServerCache.class) {
            if (version >= curVersion.get()) {
                addStatus(version, ip, LocalServerStatusEnum.WORKING);
                LOGGER.info("synced working = {}, version={},send ip={}", nodeStatusMap, version,
                    ip);
                updateDataServerStatus();
            }
        }
    }

    public void addNotWorkingServer(long version, String ip) {
        synchronized (DataServerCache.class) {
            if (version >= curVersion.get()) {
                addStatus(version, ip, LocalServerStatusEnum.INITIAL);
                LOGGER.info("add not working = {}, version={},send ip={}", nodeStatusMap, version,
                    ip);
                if (dataNodeStatus.getStatus() != LocalServerStatusEnum.WORKING) {
                    updateDataServerStatus();
                }
            }
        }
    }

    private void updateDataServerStatus() {
        if (dataNodeStatus.getStatus() != LocalServerStatusEnum.WORKING) {

            Long newVersion = newDataServerChangeItem.getVersionMap().get(
                dataServerConfig.getLocalDataCenter());

            if (newVersion == null) {
                LOGGER.info("no node change receive from meta about current dataCenter!");
                return;
            }

            if (newVersion != curVersion.get()) {
                LOGGER.info("version not match,current {} push {}", curVersion.longValue(),
                    newVersion);
                return;
            }

            Map<String, LocalServerStatusEnum> map = nodeStatusMap.get(curVersion.get());
            if (map != null) {
                Set<String> ips = map.keySet();
                if (!ips.containsAll(newDataServerChangeItem.getServerMap()
                    .get(dataServerConfig.getLocalDataCenter()).keySet())) {
                    LOGGER.info(
                        "nodeStatusMap not contains all push list,nodeStatusMap {} push {}",
                        nodeStatusMap,
                        newDataServerChangeItem.getServerMap()
                            .get(dataServerConfig.getLocalDataCenter()).keySet());
                    return;
                }
            } else {
                LOGGER.info("nodeStatusMap has not got other data node status!");
                return;
            }

            if (!HAS_NOTIFY_ALL.get()) {
                LOGGER.info("Has not notify all!");
                return;
            }

            dataNodeStatus.setStatus(LocalServerStatusEnum.WORKING);

            //after working status,must clean this map,because calculate backupTriad need add not working node,see LocalDataServerChangeEventHandler getToBeSyncMap
            resetStatusMapToWorking();
        }
    }

    public Long getCurVersion() {
        return curVersion.get();
    }

    public void notifiedAll() {
        if (HAS_NOTIFY_ALL.compareAndSet(false, true)) {
            updateDataServerStatus();
        }
    }

    public void checkAndUpdateStatus(long version) {
        synchronized (DataServerCache.class) {
            if (version == curVersion.get()) {
                updateDataServerStatus();
            }
        }
    }

    public Set<String> getNotWorking() {
        synchronized (DataServerCache.class) {
            Set<String> ret = new HashSet<>();
            Map<String, LocalServerStatusEnum> map = nodeStatusMap.get(curVersion.get());
            if (map != null) {
                map.forEach((ip, status) -> {
                    if (status != LocalServerStatusEnum.WORKING) {
                        ret.add(ip);
                    }
                });
            }
            return ret;
        }
    }

    public void updateItem(Map<String, DataNode> localDataNodes, Long version, String dataCenter) {
        synchronized (DataServerCache.class) {
            Long oldVersion = dataServerChangeItem.getVersionMap().get(dataCenter);
            Map<String, DataNode> oldList = dataServerChangeItem.getServerMap().get(dataCenter);
            Set<String> oldIps = oldList == null ? new HashSet<>() : oldList.keySet();
            Set<String> newIps = localDataNodes == null ? new HashSet<>() : localDataNodes.keySet();
            LOGGER.warn("Update DataCenter={} Item version from={} to={},nodeMap from={} to={}",
                dataCenter, oldVersion, version, oldIps, newIps);
            dataServerChangeItem.getServerMap().put(dataCenter, localDataNodes);
            dataServerChangeItem.getVersionMap().put(dataCenter, version);

        }
    }

    public Map<String, DataNode> getDataServers(String dataCenter) {
        return getDataServers(dataCenter, dataServerChangeItem);
    }

    public Map<String, DataNode> getDataServers(String dataCenter,
                                                DataServerChangeItem dataServerChangeItem) {
        return doGetDataServers(dataCenter, dataServerChangeItem);
    }

    public Map<String, DataNode> getNewDataServerMap(String dataCenter) {
        return doGetDataServers(dataCenter, newDataServerChangeItem);
    }

    private Map<String, DataNode> doGetDataServers(String dataCenter,
                                                   DataServerChangeItem dataServerChangeItem) {
        synchronized (DataServerCache.class) {
            Map<String, Map<String, DataNode>> dataserverMap = dataServerChangeItem.getServerMap();
            if (dataserverMap.containsKey(dataCenter)) {
                return dataserverMap.get(dataCenter);
            } else {
                return new HashMap<>();
            }
        }
    }

    public Long getDataCenterNewVersion(String dataCenter) {
        synchronized (DataServerCache.class) {
            Map<String, Long> versionMap = newDataServerChangeItem.getVersionMap();
            if (versionMap.containsKey(dataCenter)) {
                return versionMap.get(dataCenter);
            } else {
                return null;
            }
        }
    }

    public BackupTriad calculateOldBackupTriad(String dataInfoId, String dataCenter,
                                               DataServerConfig dataServerBootstrapConfig) {
        Map<String, Map<String, DataNode>> dataServerMap = dataServerChangeItem.getServerMap();
        Map<String, DataNode> dataNodeMap = dataServerMap.get(dataCenter);

        if (dataNodeMap != null && !dataNodeMap.isEmpty()) {

            Collection<DataNode> dataServerNodes = dataNodeMap.values();

            ConsistentHash<DataNode> consistentHash = new ConsistentHash<>(
                dataServerBootstrapConfig.getNumberOfReplicas(), dataServerNodes);

            List<DataNode> list = consistentHash.getNUniqueNodesFor(dataInfoId,
                dataServerBootstrapConfig.getStoreNodes());

            return new BackupTriad(dataInfoId, list);
        } else {
            LOGGER.warn("Calculate Old BackupTriad,old dataServer list is empty!");
            return null;
        }
    }
}