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

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.store.*;
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
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor.KeyedTask;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionRegistry.java, v 0.1 2017-11-30 18:13 shangyu.wh Exp $
 */
public class SessionRegistry implements Registry {

    private static final Logger       LOGGER      = LoggerFactory.getLogger(SessionRegistry.class);

    protected static final Logger     TASK_LOGGER = LoggerFactory.getLogger(SessionRegistry.class,
                                                      "[Task]");

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

    private KeyedThreadPoolExecutor   fetchVersionThreadPoolExecutor;

    @PostConstruct
    public void init() {
        this.fetchVersionThreadPoolExecutor = new KeyedThreadPoolExecutor("fetchVersion",
            sessionServerConfig.getSchedulerFetchDataVersionPoolSize(), SlotConfig.SLOT_NUM * 2);

        ConcurrentUtils.createDaemonThread("SessionWatchDog", new WatchDog()).start();
    }

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

    private final class WatchDog extends LoopRunnable {
        final Map<Integer, KeyedTask<FetchVersionTask>> fetchVersionTasks = Maps.newConcurrentMap();

        @Override
        public void runUnthrowable() {
            try {
                if (sessionServerConfig.isBeginDataFetchTask()) {
                    fetchVersions();
                }
            } catch (Throwable e) {
                LOGGER.error("WatchDog failed fetch verions", e);
            }
            try {
                cleanClientConnect();
            } catch (Throwable e) {
                LOGGER.error("WatchDog failed cleanClientConnect", e);
            }
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        private void fetchVersions() {
            Collection<String> checkDataInfoIds = sessionInterests.getInterestDataInfoIds();
            Map<Integer/*slotId*/, Collection<String>/*dataInfoIds*/> map = calculateDataNode(checkDataInfoIds);
            for (Map.Entry<Integer, Collection<String>> e : map.entrySet()) {
                final int slotId = e.getKey();
                KeyedTask<FetchVersionTask> task = fetchVersionTasks.get(slotId);
                if (task == null
                    || task.isOverAfter(sessionServerConfig
                        .getSchedulerFetchDataVersionIntervalMs())) {
                    task = fetchVersionThreadPoolExecutor.execute(slotId, new FetchVersionTask(
                        slotId, e.getValue()));
                    fetchVersionTasks.put(slotId, task);
                    LOGGER.info("commit FetchVersionTask, {}, size={}", slotId, task);
                } else {
                    LOGGER.info("FetchVersioning, {}, status={}", slotId, task);
                }
            }
            return;
        }
    }

    private final class FetchVersionTask implements Runnable {
        final int                slotId;
        final Collection<String> dataInfoIds;

        FetchVersionTask(int slotId, Collection<String> dataInfoIds) {
            this.slotId = slotId;
            this.dataInfoIds = dataInfoIds;
        }

        @Override
        public void run() {
            fetchChangDataProcess(slotId, dataInfoIds);
        }

        @Override
        public String toString() {
            return "FetchVersionTask{" + "slotId=" + slotId + ", dataInfoIds=" + dataInfoIds.size()
                   + '}';
        }
    }

    @Override
    public void fetchChangDataProcess() {
        Collection<String> checkDataInfoIds = sessionInterests.getInterestDataInfoIds();
        Map<Integer/*slotId*/, Collection<String>/*dataInfoIds*/> map = calculateDataNode(checkDataInfoIds);

        map.forEach((slotId, dataInfoIds) -> {
            //TODO asynchronous fetch version
            fetchChangDataProcess(slotId, dataInfoIds);
        });

    }

    private void fetchChangDataProcess(int slotId, Collection<String> dataInfoIds) {
        String leader = slotTableCache.getLeader(slotId);
        if (leader == null) {
            LOGGER
                .error("slot not assign {} when fetch dataInfoIds={}", slotId, dataInfoIds.size());
            return;
        }
        LOGGER.info("[fetchChangDataProcess] Fetch data versions from {}, {}, dataInfoIds={}",
            slotId, leader, dataInfoIds.size());
        Map<String/*datacenter*/, Map<String/*datainfoid*/, Long>> dataVersions = dataNodeService
            .fetchDataVersion(new URL(leader, sessionServerConfig.getDataServerPort()), dataInfoIds);

        if (dataVersions != null) {
            sessionRegistryStrategy.doFetchChangDataProcess(dataVersions);
        } else {
            LOGGER.warn("Fetch no change data versions info from {}, {}", slotId, leader);
        }
    }

    private Map<Integer, Collection<String>> calculateDataNode(Collection<String> dataInfoIds) {
        Map<Integer, Collection<String>> map = new HashMap<>();
        dataInfoIds.forEach(dataInfoId -> {
            int slotId = slotTableCache.slotOf(dataInfoId);
            Collection<String> list = map.computeIfAbsent(slotId, k -> Lists.newArrayList());
            list.add(dataInfoId);
        });

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
        connectIndexes.addAll(sessionDataStore.getConnectIds());
        connectIndexes.addAll(sessionInterests.getConnectIds());
        connectIndexes.addAll(sessionWatchers.getConnectIds());

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
