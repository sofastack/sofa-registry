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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-03 16:55 yuzhi.lyz Exp $
 */
public final class SlotLocalDatumStorage implements DatumStorage,
                                        SlotManager.SlotDatumStorageProvider {
    private static final Logger              LOGGER             = LoggerFactory
                                                                    .getLogger(SlotLocalDatumStorage.class);
    @Autowired
    private SlotManager                      slotManager;
    @Autowired
    private DataServerConfig                 dataServerConfig;

    private final Map<Integer, DatumStorage> localDatumStorages = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        slotManager.addSlotChangeListener(new SlotListener());
        slotManager.setSlotDatumStorageProvider(this);
    }

    @Override
    public Datum get(String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.get(dataInfoId);
    }

    @Override
    public Map<String, Datum> getAll() {
        Map<String, Datum> m = new HashMap<>();
        localDatumStorages.values().forEach(ds -> m.putAll(ds.getAll()));
        return m;
    }

    @Override
    public Map<String, Publisher> getByConnectId(String connectId) {
        Map<String, Publisher> m = new HashMap<>();
        localDatumStorages.values().forEach(ds -> {
                    Map<String, Publisher> publishers = ds.getByConnectId(connectId);
                    if (publishers != null) {
                        m.putAll(publishers);
                    }
                }
        );
        return m;
    }

    @Override
    public Set<String> getAllConnectIds() {
        Set<String> set = new HashSet<>();
        localDatumStorages.values().forEach(ds -> {
                    Set<String> ids = ds.getAllConnectIds();
                    if (ids != null) {
                        set.addAll(ids);
                    }
                }
        );
        return set;
    }

    @Override
    public MergeResult putDatum(Datum datum) {
        final DatumStorage ds = getDatumStorage(datum.getDataInfoId());
        if (ds == null) {
            return new MergeResult(LocalDatumStorage.ERROR_DATUM_SLOT, false);
        }
        synchronized (ds) {
            return ds.putDatum(datum);
        }
    }

    @Override
    public boolean cleanDatum(String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return false;
        }
        synchronized (ds) {
            return ds.cleanDatum(dataInfoId);
        }
    }

    @Override
    public boolean removePublisher(String dataInfoId, String registerId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return false;
        }
        synchronized (ds) {
            return ds.removePublisher(dataInfoId, registerId);
        }
    }

    @Override
    public Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                             Map<String, Publisher> snapshotPubMap) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        synchronized (ds) {
            return ds.putSnapshot(dataInfoId, toBeDeletedPubMap, snapshotPubMap);
        }
    }

    @Override
    public Long getVersions(String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.getVersions(dataInfoId);
    }

    @Override
    public Map<String, DatumSummary> getDatumSummary(String targetIpAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, Publisher>> getPublishers(int slotId) {
        final DatumStorage ds = localDatumStorages.get(slotId);
        if (ds == null) {
            return Collections.emptyMap();
        }
        return ds.getPublishers(slotId);
    }

    @Override
    public Map<String, DatumSummary> getDatumSummary(int slotId, String targetIpAddress) {
        final DatumStorage ds = localDatumStorages.get(slotId);
        if (ds == null) {
            return Collections.emptyMap();
        }
        return ds.getDatumSummary(targetIpAddress);
    }

    @Override
    public void merge(int slotId, Map<String, List<Publisher>> updatedPublishers, List<String> removedDataInfoIds,
                      Map<String, List<String>> removedPublishers) {
        final DatumStorage ds = localDatumStorages.get(slotId);
        if (ds == null) {
            return;
        }
        synchronized (ds) {
            for (String dataInfoId : removedDataInfoIds) {
                ds.cleanDatum(dataInfoId);
            }

            for (Map.Entry<String, List<String>> e : removedPublishers.entrySet()) {
                final String dataInfoId = e.getKey();
                for (String registerId : e.getValue()) {
                    ds.removePublisher(dataInfoId, registerId);
                }
            }

            for (Map.Entry<String, List<Publisher>> updateds : updatedPublishers.entrySet()) {
                Datum datum = new Datum(updateds.getValue().get(0), dataServerConfig.getLocalDataCenter());
                updateds.getValue().forEach(p -> {
                    datum.getPubMap().put(p.getRegisterId(), p);
                });
                ds.putDatum(datum);
            }
        }
    }

    private final class SlotListener implements SlotManager.SlotChangeListener {

        @Override
        public void onSlotAdd(int slotId, Slot.Role role) {
            localDatumStorages.computeIfAbsent(slotId, k -> {
                LocalDatumStorage lds = new LocalDatumStorage();
                lds.dataServerConfig = dataServerConfig;
                LOGGER.info("add DatumStore {}", slotId);
                return lds;
            });
        }

        @Override
        public void onSlotRemove(int slotId, Slot.Role role) {
            boolean removed = localDatumStorages.remove(slotId) != null;
            LOGGER.info("remove DatumStore {}, removed={}", slotId, removed);
        }
    }

    private DatumStorage getDatumStorage(String dataInfoId) {
        final Integer slotId = slotManager.slotOf(dataInfoId);
        return localDatumStorages.get(slotId);
    }

}