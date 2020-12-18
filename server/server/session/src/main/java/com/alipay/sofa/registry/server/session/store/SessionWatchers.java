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

import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.VersionsMapUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shangyu.wh
 * @version $Id: SessionWatchers.java, v 0.1 2018-04-17 19:00 shangyu.wh Exp $
 */
public class SessionWatchers extends AbstractDataManager<Watcher> implements Watchers {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionWatchers.class);

    public SessionWatchers() {
        super(LOGGER);
    }

    /**
     * store watcher dataInfo version
     */
    private ConcurrentHashMap<String/*dataInfoId*/, Long /*dataInfoVersion*/> watcherVersions = new ConcurrentHashMap<>();

    @Override
    public boolean add(Watcher watcher) {
        Watcher.internWatcher(watcher);
        Map<String, Watcher> watcherMap = stores
                .computeIfAbsent(watcher.getDataInfoId(), k -> Maps.newConcurrentMap());
        Watcher existingWatcher = null;
        write.lock();
        try {
            existingWatcher = watcherMap.get(watcher.getRegisterId());
            watcherMap.put(watcher.getRegisterId(), watcher);
        } finally {
            write.unlock();
        }
        // log without lock
        if (existingWatcher != null) {
            LOGGER.warn("watcher already exists,it will be overwrite! {}", existingWatcher);
        }
        return true;
    }

    @Override
    public boolean checkWatcherVersions(String dataInfoId, Long version) {
        read.lock();
        try {
            Map<String, Watcher> watcherMap = stores.get(dataInfoId);
            if (MapUtils.isEmpty(watcherMap)) {
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
    protected void postDelete(Watcher data) {
    }
}