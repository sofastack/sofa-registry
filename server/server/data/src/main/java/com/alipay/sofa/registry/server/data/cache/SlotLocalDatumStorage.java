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
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-03 16:55 yuzhi.lyz Exp $
 */
public final class SlotLocalDatumStorage implements DatumStorage, SlotManager.SlotDatumStorageProvider {
    private static final Logger           LOGGER = LoggerFactory.getLogger(SlotLocalDatumStorage.class);
    @Autowired
    private              SlotManager      slotManager;
    @Autowired
    private              DataServerConfig dataServerConfig;

    private final Map<Integer, DatumStorage> localDatumStorages = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        slotManager.addSlotChangeListener(new SlotListener());
        slotManager.setSlotDatumStorageProvider(this);
    }

    @Override
    public Datum get(String dataCenter, String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.get(dataCenter, dataInfoId);
    }

    @Override
    public Map<String, Datum> get(String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.get(dataInfoId);
    }

    @Override
    public Map<String, Map<String, Datum>> getAll() {
        Map<String, Map<String, Datum>> m = new HashMap<>();
        localDatumStorages.values().forEach(ds -> {
                    Map<String, Map<String, Datum>> dsm = ds.getAll();
                    m.forEach((dataCenter, datumMap) -> {
                        Map<String, Datum> existing = m.computeIfAbsent(dataCenter, k -> {
                            return new HashMap<>();
                        });
                        existing.putAll(datumMap);
                    });
                }
        );
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
    public MergeResult putDatum(DataChangeTypeEnum changeType, Datum datum) {
        final DatumStorage ds = getDatumStorage(datum.getDataInfoId());
        if (ds == null) {
            return new MergeResult(LocalDatumStorage.ERROR_DATUM_SLOT, false);
        }
        return ds.putDatum(changeType, datum);
    }

    @Override
    public boolean cleanDatum(String dataCenter, String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return false;
        }
        return ds.cleanDatum(dataCenter, dataInfoId);
    }

    @Override
    public boolean removePublisher(String dataCenter, String dataInfoId, String registerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                             Map<String, Publisher> snapshotPubMap) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.putSnapshot(dataInfoId, toBeDeletedPubMap, snapshotPubMap);
    }

    @Override
    public Map<String, Long> getVersions(String dataInfoId) {
        final DatumStorage ds = getDatumStorage(dataInfoId);
        if (ds == null) {
            return null;
        }
        return ds.getVersions(dataInfoId);
    }

    @Override
    public Map<String, DatumSummary> getDatumSummary(String dataCenter, String targetIpAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, DatumSummary> getDatumSummary(int slotId, String dataCenter, String targetIpAddress) {
        final DatumStorage ds = localDatumStorages.get(slotId);
        if (ds == null) {
            return Collections.emptyMap();
        }
        return ds.getDatumSummary(dataCenter, targetIpAddress);
    }

    @Override
    public void merge(int slotId, String dataCenter, Map<String, Datum> puts, Map<String, List<String>> remove) {
        final DatumStorage ds = localDatumStorages.get(slotId);
        if (ds == null) {
            return;
        }
        for (Map.Entry<String, List<String>> e : remove.entrySet()) {
            for (String registerId : e.getValue()) {
                ds.removePublisher(dataCenter, e.getKey(), registerId);
            }
        }
        for (Datum datum : puts.values()) {
            ds.putDatum(DataChangeTypeEnum.MERGE, datum);
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
