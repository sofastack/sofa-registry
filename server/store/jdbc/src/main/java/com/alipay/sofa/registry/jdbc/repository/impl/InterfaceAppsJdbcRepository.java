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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import java.util.*;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

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

  @PostConstruct
  public void init() {
    informer.setEnabled(true);
    informer.start();
  }

  @Override
  public InterfaceMapping getAppNames(String dataInfoId) {
    InterfaceAppsIndexContainer index = informer.getContainer();
    InterfaceMapping mapping = index.getAppMapping(dataInfoId);
    if (mapping == null) {
      return new InterfaceMapping(-1);
    }
    return mapping;
  }

  @Override
  public void register(String interfaceName, String appName) {
    InterfaceAppsIndexContainer c = informer.getContainer();
    if (c.containsName(interfaceName, appName)) {
      return;
    }
    refreshEntryToStorage(
        new InterfaceAppsIndexDomain(
            defaultCommonConfig.getClusterId(tableName()), interfaceName, appName));
  }

  @Override
  public void renew(String interfaceName, String appName) {
    refreshEntryToStorage(
        new InterfaceAppsIndexDomain(
            defaultCommonConfig.getClusterId(tableName()), interfaceName, appName));
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
                "insert interface app mapping {}=>{} succeed",
                entry.getInterfaceName(),
                entry.getAppName());
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
  public String tableName() {
    return TableEnum.INTERFACE_APP_INDEX.getTableName();
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
      return interfaceAppsIndexMapper.queryLargeThan(
          defaultCommonConfig.getClusterId(tableName()), start, limit);
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
        InterfaceMapping newMapping = newContainer.getAppMapping(interfaceName);
        InterfaceMapping currentMapping = current.getAppMapping(interfaceName);
        if (newMapping == null
            || newMapping.getNanosVersion() < currentMapping.getNanosVersion()
            || (newMapping.getNanosVersion() == currentMapping.getNanosVersion()
                && !newMapping.getApps().equals(currentMapping.getApps()))) {
          if (conflictCallback != null) {
            conflictCallback.callback(current, newContainer);
          }
          LOG.error("version conflict current: {}, new: {}", currentMapping, newMapping);
        }
      }
    }
  }

  interface ConflictCallback {
    void callback(InterfaceAppsIndexContainer current, InterfaceAppsIndexContainer newContainer);
  }
}
