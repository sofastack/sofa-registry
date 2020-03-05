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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.server.session.strategy.SubscriberRegisterFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.utils.DatumUtils;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberRegisterFetchTaskStrategy implements
                                                       SubscriberRegisterFetchTaskStrategy {

    private static final Logger taskLogger = LoggerFactory.getLogger(
                                               DefaultSubscriberRegisterFetchTaskStrategy.class,
                                               "[Task]");

    @Override
    public void doSubscriberRegisterFetchTask(SessionServerConfig sessionServerConfig,
                                              TaskListenerManager taskListenerManager,
                                              DataNodeService dataNodeService,
                                              CacheService sessionCacheService,
                                              Subscriber subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber can not be null!");
        }

        List<String> subscriberRegisterIdList = Collections.singletonList(subscriber
            .getRegisterId());

        boolean isOldVersion = !BaseInfo.ClientVersion.StoreData.equals(subscriber
            .getClientVersion());

        Map<String/*datacenter*/, Datum> datumMap = dataNodeService.fetchGlobal(subscriber
            .getDataInfoId());
        NodeManager nodeManager = NodeManagerFactory.getNodeManager(NodeType.META);
        List<String> remoteDataCentersWhichMissDatum = new ArrayList<>(nodeManager.getDataCenters());
        if (datumMap != null && !datumMap.isEmpty()) {
            remoteDataCentersWhichMissDatum.removeAll(datumMap.keySet());
        }

        if (!isOldVersion) {
            fireReceivedDataPushTaskCloud(datumMap, subscriberRegisterIdList, subscriber,
                taskListenerManager, remoteDataCentersWhichMissDatum);
        } else {
            fireUserDataPushTaskCloud(datumMap, subscriber, taskListenerManager,
                remoteDataCentersWhichMissDatum);
        }
    }

    private void fireReceivedDataPushTaskCloud(Map<String/*datacenter*/, Datum> datumMap,
                                               List<String> subscriberRegisterIdList,
                                               Subscriber subscriber,
                                               TaskListenerManager taskListenerManager,
                                               List<String> remoteDataCentersWhichMissDatum) {
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

        firePush(receivedData, subscriber, taskListenerManager, remoteDataCentersWhichMissDatum);
    }

    private void firePush(ReceivedData receivedData, Subscriber subscriber,
                          TaskListenerManager taskListenerManager,
                          List<String> remoteDataCentersWhichMissDatum) {
        //trigger push to client node
        TaskEvent taskEvent = new TaskEvent(TaskEvent.TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_RECEIVED_DATA, receivedData);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());
        taskEvent.setAttribute(Constant.PUSH_CLIENT_REMOTE_DATACENTERS_WHICH_MISS_DATUM,
            remoteDataCentersWhichMissDatum);

        taskLogger.info("send {} taskURL:{},taskScope:{},taskId:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), receivedData.getScope(), taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataPushTaskCloud(Map<String, Datum> datumMap, Subscriber subscriber,
                                           TaskListenerManager taskListenerManager,
                                           List<String> remoteDataCentersWhichMissDatum) {
        Datum merge = null;
        if (datumMap != null && !datumMap.isEmpty()) {
            merge = ReceivedDataConverter.getMergeDatum(datumMap);
        }

        if (subscriber.getScope() == ScopeEnum.zone) {
            fireUserDataElementPushTask(merge, subscriber, taskListenerManager,
                remoteDataCentersWhichMissDatum);
        } else {
            fireUserDataElementMultiPushTask(merge, subscriber, taskListenerManager,
                remoteDataCentersWhichMissDatum);
        }
    }

    private void fireUserDataElementPushTask(Datum datum, Subscriber subscriber,
                                             TaskListenerManager taskListenerManager,
                                             List<String> remoteDataCentersWhichMissDatum) {
        datum = DatumUtils.newDatumIfNull(datum, subscriber);
        Collection<Subscriber> subscribers = new ArrayList<>();
        subscribers.add(subscriber);

        TaskEvent taskEvent = new TaskEvent(subscriber,
            TaskEvent.TaskType.USER_DATA_ELEMENT_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());
        taskEvent.setAttribute(Constant.PUSH_CLIENT_REMOTE_DATACENTERS_WHICH_MISS_DATUM,
            remoteDataCentersWhichMissDatum);

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;
        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},taskId={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTask(Datum datum, Subscriber subscriber,
                                                  TaskListenerManager taskListenerManager,
                                                  List<String> remoteDataCentersWhichMissDatum) {
        datum = DatumUtils.newDatumIfNull(datum, subscriber);
        Collection<Subscriber> subscribers = new ArrayList<>();
        subscribers.add(subscriber);

        TaskEvent taskEvent = new TaskEvent(subscriber,
            TaskEvent.TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());
        taskEvent.setAttribute(Constant.PUSH_CLIENT_REMOTE_DATACENTERS_WHICH_MISS_DATUM,
            remoteDataCentersWhichMissDatum);

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},taskId={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }
}
