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
package com.alipay.sofa.registry.server.data.lease;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 10:30 yuzhi.lyz Exp $
 */
public final class SessionLeaseManager {
    private static final Logger            LOGGER                     = LoggerFactory
                                                                          .getLogger(SessionLeaseManager.class);

    @Autowired
    private DataServerConfig               dataServerConfig;
    @Autowired
    private DatumStorage                   localDatumStorage;
    @Autowired
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    private final Map<ProcessId, Long>     connectIdRenewTimestampMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        ConcurrentUtils.createDaemonThread("session-lease-cleaner", new LeaseCleaner()).start();
    }

    public void renewSession(ProcessId sessionProcessId) {
        connectIdRenewTimestampMap.put(sessionProcessId, System.currentTimeMillis());
    }

    public Map<ProcessId, Date> getExpireProcessId(int leaseMs) {
        Map<ProcessId, Date> expires = Maps.newHashMap();
        final long lastRenew = System.currentTimeMillis() - leaseMs;
        connectIdRenewTimestampMap.forEach((k, v) -> {
            if (v < lastRenew) {
                expires.put(k, new Date(v));
            }
        });
        return expires;
    }

    private List<ProcessId> cleanExpire(Collection<ProcessId> expires) {
        List<ProcessId> cleans = Lists.newArrayList();
        expires.forEach(p -> {
            if (sessionServerConnectionFactory.containsConnection(p)) {
                LOGGER.warn("expire session has conn, {}", p);
                return;
            }
            connectIdRenewTimestampMap.remove(p);
            cleans.add(p);
        });
        return cleans;
    }

    private void cleanStorage() {
        // make sure the existing processId be clean
        Set<ProcessId> stores = localDatumStorage.getSessionProcessIds();
        stores.forEach(p -> {
            if (!connectIdRenewTimestampMap.containsKey(p)) {
                Map<String, DatumVersion> versionMap = localDatumStorage.clean(p);
                LOGGER.info("expire session correct, {}, datums={}", p, versionMap.size());
            }
        });
    }

    private final class LeaseCleaner extends LoopRunnable {
        @Override
        public void runUnthrowable() {
            Map<ProcessId, Date> expires = getExpireProcessId(dataServerConfig.getSessionLeaseSec() * 1000);
            LOGGER.info("lease expire sessions, {}", expires);

            if (!expires.isEmpty()) {
                List<ProcessId> cleans = cleanExpire(expires.keySet());
                LOGGER.info("expire sessions clean, {}", cleans);
            }

            cleanStorage();

            // compact the unpub
            long tombstoneTimestamp = System.currentTimeMillis()
                                      - dataServerConfig.getDatumCompactDelayMs();
            Map<String, Integer> compacted = localDatumStorage.compact(tombstoneTimestamp);
            LOGGER.info("compact datum, {}", compacted);
        }

        @Override
        public void waitingUnthrowable() {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
    }
}
