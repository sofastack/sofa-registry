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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
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
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.renew.RenewService;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id: AbstractSessionRegistry.java, v 0.1 2017-11-30 18:13 shangyu.wh Exp $
 */
public class SessionRegistry implements Registry {

    private static final Logger     LOGGER       = LoggerFactory.getLogger(SessionRegistry.class);

    private static final Logger     TASK_LOGGER  = LoggerFactory.getLogger(SessionRegistry.class,
                                                     "[Task]");

    private static final Logger     RENEW_LOGGER = LoggerFactory.getLogger(
                                                     ValueConstants.LOGGER_NAME_RENEW,
                                                     "[SessionRegistry]");

    /**
     * store subscribers
     */
    @Autowired
    private Interests               sessionInterests;

    /**
     * store watchers
     */
    @Autowired
    private Watchers                sessionWatchers;

    /**
     * store publishers
     */
    @Autowired
    private DataStore               sessionDataStore;

    /**
     * transfer data to DataNode
     */
    @Autowired
    private DataNodeService         dataNodeService;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    @Autowired
    private TaskListenerManager     taskListenerManager;

    /**
     * calculate data node url
     */
    @Autowired
    private NodeManager             dataNodeManager;

    @Autowired
    private SessionServerConfig     sessionServerConfig;

    @Autowired
    private Exchange                boltExchange;

    @Autowired
    private SessionRegistryStrategy sessionRegistryStrategy;

    @Autowired
    private RenewService            renewService;

    @Autowired
    private WriteDataAcceptor       writeDataAcceptor;

    @Override
    public void register(StoreData storeData) {

        //check connect already existed
        checkConnect(storeData);

        switch (storeData.getDataType()) {
            case PUBLISHER:
                Publisher publisher = (Publisher) storeData;

                sessionDataStore.add(publisher);

                // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                // are handed over to WriteDataAcceptor
                writeDataAcceptor.accept(new WriteDataRequest() {
                    @Override
                    public Object getRequestBody() {
                        return publisher;
                    }

                    @Override
                    public WriteDataRequestType getRequestType() {
                        return WriteDataRequestType.PUBLISHER;
                    }

                    @Override
                    public String getConnectId() {
                        return publisher.getSourceAddress().getAddressString();
                    }
                });

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

    }

    @Override
    public void unRegister(StoreData<String> storeData) {

        switch (storeData.getDataType()) {
            case PUBLISHER:
                Publisher publisher = (Publisher) storeData;

                sessionDataStore.deleteById(storeData.getId(), publisher.getDataInfoId());

                //                dataNodeService.unregister(publisher);

                // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                // are handed over to WriteDataAcceptor
                writeDataAcceptor.accept(new WriteDataRequest() {
                    @Override
                    public Object getRequestBody() {
                        return publisher;
                    }

                    @Override
                    public WriteDataRequestType getRequestType() {
                        return WriteDataRequestType.UN_PUBLISHER;
                    }

                    @Override
                    public String getConnectId() {
                        return publisher.getSourceAddress().getAddressString();
                    }
                });

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
    public void cancel(List<String> connectIds) {
        //update local firstly, data node send error depend on renew check
        List<String> connectIdsWithPub = new ArrayList<>();
        removeFromSession(connectIds, connectIdsWithPub);

        // clientOff to dataNode async
        clientOffToDataNode(connectIdsWithPub);

    }

    private void removeFromSession(List<String> connectIds, List<String> connectIdsWithPub) {
        for (String connectId : connectIds) {
            if (sessionDataStore.deleteByConnectId(connectId)) {
                connectIdsWithPub.add(connectId);
            }
            sessionInterests.deleteByConnectId(connectId);
            sessionWatchers.deleteByConnectId(connectId);
        }
    }

    private void clientOffToDataNode(List<String> connectIdsWithPub) {
        // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
        // are handed over to WriteDataAcceptor
        for (String connectId : connectIdsWithPub) {
            writeDataAcceptor.accept(new WriteDataRequest() {
                @Override
                public Object getRequestBody() {
                    return connectId;
                }

                @Override
                public WriteDataRequestType getRequestType() {
                    return WriteDataRequestType.CLIENT_OFF;
                }

                @Override
                public String getConnectId() {
                    return connectId;
                }
            });
            writeDataAcceptor.remove(connectId);
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
        Map<String/*address*/, Collection<String>/*dataInfoIds*/> map = calculateDataNode(checkDataInfoIds);

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
                Node dataNode = dataNodeManager.getNode(dataInfoId);
                URL url = new URL(dataNode.getNodeUrl().getIpAddress(), sessionServerConfig.getDataServerPort());
                Collection<String> list = map.computeIfAbsent(url.getAddressString(), k -> new ArrayList<>());
                list.add(dataInfoId);
            });
        }

        return map;
    }

    private void checkConnect(StoreData storeData) {

        BaseInfo baseInfo = (BaseInfo) storeData;

        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

        Channel channel = sessionServer.getChannel(baseInfo.getSourceAddress());

        if (channel == null) {
            throw new RuntimeException(String.format(
                "Register address %s  has not connected session server!",
                baseInfo.getSourceAddress()));
        }

    }

    @Override
    public void renewDatum(String connectId) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("renewDatum: connectId={}", connectId);
        }

        List<RenewDatumRequest> renewDatumRequests = renewService.getRenewDatumRequests(connectId);
        if (renewDatumRequests != null) {
            for (RenewDatumRequest renewDatumRequest : renewDatumRequests) {
                // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                // are handed over to WriteDataAcceptor
                writeDataAcceptor.accept(new WriteDataRequest() {
                    @Override
                    public Object getRequestBody() {
                        return renewDatumRequest;
                    }

                    @Override
                    public WriteDataRequestType getRequestType() {
                        return WriteDataRequestType.RENEW_DATUM;
                    }

                    @Override
                    public String getConnectId() {
                        return connectId;
                    }
                });
            }
        }
    }

    @Override
    public void sendDatumSnapshot(String connectId) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("sendDatumSnapshot: connectId={}", connectId);
        }

        // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
        // are handed over to WriteDataAcceptor
        writeDataAcceptor.accept(new WriteDataRequest() {
            @Override
            public Object getRequestBody() {
                return connectId;
            }

            @Override
            public WriteDataRequestType getRequestType() {
                return WriteDataRequestType.DATUM_SNAPSHOT;
            }

            @Override
            public String getConnectId() {
                return connectId;
            }
        });
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