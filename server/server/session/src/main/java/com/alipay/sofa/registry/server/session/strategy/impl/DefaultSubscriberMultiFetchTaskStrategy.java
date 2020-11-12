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

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.*;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.strategy.SubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.utils.DatumUtils;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberMultiFetchTaskStrategy implements SubscriberMultiFetchTaskStrategy {
    private static final Logger taskLogger = LoggerFactory.getLogger(
                                               DefaultSubscriberMultiFetchTaskStrategy.class,
                                               "[Task]");

    private static final Logger LOGGER     = LoggerFactory
                                               .getLogger(DefaultSubscriberMultiFetchTaskStrategy.class);

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private MetaServerService   metaServerService;

    @Override
    public void doSubscriberMultiFetchTask(SessionServerConfig sessionServerConfig,
                                           TaskListenerManager taskListenerManager,
                                           CacheService sessionCacheService,
                                           String fetchDataInfoId,
                                           Collection<Subscriber> subscribers) {
        Map<String/*dataCenter*/, Datum> datumMap = getDatumsCache(fetchDataInfoId,
            sessionCacheService);

        for (ScopeEnum scopeEnum : ScopeEnum.values()) {
            Map<InetSocketAddress, Map<String, Subscriber>> map = getPushSubscribers(scopeEnum,
                subscribers);

            if (map != null && !map.isEmpty()) {
                for (Map.Entry<InetSocketAddress, Map<String, Subscriber>> entry : map.entrySet()) {
                    Map<String, Subscriber> subscriberMap = entry.getValue();
                    if (subscriberMap != null && !subscriberMap.isEmpty()) {
                        Subscriber subscriber = subscriberMap.values().iterator().next();
                        boolean isOldVersion = !BaseInfo.ClientVersion.StoreData.equals(subscriber
                            .getClientVersion());

                        if (isOldVersion) {
                            fireUserDataPushTaskCloud(entry.getKey(), datumMap,
                                subscriberMap.values(), subscriber, taskListenerManager);
                        } else {
                            fireReceivedDataPushTaskCloud(datumMap,
                                new ArrayList(subscriberMap.keySet()), subscriber,
                                taskListenerManager);
                        }
                    }
                }
            }
        }
    }

    private Map<String, Datum> getDatumsCache(String fetchDataInfoId, CacheService sessionCacheService) {

        Map<String, Datum> map = new HashMap<>();
        Collection<String> dataCenters = metaServerService.getDataCenters();
        if (dataCenters != null) {
            Collection<Key> keys = dataCenters.stream().
                    map(dataCenter -> new Key(Key.KeyType.OBJ, DatumKey.class.getName(),
                            new DatumKey(fetchDataInfoId, dataCenter))).
                    collect(Collectors.toList());

            Map<Key, Value> values = null;
            try {
                values = sessionCacheService.getValues(keys);
            } catch (CacheAccessException e) {
                // The version is set to 0, so that when session checks the datum versions regularly, it will actively re-query the data.
                for (String dataCenter : dataCenters) {
                    boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter, fetchDataInfoId);
                    LOGGER.error(String.format(
                            "error when access cache, so checkAndUpdateInterestVersionZero(return %s): %s", result,
                            e.getMessage()), e);
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
        return map;
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

    private void fireUserDataPushTaskCloud(InetSocketAddress address,
                                           Map<String/*dataCenter*/, Datum> datumMap,
                                           Collection<Subscriber> subscribers,
                                           Subscriber subscriber,
                                           TaskListenerManager taskListenerManager) {
        Datum merge = null;
        if (datumMap != null && !datumMap.isEmpty()) {
            merge = ReceivedDataConverter.getMergeDatum(datumMap);
        }

        ScopeEnum scopeEnum = subscriber.getScope();

        if (scopeEnum == ScopeEnum.zone) {
            fireUserDataElementPushTask(address, merge, subscribers, subscriber,
                taskListenerManager);
        } else {
            fireUserDataElementMultiPushTask(address, merge, subscribers, subscriber,
                taskListenerManager);
        }
    }

    private void fireUserDataElementPushTask(InetSocketAddress address, Datum datum,
                                             Collection<Subscriber> subscribers,
                                             Subscriber subscriber,
                                             TaskListenerManager taskListenerManager) {
        datum = DatumUtils.newDatumIfNull(datum, subscriber);
        TaskEvent taskEvent = new TaskEvent(TaskEvent.TaskType.USER_DATA_ELEMENT_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, new URL(address));

        int size = datum.publisherSize();

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), address, datum.getDataInfoId(), datum.getDataCenter(), size,
            subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTask(InetSocketAddress address, Datum datum,
                                                  Collection<Subscriber> subscribers,
                                                  Subscriber subscriber,
                                                  TaskListenerManager taskListenerManager) {
        datum = DatumUtils.newDatumIfNull(datum, subscriber);
        TaskEvent taskEvent = new TaskEvent(TaskEvent.TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, new URL(address));

        int size = datum.publisherSize();

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), address, datum.getDataInfoId(), datum.getDataCenter(), size,
            subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireReceivedDataPushTaskCloud(Map<String/*datacenter*/, Datum> datumMap,
                                               List<String> subscriberRegisterIdList,
                                               Subscriber subscriber,
                                               TaskListenerManager taskListenerManager) {

        ReceivedData receivedData;
        if (datumMap != null && !datumMap.isEmpty()) {

            receivedData = ReceivedDataConverter.getReceivedDataMulti(datumMap,
                subscriber.getScope(), subscriberRegisterIdList, subscriber);

        }
        //no datum
        else {
            receivedData = ReceivedDataConverter.getReceivedDataMulti(subscriber.getDataId(),
                subscriber.getGroup(), subscriber.getInstanceId(),
                ValueConstants.DEFAULT_DATA_CENTER, subscriber.getScope(),
                subscriberRegisterIdList, subscriber.getCell());

        }

        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter,
            TaskEvent.TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        // setup PUSH_CLIENT_SUBSCRIBERS, which is used in AlipayPushTaskMergeProcessor
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, Lists.newArrayList(subscriber));
        taskLogger.info("send {} taskURL:{},taskScope:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), subscriber.getScope());
        taskListenerManager.sendTaskEvent(taskEvent);
    }
}
