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
package com.alipay.sofa.registry.server.data.remoting.dataserver.task;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LogMetricsTask extends AbstractTask {
    private TaskMetrics         taskMetrics   = TaskMetrics.getInstance();

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Autowired
    private DatumCache          datumCache;

    private static final Logger EXE_LOGGER    = LoggerFactory.getLogger("DATA-PROFILE-DIGEST",
                                                  "[ExecutorMetrics]");
    private static final Logger DIGEST_LOGGER = LoggerFactory.getLogger("DATA-PROFILE-DIGEST",
                                                  "[DigestMetrics]");

    private void printExecutorMetrics() {
        taskMetrics.loggingMetrics(EXE_LOGGER);
    }

    private void printLocalDigest() {
        Map<String, Datum> datumMap = datumCache.getAll()
            .get(dataServerConfig.getLocalDataCenter());
        int datumCount = 0;
        int pubCount = 0;
        if (datumMap != null) {
            datumCount = datumMap.size();
            pubCount = datumMap.values().stream().map(Datum::getPubMap)
                .filter(map -> map != null && !map.isEmpty()).mapToInt(Map::size).sum();
        }
        DIGEST_LOGGER
            .info(String.format("[CountMonitor] datum: %d, pub: %d", datumCount, pubCount));
    }

    @Override
    public void handle() {
        printExecutorMetrics();
        printLocalDigest();
    }

    @Override
    public int getDelay() {
        return 60;
    }

    @Override
    public int getInitialDelay() {
        return 60;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public StartTaskTypeEnum getStartTaskTypeEnum() {
        return StartTaskTypeEnum.LOG_METRICS;
    }
}
