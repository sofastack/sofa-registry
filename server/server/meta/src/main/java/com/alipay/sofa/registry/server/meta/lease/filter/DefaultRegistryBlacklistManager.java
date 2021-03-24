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
package com.alipay.sofa.registry.server.meta.lease.filter;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
@Component
public class DefaultRegistryBlacklistManager implements RegistryBlacklistManager {

  @Autowired private ProvideDataRepository repository;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private Set<String> blacklist = Sets.newHashSet();

  public DefaultRegistryBlacklistManager() {}

  public DefaultRegistryBlacklistManager(ProvideDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public void addToBlacklist(String ip) {
    Set<String> newBlacklist = getFreshBlacklist();
    if (newBlacklist.add(ip)) {
      store(newBlacklist);
    }
  }

  @Override
  public void removeFromBlacklist(String ip) {
    Set<String> newBlacklist = getFreshBlacklist();
    if (newBlacklist.remove(ip)) {
      store(newBlacklist);
    }
  }

  @Override
  public boolean allowSelect(Lease<Node> lease) {
    return !blacklist.contains(lease.getRenewal().getNodeUrl().getIpAddress());
  }

  protected void load() {
    DBResponse response = repository.get(ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID);
    Set<String> current;
    if (response.getOperationStatus() == OperationStatus.NOTFOUND) {
      current = Sets.newHashSet();
    } else {
      PersistenceData data = JsonUtils.read(response.getEntity(), PersistenceData.class);
      current = JsonUtils.read(data.getData(), Set.class);
    }
    lock.writeLock().lock();
    try {
      this.blacklist = Collections.unmodifiableSet(current);
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected void store(Set<String> newBlacklist) {
    PersistenceData persistence = createDataInfo();
    persistence.setData(JsonUtils.writeValueAsString(newBlacklist));
    repository.put(
        ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID,
        JsonUtils.writeValueAsString(persistence));
    lock.writeLock().lock();
    try {
      this.blacklist = Collections.unmodifiableSet(newBlacklist);
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected Set<String> getFreshBlacklist() {
    load();
    Set<String> newBlacklist;
    lock.readLock().lock();
    try {
      newBlacklist = Sets.newHashSet(blacklist);
    } finally {
      lock.readLock().unlock();
    }
    return newBlacklist;
  }

  private PersistenceData createDataInfo() {
    DataInfo dataInfo = DataInfo.valueOf(ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID);
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setVersion(System.currentTimeMillis());
    return persistenceData;
  }
}
