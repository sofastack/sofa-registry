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
package com.alipay.sofa.registry.server.session.store.engine;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.server.session.store.ClientsGroup;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.util.Pair;

/** StoreEngine implemented based on HashMap. */
public class SimpleMemoryStoreEngine<T extends StoreData<String>> implements StoreEngine<T> {

  private final Map<String, ClientsGroup<T>> groups;

  public SimpleMemoryStoreEngine(int initialSize) {
    this.groups = new ConcurrentHashMap<>(initialSize);
  }

  @Override
  public Pair<Boolean, T> putIfAbsent(T storeData) {
    String dataInfoId = storeData.getDataInfoId();
    ClientsGroup<T> clientsGroup = groups.get(dataInfoId);
    if (clientsGroup == null) {
      clientsGroup = new ClientsGroup<>(dataInfoId, 128);
      ClientsGroup<T> exist = groups.putIfAbsent(dataInfoId, clientsGroup);
      if (exist != null) {
        // 如果之前已经存在了，直接复用之前的ClientGroups，并向其中加入新的StoreData
        // 如果exist == null，意味着新的ClientGroups被添加进去了，向新的ClientGroups加入StoreData
        clientsGroup = exist;
      }
    }

    return clientsGroup.putIfAbsent(storeData);
  }

  @Override
  public T get(String dataInfoId, String registerId) {
    ClientsGroup<T> clientsGroup = groups.get(dataInfoId);
    if (clientsGroup == null) {
      return null;
    }

    return clientsGroup.get(registerId);
  }

  @Override
  public T delete(String dataInfoId, String registerId) {
    ClientsGroup<T> clientsGroup = groups.get(dataInfoId);
    if (clientsGroup == null) {
      return null;
    }
    return clientsGroup.remove(registerId);
  }

  @Override
  public boolean delete(T storeData) {
    ClientsGroup<T> clientsGroup = groups.get(storeData.getDataInfoId());
    if (clientsGroup == null) {
      return false;
    }
    return clientsGroup.remove(storeData);
  }

  @Override
  public Collection<T> get(String dataInfoId) {
    ClientsGroup<T> clientsGroup = groups.get(dataInfoId);
    if (clientsGroup == null) {
      return null;
    }
    return clientsGroup.all();
  }

  @Override
  public Collection<T> getAll() {
    Set<T> result = new HashSet<>();
    for (ClientsGroup<T> clientsGroup : groups.values()) {
      Collection<T> storeDataCollection = clientsGroup.all();
      if (storeDataCollection != null && storeDataCollection.size() > 0) {
        result.addAll(storeDataCollection);
      }
    }
    return result;
  }

  @Override
  public Map<String, Collection<T>> filter(String group, int limit) {
    Map<String, Collection<T>> result = new HashMap<>();
    for (Map.Entry<String, ClientsGroup<T>> entry : groups.entrySet()) {
      String dataInfoId = entry.getKey();
      if (!group.equals(DataInfo.parse(dataInfoId)[2])) {
        continue;
      }
      result.put(dataInfoId, entry.getValue().limit(limit));
    }
    return result;
  }

  @Override
  public Collection<String> getNonEmptyDataInfoId() {
    Set<String> result = new HashSet<>();
    for (ClientsGroup<T> clientsGroup : groups.values()) {
      if (clientsGroup.size() > 0) {
        result.add(clientsGroup.dataInfoId());
      }
    }
    return result;
  }

  @Override
  public StoreStat stat() {
    long dataInfoIdCount = 0;
    long dataCount = 0;
    for (ClientsGroup<T> clientsGroup : groups.values()) {
      int size = clientsGroup.size();
      dataCount += size;
      if (size != 0) {
        dataInfoIdCount++;
      }
    }
    return new StoreStat(dataInfoIdCount, dataCount);
  }
}
