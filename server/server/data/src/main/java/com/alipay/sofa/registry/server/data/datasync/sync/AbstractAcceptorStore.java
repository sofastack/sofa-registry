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
package com.alipay.sofa.registry.server.data.datasync.sync;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.NotifyDataSyncRequest;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.datasync.AcceptorStore;
import com.alipay.sofa.registry.server.data.datasync.Operator;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;
import com.alipay.sofa.registry.server.data.util.DelayItem;
import com.alipay.sofa.registry.server.data.util.TimeUtil;

/**
 *
 * @author shangyu.wh
 * @version $Id: AbstractAcceptorStore.java, v 0.1 2018-03-22 12:28 shangyu.wh Exp $
 */
public abstract class AbstractAcceptorStore implements AcceptorStore {
    private static final Logger                                              LOGGER                  = LoggerFactory
                                                                                                         .getLogger(
                                                                                                             AbstractAcceptorStore.class,
                                                                                                             "[SyncDataService]");

    private static final int                                                 DEFAULT_MAX_BUFFER_SIZE = 30;
    private static final int                                                 DEFAULT_DELAY_TIMEOUT   = 3000;
    private static final int                                                 NOTIFY_RETRY            = 3;

    @Autowired
    protected IMetaServerService                                             metaServerService;

    @Autowired
    private Exchange                                                         boltExchange;

    @Autowired
    private DataServerConfig                                                 dataServerBootstrapConfig;

    @Autowired
    private DataServerConnectionFactory                                      dataServerConnectionFactory;

    @Autowired
    private DatumCache                                                       datumCache;

    private Map<String/*dataCenter*/, Map<String/*dataInfoId*/, Acceptor>> acceptors               = new ConcurrentHashMap<>();

    private Map<String/*dataCenter*/, Map<String/*dataInfoId*/, Acceptor>> notifyAcceptorsCache    = new ConcurrentHashMap<>();

    private DelayQueue<DelayItem<Acceptor>>                                  delayQueue              = new DelayQueue<>();

    @Override
    public void checkAcceptorsChangAndExpired() {
        acceptors.forEach((dataCenter, acceptorMap) -> {
            if (acceptorMap != null && !acceptorMap.isEmpty()) {
                acceptorMap.forEach((dataInfoId, acceptor) -> acceptor.checkExpired(0));
            }
        });
    }

    private String getLogByClass(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [").append(this.getClass().getSimpleName()).append("] ").append(msg);
        return sb.toString();
    }

    @Override
    public void addOperator(Operator operator) {

        Datum datum = operator.getDatum();
        String dataCenter = datum.getDataCenter();
        String dataInfoId = datum.getDataInfoId();
        try {
            Map<String/*dataInfoId*/, Acceptor> acceptorMap = acceptors.get(dataCenter);
            if (acceptorMap == null) {
                Map<String/*dataInfoId*/, Acceptor> newMap = new ConcurrentHashMap<>();
                acceptorMap = acceptors.putIfAbsent(dataCenter, newMap);
                if (acceptorMap == null) {
                    acceptorMap = newMap;
                }
            }

            Acceptor existAcceptor = acceptorMap.get(dataInfoId);
            if (existAcceptor == null) {
                Acceptor newAcceptor = new Acceptor(DEFAULT_MAX_BUFFER_SIZE, dataInfoId,
                    dataCenter, datumCache);
                existAcceptor = acceptorMap.putIfAbsent(dataInfoId, newAcceptor);
                if (existAcceptor == null) {
                    existAcceptor = newAcceptor;
                }
            }
            existAcceptor.appendOperator(operator);
            //put cache
            putCache(existAcceptor);
        } catch (Exception e) {
            LOGGER.error(getLogByClass("Append Operator error!"), e);
            throw new RuntimeException("Append Operator error!", e);
        }
    }

    private void putCache(Acceptor acceptor) {

        String dataCenter = acceptor.getDataCenter();
        String dataInfoId = acceptor.getDataInfoId();

        try {
            Map<String/*dataInfoId*/, Acceptor> acceptorMap = notifyAcceptorsCache.get(dataCenter);
            if (acceptorMap == null) {
                Map<String/*dataInfoId*/, Acceptor> newMap = new ConcurrentHashMap<>();
                acceptorMap = notifyAcceptorsCache.putIfAbsent(dataCenter, newMap);
                if (acceptorMap == null) {
                    acceptorMap = newMap;
                }
            }
            Acceptor existAcceptor = acceptorMap.putIfAbsent(dataInfoId, acceptor);
            if (existAcceptor == null) {
                addQueue(acceptor);
            }
        } catch (Exception e) {
            LOGGER.error(getLogByClass("Operator push to delay cache error!"), e);
            throw new RuntimeException("Operator push to delay cache error!", e);
        }
    }

