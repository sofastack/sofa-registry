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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.VersionsMapUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author shangyu.wh
 * @version $Id: SessionWatchers.java, v 0.1 2018-04-17 19:00 shangyu.wh Exp $
 */
public class SessionWatchers implements Watchers {

    private static final Logger                                                           LOGGER          = LoggerFactory
                                                                                                              .getLogger(SessionWatchers.class);

    private final ReentrantReadWriteLock                                                  readWriteLock   = new ReentrantReadWriteLock();
    private final Lock                                                                    read            = readWriteLock
                                                                                                              .readLock();
    private final Lock                                                                    write           = readWriteLock
                                                                                                              .writeLock();

    /**
     * store all register watchers
     */
    private ConcurrentHashMap<String/*dataInfoId*/, Map<String/*registerId*/, Watcher>> watchers        = new ConcurrentHashMap<>();

    private Map<ConnectId/*connectId*/, Map<String/*registerId*/, Watcher>>             connectIndex    = new ConcurrentHashMap<>();

    /**
     * store watcher dataInfo version
     */
    private ConcurrentHashMap<String/*dataInfoId*/, Long /*dataInfoVersion*/>           watcherVersions = new ConcurrentHashMap<>();

    @Override
    public void add(Watcher watcher) {
        Watcher.internWatcher(watcher);

        write.lock();
        try {
            Map<String, Watcher> watcherMap = watchers.computeIfAbsent(watcher.getDataInfoId(), k->Maps.newConcurrentMap());
            Watcher existingWatcher = watcherMap.get(watcher.getRegisterId());

            if (existingWatcher != null) {
                LOGGER.warn("There is watcher already exists,it will be overwrite! {}",
                    existingWatcher);
                removeConnectIndex(existingWatcher);
            }

            watcherMap.put(watcher.getRegisterId(), watcher);

            addConnectIndex(watcher);

        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean deleteByConnectId(ConnectId connectId) {
        write.lock();
        try {
            for (Map<String, Watcher> map : watchers.values()) {
                for (Iterator it = map.values().iterator(); it.hasNext();) {
                    Watcher watcher = (Watcher) it.next();
                    if (watcher != null && connectId.equals(watcher.connectId())) {
                        it.remove();
                        invalidateConnectIndex(connectId);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Delete watcher by connectId {} error!", connectId, e);
            return false;
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean checkWatcherVersions(String dataInfoId, Long version) {
        read.lock();
        try {

            Map<String, Watcher> watcherMap = watchers.get(dataInfoId);

            if (watcherMap == null || watcherMap.isEmpty()) {
                LOGGER.info("There are not Watcher Existed! Who are interest with dataInfoId {} !",
                    dataInfoId);
                return false;
            }

            return VersionsMapUtils.checkAndUpdateVersions(watcherVersions, dataInfoId, version);
        } finally {
            read.unlock();
        }
    }

    @Override
    public Collection<Watcher> getWatchers(String dataInfoId) {
        read.lock();
        try {

            if (dataInfoId == null) {
                throw new IllegalArgumentException("Input dataInfoId can not be null!");
            }
            Map<String, Watcher> watcherMap = watchers.get(dataInfoId);
            if (watcherMap == null) {
                LOGGER.info("There is not registered Watcher for : {}", dataInfoId);
                return null;
            }
            return watcherMap.values();
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean deleteById(String registerId, String dataInfoId) {
        write.lock();
        try {

            Map<String, Watcher> watcherMap = watchers.get(dataInfoId);

            if (watcherMap == null) {
                LOGGER.error("Delete failed because watcher is not registered for dataInfoId: {}",
                    dataInfoId);
                return false;
            } else {
                Watcher watcher = watcherMap.remove(registerId);

                if (watcher == null) {
                    LOGGER.error(
                        "Delete failed because watcher is not registered for registerId: {}",
                        registerId);
                    return false;
                } else {
                    removeConnectIndex(watcher);
                    return true;
                }
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public Map<String, Watcher> queryByConnectId(ConnectId connectId) {
        read.lock();
        try {
            return connectIndex.get(connectId);
        } finally {
            read.unlock();
        }
    }

    @Override
    public long count() {
        AtomicLong count = new AtomicLong(0);
        for (Map<String, Watcher> map : watchers.values()) {
            count.addAndGet(map.size());
        }
        return count.get();
    }

    private void addConnectIndex(Watcher watcher) {
        ConnectId connectId = watcher.connectId();
        Map<String/*registerId*/, Watcher> subscriberMap = connectIndex.get(connectId);
        if (subscriberMap == null) {
            Map<String/*registerId*/, Watcher> newSubscriberMap = new ConcurrentHashMap<>();
            subscriberMap = connectIndex.putIfAbsent(connectId, newSubscriberMap);
            if (subscriberMap == null) {
                subscriberMap = newSubscriberMap;
            }
        }

        subscriberMap.put(watcher.getRegisterId(), watcher);
    }

    private void removeConnectIndex(Watcher watcher) {
        ConnectId connectId = watcher.connectId();
        Map<String/*registerId*/, Watcher> subscriberMap = connectIndex.get(connectId);
        if (subscriberMap != null) {
            subscriberMap.remove(watcher.getRegisterId());
        } else {
            LOGGER.warn("ConnectId {} not existed in Index to invalidate!", connectId);
        }
    }

    private void invalidateConnectIndex(ConnectId connectId) {
        connectIndex.remove(connectId);
    }

    @Override
    public Set<ConnectId> getConnectIds() {
        return Sets.newHashSet(connectIndex.keySet());
    }

}