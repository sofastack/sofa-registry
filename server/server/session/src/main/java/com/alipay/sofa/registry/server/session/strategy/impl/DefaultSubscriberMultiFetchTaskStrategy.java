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
package com.alipay.sofa.registry.server.session.strategy.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheAccessException;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.server.session.scheduler.task.PushTaskClosure;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.strategy.SubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.google.common.collect.Lists;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberMultiFetchTaskStrategy implements SubscriberMultiFetchTaskStrategy {
    private static final Logger   taskLogger = LoggerFactory.getLogger(
                                                 DefaultSubscriberMultiFetchTaskStrategy.class,
                                                 "[Task]");

    private static final Logger   LOGGER     = LoggerFactory
                                                 .getLogger(DefaultSubscriberMultiFetchTaskStrategy.class);

    @Autowired
    protected SessionServerConfig sessionServerConfig;

    @Autowired
    protected ExecutorManager     executorManager;

    @Autowired
    protected Interests           sessionInterests;

    /**
     *
     * @param sessionServerConfig
     * @param taskListenerManager
     * @param sessionCacheService
     * @param fetchDataInfoId
     * @param subscribers must not empty
     */
    @Override
    public void doSubscriberMultiFetchTask(SessionServerConfig sessionServerConfig,
                                           TaskListenerManager taskListenerManager,
                                           CacheService sessionCacheService,
                                           String fetchDataInfoId,
                                           Collection<Subscriber> subscribers) {
        Subscriber _subscriber = subscribers.iterator().next();
        String dataId = _subscriber.getDataId();
        String instanceId = _subscriber.getInstanceId();
        String group = _subscriber.getGroup();

        Map<String/*dataCenter*/, Datum> datumMap = getDatumsCache(fetchDataInfoId, dataId,
            instanceId, group, sessionCacheService);

        for (ScopeEnum scopeEnum : ScopeEnum.values()) {
            Map<InetSocketAddress, Map<String, Subscriber>> map = getPushSubscribers(scopeEnum,
                subscribers);

            if (map != null && !map.isEmpty()) {
                for (Map.Entry<InetSocketAddress, Map<String, Subscriber>> entry : map.entrySet()) {
                    Map<String, Subscriber> subscriberMap = entry.getValue();
                    if (subscriberMap != null && !subscriberMap.isEmpty()) {
                        Subscriber subscriber = subscriberMap.values().iterator().next();
                        boolean isOldClientVersion = !BaseInfo.ClientVersion.StoreData
                            .equals(subscriber.getClientVersion());

                        if (isOldClientVersion) {
                            fireUserDataPushTaskCloud(fetchDataInfoId, datumMap,
                                subscriberMap.values(), subscriber, taskListenerManager);
                        } else {
                            fireReceivedDataPushTaskCloud(fetchDataInfoId, datumMap, new ArrayList(
                                subscriberMap.keySet()), subscriber, taskListenerManager);
                        }
                    }
                }
            }
        }
    }

    protected Map<String, Datum> getDatumsCache(String dataInfoId, String dataId, String instanceId, String group,
                                                CacheService sessionCacheService) {

        Map<String, Datum> map = new HashMap<>();
        NodeManager nodeManager = NodeManagerFactory.getNodeManager(Node.NodeType.META);
        Collection<String> dataCenters = nodeManager.getDataCenters();
        if (dataCenters != null) {
            Collection<Key> keys = dataCenters.stream().
                    map(dataCenter -> new Key(Key.KeyType.OBJ, DatumKey.class.getName(),
                            new DatumKey(dataInfoId, dataCenter))).
                    collect(Collectors.toList());

            Map<Key, Value> values = null;
            try {
                values = sessionCacheService.getValues(keys);
            } catch (CacheAccessException e) {
                // The version is set to 0, so that when session checks the datum versions regularly, it will actively re-query the data.
                for (String dataCenter : dataCenters) {
                    boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter, dataInfoId);
                    LOGGER.error(String.format(
                            "error when access cache of dataCenter(%s) fetchDataInfoId(%s), so checkAndUpdateInterestVersionZero(return %s): %s",
                            dataCenter, dataInfoId, result, e.getMessage()), e);
                }
            }

            if (values != null) {
                values.forEach((key, value) -> {
                    if (value != null && value.getPayload() != null) {
                        Datum datum = (Datum) value.getPayload();
                        String dataCenter = ((DatumKey) key.getEntityType()).getDataCenter();
                        map.put(dataCenter, datum);
                    }
                });
            }
        }
        // New subscribers register come up, must push datum to them, even empty data
        if (map.isEmpty()) {
            String localDataCenter = sessionServerConfig.getSessionServerDataCenter();
            Datum datum = createEmptyDatum(localDataCenter, dataId, instanceId, group);
            map.put(localDataCenter, datum);
        }

        return map;
    }

    protected Datum createEmptyDatum(String dataCenter, String dataId, String instanceId,
                                     String group) {
        Datum datum;
        datum = new Datum();
        datum.setDataInfoId(new DataInfo(instanceId, dataId, group).getDataInfoId());
        datum.setDataId(dataId);
        datum.setInstanceId(instanceId);
        datum.setGroup(group);
        //no datum set version as mini as
        datum.setVersion(ValueConstants.DEFAULT_NO_DATUM_VERSION);
        datum.setPubMap(new HashMap<>());
        datum.setDataCenter(dataCenter);
        return datum;
    }

    private Map<InetSocketAddress, Map<String, Subscriber>> getPushSubscribers(ScopeEnum scopeEnum,
                                                                               Collection<Subscriber> subscribers) {

        Map<InetSocketAddress, Map<String, Subscriber>> payload = new HashMap<>();

        subscribers.forEach((subscriber) -> {

            if (subscriber.getScope().equals(scopeEnum)) {
                InetSocketAddress address = new InetSocketAddress(subscriber.getSourceAddress().getIpAddress(),
                        subscriber.getSourceAddress().getPort());
                Map<String, Subscriber> map = payload.computeIfAbsent(address, k -> new HashMap<>());
                map.put(subscriber.getRegisterId(), subscriber);
                payload.put(address, map);
            }
        });

        return payload;
    }

    private void fireUserDataPushTaskCloud(String dataInfoId,
                                           Map<String/*dataCenter*/, Datum> datumMap,
                                           Collection<Subscriber> subscribers,
                                           Subscriber subscriber,
                                           TaskListenerManager taskListenerManager) {
        Datum merge = ReceivedDataConverter.getMergeDatum(datumMap);

        if (subscriber.getScope() == ScopeEnum.zone) {
            fireUserDataElementPushTaskCloud(dataInfoId, datumMap, merge, subscribers, subscriber,
                taskListenerManager);
        } else {
            fireUserDataElementMultiPushTaskCloud(dataInfoId, datumMap, merge, subscribers,
                subscriber, taskListenerManager);
        }
    }

    private void fireUserDataElementPushTaskCloud(String dataInfoId, Map<String, Datum> datumMap,
                                                  Datum datum, Collection<Subscriber> subscribers,
                                                  Subscriber subscriber,
                                                  TaskListenerManager taskListenerManager) {
        TaskEvent taskEvent = new TaskEvent(TaskEvent.TaskType.USER_DATA_ELEMENT_PUSH_TASK);

        setupTaskEventForOldClientVersion(dataInfoId, datumMap, datum, subscriber, subscribers,
            taskEvent);

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;
        taskLogger.info("send {} taskURL:{}, dataInfoId={}, dataCenter={}, pubSize={}, subSize={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTaskCloud(String dataInfoId,
                                                       Map<String, Datum> datumMap, Datum datum,
                                                       Collection<Subscriber> subscribers,
                                                       Subscriber subscriber,
                                                       TaskListenerManager taskListenerManager) {
        TaskEvent taskEvent = new TaskEvent(TaskEvent.TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);

        setupTaskEventForOldClientVersion(dataInfoId, datumMap, datum, subscriber, subscribers,
            taskEvent);

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;
        taskLogger.info("send {} taskURL:{}, dataInfoId={}, dataCenter={}, pubSize={}, subSize={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireReceivedDataPushTaskCloud(String dataInfoId,
                                               Map<String/*datacenter*/, Datum> datumMap,
                                               List<String> subscriberRegisterIdList,
                                               Subscriber subscriber,
                                               TaskListenerManager taskListenerManager) {

        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(datumMap,
            subscriber.getScope(), subscriberRegisterIdList, subscriber);

        //trigger push to client node
        TaskEvent taskEvent = createTaskEvent(dataInfoId, datumMap, subscriber, receivedData);
        taskLogger.info("send {} taskURL:{}, taskScope:{}, taskId:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), subscriber.getScope(), taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private TaskEvent createTaskEvent(String dataInfoId, Map<String, Datum> datumMap,
                                      Subscriber subscriber, ReceivedData receivedData) {
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        // setup PUSH_CLIENT_SUBSCRIBERS, which is used in AlipayPushTaskMergeProcessor
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, Lists.newArrayList(subscriber));
        taskEvent.setTaskClosure(getTaskClosureForCloud(dataInfoId, datumMap));
        return taskEvent;
    }

    private void setupTaskEventForOldClientVersion(String dataInfoId, Map<String, Datum> datumMap,
                                                   Datum datum, Subscriber subscriber,
                                                   Collection<Subscriber> subscribers,
                                                   TaskEvent taskEvent) {
        taskEvent.setTaskClosure(getTaskClosureForCloud(dataInfoId, datumMap));
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());
    }

    /**
     * （1）push success：set version zero of remoteDataCentersWhichMissDatum
     * （2）push error：set version zero of allDataCenters;
     */
    public PushTaskClosure getTaskClosureForCloud(String dataInfoId, Map<String/*dataCenter*/, Datum> datumMap) {

        NodeManager nodeManager = NodeManagerFactory.getNodeManager(NodeType.META);
        List<String> allDataCenters = new ArrayList<>(nodeManager.getDataCenters());
        List<String> remoteDataCentersWhichMissDatum = new ArrayList<>(allDataCenters);
        if (datumMap != null && !datumMap.isEmpty()) {
            remoteDataCentersWhichMissDatum.removeAll(datumMap.keySet());
        }

        PushTaskClosure pushTaskClosure = new PushTaskClosure(executorManager.getPushTaskCheckAsyncHashedWheelTimer(),
                sessionServerConfig, dataInfoId);
        pushTaskClosure.setTaskClosure((status, task) -> {

            if (status == ProcessingResult.Success) {
                //（1）push success：if remoteDataCentersWhichMissDatum exists, set version zero of remoteDataCentersWhichMissDatum
                /**
                 * If there are elements left in remoteDataCentersWhichMissDatum, means that datum of the dataCenter,
                 * cannot be obtained from remote dataServer this time, it may cause pushing empty datum wrongly,
                 * so it is necessary to ensure that it can be checked later。
                 * If the data does not exist in the dataCenter exactly, there will be no additional impact, just more memory
                 */
                remoteDataCentersWhichMissDatum.forEach(dataCenter -> {
                    boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter, dataInfoId);
                    LOGGER.warn(
                            "Push done, but datum from DataServer({}) not obtained, so set sessionInterests dataInfoId({}) version zero, return {}",
                            dataCenter, dataInfoId, result);
                });
            } else {
                //（2）push error：set version zero of allDataCenters;
                allDataCenters.forEach(dataCenter -> {
                    boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter, dataInfoId);
                    LOGGER.warn(
                            "Push error, so set sessionInterests dataInfoId({}) of dataServer({}) version zero, return {}",
                            dataInfoId, dataCenter, result);
                });
            }
        });
        return pushTaskClosure;
    }
}
