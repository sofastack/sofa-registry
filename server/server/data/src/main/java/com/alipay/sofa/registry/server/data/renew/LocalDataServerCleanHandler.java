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
package com.alipay.sofa.registry.server.data.renew;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.BackupTriad;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.util.DelayItem;

/**
 *
 * @author shangyu.wh
 * @version $Id: LocalDataServerCleanHandler.java, v 0.1 2018-07-16 17:50 shangyu.wh Exp $
 */
public class LocalDataServerCleanHandler {

    private static final Logger                         LOGGER       = LoggerFactory
                                                                         .getLogger(LocalDataServerCleanHandler.class);

    private static final Logger                         LOGGER_START = LoggerFactory
                                                                         .getLogger("DATA-START-LOGS");

    @Autowired
    private DataServerConfig                            dataServerConfig;

    @Autowired
    private DataServerCache                             dataServerCache;

    @Autowired
    private DataChangeEventCenter                       dataChangeEventCenter;

    @Autowired
    private DatumCache                                  datumCache;

    private LocalCleanTask                              task;

    /**
     * a DelayQueue that contains clean task
     */
    private final DelayQueue<DelayItem<LocalCleanTask>> EVENT_QUEUE  = new DelayQueue<>();

    /**
     * constructor
     */
    public LocalDataServerCleanHandler() {
        Executor executor = ExecutorFactory.newSingleThreadExecutor(LocalDataServerCleanHandler.class.getSimpleName());
        executor.execute(() -> {
            while (true) {
                try {
                    DelayItem<LocalCleanTask> delayItem = EVENT_QUEUE.take();
                    task = delayItem.getItem();
                    task.run();
                } catch (Throwable e) {
                    LOGGER.error("[LocalDataServerCleanHandler] handle clean task failed", e);
                }
            }
        });
        LOGGER_START.info("[LocalDataServerCleanHandler] start LocalDataServerCleanHandler success");
    }

    /**
     *
     */
    public void reset() {
        synchronized (LocalDataServerCleanHandler.class) {
            EVENT_QUEUE.clear();
            if (task != null) {
                task.stop();
            }
        }
        EVENT_QUEUE.add(new DelayItem<>(new LocalCleanTask(), dataServerConfig
            .getLocalDataServerCleanDelay()));
    }

    private class LocalCleanTask {

        private AtomicBoolean running = new AtomicBoolean(false);

        /**
         *
         */
        public void run() {
            if (running.compareAndSet(false, true)) {
                try {

                    Map<String, DataNode> dataNodeMap = dataServerCache
                        .getDataServers(dataServerConfig.getLocalDataCenter());
                    if (dataNodeMap == null || dataNodeMap.isEmpty()) {
                        LOGGER.warn("Calculate Old BackupTriad,old dataServer list is empty!");
                        return;
                    }

                    ConsistentHash<DataNode> consistentHash = new ConsistentHash<>(
                        dataServerConfig.getNumberOfReplicas(), dataNodeMap.values());

                    Map<String, Map<String, Datum>> dataMapAll = datumCache.getAll();

                    for (Entry<String, Map<String, Datum>> entryAll : dataMapAll.entrySet()) {
                        String dataCenter = entryAll.getKey();
                        Map<String, Datum> dataMap = entryAll.getValue();
                        for (Entry<String, Datum> entry : dataMap.entrySet()) {

                            String dataInfoId = entry.getKey();
                            Datum datum = entry.getValue();
                            if (!running.get()) {
                                LOGGER.info(
                                    "[LocalDataServerCleanHandler] task cancel, dataInfoId={}",
                                    dataInfoId);
                                return;
                            }

                            BackupTriad backupTriad = new BackupTriad(dataInfoId,
                                consistentHash.getNUniqueNodesFor(dataInfoId,
                                    dataServerConfig.getStoreNodes()));
                            if (!backupTriad.containsSelf()) {
                                if (datum != null) {
                                    int size = datum.getPubMap() != null ? datum.getPubMap().size()
                                        : 0;
                                    dataChangeEventCenter.clean(datum, DataSourceTypeEnum.CLEAN);
                                    LOGGER
                                        .info(
                                            "[LocalDataServerCleanHandler] clean handle, dataCenter={},dataInfoId={},pub size={}",
                                            dataCenter, dataInfoId, size);
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error("[LocalDataServerCleanHandler] clean local datum task error!", e);
                } finally {
                    EVENT_QUEUE.add(new DelayItem<>(new LocalCleanTask(), dataServerConfig
                        .getLocalDataServerCleanDelay()));
                }
            }
        }

        /**
         *
         */
        public void stop() {
            running.set(false);
        }
    }
}