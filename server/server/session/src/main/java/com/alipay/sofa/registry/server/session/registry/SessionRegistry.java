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
package com.alipay.sofa.registry.server.session.registry;

import com.alipay.sofa.registry.common.model.ConnectId;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.acceptor.ClientOffWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.PublisherWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.filter.DataIdMatchStrategy;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.server.session.wrapper.Wrapper;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptorManager;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.google.common.collect.Lists;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionRegistry.java, v 0.1 2017-11-30 18:13 shangyu.wh Exp $
 */
public class SessionRegistry implements Registry {

    private static final Logger       LOGGER                  = LoggerFactory
                                                                  .getLogger(SessionRegistry.class);

    protected static final Logger     TASK_LOGGER             = LoggerFactory.getLogger(
                                                                  SessionRegistry.class, "[Task]");

    /**
     * store subscribers
     */
    @Autowired
    private Interests                 sessionInterests;

    /**
     * store watchers
     */
    @Autowired
    private Watchers                  sessionWatchers;

    /**
     * store publishers
     */
    @Autowired
    private DataStore                 sessionDataStore;

    /**
     * transfer data to DataNode
     */
    @Autowired
    private DataNodeService           dataNodeService;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    @Autowired
    private TaskListenerManager       taskListenerManager;

    @Autowired
    private SessionServerConfig       sessionServerConfig;

    @Autowired
    private Exchange                  boltExchange;

    @Autowired
    private SessionRegistryStrategy   sessionRegistryStrategy;

    @Autowired
    private WrapperInterceptorManager wrapperInterceptorManager;

    @Autowired
    private DataIdMatchStrategy       dataIdMatchStrategy;

    @Autowired
    private WriteDataAcceptor         writeDataAcceptor;

    @Autowired
    private SlotTableCache            slotTableCache;

    @Autowired
    private AppRevisionCacheRegistry  appRevisionCacheRegistry;

    private volatile boolean          enableDataRenewSnapshot = true;

    @Override
    public void register(StoreData storeData) {

        WrapperInvocation<StoreData, Boolean> wrapperInvocation = new WrapperInvocation(
                new Wrapper<StoreData, Boolean>() {
                    @Override
                    public Boolean call() {

                        switch (storeData.getDataType()) {
                            case PUBLISHER:
                                Publisher publisher = (Publisher) storeData;
                                publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
                                sessionDataStore.add(publisher);

                                // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                                // are handed over to WriteDataAcceptor
                                writeDataAcceptor.accept(new PublisherWriteDataRequest(publisher,
                                        WriteDataRequest.WriteDataRequestType.PUBLISHER,
                                        slotTableCache.getLeader(publisher.getDataInfoId())));

                                sessionRegistryStrategy.afterPublisherRegister(publisher);
                                break;
                            case SUBSCRIBER:
                                Subscriber subscriber = (Subscriber) storeData;

                                sessionInterests.add(subscriber);

                                sessionRegistryStrategy.afterSubscriberRegister(subscriber);
                                break;
                            case WATCHER:
                                Watcher watcher = (Watcher) storeData;

                                sessionWatchers.add(watcher);

                                sessionRegistryStrategy.afterWatcherRegister(watcher);
                                break;
                            default:
                                break;
                        }
                        return null;
                    }

                    @Override
                    public Supplier<StoreData> getParameterSupplier() {
                        return () -> storeData;
                    }

                }, wrapperInterceptorManager);

        try {
            wrapperInvocation.proceed();
        } catch (Exception e) {
            throw new RuntimeException("Proceed register error!", e);
        }

    }

    @Override
    public void unRegister(StoreData<String> storeData) {

        switch (storeData.getDataType()) {
            case PUBLISHER:
                Publisher publisher = (Publisher) storeData;
                publisher.setSessionProcessId(ServerEnv.PROCESS_ID);

                sessionDataStore.deleteById(storeData.getId(), publisher.getDataInfoId());

                // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                // are handed over to WriteDataAcceptor
                writeDataAcceptor.accept(new PublisherWriteDataRequest(publisher,
                    WriteDataRequest.WriteDataRequestType.UN_PUBLISHER, slotTableCache
                        .getLeader(publisher.getDataInfoId())));

                sessionRegistryStrategy.afterPublisherUnRegister(publisher);
                break;

            case SUBSCRIBER:
                Subscriber subscriber = (Subscriber) storeData;
                sessionInterests.deleteById(storeData.getId(), subscriber.getDataInfoId());
                sessionRegistryStrategy.afterSubscriberUnRegister(subscriber);
                break;

            case WATCHER:
                Watcher watcher = (Watcher) storeData;

                sessionWatchers.deleteById(watcher.getId(), watcher.getDataInfoId());

                sessionRegistryStrategy.afterWatcherUnRegister(watcher);
                break;
            default:
                break;
        }

    }

    @Override
    public void cancel(List<ConnectId> connectIds) {
        //update local firstly, data node send error depend on renew check
        List<ConnectId> connectIdsWithPub = removeFromSession(connectIds);
        // clientOff to dataNode async
        clientOffToDataNode(connectIdsWithPub);
    }

    private List<ConnectId> removeFromSession(List<ConnectId> connectIds) {
        List<ConnectId> connectIdsWithPub = Lists.newArrayList();
        for (ConnectId connectId : connectIds) {
            if (sessionDataStore.deleteByConnectId(connectId)) {
                connectIdsWithPub.add(connectId);
            }
            sessionInterests.deleteByConnectId(connectId);
            sessionWatchers.deleteByConnectId(connectId);
        }
        return connectIdsWithPub;
    }

