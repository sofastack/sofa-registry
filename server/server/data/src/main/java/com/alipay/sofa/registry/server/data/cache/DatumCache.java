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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;

/**
 * cache of datum, providing query function to the upper module
 *
 * @author qian.lqlq
 * @version $Id: this.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
public class DatumCache {

    public static final long                          ERROR_DATUM_VERSION  = -2L;

    /**
     * row:     dataCenter
     * column:  dataInfoId
     * value:   datum
     */
    private final Map<String, Map<String, Datum>>     DATUM_MAP            = new ConcurrentHashMap<>();

    /**
     * all datum index
     *
     * row:     ip:port
     * column:  registerId
     * value:   publisher
     */
    private final Map<String, Map<String, Publisher>> ALL_CONNECT_ID_INDEX = new ConcurrentHashMap<>();

    @Autowired
    private DataServerConfig                          dataServerConfig;

    /**
     * get datum by specific dataCenter and dataInfoId
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public Datum get(String dataCenter, String dataInfoId) {
        if (DATUM_MAP.containsKey(dataCenter)) {
            Map<String, Datum> map = DATUM_MAP.get(dataCenter);
            if (map.containsKey(dataInfoId)) {
                return map.get(dataInfoId);
            }
        }
        return null;
    }

    /**
     * get datum of all datercenters by dataInfoId
     *
     * @param dataInfoId
     * @return
     */
    public Map<String, Datum> get(String dataInfoId) {
        Map<String, Datum> datumMap = new HashMap<>();
        DATUM_MAP.forEach((dataCenter, datums) -> {
            if (datums.containsKey(dataInfoId)) {
                datumMap.put(dataCenter, datums.get(dataInfoId));
            }
        });

        return datumMap;
    }

    /**
     * get datum group by dataCenter
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public Map<String, Datum> getDatumGroupByDataCenter(String dataCenter, String dataInfoId) {
        Map<String, Datum> map = new HashMap<>();
        if (StringUtils.isEmpty(dataCenter)) {
            map = this.get(dataInfoId);
        } else {
            Datum datum = this.get(dataCenter, dataInfoId);
            if (datum != null) {
                map.put(dataCenter, datum);
            }
        }
        return map;
    }

    /**
     * get all datum
     *
     * @return
     */
    public Map<String, Map<String, Datum>> getAll() {
        return DATUM_MAP;
    }

    /**
     *
     *
     * @param connectId
     * @return
     */
    public Map<String, Publisher> getByConnectId(String connectId) {
        return ALL_CONNECT_ID_INDEX.getOrDefault(connectId, null);
    }

    /**
     * get own publishers by connectId
     */
    public Map<String, Publisher> getOwnByConnectId(String connectId) {
        Map<String, Publisher> ownPubMap = new HashMap<>();
        Map<String, Publisher> allPubMap = ALL_CONNECT_ID_INDEX.getOrDefault(connectId, null);
        if (allPubMap != null) {
            for (Map.Entry<String, Publisher> entry : allPubMap.entrySet()) {
                String registerId = entry.getKey();
                Publisher publisher = entry.getValue();
                DataServerNode dataServerNode = DataServerNodeFactory.computeDataServerNode(
                    dataServerConfig.getLocalDataCenter(), publisher.getDataInfoId());
                if (DataServerConfig.IP.equals(dataServerNode.getIp())) {
                    ownPubMap.put(registerId, publisher);
                }
            }
        }
        return ownPubMap;
    }

    /**
     * put datum into cache
     *
     * @param changeType
     * @param datum
     * @return the last version before datum changed, if datum is not exist, return null
     */
    public MergeResult putDatum(DataChangeTypeEnum changeType, Datum datum) {
        MergeResult mergeResult;
        String dataCenter = datum.getDataCenter();
        String dataInfoId = datum.getDataInfoId();
        Map<String, Datum> map = getDatumMapByDataCenter(dataCenter);

        //first put UnPublisher datum(dataId group instanceId is null),can not add to cache
        if (datum.getDataId() == null && map.get(dataInfoId) == null) {
            mergeResult = new MergeResult(ERROR_DATUM_VERSION, false);
            return mergeResult;
        }

        Datum ret = map.putIfAbsent(dataInfoId, datum);
        if (ret == null) {
            Set<Entry<String, Publisher>> entries = datum.getPubMap().entrySet();
            Iterator<Entry<String, Publisher>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Entry<String, Publisher> entry = iterator.next();
                Publisher publisher = entry.getValue();
                if (!(publisher instanceof UnPublisher)) {
                    addToIndex(publisher);
                } else {
                    //first put to cache,UnPublisher data must remove,not so got error pub data exist
                    iterator.remove();
                }
            }
            mergeResult = new MergeResult(null, true);
        } else {
            if (changeType == DataChangeTypeEnum.MERGE) {
                mergeResult = mergeDatum(datum);
            } else {
                Long lastVersion = coverDatum(datum);
                mergeResult = new MergeResult(lastVersion, true);
            }
        }
        return mergeResult;
    }

    private Map<String, Datum> getDatumMapByDataCenter(String dataCenter) {
        Map<String, Datum> map = DATUM_MAP.get(dataCenter);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            Map<String, Datum> ret = DATUM_MAP.putIfAbsent(dataCenter, map);
            if (ret != null) {
                map = ret;
            }
        }
        return map;
    }

    /**
     * remove datum ant contains all pub data,and clean all the client map reference
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public boolean cleanDatum(String dataCenter, String dataInfoId) {

        Map<String, Datum> datumMap = DATUM_MAP.get(dataCenter);
        if (datumMap != null) {
            Datum cacheDatum = datumMap.remove(dataInfoId);
            if (cacheDatum != null) {
                Map<String, Publisher> cachePubMap = cacheDatum.getPubMap();

                for (Entry<String, Publisher> cachePubEntry : cachePubMap.entrySet()) {
                    String registerId = cachePubEntry.getKey();
                    Publisher cachePub = cachePubEntry.getValue();
                    //remove from cache
                    if (cachePub != null) {
                        cachePubMap.remove(registerId);
                        removeFromIndex(cachePub);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * merge datum in cache
     *
     * @param datum
     * @return
     */
    private MergeResult mergeDatum(Datum datum) {
        boolean isChanged = false;
        Datum cacheDatum = DATUM_MAP.get(datum.getDataCenter()).get(datum.getDataInfoId());
        Map<String, Publisher> cachePubMap = cacheDatum.getPubMap();
        Map<String, Publisher> pubMap = datum.getPubMap();
        for (Entry<String, Publisher> pubEntry : pubMap.entrySet()) {
            String registerId = pubEntry.getKey();
            Publisher pub = pubEntry.getValue();
            Publisher cachePub = cachePubMap.get(registerId);
            if (mergePublisher(pub, cachePubMap, cachePub)) {
                isChanged = true;
            }
        }
        Long lastVersion = cacheDatum.getVersion();
        if (isChanged) {
            cacheDatum.setVersion(datum.getVersion());
        }
        return new MergeResult(lastVersion, isChanged);
    }

    //    /**
    //     * cover datum by connectId
    //     */
    //    public void putSnapshot(Map<String, Publisher> toBeDeletedPubMap, Map<String, Publisher> snapshotPubMap) {
    //
    //        //remove all old pub
    //        for (Entry<String, Publisher> toBeDeletedPubEntry : toBeDeletedPubMap.entrySet()) {
    //            String registerId = toBeDeletedPubEntry.getKey();
    //            Publisher toBeDeletedPub = toBeDeletedPubEntry.getValue();
    //            Datum cacheDatum = DATUM_MAP.get(dataServerConfig.getLocalDataCenter()).get(toBeDeletedPub.getDataInfoId());
    //            if (cacheDatum != null) {
    //                Map<String, Publisher> cachePubMap = cacheDatum.getPubMap();
    //                cachePubMap.remove(registerId);
    //                removeFromIndex(toBeDeletedPub);
    //            }
    //        }
    //        // add all snapshot pub
    //        for (Entry<String, Publisher> pubEntry : snapshotPubMap.entrySet()) {
    //            String registerId = pubEntry.getKey();
    //            Publisher snapshotPub = pubEntry.getValue();
    //            Map<String, Datum> datumMap = getDatumMapByDataCenter(dataServerConfig.getLocalDataCenter());
    //            String dataInfoId = snapshotPub.getDataInfoId();
    //            Datum cacheDatum = datumMap.get(dataInfoId);
    //            if (cacheDatum == null) {
    //                cacheDatum = new Datum(dataInfoId, dataServerConfig.getLocalDataCenter());
    //                Datum datum = datumMap.putIfAbsent(dataInfoId, cacheDatum);
    //                if (datum != null) {
    //                    cacheDatum = datum;
    //                }
    //            }
    //            cacheDatum.getPubMap().put(registerId, snapshotPub);
    //        }
    //
    //    }

    //    /**
    //     * cover datum by connectId
    //     */
    //    public void putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
    //                               Map<String, Publisher> snapshotPubMap) {
    //        // get cache datum
    //        Map<String, Datum> datumMap = getDatumMapByDataCenter(dataServerConfig.getLocalDataCenter());
    //        Datum cacheDatum = datumMap.get(dataInfoId);
    //        if (cacheDatum == null) {
    //            cacheDatum = new Datum(dataInfoId, dataServerConfig.getLocalDataCenter());
    //            Datum datum = datumMap.putIfAbsent(dataInfoId, cacheDatum);
    //            if (datum != null) {
    //                cacheDatum = datum;
    //            }
    //        }
    //        //remove toBeDeletedPubMap from cacheDatum
    //        for (Entry<String, Publisher> toBeDeletedPubEntry : toBeDeletedPubMap.entrySet()) {
    //            String registerId = toBeDeletedPubEntry.getKey();
    //            Publisher toBeDeletedPub = toBeDeletedPubEntry.getValue();
    //            if (cacheDatum != null) {
    //                Map<String, Publisher> cachePubMap = cacheDatum.getPubMap();
    //                cachePubMap.remove(registerId);
    //                removeFromIndex(toBeDeletedPub);
    //            }
    //        }
    //        // add snapshotPubMap to cacheDatum
    //        for (Entry<String, Publisher> pubEntry : snapshotPubMap.entrySet()) {
    //            String registerId = pubEntry.getKey();
    //            Publisher snapshotPub = pubEntry.getValue();
    //            cacheDatum.getPubMap().put(registerId, snapshotPub);
    //            addToIndex(snapshotPub);
    //        }
    //
    //    }

    /**
     * cover datum by snapshot
     */
    public Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                             Map<String, Publisher> snapshotPubMap) {
        // get cache datum
        Map<String, Datum> datumMap = getDatumMapByDataCenter(dataServerConfig.getLocalDataCenter());
        Datum cacheDatum = datumMap.get(dataInfoId);
        if (cacheDatum == null) {
            cacheDatum = new Datum(dataInfoId, dataServerConfig.getLocalDataCenter());
            Publisher publisher = snapshotPubMap.values().iterator().next();
            cacheDatum.setInstanceId(publisher.getInstanceId());
            cacheDatum.setDataId(publisher.getDataId());
            cacheDatum.setGroup(publisher.getGroup());
            Datum datum = datumMap.putIfAbsent(dataInfoId, cacheDatum);
            if (datum != null) {
                cacheDatum = datum;
            }
        }
        //remove toBeDeletedPubMap from cacheDatum
        for (Entry<String, Publisher> toBeDeletedPubEntry : toBeDeletedPubMap.entrySet()) {
            String registerId = toBeDeletedPubEntry.getKey();
            Publisher toBeDeletedPub = toBeDeletedPubEntry.getValue();
            if (cacheDatum != null) {
                cacheDatum.getPubMap().remove(registerId);
                removeFromIndex(toBeDeletedPub);
            }
        }
        // add snapshotPubMap to cacheDatum
        for (Entry<String, Publisher> pubEntry : snapshotPubMap.entrySet()) {
            String registerId = pubEntry.getKey();
            Publisher snapshotPub = pubEntry.getValue();
            cacheDatum.getPubMap().put(registerId, snapshotPub);
            addToIndex(snapshotPub);
        }

        cacheDatum.updateVersion();

        return cacheDatum;
    }

    private boolean mergePublisher(Publisher pub, Map<String, Publisher> cachePubMap,
                                   Publisher cachePub) {
        boolean isChanged = false;
        String registerId = pub.getRegisterId();
        if (pub instanceof UnPublisher) {
            //remove from cache
            if (cachePub != null && pub.getRegisterTimestamp() > cachePub.getRegisterTimestamp()) {
                cachePubMap.remove(registerId);
                removeFromIndex(cachePub);
                isChanged = true;
            }
        } else {
            String connectId = getConnectId(pub);
            long version = pub.getVersion();
            long cacheVersion = cachePub == null ? 0L : cachePub.getVersion();
            String cacheConnectId = cachePub == null ? "" : getConnectId(cachePub);
            if (cacheVersion <= version) {
                cachePubMap.put(registerId, pub);
                // if version of both pub and cachePub are not equal, or sourceAddress of both are not equal, update
                // eg: sessionserver crash, client(RegistryClient but not ConfregClient) reconnect to other sessionserver, sourceAddress changed, version not changed
                if (!connectId.equals(cacheConnectId) || cacheVersion < version) {
                    removeFromIndex(cachePub);
                    addToIndex(pub);
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    /**
     *
     * @param datum
     * @return
     */
    private Long coverDatum(Datum datum) {
        String dataCenter = datum.getDataCenter();
        String dataInfoId = datum.getDataInfoId();
        Datum cacheDatum = DATUM_MAP.get(dataCenter).get(dataInfoId);
        if (datum.getVersion() != cacheDatum.getVersion()) {
            DATUM_MAP.get(dataCenter).put(dataInfoId, datum);
            Map<String, Publisher> pubMap = datum.getPubMap();
            Map<String, Publisher> cachePubMap = new HashMap<>(cacheDatum.getPubMap());
            for (Entry<String, Publisher> pubEntry : pubMap.entrySet()) {
                String registerId = pubEntry.getKey();
                Publisher pub = pubEntry.getValue();
                addToIndex(pub);
                Publisher cachePub = cachePubMap.get(registerId);
                if (cachePub != null && getConnectId(pub).equals(getConnectId(cachePub))) {
                    cachePubMap.remove(registerId);
                }
            }
            if (!cachePubMap.isEmpty()) {
                for (Publisher cachePub : cachePubMap.values()) {
                    removeFromIndex(cachePub);
                }
            }
        }
        return cacheDatum.getVersion();
    }

    private void removeFromIndex(Publisher publisher) {
        if (publisher == null) {
            return;
        }
        String connectId = getConnectId(publisher);

        // remove from ALL_CONNECT_ID_INDEX
        Map<String, Publisher> publisherMap = ALL_CONNECT_ID_INDEX.get(connectId);
        if (publisherMap != null) {
            publisherMap.remove(publisher.getRegisterId());
        }
    }

    private void addToIndex(Publisher publisher) {
        if (publisher == null) {
            return;
        }
        String connectId = getConnectId(publisher);

        // add to ALL_CONNECT_ID_INDEX
        Map<String, Publisher> publisherMap = ALL_CONNECT_ID_INDEX
                .computeIfAbsent(connectId, s -> new ConcurrentHashMap<>());
        publisherMap.put(publisher.getRegisterId(), publisher);

    }

    private String getConnectId(Publisher cachePub) {
        return cachePub.getSourceAddress().getAddressString();
    }

    /**
     * Getter method for property <tt>OWN_CONNECT_ID_INDEX</tt>.
     *
     * @return property value of OWN_CONNECT_ID_INDEX
     */
    public Set<String> getAllConnectIds() {
        return ALL_CONNECT_ID_INDEX.keySet();
    }
}