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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.store.*;
import com.alipay.sofa.registry.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

public class LogMetricsTask {
    private static final Logger EXE_LOGGER    = LoggerFactory.getLogger("SESSION-PROFILE-DIGEST",
                                                  "[ExecutorMetrics]");

    private static final Logger DIGEST_LOGGER = LoggerFactory.getLogger("SESSION-PROFILE-DIGEST",
                                                  "[DigestMetrics]");
    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private Watchers            sessionWatchers;

    @Autowired
    private ConnectionsService  connectionsService;

    @Scheduled(initialDelay = 60000L, fixedDelay = 60000L)
    public void printMetrics() {
        printExecutorMetrics();
        printLocalDigest();
    }

    private void printExecutorMetrics() {
        TaskMetrics.getInstance().loggingMetrics(EXE_LOGGER);
    }

    private void printLocalDigest() {
        int pubCount = sessionDataStore.getConnectPublishers().values().stream().mapToInt(Map::size)
            .sum();
        int subCount = sessionInterests.getConnectSubscribers().values().stream()
            .mapToInt(Map::size).sum();
        int watchCount = sessionWatchers.getConnectWatchers().values().stream().mapToInt(Map::size)
            .sum();
        int connectionCount = connectionsService.getConnections().size();
        DIGEST_LOGGER
            .info(String.format("[CountMonitor] pub: %d, sub: %d, watch: %d, connection: %d",
                pubCount, subCount, watchCount, connectionCount));
    }
}
