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

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.exception.RevisionNotExistException;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.jdbc.repository.batch.AppRevisionBatchQueryCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionJdbcRepository.java, v 0.1 2021年01月17日 15:45 xiaojian.xj Exp $
 */
public class AppRevisionJdbcRepository implements AppRevisionRepository {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");

  /** map: <revision, AppRevision> */
  private final LoadingCache<String, AppRevision> registry;

  /** map: <revision, AppRevision> */
  private final LoadingCache<String, AppRevision> heartbeatMap;

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Autowired private AppRevisionBatchQueryCallable appRevisionBatchQueryCallable;

  @Resource private InterfaceAppsJdbcRepository interfaceAppsJdbcRepository;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  public AppRevisionJdbcRepository() {
    this.registry =
        CacheBuilder.newBuilder()
            .maximumSize(10000L)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, AppRevision>() {
                  @Override
                  public AppRevision load(String revision) throws InterruptedException {

                    TaskEvent task = appRevisionBatchQueryCallable.new TaskEvent(revision);
                    InvokeFuture future = appRevisionBatchQueryCallable.commit(task);

                    if (future.isSuccess()) {
                      Object response = future.getResponse();
                      if (response == null) {
                        throw new RevisionNotExistException(revision);
                      }
                      AppRevision appRevision = (AppRevision) response;
                      return appRevision;
                    } else {
                      throw new RevisionNotExistException(revision);
                    }
                  }
                });

    this.heartbeatMap =
        CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, AppRevision>() {
                  @Override
                  public AppRevision load(String revision) throws RevisionNotExistException {

                    AppRevisionDomain domain =
                        appRevisionMapper.checkExist(defaultCommonConfig.getClusterId(), revision);
                    if (domain == null) {
                      throw new RevisionNotExistException(revision);
                    } else {
                      return new AppRevision(
                          domain.getDataCenter(), domain.getRevision(), domain.getGmtModify());
                    }
                  }
                });
  }

  @Override
  public void register(AppRevision appRevision) throws Exception {
    if (appRevision == null) {
      throw new RuntimeException("jdbc register app revision error, appRevision is null.");
    }

    // query cache, if not exist then query database
    try {
      AppRevision revision = registry.get(appRevision.getRevision());
      if (revision != null) {
        heartbeatMap.put(
            appRevision.getRevision(),
            new AppRevision(
                defaultCommonConfig.getClusterId(), appRevision.getRevision(), new Date()));
        return;
      }
    } catch (Throwable e) {
      if (e.getCause() instanceof RevisionNotExistException) {
        LOG.info(String.format("new revision: %s register.", appRevision.getRevision()));
      } else {
        LOG.error(String.format("new revision: %s register error.", appRevision.getRevision()), e);
        throw e;
      }
    }

    // new revision, save into database

    // it will ignore ON DUPLICATE KEY, return effect rows number
    interfaceAppsJdbcRepository.batchSave(
        appRevision.getAppName(), appRevision.getInterfaceMap().keySet());

    // it will ignore ON DUPLICATE KEY
    appRevisionMapper.insert(
        AppRevisionDomainConvertor.convert2Domain(defaultCommonConfig.getClusterId(), appRevision));

    registry.put(appRevision.getRevision(), appRevision);

    heartbeatMap.put(
        appRevision.getRevision(),
        new AppRevision(appRevision.getDataCenter(), appRevision.getRevision(), new Date()));
  }

  @Override
  public void refresh() {

    try {
      interfaceAppsJdbcRepository.refresh(defaultCommonConfig.getClusterId());
    } catch (Throwable e) {
      LOG.error("jdbc refresh revisions failed ", e);
      throw new RuntimeException("jdbc refresh revision failed", e);
    }
  }

  @Override
  public AppRevision queryRevision(String revision) {

    try {
      return registry.get(revision);
    } catch (ExecutionException e) {
      LOG.error(String.format("jdbc refresh revision failed, revision: %s", revision), e);
      throw new RuntimeException("jdbc refresh revision failed", e);
    }
  }

  @Override
  public AppRevision heartbeat(String revision) {

    try {
      AppRevision appRevision = heartbeatMap.get(revision);
      if (appRevision != null) {
        appRevision.setLastHeartbeat(new Date());
        heartbeatMap.put(revision, appRevision);
      }
      return appRevision;
    } catch (Throwable e) {
      if (e.getCause() instanceof RevisionNotExistException) {
        LOG.info(String.format("revision: %s heartbeat, not exist in db.", revision));
      }
      LOG.error(String.format("jdbc revision heartbeat failed, revision: %s", revision), e);
      return null;
    }
  }

  /**
   * Getter method for property <tt>heartbeatMap</tt>.
   *
   * @return property value of heartbeatMap
   */
  public Map<String, AppRevision> getHeartbeatMap() {
    return heartbeatMap.asMap();
  }

  public void invalidateHeartbeat(Collection<String> keys) {
    for (String key : keys) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Invalidating heartbeat cache key: {} {}", key);
      }
      heartbeatMap.invalidate(key);
    }
  }

  @VisibleForTesting
  LoadingCache<String, AppRevision> getRevisions() {
    return registry;
  }
}
