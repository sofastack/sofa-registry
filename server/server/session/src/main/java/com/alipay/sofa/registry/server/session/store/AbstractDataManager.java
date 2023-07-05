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
import java.util.function.BiConsumer;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-18 17:18 yuzhi.lyz Exp $
 */
public abstract class AbstractDataManager<T extends BaseInfo>
    implements DataManager<T, String, String> {

  protected final Logger logger;

  final ConnectDataIndexer connectDataIndexer = new ConnectDataIndexer(getClass().getName());

  @Autowired protected SessionServerConfig sessionServerConfig;

  AbstractDataManager(Logger logger) {
    this.logger = logger;
  }

  private Tuple<T, Boolean> addDataToStore(T data) {
    Map<String, T> dataMap = getStore().getOrCreate(data.getDataInfoId());
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

  protected Tuple<T, Boolean> addData(T data) {
    return connectDataIndexer.add(data.connectId(), DataPos.of(data), () -> addDataToStore(data));
  }

  @Override
  public T deleteById(String registerId, String dataInfoId) {
    Map<String, T> dataMap = getStore().get(dataInfoId);
    if (CollectionUtils.isEmpty(dataMap)) {
      logger.warn("Delete but not registered, {}", dataInfoId);
      return null;
    }
    T dataToDelete = dataMap.remove(registerId);

    if (dataToDelete == null) {
      logger.warn("Delete but not registered, {}, {}", dataInfoId, registerId);
    }
    return dataToDelete;
  }

  @Override
  public Map<String, T> deleteByConnectId(ConnectId connectId) {
    Store<T> store = getStore();
    Map<String, T> ret = Maps.newHashMapWithExpectedSize(128);
    for (DataPos pos : connectDataIndexer.queryByKey(connectId)) {
      Map<String, T> dataMap = store.get(pos.getDataInfoId());
      if (CollectionUtils.isEmpty(dataMap)) {
        continue;
      }
      T data = dataMap.get(pos.getRegisterId());
      if (data == null || !data.connectId().equals(connectId)) {
        continue;
      }
      if (dataMap.remove(pos.getRegisterId(), data)) {
        ret.put(data.getRegisterId(), data);
      }
    }
    return ret;
  }

  @Override
  public Map<ConnectId, Map<String, T>> deleteByConnectIds(Set<ConnectId> connectIds) {
    Map<ConnectId, Map<String, T>> ret = Maps.newHashMapWithExpectedSize(connectIds.size());
    for (ConnectId connectId : connectIds) {
      Map<String, T> m = deleteByConnectId(connectId);
      if (!CollectionUtils.isEmpty(m)) {
        ret.put(connectId, m);
      }
    }
    return ret;
  }

  @Override
  public void forEach(BiConsumer<String, Map<String, T>> consumer) {
    getStore()
        .forEach(
            (String dataInfoId, Map<String, T> datas) -> {
              if (!CollectionUtils.isEmpty(datas)) {
                consumer.accept(dataInfoId, Collections.unmodifiableMap(datas));
              }
            });
  }

  @Override
  public Collection<T> getDatas(String dataInfoId) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
    Map<String, T> dataMap = getStore().get(dataInfoId);
    if (MapUtils.isEmpty(dataMap)) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(dataMap.values());
  }

  @Override
  public Map<String, Map<String, T>> getDatas() {
    return getStore().copyMap();
  }

  @Override
  public List<T> getDataList() {
    List<T> ret = new ArrayList<>(1024);
    getStore()
        .forEach(
            (String dataInfoId, Map<String, T> datas) -> {
              ret.addAll(datas.values());
            });
    return ret;
  }

  @Override
  public Map<String, T> queryByConnectId(ConnectId connectId) {
    Map<String, T> ret = Maps.newHashMapWithExpectedSize(128);
    for (DataPos pos : connectDataIndexer.queryByKey(connectId)) {
      T data = queryById(pos.getRegisterId(), pos.getDataInfoId());
      if (data != null && data.connectId().equals(connectId)) {
        ret.put(data.getRegisterId(), data);
      }
    }
    return ret;
  }

  /**
   * query data by client node connectId
   *
   * @param connectIds connectIds
   * @return Map
   */
  @Override
  public Map<ConnectId, Map<String, T>> queryByConnectIds(Set<ConnectId> connectIds) {
    Map<ConnectId, Map<String, T>> ret = Maps.newHashMapWithExpectedSize(connectIds.size());
    for (ConnectId connectId : connectIds) {
      Map<String, T> m = queryByConnectId(connectId);
      if (!CollectionUtils.isEmpty(m)) {
        ret.put(connectId, m);
      }
    }
    return ret;
  }

  @Override
  public T queryById(String registerId, String dataInfoId) {
    final Map<String, T> datas = getStore().get(dataInfoId);
    return datas == null ? null : datas.get(registerId);
  }

  @Override
  public Tuple<Long, Long> count() {
    return getStore().count();
  }

  @Override
  public Set<ConnectId> getConnectIds() {
    return connectDataIndexer.getKeys();
  }

  @Override
  public Collection<String> getDataInfoIds() {
    return getStore().getDataInfoIds();
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

  protected abstract Store<T> getStore();

  class ConnectDataIndexer extends DataIndexer<ConnectId, DataPos> {

    public ConnectDataIndexer(String name) {
      super(name);
    }

    @Override
    protected void dataStoreForEach(BiConsumer<ConnectId, DataPos> consumer) {
      Store<T> store = getStore();
      if (store == null) {
        return;
      }
      store.forEach(
          (dataInfoId, datum) -> {
            for (T data : datum.values()) {
              consumer.accept(data.connectId(), DataPos.of(data));
            }
          });
    }
  }
}
