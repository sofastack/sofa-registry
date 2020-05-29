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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.UnPublisher;
import com.alipay.sofa.registry.server.data.change.ChangeData;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.change.SnapshotData;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;
import com.google.common.collect.Interners;

/**
 * a queue of DataChangeEvent
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEventQueue.java, v 0.1 2017-12-11 17:10 qian.lqlq Exp $
 */
public class DataChangeEventQueue {

    private static final Logger                        LOGGER                    = LoggerFactory
                                                                                     .getLogger(DataChangeEventQueue.class);

    private static final Logger                        LOGGER_START              = LoggerFactory
                                                                                     .getLogger("DATA-START-LOGS");

    private static final Logger                        RENEW_LOGGER              = LoggerFactory
                                                                                     .getLogger(
                                                                                         ValueConstants.LOGGER_NAME_RENEW,
                                                                                         "[DataChangeEventQueue]");

    /**
     *
     */
    private final String                               name;

    /**
     * a block queue that stores all data change events
     */
    private final BlockingQueue<IDataChangeEvent>      eventQueue;

    /**
     *
     */
    private final Map<String, Map<String, ChangeData>> CHANGE_DATA_MAP_FOR_MERGE = new ConcurrentHashMap<>();

    /**
     *
     */
    private final DelayQueue<ChangeData>               CHANGE_QUEUE              = new DelayQueue();

    private final int                                  notifyIntervalMs;

    private final int                                  notifyTempDataIntervalMs;

    private final ReentrantLock                        lock                      = new ReentrantLock();

    private final int                                  queueIdx;

    private DataServerConfig                           dataServerConfig;

    private DataChangeEventCenter                      dataChangeEventCenter;

    private DatumCache                                 datumCache;

    /**
     * constructor
     * @param queueIdx
     * @param dataServerConfig
     */
    public DataChangeEventQueue(int queueIdx, DataServerConfig dataServerConfig,
                                DataChangeEventCenter dataChangeEventCenter, DatumCache datumCache) {
        this.queueIdx = queueIdx;
        this.name = String.format("%s_%s", DataChangeEventQueue.class.getSimpleName(), queueIdx);
        this.dataServerConfig = dataServerConfig;
        int queueSize = dataServerConfig.getQueueSize();
        if (queueSize <= 0) {
            eventQueue = new LinkedBlockingDeque<>();
        } else {
            eventQueue = new LinkedBlockingDeque<>(queueSize);
        }
        this.notifyIntervalMs = dataServerConfig.getNotifyIntervalMs();
        this.notifyTempDataIntervalMs = dataServerConfig.getNotifyTempDataIntervalMs();
        this.dataChangeEventCenter = dataChangeEventCenter;
        this.datumCache = datumCache;
    }

