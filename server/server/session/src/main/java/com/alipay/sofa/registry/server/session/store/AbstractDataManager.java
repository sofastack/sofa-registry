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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-18 17:18 yuzhi.lyz Exp $
 */
public abstract class AbstractDataManager<T extends BaseInfo>
    implements DataManager<T, String, String> {

  protected final ConcurrentHashMap<String /*dataInfoId*/, Map<String /*registerId*/, T>> stores =
      new ConcurrentHashMap<>(1024 * 16);
  protected final Logger logger;

  @Autowired protected SessionServerConfig sessionServerConfig;

  AbstractDataManager(Logger logger) {
    this.logger = logger;
  }

  protected Tuple<T, Boolean> addData(T data) {
    Map<String, T> dataMap =
        stores.computeIfAbsent(
            data.getDataInfoId(), k -> new ConcurrentHashMap<>(getInitMapSize()));
    final String registerId = data.getRegisterId();

    // quick path
    if (dataMap.putIfAbsent(registerId, data) == null) {
      return new Tuple<>(null, true);
    }

    for (; ; ) {
      final T existing = dataMap.get(registerId);
      if (existing == null) {
        if (dataMap.putIfAbsent(registerId, data) == null) {
          return new Tuple<>(null, true);
        }
      } else {
        if (!existing.registerVersion().orderThan(data.registerVersion())) {
          logger.warn(
              "[conflict]{},{},exist={}/{},input={}/{}",
              data.getDataInfoId(),
              data.getRegisterId(),
              existing.registerVersion(),
              existing.getRegisterTimestamp(),
              data.registerVersion(),
              data.getRegisterTimestamp());
          return new Tuple<>(existing, false);
        }
        if (dataMap.replace(registerId, existing, data)) {
          return new Tuple<>(existing, true);
        }
      }
    }
  }

  @Override
  public boolean deleteById(String registerId, String dataInfoId) {
    Map<String, T> dataMap = stores.get(dataInfoId);
    if (CollectionUtils.isEmpty(dataMap)) {
      logger.warn("Delete but not registered, {}", dataInfoId);
      return false;
    }
    T dataToDelete = dataMap.remove(registerId);

    if (dataToDelete == null) {
      logger.warn("Delete but not registered, {}, {}", dataInfoId, registerId);
    }
    return dataToDelete != null;
  }

  @Override
  public Map<String, T> deleteByConnectId(ConnectId connectId) {

    Map<ConnectId, Map<String, T>> ret = deleteByConnectIds(Collections.singleton(connectId));
    Map<String, T> data = ret.get(connectId);
    if (CollectionUtils.isEmpty(data)) {
      return Maps.newHashMap();
    }

    return data;
  }

  @Override
  public Map<ConnectId, Map<String, T>> deleteByConnectIds(Set<ConnectId> connectIds) {
    Map<ConnectId, Map<String, T>> ret = Maps.newHashMap();

    for (Map<String, T> map : stores.values()) {
      // copy a map for iterate
      for (Map.Entry<String, T> e : map.entrySet()) {
        final T data = e.getValue();
        if (!connectIds.contains(data.connectId())) {
          continue;
        }
        // may be the value has removed by anther thread
        if (map.remove(e.getKey(), data)) {
          Map<String, T> remove = ret.computeIfAbsent(data.connectId(), k -> Maps.newHashMap());
          remove.put(e.getKey(), data);
        }
      }
    }
    return ret;
  }

  @Override
  public Collection<T> getDatas(String dataInfoId) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
    Map<String, T> dataMap = stores.get(dataInfoId);
    if (MapUtils.isEmpty(dataMap)) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(dataMap.values());
  }

  @Override
  public Map<String, Map<String, T>> getDatas() {
    return StoreHelpers.copyMap((Map) stores);
  }

  @Override
  public List<T> getDataList() {
    List<T> ret = new ArrayList<>(512);
    for (Map<String, T> store : stores.values()) {
      ret.addAll(store.values());
    }
    return ret;
  }

  @Override
  public Map<String, T> queryByConnectId(ConnectId connectId) {
    return StoreHelpers.getByConnectId(connectId, stores);
  }

  /**
   * query data by client node connectId
   *
   * @param connectIds
   * @return
   */
  @Override
  public Map<ConnectId, Map<String, T>> queryByConnectIds(Set<ConnectId> connectIds) {
    return StoreHelpers.getByConnectIds(connectIds, stores);
  }

  @Override
  public T queryById(String registerId, String dataInfoId) {
    final Map<String, T> datas = stores.get(dataInfoId);
    return datas == null ? null : datas.get(registerId);
  }

  @Override
  public Tuple<Long, Long> count() {
    return StoreHelpers.count(stores);
  }

  @Override
  public Set<ConnectId> getConnectIds() {
    return StoreHelpers.collectConnectIds(stores);
  }

  @Override
  public Set<String> collectProcessIds() {
    return StoreHelpers.collectProcessIds(stores);
  }

  @Override
  public Collection<String> getDataInfoIds() {
    return stores.entrySet().stream()
        .filter(e -> !(e.getValue().isEmpty()))
        .map(e -> e.getKey())
        .collect(Collectors.toSet());
  }

  public SessionServerConfig getSessionServerConfig() {
    return sessionServerConfig;
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   */
  public void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
  }

  protected abstract int getInitMapSize();
}
