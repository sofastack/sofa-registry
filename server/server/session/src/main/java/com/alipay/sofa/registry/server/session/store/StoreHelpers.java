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
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-18 16:42 yuzhi.lyz Exp $
 */
public final class StoreHelpers {
  private StoreHelpers() {}

  public static <T extends BaseInfo> Set<ConnectId> collectConnectId(Map<String, T> map) {
    Set<ConnectId> sets = Sets.newHashSet();
    map.values().forEach(r -> sets.add(r.connectId()));
    return sets;
  }

  public static <T extends BaseInfo> Set<ConnectId> collectConnectIds(
      Map<String, Map<String, T>> maps) {
    Set<ConnectId> sets = Sets.newHashSet();
    maps.values().forEach(m -> sets.addAll(collectConnectId(m)));
    return sets;
  }

  public static <T extends BaseInfo> long count(Map<String, Map<String, T>> maps) {
    long count = 0;
    for (Map<String, T> map : maps.values()) {
      count += map.size();
    }
    return count;
  }

  public static <T extends BaseInfo> Map<String, T> getByConnectId(
      ConnectId connectId, Map<String, Map<String, T>> maps) {
    Map<String, T> retMap = Maps.newHashMap();
    for (Map<String, T> m : maps.values()) {
      for (T r : m.values()) {
        if (connectId.equals(r.connectId())) {
          retMap.put(r.getRegisterId(), r);
        }
      }
    }
    return retMap;
  }

  public static <T extends BaseInfo> Set<String> collectProcessIds(
      Map<String, Map<String, T>> maps) {
    HashSet<String> processIds = Sets.newHashSet();
    for (Map<String, T> map : maps.values()) {
      for (T t : map.values()) {
        if (t.getProcessId() != null) {
          processIds.add(t.getProcessId());
        }
      }
    }
    return processIds;
  }

  public static Map<Object, Map> copyMap(Map<Object, Map> m) {
    Map<Object, Map> ret = new HashMap<>(m.size());
    for (Map.Entry<Object, Map> e : m.entrySet()) {
      if (!e.getValue().isEmpty()) {
        ret.put(e.getKey(), new HashMap<>(e.getValue()));
      }
    }
    return ret;
  }

  public static <T extends BaseInfo> Map<ConnectId, Map<String, T>> getByConnectIds(
      List<ConnectId> connectIds, ConcurrentHashMap<String, Map<String, T>> stores) {
    Map<ConnectId, Map<String, T>> retMap = Maps.newHashMap();
    for (Map<String, T> m : stores.values()) {
      for (Entry<String, T> entry : m.entrySet()) {
        T value = entry.getValue();
        if (connectIds.contains(value.connectId())) {
          Map<String, T> map = retMap.computeIfAbsent(value.connectId(), k -> Maps.newHashMap());
          map.put(entry.getKey(), value);
        }
      }
    }
    return retMap;
  }
}
