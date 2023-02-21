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

import com.alipay.remoting.util.StringUtils;
import com.alipay.sofa.registry.cache.ConsecutiveSuccess;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSliceRequest;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

public class AppRevisionCleaner
    implements MetaLeaderService.MetaLeaderElectorListener,
        ApplicationListener<ContextRefreshedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");
  private static final Logger DIGEST_LOG =
      LoggerFactory.getLogger("METADATA-DIGEST", "[AppRevisionDigest]");

  private int lastSlotId = -1;
  private final int slotNum = 256;

  final Renewer renewer = new Renewer();
  final Cleaner cleaner = new Cleaner();

  private static final boolean DEFAULT_ENABLED = true;

  @Autowired DateNowRepository dateNowRepository;

  @Autowired AppRevisionRepository appRevisionRepository;

  @Autowired MetadataConfig metadataConfig;

  @Autowired DefaultSessionServerService sessionServerService;

  @Autowired MetaLeaderService metaLeaderService;

  @Autowired ProvideDataService provideDataService;

  @Autowired MetaServerConfig metaServerConfig;

  ConsecutiveSuccess consecutiveSuccess;

  public AppRevisionCleaner() {}

  public AppRevisionCleaner(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @PostConstruct
  public void init() {
    consecutiveSuccess =
        new ConsecutiveSuccess(
            slotNum * 3, (long) metadataConfig.getRevisionRenewIntervalMinutes() * 60 * 1000 * 5);
    metaLeaderService.registerListener(this);
  }

  public void start() {
    ConcurrentUtils.createDaemonThread(
            AppRevisionCleaner.class.getSimpleName() + "-renewer", renewer)
        .start();
    ConcurrentUtils.createDaemonThread(
            AppRevisionCleaner.class.getSimpleName() + "-cleaner", cleaner)
        .start();
    LOG.info("AppRevisionCleaner started");
  }

  void renew() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    int slotId = nextSlotId();
    Collection<AppRevisionSlice> slices = Lists.newArrayList();
    try {
      for (Object result :
          sessionServerService
              .broadcastInvoke(new AppRevisionSliceRequest(slotNum, slotId), 1000 * 30)
              .values()) {
        slices.add((AppRevisionSlice) result);
      }
      for (String revision : AppRevisionSlice.merge(slices).getRevisions()) {
        appRevisionRepository.heartbeatDB(revision);
        ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
      }
      consecutiveSuccess.success();
    } catch (Throwable e) {
      LOG.error("renew app revisions failed:", e);
      consecutiveSuccess.fail();
    }
  }

  Date dateBeforeNow(int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateNowRepository.getNow());
    calendar.add(Calendar.MINUTE, -minutes);
    return calendar.getTime();
  }

  void markDeleted() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    if (!consecutiveSuccess.check()) {
      return;
    }
    List<AppRevision> expired =
        appRevisionRepository.getExpired(
            dateBeforeNow(metadataConfig.getRevisionRenewIntervalMinutes() * 5),
            metaServerConfig.getAppRevisionMaxRemove());
    // before markDeleted refresh app revision switch
    appRevisionSwitchRefresh();
    for (AppRevision revision : expired) {
      revision.setDeleted(true);
      try {
        appRevisionRepository.replace(revision);
        LOG.info("mark deleted revision: {}", revision.getRevision());
        ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
      } catch (Throwable e) {
        LOG.error("mark deleted revision failed: {}", revision.getRevision(), e);
      }
    }
  }

  private void appRevisionSwitchRefresh() {
    DBResponse<PersistenceData> ret =
        provideDataService.queryProvideData(ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID);
    AppRevisionDomainConvertor.EnableConfig enableConfig = null;
    if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
      PersistenceData data = ret.getEntity();
      String switchString = data.getData();
      if (StringUtils.isNotBlank(switchString)) {
        try {
          enableConfig =
              JsonUtils.read(switchString, AppRevisionDomainConvertor.EnableConfig.class);
        } catch (Throwable e) {
          LOG.error("Decode appRevision write switch failed", e);
        }
      }
    }
    if (enableConfig != null) {
      LOG.info(
          "appRevisionSwitch prev={}/{}",
          AppRevisionDomainConvertor.getEnableConfig().isServiceParams(),
          AppRevisionDomainConvertor.getEnableConfig().isServiceParamsLarge());
      AppRevisionDomainConvertor.setEnableConfig(enableConfig);
      LOG.info(
          "appRevisionSwitch update={}/{}",
          enableConfig.isServiceParams(),
          enableConfig.isServiceParamsLarge());
    }
  }

  @Scheduled(initialDelay = 60000, fixedRate = 60000)
  public void digestAppRevision() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    try {
      Map<String, Integer> counts = appRevisionRepository.countByApp();
      for (Map.Entry<String, Integer> entry : counts.entrySet()) {
        String app = entry.getKey();
        int count = entry.getValue();
        if (count >= metaServerConfig.getAppRevisionCountAlarmThreshold()) {
          DIGEST_LOG.info("[AppRevisionCountAlarm]app={},count={}", app, count);
        }
      }
    } catch (Throwable e) {
      DIGEST_LOG.safeError("[AppRevisionCounter] digest failed: ", e);
    }
  }

  void cleanup() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    if (!consecutiveSuccess.check()) {
      return;
    }
    int count =
        appRevisionRepository.cleanDeleted(
            dateBeforeNow(metadataConfig.getRevisionRenewIntervalMinutes() * 10),
            metaServerConfig.getAppRevisionMaxRemove());
    if (count > 0) {
      LOG.info("clean up {} revisions", count);
    }
  }

  public void setEnabled(boolean enabled) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.APP_REVISION_CLEANER_ENABLED_DATA_ID, Boolean.toString(enabled));
    try {
      provideDataService.saveProvideData(persistenceData);
    } catch (Exception e) {
      LOG.error("set app revision cleaner failed: ", e);
      throw new RuntimeException(
          StringFormatter.format("set app revision cleaner failed: {}", e.getMessage()));
    }
  }

  private boolean isEnabled() {
    DBResponse<PersistenceData> ret =
        provideDataService.queryProvideData(ValueConstants.APP_REVISION_CLEANER_ENABLED_DATA_ID);
    if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
      PersistenceData data = ret.getEntity();
      return Boolean.parseBoolean(data.getData());
    }
    return DEFAULT_ENABLED;
  }

  synchronized int nextSlotId() {
    lastSlotId = (lastSlotId + 1) % slotNum;
    return lastSlotId;
  }

  @Override
  public void becomeLeader() {
    consecutiveSuccess.clear();
  }

  @Override
  public void loseLeader() {
    consecutiveSuccess.clear();
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start();
  }

  final class Renewer extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      return (metadataConfig.getRevisionRenewIntervalMinutes() * 1000 * 60 / slotNum);
    }

    @Override
    public void runUnthrowable() {
      renew();
    }
  }

  final class Cleaner extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      int base = metadataConfig.getRevisionRenewIntervalMinutes() * 1000 * 60;
      return (int) (base + Math.random() * base);
    }

    @Override
    public void runUnthrowable() {
      if (!isEnabled()) {
        return;
      }
      markDeleted();
      cleanup();
    }
  }
}
