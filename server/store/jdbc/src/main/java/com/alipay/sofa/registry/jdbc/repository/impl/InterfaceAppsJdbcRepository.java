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
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.exception.InterfaceAppQueryException;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.jdbc.repository.batch.InterfaceAppBatchQueryCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.APPS_CACHE_HIT_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.APPS_CACHE_MISS_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.INTERFACE_APPS_REGISTER_COUNTER;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepository.java, v 0.1 2021年01月24日 19:57 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepository implements InterfaceAppsRepository {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceApps]");

  private volatile long maxId = 0L;

  /** map: <interface, appNames> */
  protected final Map<String, InterfaceMapping> interfaceApps = new ConcurrentHashMap<>();

  @Autowired private InterfaceAppBatchQueryCallable interfaceAppBatchQueryCallable;

  @Autowired private InterfaceAppsIndexMapper interfaceAppsIndexMapper;

  private int refreshLimit;

  @Autowired private MetadataConfig metadataConfig;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @PostConstruct
  public void postConstruct() {
    refreshLimit = metadataConfig.getInterfaceAppsRefreshLimit();
  }

  @Override
  public void loadMetadata() {
    // load interface_apps_version when session server startup,
    // in order to avoid large request on database after session startup,
    // it will load almost all record of this dataCenter,
    // but not promise load 100% records of this dataCenter,
    // eg: records insert after interfaceAppsIndexMapper.getTotalCount
    // and beyond refreshCount will not be load in this method, they will be load in next schedule
    final int total = interfaceAppsIndexMapper.getTotalCount(defaultCommonConfig.getClusterId());
    // add 100, query the new mappings which inserted when scanning
    final int refreshCount = MathUtils.divideCeil(total, refreshLimit) + 100;
    LOG.info(
        "begin load metadata, total count mapping {}, rounds={}, dataCenter={}",
        total,
        refreshCount,
        defaultCommonConfig.getClusterId());
    int refreshTotal = 0;
    for (int i = 0; i < refreshCount; i++) {
      final int num = this.refresh(defaultCommonConfig.getClusterId());
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
  public InterfaceMapping getAppNames(String dataInfoId) {
    InterfaceMapping appNames = interfaceApps.get(dataInfoId);
    if (appNames != null) {
      APPS_CACHE_HIT_COUNTER.inc();
      return appNames;
    }

    APPS_CACHE_MISS_COUNTER.inc();
    TaskEvent task = interfaceAppBatchQueryCallable.new TaskEvent(dataInfoId);
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
      throw new RuntimeException(
          String.format("query appNames by interface: %s error.", dataInfoId), e);
    }
  }

  /**
   * insert
   *
   * @param appName
   * @param interfaceNames
   */
  @Override
  public void batchSave(String appName, Set<String> interfaceNames) {
    for (String interfaceName : interfaceNames) {
      InterfaceAppsIndexDomain interfaceApps =
          new InterfaceAppsIndexDomain(defaultCommonConfig.getClusterId(), interfaceName, appName);
      int effectRows = interfaceAppsIndexMapper.update(interfaceApps);
      if (effectRows == 0) {
        interfaceAppsIndexMapper.insertOnReplace(interfaceApps);
      }
      INTERFACE_APPS_REGISTER_COUNTER.inc();
    }
  }

  /** refresh interfaceNames index */
  private synchronized void triggerRefreshCache(InterfaceAppsIndexDomain domain) {
    InterfaceMapping mapping = interfaceApps.get(domain.getInterfaceName());
    final long nanosLong = TimestampUtil.getNanosLong(domain.getGmtCreate());
    if (mapping == null) {
      if (domain.isReference()) {
        mapping = new InterfaceMapping(nanosLong, domain.getAppName());
      } else {
        mapping = new InterfaceMapping(nanosLong);
      }
      if (LOG.isInfoEnabled()) {
        LOG.info(
            "refresh interface: {}, ref: {}, app: {}, mapping: {}",
            domain.getInterfaceName(),
            domain.isReference(),
            domain.getAppName(),
            mapping);
      }
      interfaceApps.put(domain.getInterfaceName(), mapping);
      return;
    }
    if (nanosLong > mapping.getNanosVersion()) {
      InterfaceMapping newMapping = null;
      if (domain.isReference()) {
        newMapping = new InterfaceMapping(nanosLong, mapping.getApps(), domain.getAppName());
      } else {
        Set<String> prev = Sets.newHashSet(mapping.getApps());
        prev.remove(domain.getAppName());
        newMapping = new InterfaceMapping(nanosLong, prev, domain.getAppName());
      }
      if (LOG.isInfoEnabled()) {
        LOG.info(
            "update interface mapping: {}, ref: {}, app: {}, newMapping: {}, oldMapping: {}",
            domain.getInterfaceName(),
            domain.isReference(),
            domain.getAppName(),
            newMapping,
            mapping);
      }
      interfaceApps.put(domain.getInterfaceName(), newMapping);
    } else {
      LOG.error(
          "[IgnoreUpdateCache]ignored refresh index, interfac={}, newVersion={} , current mapping={}",
          domain.getInterfaceName(),
          nanosLong,
          mapping);
    }
  }

  public synchronized int refresh(String dataCenter) {
    final long last = maxId;
    List<InterfaceAppsIndexDomain> afters =
        interfaceAppsIndexMapper.queryLargeThan(dataCenter, last, refreshLimit);

    if (LOG.isInfoEnabled()) {
      LOG.info("refresh madId={}, afters={},", last, afters.size());
    }

    if (CollectionUtils.isEmpty(afters)) {
      return 0;
    }

    // trigger refresh interface index, must be sorted by id
    for (InterfaceAppsIndexDomain interfaceApps : afters) {
      triggerRefreshCache(interfaceApps);
    }

    // update madId
    InterfaceAppsIndexDomain max = afters.get(afters.size() - 1);
    if (maxId < max.getId()) {
      this.maxId = max.getId();
      LOG.info("update maxId {} to {}", last, maxId);
    } else {
      LOG.info("skip update maxId {}, got={}", maxId, max.getId());
    }
    return afters.size();
  }

  @VisibleForTesting
  public Map<String, InterfaceMapping> getInterfaceApps() {
    return interfaceApps;
  }
}
