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
import com.alipay.sofa.registry.jdbc.exception.AppRevisionQueryException;
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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.REVISION_CACHE_HIT_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Fetch.REVISION_CACHE_MISS_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.REVISION_HEARTBEAT_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.REVISION_REGISTER_COUNTER;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionJdbcRepository.java, v 0.1 2021年01月17日 15:45 xiaojian.xj Exp $
 */
public class AppRevisionJdbcRepository implements AppRevisionRepository {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevision]");

  /** map: <revision, AppRevision> */
  private final LoadingCache<String, AppRevision> registry;

  /** map: <revision, AppRevision> */
  private final AtomicReference<ConcurrentHashMap.KeySetView> heartbeatSet =
      new AtomicReference<>();

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

                    REVISION_CACHE_MISS_COUNTER.inc();
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
                      throw new AppRevisionQueryException(revision, future.getMessage());
                    }
                  }
                });

    heartbeatSet.set(new ConcurrentHashMap<>().newKeySet());
  }

  @Override
  public void register(AppRevision appRevision) throws Exception {
    if (appRevision == null) {
      throw new RuntimeException("jdbc register app revision error, appRevision is null.");
    }

    // query database
    try {
      AppRevisionDomain revision =
          appRevisionMapper.checkExist(
              defaultCommonConfig.getClusterId(), appRevision.getRevision());
      if (revision != null) {
        return;
      }
    } catch (Throwable e) {
      LOG.error("new revision:{} register error.", appRevision.getRevision(), e);
      throw e;
    }

    // new revision, save into database
    REVISION_REGISTER_COUNTER.inc();

    // it will ignore ON DUPLICATE KEY, return effect rows number
    interfaceAppsJdbcRepository.batchSave(
        appRevision.getAppName(), appRevision.getInterfaceMap().keySet());

    // it will ignore ON DUPLICATE KEY
    appRevisionMapper.insert(
        AppRevisionDomainConvertor.convert2Domain(defaultCommonConfig.getClusterId(), appRevision));
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
      AppRevision appRevision = registry.getIfPresent(revision);
      if (appRevision != null) {
        REVISION_CACHE_HIT_COUNTER.inc();
        return appRevision;
      }
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

    try {
      if (heartbeatSet.get().contains(revision)) {
        return true;
      }
      AppRevisionDomain domain =
          appRevisionMapper.checkExist(defaultCommonConfig.getClusterId(), revision);

      if (domain != null) {
        heartbeatSet.get().add(revision);
        return true;
      }
      return false;
    } catch (Throwable e) {
      LOG.error("jdbc revision heartbeat failed, revision: %{}", revision, e);
      return false;
    }
  }

  /**
   * Getter method for property <tt>heartbeatMap</tt>.
   *
   * @return property value of heartbeatMap
   */
  public AtomicReference<ConcurrentHashMap.KeySetView> getHeartbeatSet() {

    return heartbeatSet;
  }

  public void invalidateHeartbeat(Collection<String> keys) {
    if (LOG.isInfoEnabled()) {
      LOG.info("Invalidating heartbeat cache keys: {}", keys);
    }
    heartbeatSet.get().removeAll(keys);
  }

  @VisibleForTesting
  LoadingCache<String, AppRevision> getRevisions() {
    return registry;
  }
}
