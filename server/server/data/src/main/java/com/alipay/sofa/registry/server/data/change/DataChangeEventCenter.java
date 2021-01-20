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
package com.alipay.sofa.registry.server.data.change;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public final class DataChangeEventCenter {
    private static final Logger                   LOGGER                 = LoggerFactory
                                                                             .getLogger(DataChangeEventCenter.class);

    @Autowired
    private DataServerConfig                      dataServerConfig;

    @Autowired
    private DatumCache                            datumCache;

    @Autowired
    private SessionServerConnectionFactory        sessionServerConnectionFactory;

    @Autowired
    private Exchange                              boltExchange;

    private final Map<String, Set<String>>        dataCenter2Changes     = Maps.newConcurrentMap();
    private final ReadWriteLock                   lock                   = new ReentrantReadWriteLock();
    private final List<ChangeNotifier>            retryNotifiers         = Lists.newLinkedList();

    private final Map<String, Map<String, Datum>> dataCenter2TempChanges = Maps.newConcurrentMap();
    private final ReadWriteLock                   tempLock               = new ReentrantReadWriteLock();

    private TempChangeMerger                      tempChangeMerger;
    private ChangeMerger                          changeMerger;

    private KeyedThreadPoolExecutor               notifyExecutor;
    private KeyedThreadPoolExecutor               notifyTempExecutor;

    @PostConstruct
    public void init() {
        this.notifyExecutor = new KeyedThreadPoolExecutor("notify",
            dataServerConfig.getNotifyExecutorPoolSize(),
            dataServerConfig.getNotifyExecutorQueueSize());
        this.notifyTempExecutor = new KeyedThreadPoolExecutor("notifyTemp",
            dataServerConfig.getNotifyTempExecutorPoolSize(),
            dataServerConfig.getNotifyExecutorQueueSize());

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
                    .computeIfAbsent(publisher.getDataInfoId(), k -> new Datum(publisher, dataCenter));
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

    private final class TempNotifier implements Runnable {
        final Connection connection;
        final Datum      datum;

        TempNotifier(Connection connection, Datum datum) {
            this.connection = connection;
            this.datum = datum;
        }

        @Override
        public void run() {
            try {
                if (!connection.isFine()) {
                    LOGGER.info("temp change notify failed, conn is closed, {}", connection);
                    return;
                }
                notifyTempPub(connection, datum);
            } catch (Throwable e) {
                LOGGER.error("failed to notify temp {}, {}", connection, datum, e);
            }
        }
    }

    private final class ChangeNotifier implements Runnable {
        final Connection                connection;
        final String                    dataCenter;
        final Map<String, DatumVersion> dataInfoIds;
        final Set<String>               revisions;
        volatile int                    retryCount;

        public ChangeNotifier(Connection connection, String dataCenter,
                              Map<String, DatumVersion> dataInfoIds, Set<String> revisions) {
            this.dataCenter = dataCenter;
            this.connection = connection;
            this.dataInfoIds = dataInfoIds;
            this.revisions = revisions;
        }

        @Override
        public void run() {
            try {
                if (!connection.isFine()) {
                    LOGGER.info("change notify failed, conn is closed, {}", connection);
                    return;
                }
                DataChangeRequest request = new DataChangeRequest(dataCenter, dataInfoIds,
                    revisions);
                doNotify(request, connection);
            } catch (Throwable e) {
                LOGGER.error("failed to notify {}", this, e);
                retry(this);
            }
        }

        @Override
        public String toString() {
            return "ChangeNotifier{" + "connection=" + connection + ", dataCenter='" + dataCenter
                   + '\'' + ", dataInfoIds=" + dataInfoIds + ", retryCount=" + retryCount + '}';
        }
    }

    private void retry(ChangeNotifier notifier) {
        notifier.retryCount++;
        if (notifier.retryCount < dataServerConfig.getNotifyRetryTimes()) {
            final int maxSize = dataServerConfig.getNotifyRetryQueueSize();
            boolean retry = false;
            synchronized (retryNotifiers) {
                if (retryNotifiers.size() < maxSize) {
                    retryNotifiers.add(notifier);
                    retry = true;
                }
            }
            if (!retry) {
                LOGGER.warn("skip retry of full, {}", notifier);
            }
        }
    }

    private void notifyTempPub(Connection connection, Datum datum) {
        Datum existDatum = datumCache.get(datum.getDataCenter(), datum.getDataInfoId());
        if (existDatum != null) {
            datum.addPublishers(existDatum.getPubMap());
        }
        // TODO the version maybe confict with the existing
        datum.updateVersion();
        DataPushRequest request = new DataPushRequest(datum);
        LOGGER.info("temp pub, {}", datum);
        doNotify(request, connection);
    }

    private void doNotify(Object request, Connection connection) {
        Server sessionServer = boltExchange.getServer(dataServerConfig.getPort());
        sessionServer.sendSync(sessionServer.getChannel(connection.getRemoteAddress()), request,
            dataServerConfig.getRpcTimeout());
    }

    private final class TempChangeMerger extends LoopRunnable {

        @Override
        public void runUnthrowable() {
            final List<DataTempChangeEvent> events = Lists.newArrayList();
            tempLock.writeLock().lock();
            try {
                for (Map<String, Datum> change : dataCenter2TempChanges.values()) {
                    change.values().forEach(d -> {
                        events.add(new DataTempChangeEvent(d));
                    });
                    change.clear();
                }
            } finally {
                tempLock.writeLock().unlock();
            }
            final List<Connection> connections = sessionServerConnectionFactory.getSessionConnections();
            if (connections.isEmpty()) {
                LOGGER.warn("session conn is empty when temp change");
                return;
            }
            for (Connection connection : connections) {
                for (DataTempChangeEvent event : events) {
                    Datum datum = event.getDatum();
                    try {
                        // group by connect && dataInfoId
                        notifyTempExecutor.execute(Tuple.of(datum.getDataInfoId(), connection.getRemoteAddress()),
                                new TempNotifier(connection, datum));
                    } catch (RejectedExecutionException e) {
                        LOGGER.warn("notify temp full, {}, {}", datum, e.getMessage());
                    } catch (Throwable e) {
                        LOGGER.error("notify temp full, {}", datum, e);
                    }
                }
            }
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(dataServerConfig.getNotifyTempDataIntervalMs(),
                TimeUnit.MILLISECONDS);
        }
    }

    private final class ChangeMerger extends LoopRunnable {

        @Override
        public void runUnthrowable() {
            final List<DataChangeEvent> events = Lists.newArrayList();
            final int maxItems = dataServerConfig.getNotifyMaxItems();
            lock.writeLock().lock();
            try {
                for (Map.Entry<String, Set<String>> change : dataCenter2Changes.entrySet()) {
                    final String dataCenter = change.getKey();
                    List<String> dataInfoIds = Lists.newArrayList(change.getValue());
                    change.getValue().clear();
                    List<List<String>> parts = Lists.partition(dataInfoIds, maxItems);
                    for (int i = 0; i < parts.size(); i++) {
                        events.add(new DataChangeEvent(dataCenter, parts.get(i)));
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
            // get retrys
            List<ChangeNotifier> retrys = null;
            synchronized (retryNotifiers) {
                retrys = Lists.newArrayList(retryNotifiers);
                retryNotifiers.clear();
            }

            final List<Connection> connections = sessionServerConnectionFactory
                .getSessionConnections();
            if (connections.isEmpty()) {
                LOGGER.warn("session conn is empty when change");
                return;
            }
            for (DataChangeEvent event : events) {
                final Map<String, DatumVersion> changes = new HashMap<>(events.size());
                final Set<String> revisions = new HashSet<>(64);
                final String dataCenter = event.getDataCenter();
                for (String dataInfoId : event.getDataInfoIds()) {
                    final DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
                    if (dataInfo.typeIsSofaApp()) {
                        // get datum is slower than  get version
                        Datum datum = datumCache.get(dataCenter, dataInfoId);
                        if (datum != null) {
                            changes.put(dataInfoId, DatumVersion.of(datum.getVersion()));
                            revisions.addAll(datum.revisions());
                        }
                    } else {
                        DatumVersion datumVersion = datumCache.getVersion(dataCenter, dataInfoId);
                        if (datumVersion != null) {
                            changes.put(dataInfoId, datumVersion);
                        }
                    }
                }
                if (changes.isEmpty()) {
                    continue;
                }
                for (Connection connection : connections) {
                    // group by connection
                    notifyExecutor.execute(connection.getRemoteAddress(), new ChangeNotifier(
                        connection, event.getDataCenter(), changes, revisions));
                }
            }
            // commit retry
            for (ChangeNotifier retry : retrys) {
                notifyExecutor.execute(retry.connection.getRemoteAddress(), retry);
            }
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(dataServerConfig.getNotifyIntervalMs(),
                TimeUnit.MILLISECONDS);
        }
    }
}