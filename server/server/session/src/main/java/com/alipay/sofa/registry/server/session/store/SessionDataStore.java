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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author shangyu.wh
 * @version $Id: SessionDataStore.java, v 0.1 2017-12-01 18:14 shangyu.wh Exp $
 */
public class SessionDataStore implements DataStore {

    private static final Logger                                               LOGGER        = LoggerFactory
                                                                                                .getLogger(SessionDataStore.class);
    private final ReentrantReadWriteLock                                      readWriteLock = new ReentrantReadWriteLock();
    private final Lock                                                        write         = readWriteLock
                                                                                                .writeLock();

    /**
     * publisher store
     */
    private Map<String/*dataInfoId*/, Map<String/*registerId*/, Publisher>> registry      = new ConcurrentHashMap<>();

    /*** index */
    private Map<String/*connectId*/, Map<String/*registerId*/, Publisher>>  connectIndex  = new ConcurrentHashMap<>();

    @Override
    public void add(Publisher publisher) {
        Publisher.internPublisher(publisher);

        write.lock();
        try {
            Map<String, Publisher> publishers = registry.get(publisher.getDataInfoId());

            if (publishers == null) {
                ConcurrentHashMap<String, Publisher> newmap = new ConcurrentHashMap<>();
                publishers = registry.putIfAbsent(publisher.getDataInfoId(), newmap);
                if (publishers == null) {
                    publishers = newmap;
                }
            }

            Publisher existingPublisher = publishers.get(publisher.getRegisterId());

            if (existingPublisher != null) {
                if (existingPublisher.getVersion() != null) {
                    long oldVersion = existingPublisher.getVersion();
                    Long newVersion = publisher.getVersion();
                    if (newVersion == null) {
                        LOGGER.error("There is publisher input version can't be null!");
                        return;
                    } else if (oldVersion > newVersion) {
                        LOGGER
                            .warn(
                                "There is publisher already exists,but old version {} higher than input {},it will not be overwrite! {}",
                                oldVersion, newVersion, existingPublisher);
                        return;
                    } else if (oldVersion == newVersion) {
                        Long newTime = publisher.getRegisterTimestamp();
                        long oldTime = existingPublisher.getRegisterTimestamp();
                        if (newTime == null) {
                            LOGGER
                                .error("There is publisher input Register Timestamp can not be null!");
                            return;
                        }
                        if (oldTime > newTime) {
                            LOGGER
                                .warn(
                                    "There is publisher already exists,but old timestamp {} higher than input {},it will not be overwrite! {}",
                                    oldTime, newTime, existingPublisher);
                            return;
                        }
                    }
                }
                LOGGER
                    .warn(
                        "There is publisher already exists,version:{},it will be overwrite!Input version:{},info:{}",
                        existingPublisher.getVersion(), publisher.getVersion(), existingPublisher);
                removeFromConnectIndex(existingPublisher);
            }
            publishers.put(publisher.getRegisterId(), publisher);
            addToConnectIndex(publisher);

        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean deleteById(String registerId, String dataInfoId) {

        write.lock();
        try {
            Map<String, Publisher> publishers = registry.get(dataInfoId);

            if (publishers == null) {
                LOGGER.warn("Delete failed because publisher is not registered for dataInfoId: {}",
                    dataInfoId);
                return false;
            } else {
                Publisher publisherTodelete = publishers.remove(registerId);

                if (publisherTodelete == null) {
                    LOGGER.warn(
                        "Delete failed because publisher is not registered for registerId: {}",
                        registerId);
                    return false;

                } else {
                    removeFromConnectIndex(publisherTodelete);
                    return true;
                }
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public Map<String, Publisher> queryByConnectId(String connectId) {
        return connectIndex.get(connectId);
    }

    @Override
    public boolean deleteByConnectId(String connectId) {
        write.lock();
        try {
            for (Map<String, Publisher> map : registry.values()) {
                for (Iterator it = map.values().iterator(); it.hasNext();) {
                    Publisher publisher = (Publisher) it.next();
                    if (publisher != null
                        && connectId.equals(WordCache.getInstance().getWordCache(
                            publisher.getSourceAddress().getAddressString()
                                    + ValueConstants.CONNECT_ID_SPLIT
                                    + publisher.getTargetAddress().getAddressString()))) {
                        it.remove();
                    }
                }
            }
            connectIndex.remove(connectId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Delete publisher by connectId {} error!", connectId, e);
            return false;
        } finally {
            write.unlock();
        }
    }

    @Override
    public Collection<Publisher> getStoreDataByDataInfoId(String dataInfoId) {

        Map<String, Publisher> publishers = registry.get(dataInfoId);

        if (publishers == null) {
            LOGGER.info("There is not registered publisher for dataInfoId: {}", dataInfoId);
            return null;
        } else {
            return publishers.values();
        }
    }

    @Override
    public Publisher queryById(String registerId, String dataInfoId) {

        Map<String, Publisher> publishers = registry.get(dataInfoId);

        if (publishers == null) {
            LOGGER.warn("Publisher is not registered for dataInfoId: {}", dataInfoId);
            return null;
        }
        return publishers.get(registerId);
    }

    @Override
    public Collection<String> getStoreDataInfoIds() {
        return registry.keySet();
    }

    @Override
    public long count() {
        long count = 0;
        for (Map<String, Publisher> map : registry.values()) {
            count += map.size();
        }
        return count;
    }

    private void addToConnectIndex(Publisher publisher) {
        String connectId = WordCache.getInstance().getWordCache(
            publisher.getSourceAddress().getAddressString() + ValueConstants.CONNECT_ID_SPLIT
                    + publisher.getTargetAddress().getAddressString());

        Map<String/*registerId*/, Publisher> publisherMap = connectIndex.get(connectId);
        if (publisherMap == null) {
            Map<String/*registerId*/, Publisher> newPublisherMap = new ConcurrentHashMap<>();
            publisherMap = connectIndex.putIfAbsent(connectId, newPublisherMap);
            if (publisherMap == null) {
                publisherMap = newPublisherMap;
            }
        }

        publisherMap.put(publisher.getRegisterId(), publisher);
    }

    private void removeFromConnectIndex(Publisher publisher) {
        String connectId = WordCache.getInstance().getWordCache(
            publisher.getSourceAddress().getAddressString() + ValueConstants.CONNECT_ID_SPLIT
                    + publisher.getTargetAddress().getAddressString());
        Map<String/*registerId*/, Publisher> publisherMap = connectIndex.get(connectId);
        if (publisherMap != null) {
            publisherMap.remove(publisher.getRegisterId());
        } else {
            LOGGER.warn("ConnectId {} not existed in Index to remove!", connectId);
        }
    }

    @Override
    public Map<String, Map<String, Publisher>> getConnectPublishers() {
        return connectIndex;
    }

    @Override
    public Map<String, Map<String, Publisher>> getDataInfoIdPublishers() {
        Map<String, Map<String, Publisher>> ret = new HashMap<>(registry.size());
        for (Map.Entry<String, Map<String, Publisher>> e : registry.entrySet()) {
            ret.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return ret;
    }
}