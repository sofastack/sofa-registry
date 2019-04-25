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
package com.alipay.sofa.registry.server.session.scheduler.task;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Key.KeyType;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.ReSubscribers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataChangeFetchCloudTask.java, v 0.1 2018-03-16 15:28 shangyu.wh Exp $
 */
public class DataChangeFetchCloudTask extends AbstractSessionTask {

    private final static Logger       LOGGER     = LoggerFactory
                                                     .getLogger(DataChangeFetchCloudTask.class);

    private static final Logger       taskLogger = LoggerFactory.getLogger(
                                                     DataChangeFetchCloudTask.class, "[Task]");

    private final SessionServerConfig sessionServerConfig;

    private Interests                 sessionInterests;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    private final TaskListenerManager taskListenerManager;

    private final ExecutorManager     executorManager;

    private String                    fetchDataInfoId;

    private final CacheService        sessionCacheService;

    public DataChangeFetchCloudTask(SessionServerConfig sessionServerConfig,
                                    TaskListenerManager taskListenerManager,
                                    Interests sessionInterests, ExecutorManager executorManager,
                                    CacheService sessionCacheService) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
        this.sessionInterests = sessionInterests;
        this.executorManager = executorManager;
        this.sessionCacheService = sessionCacheService;
    }

    @Override
    public long getExpiryTime() {
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof String)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.fetchDataInfoId = (String) obj;
    }

    @Override
    public void execute() {

        Map<String/*dataCenter*/, Datum> datumMap = getDatumsCache();

        if (datumMap != null && !datumMap.isEmpty()) {

            PushTaskClosure pushTaskClosure = getTaskClosure(datumMap);

            for (ScopeEnum scopeEnum : ScopeEnum.values()) {
                Map<InetSocketAddress, Map<String, Subscriber>> map = getCache(fetchDataInfoId,
                    scopeEnum);
                if (map != null && !map.isEmpty()) {
                    for (Entry<InetSocketAddress, Map<String, Subscriber>> entry : map.entrySet()) {
                        Map<String, Subscriber> subscriberMap = entry.getValue();
                        if (subscriberMap != null && !subscriberMap.isEmpty()) {
                            List<String> subscriberRegisterIdList = new ArrayList<>(
                                subscriberMap.keySet());

                            //select one row decide common info
                            Subscriber subscriber = subscriberMap.values().iterator().next();

                            //remove stopPush subscriber avoid push duplicate
                            evictReSubscribers(subscriberMap.values());

                            fireReceivedDataMultiPushTask(datumMap, subscriberRegisterIdList,
                                scopeEnum, subscriber, subscriberMap, pushTaskClosure);
                        }
                    }
                }
            }

            pushTaskClosure.start();
        } else {
            LOGGER.error("Get publisher data error,which dataInfoId:{}", fetchDataInfoId);
        }
    }

    public PushTaskClosure getTaskClosure(Map<String/*dataCenter*/, Datum> datumMap) {
        PushTaskClosure pushTaskClosure = new PushTaskClosure(executorManager.getPushTaskClosureExecutor());
        pushTaskClosure.setTaskClosure((status, task) -> {
            if (status == ProcessingResult.Success) {
                if (sessionServerConfig.isStopPushSwitch()) {
                    LOGGER.info("Stop Push switch on,dataInfoId {} version can not be update!", fetchDataInfoId);
                    return;
                }
                datumMap.forEach((dataCenter, datum) -> {
                    String dataInfoId = fetchDataInfoId;
                    Long version = datum.getVersion();
                    boolean result = sessionInterests.checkAndUpdateInterestVersions(dataCenter, dataInfoId, version);
                    if (result) {
                        LOGGER.info("Push all tasks success,dataCenter:{} dataInfoId:{} version:{} update!", dataCenter,
                                dataInfoId, version);
                    } else {
                        LOGGER.info(
                                "Push all tasks success,but dataCenter:{} dataInfoId:{} version:{} need not update!",
                                dataCenter,
                                dataInfoId, version);
                    }
                });
            } else {
                LOGGER.warn("Push tasks found error,subscribers version can not be update!dataInfoId={}",
                        fetchDataInfoId);
            }
        });
        return pushTaskClosure;
    }

    private void evictReSubscribers(Collection<Subscriber> subscribersPush) {
        if (this.sessionInterests instanceof ReSubscribers) {
            ReSubscribers reSubscribers = (ReSubscribers) sessionInterests;
            subscribersPush.forEach(reSubscribers::deleteReSubscriber);
        }
    }

    private Map<InetSocketAddress, Map<String, Subscriber>> getCache(String dataInfoId,
                                                                     ScopeEnum scopeEnum) {

        return sessionInterests.querySubscriberIndex(dataInfoId, scopeEnum);
    }

    private Map<String, Datum> getDatumsCache() {

        Map<String, Datum> map = new HashMap<>();
        NodeManager nodeManager = NodeManagerFactory.getNodeManager(NodeType.META);
        Collection<String> dataCenters = nodeManager.getDataCenters();
        if (dataCenters != null) {
            Collection<Key> keys = dataCenters.stream().
                    map(dataCenter -> new Key(KeyType.OBJ, DatumKey.class.getName(),
                            new DatumKey(fetchDataInfoId, dataCenter))).
                    collect(Collectors.toList());

            Map<Key, Value> values = sessionCacheService.getValues(keys);

            if (values != null) {
                values.forEach((key, value) -> {
                    if (value != null && value.getPayload() != null) {
                        map.put(((DatumKey) key.getEntityType()).getDataCenter(), (Datum) value.getPayload());
                    }
                });
            }

        }
        return map;
    }

    private void fireReceivedDataMultiPushTask(Map<String, Datum> datums,
                                               List<String> subscriberRegisterIdList,
                                               ScopeEnum scopeEnum, Subscriber subscriber,
                                               Map<String, Subscriber> subscriberMap,
                                               PushTaskClosure pushTaskClosure) {
        boolean isOldVersion = !ClientVersion.StoreData.equals(subscriber.getClientVersion());
        if (!isOldVersion) {
            fireReceiveDataPushTask(datums, subscriberRegisterIdList, scopeEnum, subscriber,
                subscriberMap, pushTaskClosure);
        } else {
            if (subscriber.getScope() == ScopeEnum.zone) {
                fireUserDataElementPushTask(ReceivedDataConverter.getMergeDatum(datums),
                    subscriber, subscriberMap, pushTaskClosure);
            } else {
                fireUserDataElementMultiPushTask(ReceivedDataConverter.getMergeDatum(datums),
                    subscriber, subscriberMap, pushTaskClosure);
            }
        }
    }

    private void fireReceiveDataPushTask(Map<String, Datum> datums,
                                         List<String> subscriberRegisterIdList,
                                         ScopeEnum scopeEnum, Subscriber subscriber,
                                         Map<String, Subscriber> subscriberMap,
                                         PushTaskClosure pushTaskClosure) {
        Collection<Subscriber> subscribers = new ArrayList<>(subscriberMap.values());
        LOGGER.info("Datums push={}", datums);
        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(datums, scopeEnum,
            subscriberRegisterIdList, subscriber);

        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        taskEvent.setTaskClosure(pushTaskClosure);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskLogger.info("send {} taskURL:{},taskScope:{},version:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), scopeEnum, receivedData.getVersion());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementPushTask(Datum datum, Subscriber subscriber,
                                             Map<String, Subscriber> subscriberMap,
                                             PushTaskClosure pushTaskClosure) {

        Collection<Subscriber> subscribers = new ArrayList<>(subscriberMap.values());

        TaskEvent taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_PUSH_TASK);
        taskEvent.setTaskClosure(pushTaskClosure);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());

        int size = datum != null && datum.getPubMap() != null ? datum.getPubMap().size() : 0;

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTask(Datum datum, Subscriber subscriber,
                                                  Map<String, Subscriber> subscriberMap,
                                                  PushTaskClosure pushTaskClosure) {
        Collection<Subscriber> subscribers = new ArrayList<>(subscriberMap.values());

        TaskEvent taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);
        taskEvent.setTaskClosure(pushTaskClosure);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());

        int size = datum != null && datum.getPubMap() != null ? datum.getPubMap().size() : 0;

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), subscriber.getSourceAddress(), datum.getDataInfoId(),
            datum.getDataCenter(), size, subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getDataChangeFetchTaskRetryTimes());
    }

    @Override
    public String toString() {
        return "DATA_CHANGE_FETCH_CLOUD_TASK{" + "taskId='" + getTaskId() + '\''
               + ", fetchDataInfoId=" + fetchDataInfoId + ", expiryTime='" + getExpiryTime() + '\''
               + '}';
    }
}