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

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
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
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepository.java, v 0.1 2021年01月24日 19:57 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepository implements InterfaceAppsRepository, JdbcRepository {

    private static final Logger                   LOG                 = LoggerFactory.getLogger(
                                                                          "METADATA-EXCHANGE",
                                                                          "[InterfaceApps]");

    private volatile Timestamp                    lastModifyTimestamp = new Timestamp(-1);

    /**
     * map: <interface, appNames>
     */
    protected final Map<String, InterfaceMapping> interfaceApps       = new ConcurrentHashMap<>();

    @Autowired
    private InterfaceAppBatchQueryCallable        interfaceAppBatchQueryCallable;

    @Autowired
    private InterfaceAppsIndexMapper              interfaceAppsIndexMapper;

    private int                                   refreshLimit;

    @Autowired
    private MetadataConfig                        metadataConfig;

    @PostConstruct
    public void postConstruct() {
        refreshLimit = metadataConfig.getInterfaceAppsRefreshLimit();
    }

    @Override
    public void loadMetadata(String dataCenter) {
        // load interface_apps_version when session server startup,
        // in order to avoid large request on database after session startup,
        // it will load almost all record of this dataCenter,
        // but not promise load 100% records of this dataCenter,
        // eg: records insert or update after interfaceAppsIndexMapper.getTotalCount
        // and beyond refreshCount will not be load in this method, they will be load in next schedule
        final int total = interfaceAppsIndexMapper.getTotalCount(dataCenter);
        final int refreshCount = MathUtils.divideCeil(total, refreshLimit);
        LOG.info("begin load metadata, total count mapping {}, rounds={}, dataCenter={}",
            total, refreshCount, dataCenter);
        int refreshTotal = 0;
        for (int i = 0; i < refreshCount; i++) {
            final int num = this.refresh(dataCenter);
            LOG.info("load metadata in round={}, num={}", i, num);
            refreshTotal += num;
            if (num == 0) {
                break;
            }
        }
        LOG.info("finish load metadata, total={}", refreshTotal);
    }

    /**
     * get revisions by interfaceName
     *
     * @param dataInfoId
     * @return return appNames
     */
    @Override
    public InterfaceMapping getAppNames(String dataCenter, String dataInfoId) {
        InterfaceMapping appNames = interfaceApps.get(dataInfoId);
        if (appNames != null) {
            return appNames;
        }

        final InterfaceAppIndexQueryModel query = new InterfaceAppIndexQueryModel(dataCenter,
            dataInfoId);

        TaskEvent task = interfaceAppBatchQueryCallable.new TaskEvent(query);
        InvokeFuture future = interfaceAppBatchQueryCallable.commit(task);
        try {
            if (future.isSuccess()) {
                Object response = future.getResponse();
                if (response == null) {
                    appNames = new InterfaceMapping(-1);
                } else {
                    appNames = (InterfaceMapping) response;
                }
                LOG.info("update interfaceMapping {}, {}", dataInfoId, appNames);
                interfaceApps.put(dataInfoId, appNames);
                return appNames;
            }
            LOG.error("query appNames by interface: {} fail.", dataInfoId);
            throw new InterfaceAppQueryException(dataInfoId);

        } catch (Throwable e) {
            LOG.error("query appNames by interface: {} error.", dataInfoId, e);
            throw new RuntimeException(String.format("query appNames by interface: %s error.",
                dataInfoId), e);
        }
    }

    /**
     * insert
     *
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
    private synchronized void triggerRefreshCache(InterfaceAppsIndexDomain domain) {
        InterfaceMapping mapping = interfaceApps.get(domain.getInterfaceName());
        final long nanosLong = TimestampUtil.getNanosLong(domain.getGmtModify());
        if (mapping == null) {
            if (domain.isReference()) {
                mapping = new InterfaceMapping(nanosLong, domain.getAppName());
            } else {
                mapping = new InterfaceMapping(nanosLong);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("refresh interface: {}, ref: {}, app: {}, mapping: {}",
                    domain.getInterfaceName(), domain.isReference(), domain.getAppName(), mapping);
            }
            interfaceApps.put(domain.getInterfaceName(), mapping);
            return;
        }
        if (nanosLong >= mapping.getNanosVersion()) {
            InterfaceMapping newMapping = null;
            if (domain.isReference()) {
                newMapping = new InterfaceMapping(nanosLong, mapping.getApps(), domain.getAppName());
            } else {
                Set<String> prev = Sets.newHashSet(mapping.getApps());
                prev.remove(domain.getAppName());
                newMapping = new InterfaceMapping(nanosLong, prev, domain.getAppName());
            }
            if (LOG.isInfoEnabled()) {
                LOG
                    .info(
                        "update interface mapping: {}, ref: {}, app: {}, newMapping: {}, oldMapping: {}",
                        domain.getInterfaceName(), domain.isReference(), domain.getAppName(),
                        newMapping, mapping);
            }
            interfaceApps.put(domain.getInterfaceName(), newMapping);
        } else {
            LOG.info("ignored refresh index {}, current mapping={}", domain, mapping);
        }
    }

    public synchronized int refresh(String dataCenter) {
        final Timestamp last = lastModifyTimestamp;
        List<InterfaceAppsIndexDomain> afters = interfaceAppsIndexMapper.queryModifyAfterThan(
            dataCenter, last, refreshLimit);
        List<InterfaceAppsIndexDomain> equals = interfaceAppsIndexMapper.queryModifyEquals(
            dataCenter, last);
        if (LOG.isInfoEnabled()) {
            LOG.info("refresh lastTimestamp={}, equals={}, afters={},", last,
                equals.size(), afters.size());
        }

        equals.addAll(afters);
        if (CollectionUtils.isEmpty(equals)) {
            return 0;
        }

        // trigger refresh interface index, must be sorted by gmtModify
        for (InterfaceAppsIndexDomain interfaceApps : equals) {
            triggerRefreshCache(interfaceApps);
        }

        // update MAX_MODIFY_TIMESTAMP
        InterfaceAppsIndexDomain max = equals.get(equals.size() - 1);
        if (lastModifyTimestamp.before(max.getGmtModify())) {
            this.lastModifyTimestamp = max.getGmtModify();
            LOG.info("update lastModifyTimestamp {} to {}", last, lastModifyTimestamp);
        } else {
            LOG.info("skip update lastModifyTimestamp {}, got={}", lastModifyTimestamp,
                max.getGmtModify());
        }
        return equals.size();
    }

}