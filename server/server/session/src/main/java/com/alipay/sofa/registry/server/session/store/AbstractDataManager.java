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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-18 17:18 yuzhi.lyz Exp $
 */
public abstract class AbstractDataManager<T extends BaseInfo> implements
                                                              DataManager<T, String, String> {
    private final ReentrantReadWriteLock                                                    readWriteLock = new ReentrantReadWriteLock();
    protected final Lock                                                                    read          = readWriteLock
                                                                                                              .readLock();
    protected final Lock                                                                    write         = readWriteLock
                                                                                                              .writeLock();

    protected final ConcurrentHashMap<String/*dataInfoId*/, Map<String/*registerId*/, T>> stores        = new ConcurrentHashMap<>();
    protected final Logger                                                                  logger;

    @Autowired
    protected SessionServerConfig                                                           sessionServerConfig;

    AbstractDataManager(Logger logger) {
        this.logger = logger;
    }


    protected T addData(T data) {
        Map<String, T> datas = stores.computeIfAbsent(data.getDataInfoId(),
                k -> Maps.newConcurrentMap());

        T existing = datas.put(data.getRegisterId(), data);
        return existing;
    }


    @Override
    public boolean deleteById(String registerId, String dataInfoId) {
        Map<String, T> datas = stores.get(dataInfoId);
        if (datas == null) {
            logger.error("Delete failed because is not registered for {}", dataInfoId);
            return false;
        }
        boolean modified = false;
        write.lock();
        try {
            T dataToDelete = datas.remove(registerId);
            if (dataToDelete != null) {
                modified = true;
            }
        } finally {
            write.unlock();
        }
        if (!modified) {
            logger.error("Delete failed because is not registered for {}, {}", dataInfoId,
                registerId);
        }
        return modified;
    }

    @Override
    public boolean deleteByConnectId(ConnectId connectId) {
        boolean modified = false;
        write.lock();
        try {
            for (Map<String, T> map : stores.values()) {
                for (Iterator it = map.values().iterator(); it.hasNext();) {
                    T data = (T) it.next();
                    if (connectId.equals(data.connectId())) {
                        modified = true;
                        it.remove();
                    }
                }
            }
        } finally {
            write.unlock();
        }
        return modified;
    }

    @Override
    public Collection<T> getDatas(String dataInfoId) {
        ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
        Map<String, T> dataMap = stores.get(dataInfoId);
        if (MapUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(dataMap.values());
    }

    @Override
    public Map<String, Map<String, T>> getDatas() {
        return StoreHelpers.copyMap((Map) stores);
    }

    @Override
    public Map<String, T> queryByConnectId(ConnectId connectId) {
        return StoreHelpers.getByConnectId(connectId, stores);
    }

    @Override
    public T queryById(String registerId, String dataInfoId) {
        final Map<String, T> datas = stores.get(dataInfoId);
        return datas == null ? null : datas.get(registerId);
    }

    @Override
    public long count() {
        return StoreHelpers.count(stores);
    }

    @Override
    public Set<ConnectId> getConnectIds() {
        return StoreHelpers.collectConnectIds(stores);
    }

    @Override
    public Set<String> collectProcessIds() {
        return StoreHelpers.collectProcessIds(stores);
    }

    @Override
    public Collection<String> getDataInfoIds() {
        return stores.entrySet().stream().filter(e -> !(e.getValue().isEmpty())).map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    public SessionServerConfig getSessionServerConfig() {
        return sessionServerConfig;
    }

    /**
     * Setter method for property <tt>sessionServerConfig</tt>.
     *
     * @param sessionServerConfig value to be assigned to property sessionServerConfig
     */
    public void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
        this.sessionServerConfig = sessionServerConfig;
    }
}
