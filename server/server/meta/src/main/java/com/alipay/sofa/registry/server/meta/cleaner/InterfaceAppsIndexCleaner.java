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
package com.alipay.sofa.registry.server.meta.cleaner;

import com.alipay.sofa.registry.cache.ConsecutiveSuccess;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class InterfaceAppsIndexCleaner
    implements MetaLeaderService.MetaLeaderElectorListener,
        ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceApps]");

  private static final boolean DEFAULT_ENABLED = false;

  final Renewer renewer = new Renewer();

  final Cleaner cleaner = new Cleaner();

  @Autowired AppRevisionRepository appRevisionRepository;

  @Autowired InterfaceAppsRepository interfaceAppsRepository;

  @Autowired MetaLeaderService metaLeaderService;

  @Autowired MetadataConfig metadataConfig;

  @Autowired ProvideDataService provideDataService;

  @Autowired DateNowRepository dateNowRepository;

  @Autowired MetaServerConfig metaServerConfig;

  ConsecutiveSuccess consecutiveSuccess;

  @PostConstruct
  public void init() {
    consecutiveSuccess =
        new ConsecutiveSuccess(
            3, (long) metadataConfig.getRevisionRenewIntervalMinutes() * 60 * 1000 * 5);
    metaLeaderService.registerListener(this);
  }

  public InterfaceAppsIndexCleaner() {}

  public InterfaceAppsIndexCleaner(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start();
  }

  public void start() {
    ConcurrentUtils.createDaemonThread(
            InterfaceAppsIndexCleaner.class.getSimpleName() + "-renewer", renewer)
        .start();
    ConcurrentUtils.createDaemonThread(
            InterfaceAppsIndexCleaner.class.getSimpleName() + "-cleaner", cleaner)
        .start();
    LOG.info("InterfaceAppsIndexCleaner started");
  }

  protected AppRevision revisionConvert(AppRevision revision) {
    return revision;
  }

  public void renew() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    try {
      long start = 0;
      int page = 100;
      Map<String, Set<String>> mappings = Maps.newHashMap();
      while (true) {
        List<AppRevision> revisions = appRevisionRepository.listFromStorage(start, page);
        for (AppRevision revision : revisions) {
          start = Math.max(start, revision.getId());
          if (revision.isDeleted()) {
            continue;
          }

          String appName = revision.getAppName();
          for (String interfaceName : revision.getInterfaceMap().keySet()) {
            mappings.computeIfAbsent(appName, k -> Sets.newHashSet()).add(interfaceName);
          }
        }
        if (revisions.size() < page) {
          break;
        }
      }
      for (Map.Entry<String, Set<String>> entry : mappings.entrySet()) {
        String appName = entry.getKey();
        for (String interfaceName : entry.getValue()) {
          interfaceAppsRepository.renew(interfaceName, appName);
          ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        }
      }
      LOG.info("renew interface apps index succeed");
      consecutiveSuccess.success();
    } catch (Throwable e) {
      consecutiveSuccess.fail();
      LOG.error("renew interface apps index failed:", e);
    }
  }

  private boolean isEnabled() {
    DBResponse<PersistenceData> ret =
        provideDataService.queryProvideData(ValueConstants.INTERFACE_APP_CLEANER_ENABLED_DATA_ID);
    if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
      PersistenceData data = ret.getEntity();
      return Boolean.parseBoolean(data.getData());
    }
    return DEFAULT_ENABLED;
  }

  Date dateBeforeNow(int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateNowRepository.getNow());
    calendar.add(Calendar.MINUTE, -minutes);
    return calendar.getTime();
  }

  void cleanup() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    if (!consecutiveSuccess.check()) {
      return;
    }
    int count =
        interfaceAppsRepository.cleanDeleted(
            dateBeforeNow(metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 15),
            metaServerConfig.getInterfaceMaxRemove());
    if (count > 0) {
      LOG.info("clean up {} interface app", count);
    }
  }

  public void startRenew() {
    renewer.wakeup();
  }

  public void startCleaner() {
    cleaner.wakeup();
  }

  public void setEnabled(boolean enabled) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.INTERFACE_APP_CLEANER_ENABLED_DATA_ID, Boolean.toString(enabled));
    try {
      provideDataService.saveProvideData(persistenceData);
    } catch (Exception e) {
      LOG.error("set interface app cleaner failed: ", e);
      throw new RuntimeException(
          StringFormatter.format("set interface app cleaner failed: {}", e.getMessage()));
    }
  }

  @Override
  public void becomeLeader() {
    consecutiveSuccess.clear();
  }

  @Override
  public void loseLeader() {
    consecutiveSuccess.clear();
  }

  final class Renewer extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      return (metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 60 * 1000);
    }

    @Override
    public void runUnthrowable() {
      renew();
    }
  }

  final class Cleaner extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      int base = metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 1000 * 60;
      return (int) (base + Math.random() * base);
    }

    @Override
    public void runUnthrowable() {
      if (!isEnabled()) {
        return;
      }
      cleanup();
    }
  }
}
