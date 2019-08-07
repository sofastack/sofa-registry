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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.NotifyProvideDataChange;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistConstants;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManager;
import com.alipay.sofa.registry.server.session.node.service.MetaNodeService;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.ReSubscribers;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTask.java, v 0.1 2017-12-07 16:23 shangyu.wh Exp $
 */
public class ProvideDataChangeFetchTask extends AbstractSessionTask {

    private static final Logger       TASK_LOGGER = LoggerFactory.getLogger(
                                                      ProvideDataChangeFetchTask.class, "[Task]");

    private static final Logger       LOGGER      = LoggerFactory
                                                      .getLogger(ProvideDataChangeFetchTask.class);

    private final SessionServerConfig sessionServerConfig;
    /**
     * trigger push client process
     */
    private final TaskListenerManager taskListenerManager;
    /**
     * Meta Node service
     */
    private final MetaNodeService     metaNodeService;

    private final Watchers            sessionWatchers;

    private final Exchange            boltExchange;

    private final Interests           sessionInterests;

    private final Registry            sessionRegistry;

    private final BlacklistManager    blacklistManager;

    private NotifyProvideDataChange   notifyProvideDataChange;

    public ProvideDataChangeFetchTask(SessionServerConfig sessionServerConfig,
                                      TaskListenerManager taskListenerManager,
                                      MetaNodeService metaNodeService, Watchers sessionWatchers,
                                      Exchange boltExchange, Interests sessionInterests,
                                      Registry sessionRegistry, BlacklistManager blacklistManager) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
        this.metaNodeService = metaNodeService;
        this.sessionWatchers = sessionWatchers;
        this.boltExchange = boltExchange;
        this.sessionInterests = sessionInterests;
        this.sessionRegistry = sessionRegistry;
        this.blacklistManager = blacklistManager;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof NotifyProvideDataChange)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.notifyProvideDataChange = (NotifyProvideDataChange) obj;
    }

    @Override
    public void execute() {

        ProvideData provideData = null;
        String dataInfoId = notifyProvideDataChange.getDataInfoId();
        if (notifyProvideDataChange.getDataOperator() != DataOperator.REMOVE) {
            provideData = metaNodeService.fetchData(dataInfoId);

            if (ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID.equals(dataInfoId)) {
                //push stop switch
                if (provideData != null) {
                    if (provideData.getProvideData() == null || provideData.getProvideData().getObject() == null) {
                        LOGGER.info("Fetch session stop push switch no data existed,config not change!");
                        return;
                    }
                    String data = (String) provideData.getProvideData().getObject();
                    LOGGER.info("Fetch session stop push data switch {} success!", data);

                    //receive stop push switch off
                    if (data != null) {
                        boolean switchData = Boolean.valueOf(data);
                        boolean ifChange = sessionServerConfig.isStopPushSwitch() != switchData;
                        sessionServerConfig.setStopPushSwitch(switchData);
                        if (!switchData) {
                            //avoid duplicate false receive
                            if (ifChange) {
                                fireReSubscriber();
                            }
                        } else {
                            //stop push and stop fetch data task
                            sessionServerConfig.setBeginDataFetchTask(false);
                        }
                    } else {
                        LOGGER.error("Fetch session stop push data switch is null!");
                    }
                    return;
                } else {
                    LOGGER.info("Fetch session stop push switch data null,config not change!");
                }
                return;
            }else if(ValueConstants.BLACK_LIST_DATA_ID.equals(dataInfoId)){
                //black list data
                if (provideData.getProvideData() == null
                        || provideData.getProvideData().getObject() == null) {
                    LOGGER.info("Fetch session blacklist no data existed,current config not change!");
                    return;
                }
                String data = (String) provideData.getProvideData().getObject();
                if (data != null) {
                    Map<String, Map<String, Set<String>>> blacklistConfigMap = blacklistManager.convertBlacklistConfig(data);
                    clientOffBlackIp(blacklistConfigMap);
                    LOGGER.info("Fetch session blacklist data switch {} success!", data);
                }else {
                    LOGGER.info("Fetch session blacklist data null,current config not change!");
                }
                return;
            }
            if (provideData == null) {
                LOGGER.warn("Notify provider data Change request {} fetch no provider data!", notifyProvideDataChange);
                return;
            }
        }
        DataInfo dataInfo = DataInfo.valueOf(dataInfoId);

        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
        if (sessionServer != null) {
            for (Channel channel : sessionServer.getChannels()) {

                //filter all connect client has watcher registerId
                String connectId = NetUtil.toAddressString(channel.getRemoteAddress());
                Map<String, Watcher> map = getCache(connectId);
                List<String> registerIds = new ArrayList<>();
                map.forEach((registerId, watchers) -> {
                    if (watchers != null && watchers.getDataInfoId().equals(dataInfoId)) {
                        registerIds.add(registerId);
                    }
                });
                if (!registerIds.isEmpty()) {
                    ReceivedConfigData receivedConfigData;
                    if (notifyProvideDataChange.getDataOperator() == DataOperator.REMOVE) {
                        receivedConfigData = ReceivedDataConverter.getReceivedConfigData(null, dataInfo,
                                notifyProvideDataChange.getVersion());
                    } else {
                        receivedConfigData = ReceivedDataConverter.getReceivedConfigData(
                                provideData.getProvideData(), dataInfo, provideData.getVersion());
                    }
                    receivedConfigData.setConfiguratorRegistIds(registerIds);
                    firePushTask(receivedConfigData, new URL(channel.getRemoteAddress()));
                }
            }
        }
    }

    /**
     * open push switch to push all reSubscribers
     */
    private void fireReSubscriber() {

        //try catch avoid to error cancel beginDataFetchTask switch on
        try {
            //begin push fire data fetch task first,avoid reSubscriber push duplicate
            sessionRegistry.fetchChangDataProcess();
        } catch (Throwable e) {
            LOGGER.error("Open push switch first fetch task execute error",e);
        }

        try {
            //wait 1 MINUTES for dataFetch task evict duplicate subscriber push
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("Wait for dataFetch Task Interrupted!");
        }

        //fetch task process 1 minutes,can schedule execute fetch task
        sessionServerConfig.setBeginDataFetchTask(true);

        if (this.sessionInterests instanceof ReSubscribers) {
            ReSubscribers reSubscriber = (ReSubscribers) sessionInterests;

            Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>> reSubscribers = reSubscriber
                    .getReSubscribers();

            if (reSubscribers != null && !reSubscribers.isEmpty()) {
                reSubscribers.forEach(
                        (dataInfoId, subscribers) -> fireSubscriberMultiFetchTask(dataInfoId, subscribers.values()));
                reSubscriber.clearReSubscribers();
            }
        }
    }

    private void fireSubscriberMultiFetchTask(String dataInfoId, Collection<Subscriber> subscribers) {
        //trigger fetch data for subscriber,and push to client node
        if (!CollectionUtils.isEmpty(subscribers)) {
            TaskEvent taskEvent = new TaskEvent(dataInfoId, TaskType.SUBSCRIBER_MULTI_FETCH_TASK);
            taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
            TASK_LOGGER.info("send " + taskEvent.getTaskType()
                             + " subscribersSize:{},dataInfoId:{}", subscribers.size(), dataInfoId);
            taskListenerManager.sendTaskEvent(taskEvent);
        }
    }

    private void firePushTask(ReceivedConfigData receivedConfigData, URL clientUrl) {

        Map<ReceivedConfigData, URL> parameter = new HashMap<>();
        parameter.put(receivedConfigData, clientUrl);
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_CONFIG_PUSH_TASK);
        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " taskEvent:{}", taskEvent);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void clientOffBlackIp(Map<String, Map<String, Set<String>>> blacklistConfigMap) {

        if (blacklistConfigMap != null) {
            Set<String> ipSet = new HashSet();

            for (Map.Entry<String, Map<String, Set<String>>> configEntry : blacklistConfigMap
                .entrySet()) {
                if (BlacklistConstants.FORBIDDEN_PUB.equals(configEntry.getKey())
                    || BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX.equals(configEntry.getKey())) {
                    Map<String, Set<String>> typeMap = configEntry.getValue();
                    if (typeMap != null) {
                        for (Map.Entry<String, Set<String>> typeEntry : typeMap.entrySet()) {
                            if (BlacklistConstants.IP_FULL.equals(typeEntry.getKey())) {
                                if (typeEntry.getValue() != null) {
                                    ipSet.addAll(typeEntry.getValue());
                                }
                            }
                        }
                    }
                }

            }

            sessionRegistry.remove(getIpConnects(ipSet));
        }
    }

    public List<String> getIpConnects(Set<String> _ipList) {

        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

        List<String> connections = new ArrayList<>();

        if (sessionServer != null) {
            Collection<Channel> channels = sessionServer.getChannels();
            for (Channel channel : channels) {
                String key = NetUtil.toAddressString(channel.getRemoteAddress());
                String ip = key.substring(0, key.indexOf(":"));
                if (_ipList.contains(ip)) {
                    connections.add(key);
                }
            }
        }

        return connections;
    }

    private Map<String/*registerId*/, Watcher> getCache(String connectId) {
        Map<String/*registerId*/, Watcher> map = sessionWatchers.queryByConnectId(connectId);
        return map == null ? new ConcurrentHashMap<>() : map;
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getSubscriberRegisterFetchRetryTimes());
    }

    @Override
    public String toString() {
        return "PROVIDE_DATA_CHANGE_FETCH_TASK{" + "taskId='" + getTaskId() + '\''
               + ", notifyProvideDataChange=" + notifyProvideDataChange + ", retryTimes='"
               + sessionServerConfig.getSubscriberRegisterFetchRetryTimes() + '\'' + '}';
    }
}