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

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
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

    private Map<String/*connectId*/, Map<String/*registerId*/, Publisher>>  connectIndex  = new ConcurrentHashMap<>();

    @Override
    public void add(Publisher publisher) {

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
            }
            publishers.put(publisher.getRegisterId(), publisher);

            addIndex(publisher);

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
                LOGGER.error(
                    "Delete failed because publisher is not registered for dataInfoId: {}",
                    dataInfoId);
                return false;
            } else {
                Publisher publisherTodelete = publishers.remove(registerId);

                if (publisherTodelete == null) {
                    LOGGER.error(
                        "Delete failed because publisher is not registered for registerId: {}",
                        registerId);
                    return false;

                } else {
                    removeIndex(publisherTodelete);
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
                        && connectId.equals(publisher.getSourceAddress().getAddressString())) {
                        it.remove();
                        invalidateIndex(publisher);
                    }
                }
            }
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
            LOGGER.error("Publisher is not registered for dataInfoId: {}", dataInfoId);
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
        AtomicLong count = new AtomicLong(0);
        for (Map<String, Publisher> map : registry.values()) {
            count.addAndGet(map.size());
        }
        return count.get();
    }

    private void addIndex(Publisher publisher) {

        addConnectIndex(publisher);
    }

    private void addConnectIndex(Publisher publisher) {

        String connectId = publisher.getSourceAddress().getAddressString();
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

    private void removeIndex(Publisher publisher) {
        removeConnectIndex(publisher);
    }

    private void removeConnectIndex(Publisher publisher) {
        String connectId = publisher.getSourceAddress().getAddressString();
        Map<String/*registerId*/, Publisher> publisherMap = connectIndex.get(connectId);
        if (publisherMap != null) {
            publisherMap.remove(publisher.getRegisterId());
        } else {
            LOGGER.warn("ConnectId {} not existed in Index to remove!", connectId);
        }
    }

    private void invalidateIndex(Publisher publisher) {
        String connectId = publisher.getSourceAddress().getAddressString();
        invalidateConnectIndex(connectId);
    }

    private void invalidateConnectIndex(String connectId) {
        connectIndex.remove(connectId);
    }

}