    private void removeCache(Acceptor acceptor) {
        String dataCenter = acceptor.getDataCenter();
        String dataInfoId = acceptor.getDataInfoId();

        try {
            Map<String/*dataInfoId*/, Acceptor> acceptorMap = notifyAcceptorsCache.get(dataCenter);
            if (acceptorMap != null) {
                boolean result = acceptorMap.remove(dataInfoId, acceptor);
                if (result) {
                    //data change notify
                    notifyChange(acceptor);
                }
            }
        } catch (Exception e) {
            LOGGER.error(getLogByClass("Operator remove from delay cache error!"), e);
            throw new RuntimeException("Operator remove from delay cache error!", e);
        }
    }

    private void addQueue(Acceptor acceptor) {
        delayQueue.put(new DelayItem(acceptor, DEFAULT_DELAY_TIMEOUT));
    }

    private void notifyChange(Acceptor acceptor) {

        Long lastVersion = acceptor.getLastVersion();

        //may be delete by expired
        if (lastVersion == null) {
            LOGGER
                .warn(getLogByClass("There is not data in acceptor queue!maybe has been expired!"));
            lastVersion = 0L;
        }

        if (LOGGER.isDebugEnabled()) {
            acceptor.printInfo();
        }

        NotifyDataSyncRequest request = new NotifyDataSyncRequest(acceptor.getDataInfoId(),
            acceptor.getDataCenter(), lastVersion, getType());

        List<String> targetDataIps = getTargetDataIp(acceptor.getDataInfoId());
        for (String targetDataIp : targetDataIps) {

            if (DataServerConfig.IP.equals(targetDataIp)) {
                continue;
            }

            Server syncServer = boltExchange.getServer(dataServerBootstrapConfig.getSyncDataPort());

            for (int tryCount = 0; tryCount < NOTIFY_RETRY; tryCount++) {
                try {

                    Connection connection = dataServerConnectionFactory.getConnection(targetDataIp);
                    if (connection == null) {
                        LOGGER.error(getLogByClass(String.format(
                            "Can not get notify data server connection!ip: %s,retry=%s",
                            targetDataIp, tryCount)));
                        TimeUtil.randomDelay(1000);
                        continue;
                    }
                    LOGGER.info(
                        getLogByClass("Notify data server {} change data {} to sync,retry={}"),
                        connection.getRemoteIP(), request, tryCount);

                    syncServer.sendSync(syncServer.getChannel(connection.getRemoteAddress()),
                        request, 1000);
                    break;
                } catch (Exception e) {
                    LOGGER.error(getLogByClass(String.format(
                        "Notify data server %s failed, NotifyDataSyncRequest:%s,retry=%s",
                        targetDataIp, request, tryCount)), e);
                    TimeUtil.randomDelay(1000);
                }
            }
        }
    }

    abstract public List<String> getTargetDataIp(String dataInfoId);

    @Override
    public void changeDataCheck() {

        while (true) {
            try {
                DelayItem<Acceptor> delayItem = delayQueue.take();
                Acceptor acceptor = delayItem.getItem();
                removeCache(acceptor); // compare and remove
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    @Override
    public SyncData getSyncData(SyncDataRequest syncDataRequest) {

        String dataCenter = syncDataRequest.getDataCenter();
        String dataInfoId = syncDataRequest.getDataInfoId();

        Long currentVersion = syncDataRequest.getVersion();
        try {
            Map<String/*dataInfoId*/, Acceptor> acceptorMap = acceptors.get(dataCenter);
            if (acceptorMap == null) {
                LOGGER.error(
                    getLogByClass("Can not find Sync Data acceptor instance,dataCenter:{}"),
                    dataCenter);
                throw new RuntimeException("Can not find Sync Data acceptor instance!");
            }

            Acceptor existAcceptor = acceptorMap.get(dataInfoId);
            if (existAcceptor == null) {
                LOGGER.error(
                    getLogByClass("Can not find Sync Data acceptor instance,dataInfoId:{}"),
                    dataInfoId);
                throw new RuntimeException("Can not find Sync Data acceptor instance!");
            }
            return existAcceptor.process(currentVersion);
        } catch (Exception e) {
            LOGGER.error(getLogByClass("Get change SyncData error!"), e);
            throw new RuntimeException("Get change SyncData error!", e);
        }
    }

    /**
     * Getter method for property <tt>dataServerBootstrapConfig</tt>.
     *
     * @return property value of dataServerBootstrapConfig
     */
    public DataServerConfig getDataServerConfig() {
        return dataServerBootstrapConfig;
    }
}