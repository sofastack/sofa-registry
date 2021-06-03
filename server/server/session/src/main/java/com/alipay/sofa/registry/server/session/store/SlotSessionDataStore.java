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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 16:24 yuzhi.lyz Exp $
 */
public class SlotSessionDataStore implements DataStore {

  @Autowired SlotTableCache slotTableCache;

  private final Map<Integer, DataStore> slot2DataStores = new ConcurrentHashMap<>();

  private DataStore getOrCreateDataStore(String dataInfoId) {
    int slotId = slotTableCache.slotOf(dataInfoId);
    return slot2DataStores.computeIfAbsent(slotId, k -> new SessionDataStore());
  }

  @Override
  public Collection<Publisher> getDatas(String dataInfoId) {
    DataStore ds = getOrCreateDataStore(dataInfoId);
    return ds.getDatas(dataInfoId);
  }

  @Override
  public List<Publisher> getDataList() {
    List<Publisher> ret = new ArrayList<>(4096);
    for (DataStore ds : slot2DataStores.values()) {
      ret.addAll(ds.getDataList());
    }
    return ret;
  }

  @Override
  public Publisher queryById(String registerId, String dataInfoId) {
    DataStore ds = getOrCreateDataStore(dataInfoId);
    return ds.queryById(registerId, dataInfoId);
  }

  @Override
  public Collection<String> getDataInfoIds() {
    Set<String> set = Sets.newHashSetWithExpectedSize(1024);
    for (DataStore ds : slot2DataStores.values()) {
      set.addAll(ds.getDataInfoIds());
    }
    return set;
  }

  @Override
  public Set<ConnectId> getConnectIds() {
    Set<ConnectId> ret = Sets.newHashSet();
    slot2DataStores.values().forEach(d -> ret.addAll(d.getConnectIds()));
    return ret;
  }

  @Override
  public Set<String> collectProcessIds() {
    Set<String> ret = Sets.newHashSet();
    slot2DataStores.values().forEach(d -> ret.addAll(d.collectProcessIds()));
    return ret;
  }

  @Override
  public Map<String, Map<String, Publisher>> getDatas() {
    Map<String, Map<String, Publisher>> ret = new HashMap<>(512);
    for (DataStore ds : slot2DataStores.values()) {
      Map<String, Map<String, Publisher>> m = ds.getDatas();
      for (Map.Entry<String, Map<String, Publisher>> e : m.entrySet()) {
        Map<String, Publisher> publisherMap =
            ret.computeIfAbsent(e.getKey(), k -> new HashMap<>(128));
        publisherMap.putAll(e.getValue());
      }
    }
    return ret;
  }

  @Override
  public Map<String, Map<String, Publisher>> getDataInfoIdPublishers(int slotId) {
    DataStore ds = slot2DataStores.computeIfAbsent(slotId, k -> new SessionDataStore());
    return ds.getDatas();
  }

  @Override
  public boolean add(Publisher publisher) {
    DataStore ds = getOrCreateDataStore(publisher.getDataInfoId());
    return ds.add(publisher);
  }

  @Override
  public boolean deleteById(String registerId, String dataInfoId) {
    DataStore ds = getOrCreateDataStore(dataInfoId);
    return ds.deleteById(registerId, dataInfoId);
  }

  @Override
  public Map<String, Publisher> queryByConnectId(ConnectId connectId) {
    Map<String, Publisher> ret = Maps.newHashMapWithExpectedSize(128);
    for (DataStore ds : slot2DataStores.values()) {
      Map<String, Publisher> m = ds.queryByConnectId(connectId);
      if (!CollectionUtils.isEmpty(m)) {
        ret.putAll(m);
      }
    }
    return ret;
  }

  @Override
  public Map<ConnectId, Map<String, Publisher>> queryByConnectIds(List<ConnectId> connectIds) {
    if (CollectionUtils.isEmpty(connectIds)) {
      return Collections.EMPTY_MAP;
    }
    Map<ConnectId, Map<String, Publisher>> ret = Maps.newHashMap();
    for (DataStore ds : slot2DataStores.values()) {
      Map<ConnectId, Map<String, Publisher>> m = ds.queryByConnectIds(connectIds);
      for (Entry<ConnectId, Map<String, Publisher>> entry : m.entrySet()) {
        Map<String, Publisher> map = ret.computeIfAbsent(entry.getKey(), k -> Maps.newHashMap());
        map.putAll(entry.getValue());
      }
    }
    return ret;
  }

  @Override
  public Map<String, Publisher> deleteByConnectId(ConnectId connectId) {
    Map<String, Publisher> ret = Maps.newHashMap();
    for (DataStore ds : slot2DataStores.values()) {
      ret.putAll(ds.deleteByConnectId(connectId));
    }
    return ret;
  }

  @Override
  public Map<ConnectId, Map<String, Publisher>> deleteByConnectIds(List<ConnectId> connectIds) {
    Map<ConnectId, Map<String, Publisher>> ret = Maps.newHashMap();
    for (DataStore ds : slot2DataStores.values()) {
      Map<ConnectId, Map<String, Publisher>> publisherMap = ds.deleteByConnectIds(connectIds);
      for (Entry<ConnectId, Map<String, Publisher>> entry : publisherMap.entrySet()) {
        Map<String, Publisher> remove = ret.computeIfAbsent(entry.getKey(), k -> Maps.newHashMap());
        remove.putAll(entry.getValue());
      }
    }
    return ret;
  }

  @Override
  public long count() {
    long count = 0;
    for (DataStore ds : slot2DataStores.values()) {
      count += ds.count();
    }
    return count;
  }
}
