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
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSliceRequest;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class AppRevisionCleaner
    implements MetaLeaderService.MetaLeaderElectorListener,
        ApplicationListener<ContextRefreshedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");

  private int lastSlotId = -1;
  private final int slotNum = 256;

  final Renewer renewer = new Renewer();
  final Cleaner cleaner = new Cleaner();
  private final int maxRemoved = 500;

  @Autowired AppRevisionMapper appRevisionMapper;

  @Autowired DefaultCommonConfig defaultCommonConfig;

  @Autowired MetadataConfig metadataConfig;

  @Autowired DefaultSessionServerService sessionServerService;

  @Autowired MetaLeaderService metaLeaderService;

  ConsecutiveSuccess consecutiveSuccess;

  public AppRevisionCleaner() {}

  public AppRevisionCleaner(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @PostConstruct
  public void init() {
    consecutiveSuccess =
        new ConsecutiveSuccess(
            slotNum * 3, metadataConfig.getRevisionRenewIntervalMinutes() * 60 * 1000 * 4);
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
        appRevisionMapper.heartbeat(defaultCommonConfig.getClusterId(), revision);
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
    calendar.setTime(appRevisionMapper.getNow().getNow());
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
    List<AppRevisionDomain> expiredDomains =
        appRevisionMapper.getExpired(
            defaultCommonConfig.getClusterId(),
            dateBeforeNow(metadataConfig.getRevisionRenewIntervalMinutes() * 5),
            maxRemoved);
    for (AppRevisionDomain domain : expiredDomains) {
      domain.setDeleted(true);
      appRevisionMapper.replace(domain);
      LOG.info("mark deleted revision: {}", domain.getRevision());
      ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
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
        appRevisionMapper.cleanDeleted(
            defaultCommonConfig.getClusterId(),
            dateBeforeNow(metadataConfig.getRevisionRenewIntervalMinutes() * 10),
            maxRemoved);
    if (count > 0) {
      LOG.info("clean up {} revisions", count);
    }
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
      markDeleted();
      cleanup();
    }
  }
}
