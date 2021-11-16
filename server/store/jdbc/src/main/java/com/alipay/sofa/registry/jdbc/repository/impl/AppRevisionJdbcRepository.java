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

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.REVISION_CACHE_HIT_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.REVISION_CACHE_MISS_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.REVISION_REGISTER_COUNTER;

import com.alipay.sofa.registry.cache.CacheCleaner;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.concurrent.CachedExecutor;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.exception.RevisionNotExistException;
import com.alipay.sofa.registry.jdbc.informer.BaseInformer;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionJdbcRepository.java, v 0.1 2021年01月17日 15:45 xiaojian.xj Exp $
 */
public class AppRevisionJdbcRepository implements AppRevisionRepository, RecoverConfig {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");

  private static final Logger COUNT_LOG =
      LoggerFactory.getLogger("METADATA-COUNT", "[AppRevision]");

  /** map: <revision, AppRevision> */
  private final LoadingCache<String, AppRevision> registry;

  private final Cache<String, Boolean> localRevisions =
      CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();

  private final CachedExecutor<String, Boolean> cachedExecutor = new CachedExecutor<>(1000 * 10);

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Resource private InterfaceAppsJdbcRepository interfaceAppsJdbcRepository;

  @Autowired private DateNowRepository dateNowRepository;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  final Informer informer;

  public AppRevisionJdbcRepository() {
    this.registry =
        CacheBuilder.newBuilder()
            .maximumSize(10000L)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, AppRevision>() {
                  @Override
                  public AppRevision load(String revision) throws InterruptedException {
                    REVISION_CACHE_MISS_COUNTER.inc();
                    AppRevisionDomain revisionDomain =
                        appRevisionMapper.queryRevision(
                            defaultCommonConfig.getClusterId(tableName()), revision);
                    if (revisionDomain == null || revisionDomain.isDeleted()) {
                      throw new RevisionNotExistException(revision);
                    }
                    return AppRevisionDomainConvertor.convert2Revision(revisionDomain);
                  }
                });
    CacheCleaner.autoClean(localRevisions, 1000 * 60 * 10);
    informer = new Informer();
  }

  @PostConstruct
  public void init() {
    informer.setEnabled(true);
    informer.start();

    Timer timer = new Timer("AppRevisionDigest", true);
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            try {
              LOG.info("informer revision size: {}", informer.getContainer().size());
            } catch (Throwable t) {
              LOG.safeError("informer revision size digest error", (Throwable) t);
            }
          }
        },
        60 * 1000,
        60 * 1000);
  }

  @Override
  public void register(AppRevision appRevision) throws Exception {
    if (appRevision == null) {
      throw new RuntimeException("jdbc register app revision error, appRevision is null.");
    }
    for (String interfaceName : appRevision.getInterfaceMap().keySet()) {
      interfaceAppsJdbcRepository.register(interfaceName, appRevision.getAppName());
    }
    localRevisions.put(appRevision.getRevision(), true);
    if (informer.getContainer().containsRevisionId(appRevision.getRevision())) {
      return;
    }
    AppRevisionDomain domain =
        AppRevisionDomainConvertor.convert2Domain(
            defaultCommonConfig.getClusterId(tableName()), appRevision);
    // new revision, save into database
    REVISION_REGISTER_COUNTER.inc();
    refreshEntryToStorage(domain);
  }

  @Override
  public AppRevision queryRevision(String revision) {
    AppRevision appRevision;
    appRevision = registry.getIfPresent(revision);
    if (appRevision != null) {
      REVISION_CACHE_HIT_COUNTER.inc();
      return appRevision;
    }
    try {
      return registry.get(revision);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RevisionNotExistException) {
        LOG.info("jdbc query revision failed, revision: {} not exist in db", revision, e);
        return null;
      }

      LOG.error("jdbc query revision error, revision: {}", revision, e);
      throw new RuntimeException("jdbc refresh revision failed", e);
    }
  }

  @Override
  public boolean heartbeat(String revision) {
    localRevisions.put(revision, true);
    return informer.getContainer().containsRevisionId(revision);
  }

  @VisibleForTesting
  LoadingCache<String, AppRevision> getRevisions() {
    return registry;
  }

  @VisibleForTesting
  void cleanCache() {
    registry.invalidateAll();
    cachedExecutor.clean();
  }

  @Override
  public Map<String, Integer> countByApp() {
    Map<String, Integer> counts = Maps.newHashMap();
    informer
        .getContainer()
        .foreach(
            (String revision, String appname) -> {
              counts.put(appname, counts.getOrDefault(appname, 0) + 1);
            });
    return counts;
  }

  protected void refreshEntryToStorage(AppRevisionDomain entry) {
    try {
      cachedExecutor.execute(
          entry.getRevision(),
          () -> {
            if (appRevisionMapper.heartbeat(entry.getDataCenter(), entry.getRevision()) == 0) {
              appRevisionMapper.replace(entry);
            }
            LOG.info("insert revision {}, succeed", entry.getRevision());
            return true;
          });
    } catch (Exception e) {
      LOG.error("refresh to db failed: ", e);
      throw new RuntimeException(
          StringFormatter.format("refresh to db failed: {}", e.getMessage()));
    }
  }

  public Collection<String> availableRevisions() {
    return localRevisions.asMap().keySet();
  }

  @Override
  public List<AppRevision> listFromStorage(long start, int limit) {
    List<AppRevisionDomain> domains =
        appRevisionMapper.listRevisions(
            defaultCommonConfig.getClusterId(tableName()), start, limit);
    return AppRevisionDomainConvertor.convert2Revisions(domains);
  }

  @Override
  public void waitSynced() {
    informer.waitSynced();
  }

  @Override
  public List<AppRevision> getExpired(Date beforeTime, int limit) {
    List<AppRevisionDomain> expired =
        appRevisionMapper.getExpired(
            defaultCommonConfig.getClusterId(tableName()), beforeTime, limit);
    return AppRevisionDomainConvertor.convert2Revisions(expired);
  }

  @Override
  public void replace(AppRevision appRevision) {
    appRevisionMapper.replace(
        AppRevisionDomainConvertor.convert2Domain(
            defaultCommonConfig.getClusterId(tableName()), appRevision));
  }

  @Override
  public int cleanDeleted(Date beforeTime, int limit) {
    return appRevisionMapper.cleanDeleted(
        defaultCommonConfig.getClusterId(tableName()), beforeTime, limit);
  }

  @Override
  public String tableName() {
    return TableEnum.APP_REVISION.getTableName();
  }

  class Informer extends BaseInformer<AppRevisionDomain, AppRevisionContainer> {

    public Informer() {
      super("AppRevision", LOG);
    }

    @Override
    protected AppRevisionContainer containerFactory() {
      return new AppRevisionContainer();
    }

    @Override
    protected List<AppRevisionDomain> listFromStorage(long start, int limit) {
      return appRevisionMapper.listRevisions(
          defaultCommonConfig.getClusterId(tableName()), start, limit);
    }

    @Override
    protected Date getNow() {
      return dateNowRepository.getNow();
    }
  }
}
