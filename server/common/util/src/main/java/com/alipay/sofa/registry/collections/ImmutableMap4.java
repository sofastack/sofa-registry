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
package com.alipay.sofa.registry.collections;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;

/** unsupported k v is null */
public final class ImmutableMap4<K, V> extends AbstractImmutableMap<K, V> {
  private static final Entry NULL_ENTRY = new AbstractMap.SimpleEntry(null, null);
  private final K k0;
  private final K k1;
  private final K k2;
  private final K k3;

  private final V v0;
  private final V v1;
  private final V v2;
  private final V v3;

  private ImmutableMap4(List<Entry<K, V>> entries) {
    this.k0 = entries.get(0).getKey();
    this.v0 = entries.get(0).getValue();

    this.k1 = entries.get(1).getKey();
    this.v1 = entries.get(1).getValue();

    this.k2 = entries.get(2).getKey();
    this.v2 = entries.get(2).getValue();

    this.k3 = entries.get(3).getKey();
    this.v3 = entries.get(3).getValue();
  }

  public static <K, V> ImmutableMap4<K, V> newMap(Map<K, V> map) {
    if (map.size() > 4) {
      return null;
    }
    List<Entry<K, V>> list = Lists.newArrayListWithCapacity(4);
    for (Entry<K, V> e : map.entrySet()) {
      if (e.getKey() == null || e.getValue() == null) {
        // not support null kv
        return null;
      }
      list.add(e);
    }
    for (int i = list.size(); i < 4; i++) {
      list.add(NULL_ENTRY);
    }
    return new ImmutableMap4<>(list);
  }

  @Override
  public int size() {
    int count = 0;
    if (k0 != null) {
      count++;
    }
    if (k1 != null) {
      count++;
    }
    if (k2 != null) {
      count++;
    }
    if (k3 != null) {
      count++;
    }
    return count;
  }

  @Override
  public boolean containsKey(Object key) {
    if (key == null) {
      return false;
    }
    return key.equals(k0) || key.equals(k1) || key.equals(k2) || key.equals(k3);
  }

  @Override
  public boolean containsValue(Object value) {
    if (value == null) {
      return false;
    }
    return value.equals(v0) || value.equals(v1) || value.equals(v2) || value.equals(v3);
  }

  @Override
  public V get(Object key) {
    if (key == null) {
      return null;
    }
    if (key.equals(k0)) {
      return v0;
    }
    if (key.equals(k1)) {
      return v1;
    }
    if (key.equals(k2)) {
      return v2;
    }
    if (key.equals(k3)) {
      return v3;
    }
    return null;
  }

  @Override
  public Map<K, V> toMap() {
    int size = size();
    if (size == 0) {
      return Collections.emptyMap();
    }
    Map<K, V> ret = Maps.newHashMapWithExpectedSize(size);
    if (k0 != null) {
      ret.put(k0, v0);
    }
    if (k1 != null) {
      ret.put(k1, v1);
    }
    if (k2 != null) {
      ret.put(k2, v2);
    }
    if (k3 != null) {
      ret.put(k3, v3);
    }
    return Collections.unmodifiableMap(ret);
  }
}
