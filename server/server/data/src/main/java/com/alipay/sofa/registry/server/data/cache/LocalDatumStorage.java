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

import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
     * column:  dataInfoId
     * value:   datum
     */
    protected final Map<String, Datum>                  DATUM_MAP            = new ConcurrentHashMap<>();

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
     * get datum by specific dataInfoId
     *
     * @param dataInfoId
     * @return
     */
    public Datum get(String dataInfoId) {
        return DATUM_MAP.get(dataInfoId);
    }

    /**
     * get all datum
     *
     * @return
     */
    public Map<String, Datum> getAll() {
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
     * @param datum
     * @return the last version before datum changed, if datum is not exist, return null
     */
    public MergeResult putDatum(Datum datum) {
        MergeResult mergeResult;
        String dataInfoId = datum.getDataInfoId();

        //first put UnPublisher datum(dataId group instanceId is null),can not add to cache
        if (datum.getDataId() == null && DATUM_MAP.get(dataInfoId) == null) {
            mergeResult = new MergeResult(ERROR_DATUM_VERSION, false);
            return mergeResult;
        }

        // filter out the unPubs of datum when first put.
        // Otherwise, "syncData" or "fetchData" when get Datum with unPubs, which will result something error
        boolean[] exists = { true };
        Datum cacheDatum = DATUM_MAP.computeIfAbsent(dataInfoId, k -> filterUnPubs(exists, datum));
        if (!exists[0]) {
            Iterator<Entry<String, Publisher>> iterator = datum.getPubMap().entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Publisher> entry = iterator.next();
                Publisher publisher = entry.getValue();
                addToIndex(publisher);
            }
            mergeResult = new MergeResult(null, true);
        } else {
            mergeResult = mergeDatum(cacheDatum, datum);
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

    /**
     * remove datum ant contains all pub data,and clean all the client map reference
     * @param dataInfoId
     * @return
     */
    @Override
    public boolean cleanDatum(String dataInfoId) {
        Datum cacheDatum = DATUM_MAP.remove(dataInfoId);
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
        return false;
    }

    @Override
    public boolean removePublisher(String dataInfoId, String registerId) {
        Datum datum = DATUM_MAP.get(dataInfoId);
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
        Datum cacheDatum = DATUM_MAP.get(dataInfoId);
        if (cacheDatum == null) {
            cacheDatum = new Datum(dataInfoId, dataServerConfig.getLocalDataCenter());
            Publisher publisher = snapshotPubMap.values().iterator().next();
            cacheDatum.setInstanceId(publisher.getInstanceId());
            cacheDatum.setDataId(publisher.getDataId());
            cacheDatum.setGroup(publisher.getGroup());
            Datum datum = DATUM_MAP.putIfAbsent(dataInfoId, cacheDatum);
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
    public Long getVersions(String dataInfoId) {
        Datum datum = this.get(dataInfoId);
        return datum != null ? datum.getVersion() : null;
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

    @Override
    public Map<String, DatumSummary> getDatumSummary(String targetIpAddress) {
        Map<String, DatumSummary> summarys = new HashMap<>();
        DATUM_MAP.forEach((k, datum) -> {
            summarys.put(k, PublisherDigestUtil.getDatumSummary(datum, targetIpAddress));
        });
        return summarys;
    }

    @Override
    public Map<String, Map<String, Publisher>> getPublishers(int slotId) {
        Map<String, Map<String, Publisher>> m = new HashMap<>(DATUM_MAP.size());
        DATUM_MAP.forEach((k, v) -> {
            m.put(k, new HashMap<>(v.getPubMap()));
        });
        return m;
    }
}