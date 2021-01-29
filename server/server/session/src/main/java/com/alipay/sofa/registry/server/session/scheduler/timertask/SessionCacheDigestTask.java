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

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SessionCacheDigestTask.java, v 0.1 2020年08月03日 14:37 xiaojian.xj Exp $
 */
public class SessionCacheDigestTask {

    private static final Logger LOGGER = LoggerFactory.getLogger("CACHE-DIGEST");

    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @PostConstruct
    public void init() {
        final int intervalSec = sessionServerConfig.getCacheDigestIntervalSecs();
        if (intervalSec <= 0) {
            LOGGER.info("cache digest off with intervalSec={}", intervalSec);
            return;
        }
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("CacheDigestTask"));
        executor.scheduleWithFixedDelay(() -> {

                    try {
                        Collection<String> storeDataInfoIds = sessionDataStore.getDataInfoIds();
                        Collection<String> interestDataInfoIds = sessionInterests.getDataInfoIds();
                        Set<String> dataInfoIds = new HashSet<>(storeDataInfoIds.size()+interestDataInfoIds.size());

                        dataInfoIds.addAll(storeDataInfoIds);
                        dataInfoIds.addAll(interestDataInfoIds);

                        dataInfoIds.stream().forEach(dataInfoId -> {
                            Collection<Publisher> publishers = sessionDataStore.getDatas(dataInfoId);
                            Collection<Subscriber> subscribers = sessionInterests.getDatas(dataInfoId);

                            LOGGER.info("[dataInfo] {}; {}; {}; {}; [{}]; [{}]",
                                    sessionServerConfig.getSessionServerDataCenter(), dataInfoId,
                                    publishers.size(), subscribers.size(),
                                    logPubOrSub(publishers), logPubOrSub(subscribers));
                            // avoid io is too busy
                            ConcurrentUtils.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);
                        });

                    } catch (Throwable t) {
                        LOGGER.error("[CacheDigestTask] cache digest error", t);
                    }

                }, intervalSec, intervalSec, TimeUnit.SECONDS);
    }

    private String logPubOrSub(Collection<? extends BaseInfo> infos) {

        return Optional.ofNullable(infos).orElse(new ArrayList<>()).stream()
                .filter(info -> info != null).map(info -> logUrl(info.getSourceAddress()))
                .collect(Collectors.joining(","));
    }

    private String logUrl(URL url) {
        return url == null ? "null" : url.getAddressString();
    }

}