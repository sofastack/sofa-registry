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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

public class InterfaceAppsIndexCleaner implements MetaLeaderService.MetaLeaderElectorListener {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceApps]");

  final Renewer renewer = new Renewer();
  final Cleaner cleaner = new Cleaner();

  private final int maxRemoved = 100;

  @Autowired AppRevisionMapper appRevisionMapper;

  @Autowired InterfaceAppsIndexMapper interfaceAppsIndexMapper;

  @Autowired DefaultCommonConfig defaultCommonConfig;

  @Autowired MetaLeaderService metaLeaderService;

  @Autowired MetadataConfig metadataConfig;

  ConsecutiveSuccess consecutiveSuccess;

  public InterfaceAppsIndexCleaner() {}

  public InterfaceAppsIndexCleaner(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @PostConstruct
  public void init() {
    consecutiveSuccess =
        new ConsecutiveSuccess(
            1, metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 60 * 1000 * 2);
    ConcurrentUtils.createDaemonThread(
            InterfaceAppsIndexCleaner.class.getSimpleName() + "-renewer", renewer)
        .start();
    ConcurrentUtils.createDaemonThread(
            InterfaceAppsIndexCleaner.class.getSimpleName() + "-cleaner", cleaner)
        .start();
    metaLeaderService.registerListener(this);
  }

  void renew() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    try {
      long start = 0;
      int page = 100;
      Map<String, Set<String>> mappings = Maps.newHashMap();
      while (true) {
        List<AppRevisionDomain> revisionDomains =
            appRevisionMapper.listRevisions(defaultCommonConfig.getClusterId(), start, page);
        for (AppRevisionDomain domain : revisionDomains) {
          start = Math.max(start, domain.getId());
          if (domain.isDeleted()) {
            continue;
          }
          AppRevision revision = AppRevisionDomainConvertor.convert2Revision(domain);
          String appName = domain.getAppName();
          for (String interfaceName : revision.getInterfaceMap().keySet()) {
            mappings.computeIfAbsent(appName, k -> Sets.newHashSet()).add(interfaceName);
          }
        }
        if (revisionDomains.size() < page) {
          break;
        }
      }
      for (Map.Entry<String, Set<String>> entry : mappings.entrySet()) {
        String appName = entry.getKey();
        for (String interfaceName : entry.getValue()) {
          InterfaceAppsIndexDomain domain =
              new InterfaceAppsIndexDomain(
                  defaultCommonConfig.getClusterId(), interfaceName, appName);
          if (interfaceAppsIndexMapper.update(domain) == 0) {
            interfaceAppsIndexMapper.replace(domain);
          }
          ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        }
      }
      consecutiveSuccess.success();
    } catch (Throwable e) {
      LOG.error("renew interface apps index failed:", e);
      consecutiveSuccess.fail();
    }
  }

  private Date dateBeforeNow(int minutes) {
    Calendar calendar = Calendar.getInstance();
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
    List<InterfaceAppsIndexDomain> expiredDomains =
        interfaceAppsIndexMapper.getExpired(
            defaultCommonConfig.getClusterId(),
            dateBeforeNow(metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 5),
            maxRemoved);
    for (InterfaceAppsIndexDomain domain : expiredDomains) {
      domain.setReference(false);
      interfaceAppsIndexMapper.replace(domain);
      LOG.info(
          "mark deleted interface app mapping: {}=>{}",
          domain.getInterfaceName(),
          domain.getAppName());
      ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
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
      int base = metadataConfig.getInterfaceAppsIndexRenewIntervalMinutes() * 60 * 1000;
      return (int) (base + Math.random() * base);
    }

    @Override
    public void runUnthrowable() {
      markDeleted();
    }
  }
}
