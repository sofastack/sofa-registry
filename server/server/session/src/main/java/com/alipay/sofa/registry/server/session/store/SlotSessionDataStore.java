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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 16:24 yuzhi.lyz Exp $
 */
public class SlotSessionDataStore implements DataStore {
    private static final Logger           LOGGER          = LoggerFactory
                                                              .getLogger(SlotSessionDataStore.class);

    @Autowired
    private SessionServerConfig           sessionServerConfig;

    @Autowired
    private SlotTableCache                slotTableCache;

    private final Map<Integer, DataStore> slot2DataStores = new ConcurrentHashMap<>();

    private DataStore getOrCreateDataStore(String dataInfoId) {
        int slotId = slotTableCache.slotOf(dataInfoId);
        return slot2DataStores.computeIfAbsent(slotId, k -> new SessionDataStore());
    }

    @Override
    public Collection<Publisher> getStoreDataByDataInfoId(String dataInfoId) {
        DataStore ds = getOrCreateDataStore(dataInfoId);
        return ds.getStoreDataByDataInfoId(dataInfoId);
    }

    @Override
    public Publisher queryById(String registerId, String dataInfoId) {
        DataStore ds = getOrCreateDataStore(dataInfoId);
        return ds.queryById(registerId, dataInfoId);
    }

    @Override
    public Collection<String> getStoreDataInfoIds() {
        Set<String> set = new HashSet<>(128);
        slot2DataStores.values().forEach(ds -> {
            set.addAll(ds.getStoreDataInfoIds());
        });
        return set;
    }

    @Override
    public Set<ConnectId> getConnectIds() {
        Set<ConnectId> ret = Sets.newHashSet();
        slot2DataStores.values().forEach(d -> ret.addAll(d.getConnectIds()));
        return ret;
    }

    @Override
    public Map<String, Map<String, Publisher>> getDataInfoIdPublishers() {
        Map<String, Map<String, Publisher>> ret = new HashMap<>(512);
        for (DataStore ds : slot2DataStores.values()) {
            Map<String, Map<String, Publisher>> m = ds.getDataInfoIdPublishers();
            for (Map.Entry<String, Map<String, Publisher>> e : m.entrySet()) {
                Map<String, Publisher> publisherMap = ret.computeIfAbsent(e.getKey(), k -> new HashMap<>(128));
                publisherMap.putAll(e.getValue());
            }
        }
        return ret;
    }

    @Override
    public Map<String, Map<String, Publisher>> getDataInfoIdPublishers(int slotId) {
        DataStore ds = slot2DataStores.computeIfAbsent(slotId, k -> new SessionDataStore());
        return ds.getDataInfoIdPublishers();
    }

    @Override
    public void add(Publisher publisher) {
        DataStore ds = getOrCreateDataStore(publisher.getDataInfoId());
        ds.add(publisher);
    }

    @Override
    public boolean deleteById(String registerId, String dataInfoId) {
        DataStore ds = getOrCreateDataStore(dataInfoId);
        return ds.deleteById(registerId, dataInfoId);
    }

    @Override
    public Map<String, Publisher> queryByConnectId(ConnectId connectId) {
        Map<String, Publisher> ret = new HashMap<>(128);
        slot2DataStores.values().forEach(ds -> {
            Map<String, Publisher> m = ds.queryByConnectId(connectId);
            if (!CollectionUtils.isEmpty(m)) {
                ret.putAll(m);
            }
        });
        return ret;
    }

    @Override
    public boolean deleteByConnectId(ConnectId connectId) {
        boolean deleted = false;
        for (DataStore ds : slot2DataStores.values()) {
            if (ds.deleteByConnectId(connectId)) {
                deleted = true;
            }
        }
        return deleted;
    }

    @Override
    public long count() {
        long count = 0;
        for (DataStore ds : slot2DataStores.values()) {
            count += ds.count();
        }
        return count;
    }
}
