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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppIndexQueryModel;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.exception.InterfaceAppQueryException;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.jdbc.repository.JdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.batch.InterfaceAppBatchQueryCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import com.alipay.sofa.registry.util.SingleFlight;
import com.alipay.sofa.registry.util.TimestampUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepository.java, v 0.1 2021年01月24日 19:57 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepository implements InterfaceAppsRepository, JdbcRepository {

    private static final Logger                            LOG                  = LoggerFactory
                                                                                    .getLogger(InterfaceAppsJdbcRepository.class);

    private static volatile Timestamp                      MAX_MODIFY_TIMESTAMP = new Timestamp(-1);

    /**
     * map: <interface, appNames>
     */
    protected final Map<String, Tuple<Long, Set<String>>>  interfaceApps        = new ConcurrentHashMap<>();

    @Autowired
    private InterfaceAppBatchQueryCallable                 interfaceAppBatchQueryCallable;

    @Autowired
    private InterfaceAppsIndexMapper                       interfaceAppsIndexMapper;

    private final Map<String, InterfaceAppIndexQueryModel> interfaceQueryParam  = new ConcurrentHashMap<>();

    private SingleFlight                                   singleFlight         = new SingleFlight();

    private Integer                                        REFRESH_LIMIT;

    @Autowired
    private MetadataConfig                                 metadataConfig;

    @PostConstruct
    public void postConstruct() {
        REFRESH_LIMIT = metadataConfig.getInterfaceAppsRefreshLimit();
    }

    @Override
    public void loadMetadata(String dataCenter) {
        // load interface_apps_version when session server startup,
        // in order to avoid large request on database after session startup,
        // it will load almost all record of this dataCenter,
        // but not promise load 100% records of this dataCenter,
        // eg: records insert or update after interfaceAppsIndexMapper.getTotalCount
        // and beyond refreshCount will not be load in this method, they will be load in next schedule
        int refreshCount = interfaceAppsIndexMapper.getTotalCount(dataCenter) / REFRESH_LIMIT;
        for (int i = 0; i <= refreshCount; i++) {
            this.refresh(dataCenter);
        }
    }

    /**
     * get revisions by interfaceName
     * @param dataInfoId
     * @return return appNames
     */
    @Override
    public Tuple<Long, Set<String>> getAppNames(String dataCenter, String dataInfoId) {

        Tuple<Long, Set<String>> appNames = interfaceApps.get(dataInfoId);
        if (appNames != null) {
            return appNames;
        }

        InterfaceAppIndexQueryModel query = getInterfaceQueryModel(dataCenter, dataInfoId);

        TaskEvent task = interfaceAppBatchQueryCallable.new TaskEvent(query);
        InvokeFuture future = interfaceAppBatchQueryCallable.commit(task);
        try {

            if (future.isSuccess()) {
                Object response = future.getResponse();
                if (response == null) {
                    interfaceApps.put(dataInfoId, new Tuple<>(-1L, new HashSet<>()));
                } else {
                    appNames = (Tuple<Long, Set<String>>) response;
                    interfaceApps.put(dataInfoId, appNames);
                }
                return appNames;
            }
            LOG.error(String.format("query appNames by interface: %s fail.", dataInfoId));
            throw new InterfaceAppQueryException(dataInfoId);

        } catch (InterruptedException e) {
            LOG.error(String.format("query appNames by interface: %s error.", dataInfoId), e);
            throw new RuntimeException(String.format("query appNames by interface: %s error.",
                dataInfoId), e);
        }
    }

    private InterfaceAppIndexQueryModel getInterfaceQueryModel(String dataCenter, String dataInfoId) {
        InterfaceAppIndexQueryModel query = interfaceQueryParam.get(dataInfoId);
        if (query == null) {
            query = new InterfaceAppIndexQueryModel(dataCenter, dataInfoId);
            interfaceQueryParam.put(dataInfoId, query);
        }
        return query;
    }

    /**
     * insert
     * @param dataCenter
     * @param appName
     * @param interfaceNames
     */
    @Override
    public int batchSave(String dataCenter, String appName, Set<String> interfaceNames) {
        List<InterfaceAppsIndexDomain> saves = new ArrayList<>();
        for (String interfaceName : interfaceNames) {
            saves.add(new InterfaceAppsIndexDomain(dataCenter, interfaceName, appName));
        }

        return interfaceAppsIndexMapper.insert(saves);
    }

    /**
     * refresh interfaceNames index
     */
    private void triggerRefreshCache(InterfaceAppsIndexDomain domain) throws Exception {

        Tuple<Long, Set<String>> tuple = interfaceApps.get(domain.getInterfaceName());
        long nanosLong = TimestampUtil.getNanosLong(domain.getGmtModify());
        if (tuple == null || tuple.o1 == null) {
            if (domain.isReference()) {
                tuple = new Tuple(nanosLong, new HashSet() {
                    {
                        add(domain.getAppName());
                    }
                });
            } else {
                tuple = new Tuple(nanosLong, new HashSet());
            }
            LOG.info(String.format("refresh interface: %s, ref: %s, app: %s, tuple: %s",
                domain.getInterfaceName(), domain.isReference(), domain.getAppName(), tuple));
            interfaceApps.put(domain.getInterfaceName(), tuple);
        } else if (nanosLong >= tuple.o1) {
            Tuple<Long, Set<String>> newTuple;
            if (domain.isReference()) {
                tuple.o2.add(domain.getAppName());
                newTuple = new Tuple(nanosLong, tuple.o2);
            } else {
                tuple.o2.remove(domain.getAppName());
                newTuple = new Tuple(nanosLong, tuple.o2);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("refresh interface: %s, ref: %s, app: %s, tuple: %s",
                    domain.getInterfaceName(), domain.isReference(), domain.getAppName(), tuple));
            }
            interfaceApps.put(domain.getInterfaceName(), newTuple);
        }
    }

    public void refresh(String dataCenter) {
        try {
            singleFlight.execute("refresh" + dataCenter, () -> {
                List<InterfaceAppsIndexDomain> afters = interfaceAppsIndexMapper.queryModifyAfterThan(dataCenter, MAX_MODIFY_TIMESTAMP,
                        REFRESH_LIMIT);

                List<InterfaceAppsIndexDomain> equals = interfaceAppsIndexMapper.queryModifyEquals(dataCenter, MAX_MODIFY_TIMESTAMP);

                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("refresh equal size: %s, after size: %s,", equals.size(), afters.size()));
                }

                equals.addAll(afters);
                if (CollectionUtils.isEmpty(equals)) {
                    return null;
                }

                // trigger refresh interface index
                for (InterfaceAppsIndexDomain interfaceApps : equals) {
                    triggerRefreshCache(interfaceApps);
                }

                // update MAX_MODIFY_TIMESTAMP
                InterfaceAppsIndexDomain last = equals.get(equals.size() - 1);
                if (MAX_MODIFY_TIMESTAMP.before(last.getGmtModify())) {
                    synchronized (MAX_MODIFY_TIMESTAMP) {
                        if (MAX_MODIFY_TIMESTAMP.before(last.getGmtModify())) {
                            MAX_MODIFY_TIMESTAMP.setTime(last.getGmtModify().getTime());
                            MAX_MODIFY_TIMESTAMP.setNanos(last.getGmtModify().getNanos());
                            if (LOG.isInfoEnabled()) {
                                LOG.info(String.format("update max_modify_timestamp, change to: %s", MAX_MODIFY_TIMESTAMP.getTime()));
                            }
                        }
                    }
                }

                return null;
            });
        } catch (Exception e) {
            LOG.error("refresh interface apps version error.", e);
        }
    }

    /**
     * refresh interfaceNames index
     */
    private void refreshServiceIndex(String dataCenter, List<String> interfaceNames)
                                                                                    throws Exception {

        Map<String, InvokeFuture> interfaceFuture = new LinkedHashMap<>();
        for (String interfaceName : interfaceNames) {
            InterfaceAppIndexQueryModel query = getInterfaceQueryModel(dataCenter, interfaceName);
            TaskEvent task = interfaceAppBatchQueryCallable.new TaskEvent(query);
            InvokeFuture future = interfaceAppBatchQueryCallable.commit(task);
            interfaceFuture.put(interfaceName, future);
        }

        for (Entry<String, InvokeFuture> future : interfaceFuture.entrySet()) {
            if (future.getValue().isSuccess()) {
                Object response = future.getValue().getResponse();
                if (response == null) {
                    Tuple<Long, Set<String>> tuple = new Tuple<>(-1L, new HashSet<>());
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format(
                            "interface: %s put data version: %s, put data value: %s",
                            future.getKey(), tuple.o1, tuple.o2));
                    }
                    interfaceApps.put(future.getKey(), tuple);
                } else {
                    Tuple<Long, Set<String>> appNames = (Tuple<Long, Set<String>>) response;
                    interfaceApps.put(future.getKey(), appNames);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format(
                            "interface: %s put data version: %s, put data value: %s",
                            future.getKey(), appNames.o1, appNames.o2));
                    }
                }

            }
        }

    }

}