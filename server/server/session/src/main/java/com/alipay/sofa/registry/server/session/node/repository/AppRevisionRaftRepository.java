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
package com.alipay.sofa.registry.server.session.node.repository;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jraft.repository.impl.InterfaceAppsRaftRepository;
import com.alipay.sofa.registry.jraft.repository.impl.RaftRepository;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.AppRevisionNodeService;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.util.RevisionUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRaftRepository.java, v 0.1 2021年01月17日 15:57 xiaojian.xj Exp $
 */
public class AppRevisionRaftRepository implements AppRevisionRepository, RaftRepository {

  private static final Logger LOG = LoggerFactory.getLogger(AppRevisionRaftRepository.class);

  @Autowired private AppRevisionNodeService appRevisionNodeService;

  @Resource private InterfaceAppsRaftRepository interfaceAppsRaftRepository;

  /** map: <revision, AppRevision> */
  private final Map<String, AppRevision> registry = new ConcurrentHashMap<>();

  private volatile String keysDigest = "";

  private SingleFlight singleFlight = new SingleFlight();

  @Override
  public void register(AppRevision appRevision) throws Exception {
    if (this.registry.containsKey(appRevision.getRevision())) {
      return;
    }

    singleFlight.execute(
        "revisionRegister" + appRevision.getRevision(),
        () -> {
          appRevisionNodeService.register(appRevision);
          interfaceAppsRaftRepository.onNewRevision(appRevision);
          registry.putIfAbsent(appRevision.getRevision(), appRevision);
          return null;
        });
  }

  @Override
  public void refresh() {
    try {
      singleFlight.execute(
          "refreshAll",
          () -> {
            List<String> allRevisionIds = appRevisionNodeService.checkRevisions(keysDigest);
            if (allRevisionIds == null || allRevisionIds.size() == 0) {
              return Collections.emptyList();
            }
            Set<String> newRevisionIds =
                Sets.difference(new HashSet<>(allRevisionIds), registry.keySet());
            LOG.info("refresh revisions: {}, newRevisionIds: {} ", keysDigest, newRevisionIds);
            List<AppRevision> query =
                appRevisionNodeService.fetchMulti(new ArrayList<>(newRevisionIds));
            for (AppRevision rev : query) {
              interfaceAppsRaftRepository.onNewRevision(rev);
              registry.putIfAbsent(rev.getRevision(), rev);
            }
            if (query.size() > 0) {
              keysDigest = generateKeysDigest(query);
            }
            return query;
          });

    } catch (Exception e) {
      LOG.error("refresh revisions failed ", e);
      throw new RuntimeException("refresh revision failed", e);
    }
  }

  @Override
  public AppRevision queryRevision(String revision) {

    for (int i = 0; i < 2; i++) {
      // 第一次可能会被前一个revision的请求合并到导致虽然在meta内没有fetch回来，第二个fetch肯定能拿到对应 revisipn
      AppRevision revisionRegister = registry.get(revision);
      if (revisionRegister != null) {
        return revisionRegister;
      }
      // sync from meta raftdata
      refresh();
    }

    return registry.get(revision);
  }

  @Override
  public AppRevision heartbeat(String revision) {
    AppRevision appRevision = registry.get(revision);
    if (appRevision != null) {
      appRevision.setLastHeartbeat(new Date());
    }
    return appRevision;
  }

  private String generateKeysDigest(List<AppRevision> revisions) {
    List<String> keys = new ArrayList<>();
    for (AppRevision appRevision : revisions) {
      keys.add(appRevision.getRevision());
    }
    return RevisionUtils.revisionsDigest(keys);
  }
}
