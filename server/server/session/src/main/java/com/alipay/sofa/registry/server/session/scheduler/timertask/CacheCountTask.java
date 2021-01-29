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
package com.alipay.sofa.registry.server.session.scheduler.timertask;

import com.alipay.sofa.registry.common.model.DataUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author qian.lqlq
 * @version $Id: CacheDigestTask.java, v 0.1 2018－04－27 17:40 qian.lqlq Exp $
 */
public class CacheCountTask {
    private static final Logger LOGGER       = LoggerFactory.getLogger(CacheCountTask.class,
                                                 "[CacheCountTask]");
    private static final Logger COUNT_LOGGER = LoggerFactory.getLogger("CACHE-COUNT");

    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private Watchers            sessionWatchers;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @PostConstruct
    public void init() {
        final int intervalSec = sessionServerConfig.getCacheCountIntervalSecs();
        if (intervalSec <= 0) {
            LOGGER.info("cache count off with intervalSecs={}", intervalSec);
            return;
        }
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("CacheCountTask"));
        executor.scheduleWithFixedDelay(() -> {
            try {
                syncCount();
            } catch (Throwable t) {
                LOGGER.error("cache count error", t);
            }
        }, intervalSec, intervalSec, TimeUnit.SECONDS);
    }

    public void syncCount() {
        List<Publisher> pubs = sessionDataStore.getDataList();
        Map<String, Map<String, Map<String, Integer>>> pubCounts = DataUtils.countGroupBy(pubs);
        printCount("[Pub]", pubCounts);

        List<Subscriber> subs = sessionInterests.getDataList();
        Map<String, Map<String, Map<String, Integer>>> subCounts = DataUtils.countGroupBy(subs);
        printCount("[Sub]", subCounts);

        List<Watcher> wats = sessionWatchers.getDataList();
        Map<String, Map<String, Map<String, Integer>>> watCounts = DataUtils.countGroupBy(wats);
        printCount("[Wat]", watCounts);
    }

    private static void printCount(String prefix,
                                   Map<String, Map<String, Map<String, Integer>>> counts) {
        for (Entry<String, Map<String, Map<String, Integer>>> count : counts.entrySet()) {
            final String instanceId = count.getKey();
            for (Entry<String, Map<String, Integer>> groupCounts : count.getValue().entrySet()) {
                final String group = groupCounts.getKey();
                for (Entry<String, Integer> apps : groupCounts.getValue().entrySet()) {
                    COUNT_LOGGER.info("{},{},{},{},{}", prefix, instanceId, group, apps.getKey(),
                        apps.getValue());
                }
            }
        }
    }
}