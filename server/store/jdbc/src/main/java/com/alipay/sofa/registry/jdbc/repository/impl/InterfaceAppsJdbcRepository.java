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
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.concurrent.CachedExecutor;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.informer.BaseInformer;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.jdbc.repository.impl.AppRevisionJdbcRepository.Informer;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepository.java, v 0.1 2021年01月24日 19:57 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepository implements InterfaceAppsRepository, RecoverConfig {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceApps]");

  @Autowired private InterfaceAppsIndexMapper interfaceAppsIndexMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Autowired private DateNowRepository dateNowRepository;

  private final CachedExecutor<Tuple<String, String>, Boolean> cachedExecutor =
      new CachedExecutor<>(1000 * 10);

  final Informer informer;

  public InterfaceAppsJdbcRepository() {
    informer = new Informer();
  }

  private Set<String> dataCenters = Sets.newConcurrentHashSet();

  @Override
  public InterfaceMapping getAppNames(String dataInfoId) {
    InterfaceAppsIndexContainer index = informer.getContainer();
    Map<String, InterfaceMapping> mappings = index.getAppMapping(dataInfoId);
    if (CollectionUtils.isEmpty(mappings)) {
      return new InterfaceMapping(-1);
    }

    long maxVersion = -1L;
    Set<String> apps = Sets.newHashSet();
    for (InterfaceMapping value : mappings.values()) {
      if (value.getNanosVersion() > maxVersion) {
        maxVersion = value.getNanosVersion();
      }
      apps.addAll(value.getApps());
    }
    InterfaceMapping ret = new InterfaceMapping(maxVersion, apps);

    return ret;
  }

  @Override
  public void register(String appName, Set<String> interfaceNames) {
    String localDataCenter = defaultCommonConfig.getDefaultClusterId();
    InterfaceAppsIndexContainer c = informer.getContainer();

    for (String interfaceName : interfaceNames) {
      if (c.containsName(localDataCenter, interfaceName, appName)) {
        continue;
      }
      refreshEntryToStorage(new InterfaceAppsIndexDomain(localDataCenter, interfaceName, appName));
    }
  }

  @Override
  public void renew(String interfaceName, String appName) {
    refreshEntryToStorage(
        new InterfaceAppsIndexDomain(
            defaultCommonConfig.getDefaultClusterId(), interfaceName, appName));
  }

  @Override
  public void startSynced() {
    ParaCheckUtil.checkNotEmpty(dataCenters, "dataCenters");

    // after set datacenters
    informer.setEnabled(true);
    informer.start();
  }

  @Override
  public void waitSynced() {
    informer.waitSynced();
  }

  protected void refreshEntryToStorage(InterfaceAppsIndexDomain entry) {
    try {
      cachedExecutor.execute(
          new Tuple<>(entry.getInterfaceName(), entry.getAppName()),
          () -> {
            if (interfaceAppsIndexMapper.update(entry) == 0) {
              interfaceAppsIndexMapper.replace(entry);
            }
            LOG.info(
                "insert interface app mapping {}=>{} succeed,entry:{}",
                entry.getInterfaceName(),
                entry.getAppName(),
                entry);
            return true;
          });
    } catch (Exception e) {
      LOG.error("refresh to db failed: ", e);
      throw new RuntimeException(
          StringFormatter.format("refresh to db failed: {}", e.getMessage()));
    }
  }

  @VisibleForTesting
  void cleanCache() {
    cachedExecutor.clean();
  }

  public long getDataVersion() {
    return informer.getLastLoadId();
  }

  @Override
  public Map<String, Map<String, InterfaceMapping>> allServiceMapping() {
    return informer.getContainer().allServiceMapping();
  }

  @Override
  public String tableName() {
    return TableEnum.INTERFACE_APP_INDEX.getTableName();
  }

  @Override
  public Set<String> dataCenters() {
    return new HashSet<>(dataCenters);
  }

  @Override
  public synchronized void setDataCenters(Set<String> dataCenters) {

    if (!this.dataCenters.equals(dataCenters)) {
      // wakeup list loop to rebuild container
      LOG.info("dataCenters change from {} to {}", this.dataCenters, dataCenters);
      this.dataCenters = dataCenters;
      informer.listWakeup();
    }
  }

  class Informer extends BaseInformer<InterfaceAppsIndexDomain, InterfaceAppsIndexContainer> {
    private ConflictCallback conflictCallback;

    public Informer() {
      super("InterfaceAppsIndex", LOG);
    }

    @Override
    protected InterfaceAppsIndexContainer containerFactory() {
      return new InterfaceAppsIndexContainer();
    }

    @Override
    protected List<InterfaceAppsIndexDomain> listFromStorage(long start, int limit) {
      List<InterfaceAppsIndexDomain> res =
          interfaceAppsIndexMapper.queryLargeThan(dataCenters, start, limit);
      LOG.info(
          "query apps by interface, dataCenters:{}, start:{}, limit:{}, res:{}",
          dataCenters,
          start,
          limit,
          res.size());
      return res;
    }

    @Override
    protected Date getNow() {
      return dateNowRepository.getNow();
    }

    @VisibleForTesting
    public void setConflictCallback(ConflictCallback runnable) {
      conflictCallback = runnable;
    }

    @Override
    protected void preList(InterfaceAppsIndexContainer newContainer) {
      InterfaceAppsIndexContainer current = this.container;
      for (String interfaceName : current.interfaces()) {
        Map<String, InterfaceMapping> newMappings = newContainer.getAppMapping(interfaceName);
        Map<String, InterfaceMapping> currentMappings = current.getAppMapping(interfaceName);
        for (Entry<String, InterfaceMapping> entry : currentMappings.entrySet()) {
          InterfaceMapping newMapping = newMappings.get(entry.getKey());
          InterfaceMapping currentMapping = entry.getValue();
          if (newMapping == null
              || newMapping.getNanosVersion() < currentMapping.getNanosVersion()
              || (newMapping.getNanosVersion() == currentMapping.getNanosVersion()
                  && !newMapping.getApps().equals(currentMapping.getApps()))) {
            if (conflictCallback != null) {
              conflictCallback.callback(current, newContainer);
            }
            LOG.error(
                "version conflict dataCenter: {}, current: {}, new: {}",
                entry.getKey(),
                currentMapping,
                newMapping);
          }
        }
      }
    }
  }

  interface ConflictCallback {
    void callback(InterfaceAppsIndexContainer current, InterfaceAppsIndexContainer newContainer);
  }

  /**
   * Setter method for property <tt>interfaceAppsIndexMapper</tt>.
   *
   * @param interfaceAppsIndexMapper value to be assigned to property interfaceAppsIndexMapper
   * @return InterfaceAppsJdbcRepository
   */
  @VisibleForTesting
  public InterfaceAppsJdbcRepository setInterfaceAppsIndexMapper(
      InterfaceAppsIndexMapper interfaceAppsIndexMapper) {
    this.interfaceAppsIndexMapper = interfaceAppsIndexMapper;
    return this;
  }

  /**
   * Setter method for property <tt>defaultCommonConfig</tt>.
   *
   * @param defaultCommonConfig value to be assigned to property defaultCommonConfig
   * @return InterfaceAppsJdbcRepository
   */
  @VisibleForTesting
  public InterfaceAppsJdbcRepository setDefaultCommonConfig(
      DefaultCommonConfig defaultCommonConfig) {
    this.defaultCommonConfig = defaultCommonConfig;
    return this;
  }
}
