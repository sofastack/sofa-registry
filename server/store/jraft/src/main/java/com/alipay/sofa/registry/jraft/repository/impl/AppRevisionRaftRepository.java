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
package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRaftRepository.java, v 0.1 2021年01月17日 15:57 xiaojian.xj Exp $
 */
public class AppRevisionRaftRepository implements AppRevisionRepository {

  /** map: <revision, AppRevision> */
  private final Map<String, AppRevision> registry = new ConcurrentHashMap<>();

  @Override
  public void register(AppRevision appRevision) {
    if (this.registry.containsKey(appRevision.getRevision())) {
      return;
    }
  }

  /**
   * check if revisionId exist
   *
   * @param revisionId revisionId
   * @return boolean
   */
  @Override
  public boolean exist(String revisionId) {
    return false;
  }

  @Override
  public AppRevision queryRevision(String revision) {
    return registry.get(revision);
  }

  @Override
  public boolean heartbeat(String revision) {
    return false;
  }

  @Override
  public boolean heartbeatDB(String revision) {
    return false;
  }

  @Override
  public void waitSynced() {}

  @Override
  public List<AppRevision> getExpired(Date beforeTime, int limit) {
    return null;
  }

  @Override
  public void replace(AppRevision appRevision) {}

  @Override
  public int cleanDeleted(Date beforeTime, int limit) {
    return 0;
  }

  @Override
  public Map<String, Integer> countByApp() {
    return Collections.emptyMap();
  }

  @Override
  public Set<String> allRevisionIds() {
    return null;
  }

  @Override
  public Collection<String> availableRevisions() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public List<AppRevision> listFromStorage(long start, int limit) {
    return null;
  }

  @Override
  public void startSynced() {}

  @Override
  public Set<String> dataCenters() {
    return null;
  }

  @Override
  public void setDataCenters(Set<String> dataCenters) {}
}