    /**
     * receive event when data changed
     *
     * @param event
     */
    public void onChange(IDataChangeEvent event) {
        try {
            eventQueue.add(event);
        } catch (Throwable e) {
            LOGGER.error("Error onChange: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     * @throws InterruptedException
     */
    public ChangeData take() throws InterruptedException {
        ChangeData changeData = CHANGE_QUEUE.take();
        lock.lock();
        try {
            removeMapForMerge(changeData);
            return changeData;
        } finally {
            lock.unlock();
        }
    }

    private void removeMapForMerge(ChangeData changeData) {
        Datum datum = changeData.getDatum();
        if (changeData.getSourceType() != DataSourceTypeEnum.PUB_TEMP && datum != null) {
            CHANGE_DATA_MAP_FOR_MERGE.get(datum.getDataCenter()).remove(datum.getDataInfoId());
        }
    }

    /**
     *
     * @param dataCenter
     * @param dataInfoId
     * @param sourceType
     * @param changeType
     * @return
     */
    private ChangeData getChangeData(String dataCenter, String dataInfoId,
                                     DataSourceTypeEnum sourceType, DataChangeTypeEnum changeType) {
        Map<String, ChangeData> map = CHANGE_DATA_MAP_FOR_MERGE.get(dataCenter);
        if (map == null) {
            Map<String, ChangeData> newMap = new ConcurrentHashMap<>();
            map = CHANGE_DATA_MAP_FOR_MERGE.putIfAbsent(dataCenter, newMap);
            if (map == null) {
                map = newMap;
            }
        }

        ChangeData changeData = map.get(dataInfoId);
        if (changeData == null) {
            ChangeData newChangeData = new ChangeData(null, this.notifyIntervalMs, sourceType,
                changeType);
            changeData = map.putIfAbsent(dataInfoId, newChangeData);
            if (changeData == null) {
                changeData = newChangeData;
            }
            CHANGE_QUEUE.put(changeData);
        }
        return changeData;
    }

    /**
     *
     */
    public void start() {
        Executor executor = ExecutorFactory
                .newSingleThreadExecutor(String.format("%s_%s", DataChangeEventQueue.class.getSimpleName(), getName()));
        executor.execute(() -> {
            while (true) {
                try {
                    IDataChangeEvent event = eventQueue.take();
                    DataChangeScopeEnum scope = event.getScope();
                    if (scope == DataChangeScopeEnum.DATUM) {
                        DataChangeEvent dataChangeEvent = (DataChangeEvent) event;
                        //Temporary push data will be notify as soon as,and not merge to normal pub data;
                        if (dataChangeEvent.getSourceType() == DataSourceTypeEnum.PUB_TEMP) {
                            addTempChangeData(dataChangeEvent.getDatum(), dataChangeEvent.getChangeType(),
                                    dataChangeEvent.getSourceType());
                        } else {
                            handleDatum(dataChangeEvent.getChangeType(), dataChangeEvent.getSourceType(),
                                    dataChangeEvent.getDatum());
                        }
                    } else if (scope == DataChangeScopeEnum.CLIENT) {
                        handleClientOff((ClientChangeEvent) event);
                    } else if (scope == DataChangeScopeEnum.SNAPSHOT) {
                        handleSnapshot((DatumSnapshotEvent) event);
                    }
                } catch (Throwable e) {
                    LOGGER.error("[{}] handle change event failed", getName(), e);
                }
            }
        });
        LOGGER_START.info("[{}] start DataChangeEventQueue success", getName());
    }

    private void handleClientOff(ClientChangeEvent event) {
        String connectId = event.getHost();
        synchronized (Interners.newWeakInterner().intern(connectId)) {
            Map<String, Publisher> pubMap = datumCache.getByConnectId(connectId);
            if (pubMap != null && !pubMap.isEmpty()) {
                LOGGER.info(
                    "[{}] client off begin, connectId={}, occurTimestamp={}, all pubSize={}",
                    getName(), connectId, event.getOccurredTimestamp(), pubMap.size());
                int count = 0;
                for (Publisher publisher : pubMap.values()) {
                    // Only care dataInfoIds which belong to this queue
                    if (!belongTo(publisher.getDataInfoId())) {
                        continue;
                    }

                    DataServerNode dataServerNode = DataServerNodeFactory.computeDataServerNode(
                        dataServerConfig.getLocalDataCenter(), publisher.getDataInfoId());
                    //current dataCenter backup data need not unPub,it will be unPub by backup sync event
                    if (DataServerConfig.IP.equals(dataServerNode.getIp())) {
                        Datum datum = new Datum(new UnPublisher(publisher.getDataInfoId(),
                            publisher.getRegisterId(), event.getOccurredTimestamp()),
                            event.getDataCenter(), event.getVersion());
                        datum.setContainsUnPub(true);
                        handleDatum(DataChangeTypeEnum.MERGE, DataSourceTypeEnum.PUB, datum);
                        count++;
                    }
                }
                LOGGER
                    .info(
                        "[{}] client off handle, connectId={}, occurTimestamp={}, version={}, handle pubSize={}",
                        getName(), connectId, event.getOccurredTimestamp(), event.getVersion(),
                        count);
            } else {
                LOGGER.info("[{}] no datum to handle, connectId={}", getName(), connectId);
            }
        }
    }

    private void handleDatum(DataChangeTypeEnum changeType, DataSourceTypeEnum sourceType,
                             Datum targetDatum) {
        lock.lock();
        try {
            //get changed datum
            ChangeData changeData = getChangeData(targetDatum.getDataCenter(),
                targetDatum.getDataInfoId(), sourceType, changeType);
            Datum cacheDatum = changeData.getDatum();
            if (changeType == DataChangeTypeEnum.COVER || cacheDatum == null) {
                changeData.setDatum(targetDatum);
            } else {
                Map<String, Publisher> targetPubMap = targetDatum.getPubMap();
                Map<String, Publisher> cachePubMap = cacheDatum.getPubMap();
                for (Publisher pub : targetPubMap.values()) {
                    String registerId = pub.getRegisterId();
                    Publisher cachePub = cachePubMap.get(registerId);
                    if (cachePub != null) {
                        // if the registerTimestamp of cachePub is greater than the registerTimestamp of pub, it means
                        // that pub is not the newest data, should be ignored
                        if (pub.getRegisterTimestamp() < cachePub.getRegisterTimestamp()) {
                            continue;
                        }
                        // if pub and cachePub both are publisher, and sourceAddress of both are equal,
                        // and version of cachePub is greater than version of pub, should be ignored
                        if (!(pub instanceof UnPublisher) && !(cachePub instanceof UnPublisher)
                            && pub.getSourceAddress().equals(cachePub.getSourceAddress())
                            && cachePub.getVersion() > pub.getVersion()) {
                            continue;
                        }
                    }
                    cachePubMap.put(registerId, pub);
                    cacheDatum.setVersion(targetDatum.getVersion());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void handleSnapshot(DatumSnapshotEvent event) {
        String connectId = event.getConnectId();
        Map<String, Publisher> cachePubMap = event.getCachePubMap();
        Map<String, Publisher> snapshotPubMap = event.getPubMap();

        // build SnapshotData
        Map<String, SnapshotData> dataInfoId2SnapshotData = new HashMap<>();
        synchronized (Interners.newWeakInterner().intern(connectId)) {
            for (Map.Entry<String, Publisher> entry : snapshotPubMap.entrySet()) {
                String registerId = entry.getKey();
                Publisher publisher = entry.getValue();
                String dataInfoId = publisher.getDataInfoId();

                // Only care dataInfoIds which belong to this queue
                if (!belongTo(dataInfoId)) {
                    continue;
                }

                SnapshotData snapshotData = getOrCreateSnapshotData(dataInfoId2SnapshotData,
                    dataInfoId);
                snapshotData.getSnapshotPubMap().put(registerId, publisher);
            }
            for (Map.Entry<String, Publisher> entry : cachePubMap.entrySet()) {
                String registerId = entry.getKey();
                Publisher publisher = entry.getValue();
                String dataInfoId = publisher.getDataInfoId();

                // Only care dataInfoIds which belong to this queue
                if (!belongTo(dataInfoId)) {
                    continue;
                }

                SnapshotData snapshotData = getOrCreateSnapshotData(dataInfoId2SnapshotData,
                    dataInfoId);
                snapshotData.getToBeDeletedPubMap().put(registerId, publisher);
            }
        }

        // put all SnapshotDatas to queue
        for (SnapshotData snapshotData : dataInfoId2SnapshotData.values()) {
            RENEW_LOGGER
                .info(
                    "SnapshotData: connectId={}, dataInfoId={}, cachePubSize={}, snapshotPubSize={}",
                    connectId, snapshotData.getDataInfoId(), snapshotData.getToBeDeletedPubMap()
                        .size(), snapshotData.getSnapshotPubMap().size());
            CHANGE_QUEUE.put(snapshotData);
        }
    }

    private SnapshotData getOrCreateSnapshotData(Map<String, SnapshotData> dataInfoId2SnapshotData,
                                                 String dataInfoId) {
        SnapshotData snapshotData = dataInfoId2SnapshotData.get(dataInfoId);
        if (snapshotData == null) {
            snapshotData = new SnapshotData(dataInfoId, new HashMap<>(), new HashMap<>());
            dataInfoId2SnapshotData.put(dataInfoId, snapshotData);
        }
        return snapshotData;
    }

    private void addTempChangeData(Datum targetDatum, DataChangeTypeEnum changeType,
                                   DataSourceTypeEnum sourceType) {
        ChangeData tempChangeData = new ChangeData(targetDatum, this.notifyTempDataIntervalMs,
            sourceType, changeType);
        CHANGE_QUEUE.put(tempChangeData);
    }

    /**
     * Determine whether dataInfoId belongs to the current queue
     */
    private boolean belongTo(String dataInfoId) {
        return this.queueIdx == this.dataChangeEventCenter.hash(dataInfoId);
    }
}