    private void clientOffToDataNode(List<ConnectId> connectIdsWithPub) {
        // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
        // are handed over to WriteDataAcceptor
        for (ConnectId connectId : connectIdsWithPub) {
            writeDataAcceptor.accept(new ClientOffWriteDataRequest(connectId));
        }
    }

    @Override
    public void fetchChangData() {
        if (!sessionServerConfig.isBeginDataFetchTask()) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("fetchChangData task sleep InterruptedException", e);
            }
            return;
        }

        fetchChangDataProcess();
    }

    @Override
    public void fetchChangDataProcess() {
        //check dataInfoId's sub list is not empty
        List<String> checkDataInfoIds = new ArrayList<>();
        sessionInterests.getInterestDataInfoIds().forEach((dataInfoId) -> {
            Collection<Subscriber> subscribers = sessionInterests.getInterests(dataInfoId);
            if (subscribers != null && !subscribers.isEmpty()) {
                checkDataInfoIds.add(dataInfoId);
            }
        });
        Set<String> fetchDataInfoIds = new HashSet<>();

        for (String dataInfoId : checkDataInfoIds) {
            fetchDataInfoIds.add(dataInfoId);
            fetchDataInfoIds.addAll(appRevisionCacheRegistry.getAppRevisions(dataInfoId).keySet());
        }

        LOGGER.info("[fetchChangDataProcess] Fetch data versions for {} dataInfoIds",
                fetchDataInfoIds.size());

        Map<String/*address*/, Collection<String>/*dataInfoIds*/> map = calculateDataNode(
                fetchDataInfoIds);

        map.forEach((address, dataInfoIds) -> {

            //TODO asynchronous fetch version
            Map<String/*datacenter*/, Map<String/*datainfoid*/, Long>> dataVersions = dataNodeService
                    .fetchDataVersion(URL.valueOf(address), dataInfoIds);

            if (dataVersions != null) {
                sessionRegistryStrategy.doFetchChangDataProcess(dataVersions);
            } else {
                LOGGER.warn("Fetch no change data versions info from {}", address);
            }
        });

    }

    private Map<String, Collection<String>> calculateDataNode(Collection<String> dataInfoIds) {

        Map<String, Collection<String>> map = new HashMap<>();
        if (dataInfoIds != null) {
            dataInfoIds.forEach(dataInfoId -> {
                URL url = new URL(slotTableCache.getLeader(dataInfoId), sessionServerConfig.getDataServerPort());
                Collection<String> list = map.computeIfAbsent(url.getAddressString(), k -> new ArrayList<>());
                list.add(dataInfoId);
            });
        }

        return map;
    }

    public void remove(List<ConnectId> connectIds) {

        List<ConnectId> connectIdsAll = new ArrayList<>();
        connectIds.forEach(connectId -> {
            Map pubMap = getSessionDataStore().queryByConnectId(connectId);
            boolean pubExisted = pubMap != null && !pubMap.isEmpty();

            Map<String, Subscriber> subMap = getSessionInterests().queryByConnectId(connectId);
            boolean subExisted = false;
            if (subMap != null && !subMap.isEmpty()) {
                subExisted = true;

                subMap.forEach((registerId, sub) -> {
                    if (isFireSubscriberPushEmptyTask(sub.getDataId())) {
                        fireSubscriberPushEmptyTask(sub);
                    }
                });
            }

            if (pubExisted || subExisted) {
                connectIdsAll.add(connectId);
            }
        });
        cancel(connectIdsAll);
    }

    protected boolean isFireSubscriberPushEmptyTask(String dataId) {
        return dataIdMatchStrategy.match(dataId, () -> sessionServerConfig.getBlacklistSubDataIdRegex());
    }

    private void fireSubscriberPushEmptyTask(Subscriber subscriber) {
        //trigger empty data push
        TaskEvent taskEvent = new TaskEvent(subscriber,
            TaskEvent.TaskType.SUBSCRIBER_PUSH_EMPTY_TASK);
        TASK_LOGGER.info("send " + taskEvent.getTaskType() + " taskEvent:{}", taskEvent);
        getTaskListenerManager().sendTaskEvent(taskEvent);
    }

    public void cleanClientConnect() {

        Set<ConnectId> connectIndexes = new HashSet<>();
        Set<ConnectId> pubIndexes = sessionDataStore.getConnectIds();
        Set<ConnectId> subIndexes = sessionInterests.getConnectIds();
        Set<ConnectId> watchIndexes = sessionWatchers.getConnectIds();
        connectIndexes.addAll(pubIndexes);
        connectIndexes.addAll(subIndexes);
        connectIndexes.addAll(watchIndexes);

        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

        List<ConnectId> connectIds = new ArrayList<>();
        for (ConnectId connectId : connectIndexes) {
            Channel channel = sessionServer.getChannel(new URL(connectId.getClientHostAddress(),
                connectId.getClientPort()));
            if (channel == null) {
                connectIds.add(connectId);
                LOGGER.warn("Client connect has not existed!it must be remove!connectId:{}",
                    connectId);
            }
        }
        if (!connectIds.isEmpty()) {
            cancel(connectIds);
        }
    }

    /**
     * Getter method for property <tt>sessionInterests</tt>.
     *
     * @return property value of sessionInterests
     */
    protected Interests getSessionInterests() {
        return sessionInterests;
    }

    /**
     * Getter method for property <tt>sessionDataStore</tt>.
     *
     * @return property value of sessionDataStore
     */
    protected DataStore getSessionDataStore() {
        return sessionDataStore;
    }

    /**
     * Getter method for property <tt>taskListenerManager</tt>.
     *
     * @return property value of taskListenerManager
     */
    protected TaskListenerManager getTaskListenerManager() {
        return taskListenerManager;
    }
}