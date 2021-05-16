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

import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.jdbc.repository.batch.AppRevisionHeartbeatBatchCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.REVISION_GC_COUNTER;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionHeartbeatJdbcRepository.java, v 0.1 2021年02月09日 17:14 xiaojian.xj Exp $
 */
public class AppRevisionHeartbeatJdbcRepository implements AppRevisionHeartbeatRepository {

  private static final Logger LOG =
      LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevisionHeartbeat]");

  @Resource private AppRevisionJdbcRepository appRevisionJdbcRepository;

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Autowired private AppRevisionHeartbeatBatchCallable appRevisionHeartbeatBatchCallable;

  @Autowired private MetadataConfig metadataConfig;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private SingleFlight singleFlight = new SingleFlight();

  private Integer REVISION_GC_LIMIT;

  private static final Integer heartbeatCheckerSize = 1000;

  @PostConstruct
  public void postConstruct() {
    REVISION_GC_LIMIT = metadataConfig.getRevisionGcLimit();
  }

  @Override
  public void doAppRevisionHeartbeat() {

    try {
      singleFlight.execute(
          "app_revision_heartbeat",
          () -> {
            Map<String, InvokeFuture> futureMap = new HashMap<>();

            Set<String> heartbeatSet =
                appRevisionJdbcRepository
                    .getHeartbeatSet()
                    .getAndSet(new ConcurrentHashMap<>().newKeySet());
            for (String revision : heartbeatSet) {
              TaskEvent taskEvent = appRevisionHeartbeatBatchCallable.new TaskEvent(revision);
              InvokeFuture future = appRevisionHeartbeatBatchCallable.commit(taskEvent);
              futureMap.put(revision, future);
            }

            for (Entry<String, InvokeFuture> entry : futureMap.entrySet()) {

              InvokeFuture future = entry.getValue();
              try {
                future.getResponse();
              } catch (InterruptedException e) {
                LOG.error("app_revision: {} heartbeat error.", entry.getKey(), e);
              }
            }
            return null;
          });
    } catch (Exception e) {
      LOG.error("app_revision heartbeat error.", e);
    }
  }

  @Override
  public void doHeartbeatCacheChecker() {
    try {

      Set<String> heartbeatSet = appRevisionJdbcRepository.getHeartbeatSet().get();
      List<String> revisions = new ArrayList(heartbeatSet);

      List<String> exists = new ArrayList<>();
      int round = MathUtils.divideCeil(revisions.size(), heartbeatCheckerSize);
      for (int i = 0; i < round; i++) {
        int start = i * heartbeatCheckerSize;
        int end =
            start + heartbeatCheckerSize < revisions.size()
                ? start + heartbeatCheckerSize
                : revisions.size();
        List<String> subRevisions = revisions.subList(start, end);
        exists.addAll(
            appRevisionMapper.batchCheck(defaultCommonConfig.getClusterId(), subRevisions));
      }

      SetView<String> difference = Sets.difference(new HashSet<>(revisions), new HashSet<>(exists));
      LOG.info("[doHeartbeatCacheChecker] reduces heartbeat size: {}", difference.size());
      appRevisionJdbcRepository.invalidateHeartbeat(difference);

    } catch (Exception e) {
      LOG.error("app_revision heartbeat cache checker error.", e);
    }
  }

  @Override
  public void doAppRevisionGc(int silenceHour) {

    try {
      singleFlight.execute(
          "app_revision_gc",
          () -> {
            Date date = DateUtils.addHours(new Date(), -silenceHour);
            List<String> revisions =
                appRevisionMapper.queryGcRevision(
                    defaultCommonConfig.getClusterId(), date, REVISION_GC_LIMIT);

            if (LOG.isInfoEnabled()) {
              LOG.info("app_revision tobe gc size: {}, revisions: {}", revisions.size(), revisions);
            }
            for (String revision : revisions) {
              // delete app_revision
              REVISION_GC_COUNTER.inc();
              appRevisionMapper.deleteAppRevision(defaultCommonConfig.getClusterId(), revision);
            }

            return null;
          });
    } catch (Exception e) {
      LOG.error("app_revision gc error.", e);
    }
  }
}
