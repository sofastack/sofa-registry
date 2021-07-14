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
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.springframework.util.CollectionUtils;

public class SimpleStore<T> implements Store<T> {
  protected final Map<String /*dataInfoId*/, Map<String /*registerId*/, T>> stores;
  private final int registerIdCount;

  public SimpleStore(int dataIdCount, int registerIdCount) {
    stores = new ConcurrentHashMap<>(dataIdCount);
    this.registerIdCount = registerIdCount;
  }

  @Override
  public Map<String, T> get(String dataInfoId) {
    return stores.get(dataInfoId);
  }

  @Override
  public Map<String, T> getOrCreate(String dataInfoId) {
    return stores.computeIfAbsent(dataInfoId, k -> new ConcurrentHashMap<>(registerIdCount));
  }

  @Override
  public void forEach(BiConsumer<String, Map<String, T>> consumer) {
    stores.forEach(consumer);
  }

  @Override
  public Map<String, Map<String, T>> copyMap() {
    Map<String, Map<String, T>> ret = Maps.newHashMapWithExpectedSize(stores.size());
    for (Map.Entry<String, Map<String, T>> e : stores.entrySet()) {
      if (!e.getValue().isEmpty()) {
        ret.put(e.getKey(), new HashMap<>(e.getValue()));
      }
    }
    return ret;
  }

  @Override
  public Tuple<Long, Long> count() {
    long dataInfoIdCount = 0;
    long dataCount = 0;
    for (Map<String, T> map : stores.values()) {
      int size = map.size();
      dataCount += size;
      if (size != 0) {
        dataInfoIdCount++;
      }
    }
    return Tuple.of(dataInfoIdCount, dataCount);
  }

  @Override
  public Collection<String> getDataInfoIds() {
    Set<String> ret = Sets.newHashSetWithExpectedSize(stores.values().size());
    for (Map.Entry<String, Map<String, T>> e : stores.entrySet()) {
      if (!CollectionUtils.isEmpty(e.getValue())) {
        ret.add(e.getKey());
      }
    }
    return ret;
  }
}
