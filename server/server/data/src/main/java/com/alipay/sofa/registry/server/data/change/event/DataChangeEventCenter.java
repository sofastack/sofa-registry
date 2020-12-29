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
package com.alipay.sofa.registry.server.data.change.event;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public final class DataChangeEventCenter {
    private static final Logger                   LOGGER                 = LoggerFactory
                                                                             .getLogger(DataChangeEventCenter.class);

    /**
     * count of DataChangeEventQueue
     */
    private int                                   queueCount;

    /**
     * queues of DataChangeEvent
     */
    private DataChangeEventQueue[]                dataChangeEventQueues;

    @Autowired
    private DataServerConfig                      dataServerConfig;

    private final Map<String, Set<String>>        dataCenter2Changes     = Maps.newConcurrentMap();
    private final ReadWriteLock                   lock                   = new ReentrantReadWriteLock();

    private final Map<String, Map<String, Datum>> dataCenter2TempChanges = Maps.newConcurrentMap();
    private final ReadWriteLock                   tempLock               = new ReentrantReadWriteLock();

    private TempChangeMerger                      tempChangeMerger;
    private ChangeMerger                          changeMerger;

    @PostConstruct
    public void init() {
        queueCount = dataServerConfig.getQueueCount();
        dataChangeEventQueues = new DataChangeEventQueue[queueCount];
        for (int idx = 0; idx < queueCount; idx++) {
            dataChangeEventQueues[idx] = new DataChangeEventQueue(idx,
                dataServerConfig.getQueueSize());
        }
        this.changeMerger = new ChangeMerger();
        this.tempChangeMerger = new TempChangeMerger();

        ConcurrentUtils.createDaemonThread("changeMerger", changeMerger).start();
        ConcurrentUtils.createDaemonThread("tempChangeMerger", tempChangeMerger).start();

        LOGGER.info("start DataChange NotifyIntervalMs={}", dataServerConfig.getNotifyIntervalMs());
    }

    public void onTempPubChange(Publisher publisher, String dataCenter) {
        Map<String, Datum> changes = dataCenter2TempChanges
                .computeIfAbsent(dataCenter, k -> Maps.newConcurrentMap());
        tempLock.readLock().lock();
        try {
            Datum existing = changes
                    .computeIfAbsent(publisher.getRegisterId(), k -> new Datum(publisher, dataCenter));
            existing.addPublisher(publisher);
        } finally {
            tempLock.readLock().unlock();
        }
    }

    public void onChange(Collection<String> dataInfoIds, String dataCenter) {
        Set<String> changes = dataCenter2Changes.computeIfAbsent(dataCenter, k -> Sets.newConcurrentHashSet());
        lock.readLock().lock();
        try {
            changes.addAll(dataInfoIds);
        } finally {
            lock.readLock().unlock();
        }
    }

    private abstract class Merger extends LoopRunnable {
        final int  intervalMs;
        final Lock lock;

        Merger(int intervalMs, Lock lock) {
            this.intervalMs = intervalMs;
            this.lock = lock;
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(intervalMs, TimeUnit.MILLISECONDS);
        }

        // dataInfoId -> events of datacenter
        void commit(Map<String, List<IDataChangeEvent>> events) {
            // TODO Needs optimization, merge the dataInfoIds as one notify
            for (Map.Entry<String, List<IDataChangeEvent>> e : events.entrySet()) {
                DataChangeEventQueue queue = getQueue(e.getKey());
                for (IDataChangeEvent event : e.getValue()) {
                    if (!queue.onChange(event)) {
                        LOGGER.error("failed to commit change event, {}", event);
                    } else {
                        LOGGER.info("commit change event, {}", event);
                    }
                }
            }
        }
    }

    private final class TempChangeMerger extends Merger {
        TempChangeMerger() {
            super(dataServerConfig.getNotifyTempDataIntervalMs(), tempLock.writeLock());
        }

        @Override
        public void runUnthrowable() {
            final Map<String, List<IDataChangeEvent>> events = Maps.newHashMap();
            lock.lock();
            try {
                for (Map<String, Datum> change : dataCenter2TempChanges.values()) {
                    change.values().forEach(d -> {
                        List<IDataChangeEvent> list = events
                                .computeIfAbsent(d.getDataInfoId(), k -> Lists.newArrayList());
                        list.add(new DataTempChangeEvent(d));
                    });
                    change.clear();
                }
            } finally {
                lock.unlock();
            }
            commit(events);
        }
    }

    private final class ChangeMerger extends Merger {
        ChangeMerger() {
            super(dataServerConfig.getNotifyIntervalMs(), DataChangeEventCenter.this.lock.writeLock());
        }

        @Override
        public void runUnthrowable() {
            final Map<String, List<IDataChangeEvent>> events = Maps.newHashMap();
            lock.lock();
            try {
                for (Map.Entry<String, Set<String>> change : dataCenter2Changes.entrySet()) {
                    final String dataCenter = change.getKey();
                    Set<String> dataInfoIds = change.getValue();
                    for (String dataInfoId : dataInfoIds) {
                        List<IDataChangeEvent> list = events
                                .computeIfAbsent(dataInfoId, k -> Lists.newArrayList());
                        list.add(new DataChangeEvent(dataCenter, dataInfoId));
                    }
                    dataInfoIds.clear();
                }
            } finally {
                lock.unlock();
            }
            commit(events);
        }
    }

    private DataChangeEventQueue getQueue(String dataInfoId) {
        return dataChangeEventQueues[hash(dataInfoId)];
    }

    public int hash(String key) {
        if (queueCount > 1) {
            return Math.abs(key.hashCode() % queueCount);
        } else {
            return 0;
        }
    }

    public DataChangeEventQueue[] getQueues() {
        return dataChangeEventQueues;
    }
}