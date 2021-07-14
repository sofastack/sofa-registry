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

import com.alipay.sofa.registry.common.model.Tuple;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.glassfish.jersey.internal.guava.Sets;

public class SlotStore<T> implements Store<T> {
  private final Map<Integer, SimpleStore<T>> slots = new ConcurrentHashMap<>(512);
  private final SlotOfCaller slotOfCaller;

  public SlotStore(SlotOfCaller slotOfCaller) {
    this.slotOfCaller = slotOfCaller;
  }

  private SimpleStore<T> getOrCreateStore(int slotId) {
    return slots.computeIfAbsent(slotId, k -> new SimpleStore<>(256, 256));
  }

  private SimpleStore<T> getOrCreateStore(String dataInfoId) {
    int slotId = slotOfCaller.slotOf(dataInfoId);
    return getOrCreateStore(slotId);
  }

  @Override
  public Map<String, T> get(String dataInfoId) {
    return getOrCreateStore(dataInfoId).get(dataInfoId);
  }

  @Override
  public Map<String, T> getOrCreate(String dataInfoId) {
    return getOrCreateStore(dataInfoId).getOrCreate(dataInfoId);
  }

  @Override
  public void forEach(BiConsumer<String, Map<String, T>> consumer) {
    for (SimpleStore<T> store : slots.values()) {
      store.forEach(consumer);
    }
  }

  @Override
  public Map<String, Map<String, T>> copyMap() {
    Map<String, Map<String, T>> ret = Maps.newHashMapWithExpectedSize(1024 * 16);
    for (SimpleStore<T> store : slots.values()) {
      ret.putAll(store.copyMap());
    }
    return ret;
  }

  public Map<String, Map<String, T>> copyMap(int slotId) {
    SimpleStore<T> store = getOrCreateStore(slotId);
    return store.copyMap();
  }

  public Tuple<Long, Long> count() {
    long dataInfoIdCount = 0;
    long dataCount = 0;
    for (SimpleStore<T> store : slots.values()) {
      Tuple<Long, Long> ret = store.count();
      dataInfoIdCount += ret.getFirst();
      dataCount += ret.getSecond();
    }
    return new Tuple<>(dataInfoIdCount, dataCount);
  }

  @Override
  public Collection<String> getDataInfoIds() {
    Set<String> ret = Sets.newHashSetWithExpectedSize(1024 * 16);
    for (SimpleStore<T> store : slots.values()) {
      ret.addAll(store.getDataInfoIds());
    }
    return ret;
  }

  public interface SlotOfCaller {
    int slotOf(String dataInfoId);
  }
}
