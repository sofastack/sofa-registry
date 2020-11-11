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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.sun.org.apache.bcel.internal.generic.RET;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;

/**
 * datum storage of local dataCenter
 *
 * @author kezhu.wukz
 * @version $Id: LocalDatumAccessService.java, v 0.1 2019-12-06 15:22 kezhu.wukz Exp $
 */
public class LocalDatumStorage implements DatumStorage {

    public static final long                            ERROR_DATUM_VERSION  = -2L;
    public static final long                            ERROR_DATUM_SLOT     = -3L;

    /**
     * row:     dataCenter
     * column:  dataInfoId
     * value:   datum
     */
    protected final Map<String, Map<String, Datum>>     DATUM_MAP            = new ConcurrentHashMap<>();

    /**
     * all datum index
     *
     * row:     ip:port
     * column:  registerId
     * value:   publisher
     */
    protected final Map<String, Map<String, Publisher>> ALL_CONNECT_ID_INDEX = new ConcurrentHashMap<>();

    @Autowired
    protected DataServerConfig                          dataServerConfig;

    /**
     * get datum by specific dataCenter and dataInfoId
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public Datum get(String dataCenter, String dataInfoId) {
        Map<String, Datum> map = DATUM_MAP.get(dataCenter);
        if (map != null) {
            return map.get(dataInfoId);
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
            Datum datum = datums.get(dataInfoId);
            if (datum != null) {
                datumMap.put(dataCenter, datum);
            }
        });

        return datumMap;
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

        // filter out the unPubs of datum when first put.
        // Otherwise, "syncData" or "fetchData" when get Datum with unPubs, which will result something error
        boolean[] exists = { true };
        Datum cacheDatum = map.computeIfAbsent(dataInfoId, k -> filterUnPubs(exists, datum));
        if (!exists[0]) {
            Iterator<Entry<String, Publisher>> iterator = datum.getPubMap().entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Publisher> entry = iterator.next();
                Publisher publisher = entry.getValue();
                addToIndex(publisher);
            }
            mergeResult = new MergeResult(null, true);
        } else {
            if (changeType == DataChangeTypeEnum.MERGE) {
                mergeResult = mergeDatum(cacheDatum, datum);
            } else {
                Long lastVersion = coverDatum(datum);
                mergeResult = new MergeResult(lastVersion, true);
            }
        }
        return mergeResult;
    }

    /**
     * remove unPubs from datum
     */
    private Datum filterUnPubs(boolean[] exists, Datum datum) {
        Iterator<Entry<String, Publisher>> iterator = datum.getPubMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Publisher> entry = iterator.next();
            Publisher publisher = entry.getValue();
            if (publisher instanceof UnPublisher) {
                //first put to cache,UnPublisher data must remove,not so got error pub data exist
                iterator.remove();
            }
        }
        exists[0] = false;
        return datum;
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

    @Override
    public boolean removePublisher(String dataCenter, String dataInfoId, String registerId) {
        Map<String, Datum> datumMap = DATUM_MAP.get(dataCenter);
        if (datumMap != null) {
            return false;
        }
        Datum datum = datumMap.get(dataInfoId);
        if (datum == null) {
            return false;
        }
        Publisher publisher = datum.getPubMap().remove(registerId);
        if (publisher == null) {
            return false;
        }
        removeFromIndex(publisher);
        return true;
    }

    /**
     * merge datum in cache
     *
     * @param datum
     * @return
     */
    private MergeResult mergeDatum(Datum cacheDatum, Datum datum) {
        boolean isChanged = false;
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
            Publisher cachePub = cacheDatum.getPubMap().put(registerId, snapshotPub);
            if (cachePub != null) {
                removeFromIndex(cachePub);
            }
            addToIndex(snapshotPub);
        }

        cacheDatum.updateVersion();

        return cacheDatum;
    }

    @Override
    public Map<String, Long> getVersions(String dataInfoId) {
        Map<String, Long> versions = new HashMap<>(1);
        Map<String, Datum> datumMap = this.get(dataInfoId);
        if (datumMap != null) {
            for (Map.Entry<String, Datum> entry : datumMap.entrySet()) {
                String dataCenter = entry.getKey();
                Datum datum = entry.getValue();
                versions.put(dataCenter, datum.getVersion());
            }
        }
        return versions;
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
            long version = pub.getVersion();
            long cacheVersion = cachePub == null ? 0L : cachePub.getVersion();
            if (cacheVersion <= version) {
                cachePubMap.put(registerId, pub);
                // connectId and cacheConnectId may not be equal, so indexes need to be deleted and added, rather than overwritten directly.
                // why connectId and cacheConnectId may not be equal?
                // eg: sessionserver crash, client(RegistryClient but not ConfregClient) reconnect to other sessionserver, sourceAddress changed, version not changed
                removeFromIndex(cachePub);
                addToIndex(pub);
                isChanged = true;
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
        return WordCache.getInstance().getWordCache(
            cachePub.getSourceAddress().getAddressString() + ValueConstants.CONNECT_ID_SPLIT
                    + cachePub.getTargetAddress().getAddressString());
    }

    /**
     * Getter method for property <tt>OWN_CONNECT_ID_INDEX</tt>.
     *
     * @return property value of OWN_CONNECT_ID_INDEX
     */
    public Set<String> getAllConnectIds() {
        return ALL_CONNECT_ID_INDEX.keySet();
    }

    public Map<String, DatumSummary> getDatumSummary(String dataCenter, String targetIpAddress) {
        Map<String, Datum> datums = DATUM_MAP.get(dataCenter);
        if (datums == null) {
            return Collections.emptyMap();
        }
        Map<String, DatumSummary> summarys = new HashMap<>(datums.size());
        datums.forEach((k, datum) -> {
            summarys.put(k, PublisherDigestUtil.getDatumSummary(datum, targetIpAddress));
        });
        return summarys;
    }
}