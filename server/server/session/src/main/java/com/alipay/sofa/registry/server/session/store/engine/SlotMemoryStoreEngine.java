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

import com.alipay.sofa.registry.common.model.store.StoreData;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.util.Pair;

/**
 * Combine a series of SimpleMemoryStoreEngine and map and store StoreData according to a certain
 * strategy.
 */
public class SlotMemoryStoreEngine<T extends StoreData<String>> implements SlotStoreEngine<T> {

  private final SlotCaller slotCaller;
  private final Map<Integer, SimpleMemoryStoreEngine<T>> slotsStore;

  public SlotMemoryStoreEngine(SlotCaller slotCaller) {
    this.slotCaller = slotCaller;
    this.slotsStore = new ConcurrentHashMap<>(512);
  }

  private SimpleMemoryStoreEngine<T> getOrCreateStore(String dataInfoId) {
    int slotId = slotCaller.slotOf(dataInfoId);
    return slotsStore.computeIfAbsent(slotId, integer -> new SimpleMemoryStoreEngine<>(256));
  }

  @Override
  public Pair<Boolean, T> putIfAbsent(T storeData) {
    SimpleMemoryStoreEngine<T> storeEngine = getOrCreateStore(storeData.getDataInfoId());
    return storeEngine.putIfAbsent(storeData);
  }

  @Override
  public T get(String dataInfoId, String registerId) {
    SimpleMemoryStoreEngine<T> storeEngine = getOrCreateStore(dataInfoId);
    return storeEngine.get(dataInfoId, registerId);
  }

  @Override
  public T delete(String dataInfoId, String registerId) {
    SimpleMemoryStoreEngine<T> storeEngine = getOrCreateStore(dataInfoId);
    return storeEngine.delete(dataInfoId, registerId);
  }

  @Override
  public boolean delete(T storeData) {
    SimpleMemoryStoreEngine<T> storeEngine = getOrCreateStore(storeData.getDataInfoId());
    return storeEngine.delete(storeData);
  }

  @Override
  public Collection<T> get(String dataInfoId) {
    int slotId = slotCaller.slotOf(dataInfoId);
    SimpleMemoryStoreEngine<T> storeEngine = slotsStore.get(slotId);
    if (storeEngine == null) {
      return null;
    }
    return storeEngine.get(dataInfoId);
  }

  @Override
  public Collection<T> getAll() {
    Set<T> result = new HashSet<>();
    for (SimpleMemoryStoreEngine<T> storeEngine : slotsStore.values()) {
      result.addAll(storeEngine.getAll());
    }
    return result;
  }

  @Override
  public Map<String, Collection<T>> filter(String group, int limit) {
    Map<String, Collection<T>> result = new HashMap<>();
    for (SimpleMemoryStoreEngine<T> storeEngine : slotsStore.values()) {
      result.putAll(storeEngine.filter(group, limit));
    }
    return result;
  }

  @Override
  public Collection<String> getNonEmptyDataInfoId() {
    Set<String> result = new HashSet<>();
    for (SimpleMemoryStoreEngine<T> storeEngine : slotsStore.values()) {
      result.addAll(storeEngine.getNonEmptyDataInfoId());
    }
    return result;
  }

  @Override
  public StoreStat stat() {
    long dataInfoIdCount = 0;
    long dataCount = 0;
    for (SimpleMemoryStoreEngine<T> store : slotsStore.values()) {
      StoreStat storeStat = store.stat();
      dataInfoIdCount += storeStat.nonEmptyDataIdSize();
      dataCount += storeStat.size();
    }
    return new StoreStat(dataInfoIdCount, dataCount);
  }

  @Override
  public Collection<T> getSlotStoreData(int slotId) {
    SimpleMemoryStoreEngine<T> simpleMemoryStoreEngine = slotsStore.get(slotId);
    if (simpleMemoryStoreEngine == null) {
      return Collections.emptyList();
    }
    return simpleMemoryStoreEngine.getAll();
  }

  public interface SlotCaller {
    int slotOf(String dataInfoId);
  }
}
