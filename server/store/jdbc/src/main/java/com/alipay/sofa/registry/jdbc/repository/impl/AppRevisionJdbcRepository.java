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
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionJdbcRepository.java, v 0.1 2021年01月17日 15:45 xiaojian.xj Exp $
 */
public class AppRevisionJdbcRepository implements AppRevisionRepository, RecoverConfig {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");

  /** map: <revision, AppRevision> */
  private final LoadingCache<String, AppRevision> registry;

  private final Cache<String, Boolean> localRevisions =
      CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();

  private final CachedExecutor<String, Boolean> cachedExecutor = new CachedExecutor<>(1000 * 10);

  private final ScheduledExecutorService revisionDigestService =
      new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("RevisionDigest"));

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Resource private InterfaceAppsJdbcRepository interfaceAppsJdbcRepository;

  @Autowired private DateNowRepository dateNowRepository;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private final Informer informer = new Informer();

  private Set<String> dataCenters = Sets.newConcurrentHashSet();

  public AppRevisionJdbcRepository() {
    this.registry =
        CacheBuilder.newBuilder()
            .maximumSize(10000L)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, AppRevision>() {
                  @Override
                  public AppRevision load(String revision) throws InterruptedException {
                    ParaCheckUtil.checkNotEmpty(dataCenters, "dataCenters");

                    REVISION_CACHE_MISS_COUNTER.inc();
                    List<AppRevisionDomain> revisionDomains =
                        appRevisionMapper.queryRevision(dataCenters, revision);
                    if (CollectionUtils.isEmpty(revisionDomains)) {
                      throw new RevisionNotExistException(revision);
                    }
                    for (AppRevisionDomain revisionDomain : revisionDomains) {
                      if (!revisionDomain.isDeleted()) {
                        return AppRevisionDomainConvertor.convert2Revision(revisionDomain);
                      }
                    }
                    throw new RevisionNotExistException(revision);
                  }
                });
    CacheCleaner.autoClean(localRevisions, 1000 * 60 * 10);
  }

  @PostConstruct
  public void init() {

    revisionDigestService.scheduleAtFixedRate(
        () -> {
          try {
            LOG.info("informer revision size: {}", informer.getContainer().size());
          } catch (Throwable t) {
            LOG.safeError("informer revision size digest error", (Throwable) t);
          }
        },
        60,
        60,
        TimeUnit.SECONDS);
  }

  @Override
  public void register(AppRevision appRevision) throws Exception {
    if (appRevision == null) {
      throw new RuntimeException("jdbc register app revision error, appRevision is null.");
    }
    interfaceAppsJdbcRepository.register(
        appRevision.getAppName(), appRevision.getInterfaceMap().keySet());

    localRevisions.put(appRevision.getRevision(), true);
    if (informer.getContainer().containsRevisionId(appRevision.getRevision())) {
      return;
    }
    AppRevisionDomain domain =
        AppRevisionDomainConvertor.convert2Domain(
            defaultCommonConfig.getDefaultClusterId(), appRevision);
    // new revision, save into database
    REVISION_REGISTER_COUNTER.inc();
    refreshEntryToStorage(domain);
  }

  /**
   * check if revisionId exist
   *
   * @param revisionId revisionId
   * @return boolean
   */
  @Override
  public boolean exist(String revisionId) {
    return informer.getContainer().containsRevisionId(revisionId);
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

  @Override
  public boolean heartbeatDB(String revision) {
    int effect =
        appRevisionMapper.heartbeat(defaultCommonConfig.getClusterId(tableName()), revision);
    if (effect == 0) {
      LOG.error("revision: {} heartbeat fail.", revision);
    }
    return effect > 0;
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

  @Override
  public Set<String> allRevisionIds() {
    return informer.getContainer().allRevisionIds();
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
        appRevisionMapper.listRevisions(defaultCommonConfig.getDefaultClusterId(), start, limit);
    return AppRevisionDomainConvertor.convert2Revisions(domains);
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

  @Override
  public List<AppRevision> getExpired(Date beforeTime, int limit) {
    List<AppRevisionDomain> expired =
        appRevisionMapper.getExpired(defaultCommonConfig.getDefaultClusterId(), beforeTime, limit);
    return AppRevisionDomainConvertor.convert2Revisions(expired);
  }

  @Override
  public void replace(AppRevision appRevision) {
    appRevisionMapper.replace(
        AppRevisionDomainConvertor.convert2Domain(
            defaultCommonConfig.getDefaultClusterId(), appRevision));
  }

  @Override
  public int cleanDeleted(Date beforeTime, int limit) {
    return appRevisionMapper.cleanDeleted(
        defaultCommonConfig.getDefaultClusterId(), beforeTime, limit);
  }

  @Override
  public String tableName() {
    return TableEnum.APP_REVISION.getTableName();
  }

  @Override
  public Set<String> dataCenters() {
    return new HashSet<>(dataCenters);
  }

  @Override
  public synchronized void setDataCenters(Set<String> dataCenters) {
    if (!this.dataCenters.equals(dataCenters)) {
      LOG.info("dataCenters change from {} to {}", this.dataCenters, dataCenters);
      this.dataCenters = dataCenters;
    }
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
          defaultCommonConfig.getDefaultClusterId(), start, limit);
    }

    @Override
    protected Date getNow() {
      return dateNowRepository.getNow();
    }
  }
}
