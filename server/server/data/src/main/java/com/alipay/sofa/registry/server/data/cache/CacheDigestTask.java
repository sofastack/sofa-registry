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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.server.data.constants.Constant;
import com.alipay.sofa.registry.util.SchedulerCornUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;

/**
 *
 * @author qian.lqlq
 * @version $Id: CacheDigestTask.java, v 0.1 2018－04－27 17:40 qian.lqlq Exp $
 */
public class CacheDigestTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheDigestTask.class);

    @Autowired
    private DatumCache          datumCache;

    /**
     *
     */
    public void start() {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("CacheDigestTask"));
        executor.scheduleAtFixedRate(() -> {
            try {
                Map<String, Map<String, Datum>> allMap = datumCache.getAll();
                if (!allMap.isEmpty()) {
                    for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
                        String dataCenter = dataCenterEntry.getKey();
                        Map<String, Datum> datumMap = dataCenterEntry.getValue();
                        LOGGER.info("[CacheDigestTask] size of datum in {} is {}", dataCenter, datumMap.size());
                        for (Entry<String, Datum> dataInfoEntry : datumMap.entrySet()) {
                            String dataInfoId = dataInfoEntry.getKey();
                            Datum data = dataInfoEntry.getValue();
                            Map<String, Publisher> pubMap = data.getPubMap();
                            StringBuilder pubStr = new StringBuilder();
                            if (!CollectionUtils.isEmpty(pubMap)) {
                                for (Publisher publisher : pubMap.values()) {
                                    pubStr.append(logPublisher(publisher)).append(";");
                                }
                            }
                            LOGGER.info("[Datum]{},{},{},[{}]", dataInfoId,
                                    data.getVersion(), dataCenter, pubStr.toString());
                        }
                        int pubCount = datumMap.values().stream().map(Datum::getPubMap)
                                .filter(map -> map != null && !map.isEmpty()).mapToInt(Map::size).sum();
                        LOGGER.info("[CacheDigestTask] size of publisher in {} is {}", dataCenter, pubCount);
                    }
                } else {
                    LOGGER.info("[CacheDigestTask] datum cache is empty");
                }

            } catch (Throwable t) {
                LOGGER.error("[CacheDigestTask] cache digest error", t);
            }
        }, SchedulerCornUtil.calculateInitialDelay(Constant.CACHE_PRINTER_CRON)/1000, 600, TimeUnit.SECONDS);
    }

    private String logPublisher(Publisher publisher) {
        if (publisher != null) {
            URL url = publisher.getSourceAddress();
            String urlStr = url != null ? url.getAddressString() : "null";
            return String.format("%s,%s,%s,%s", publisher.getRegisterId(),
                publisher.getRegisterTimestamp(), urlStr, publisher.getVersion());
        }
        return "";
    }
}