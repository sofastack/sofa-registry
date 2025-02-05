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

import com.alipay.sofa.registry.common.model.store.StoreData;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A series of StoreData collections with the same dataInfoId. */
public class ClientsGroup<T extends StoreData<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientsGroup.class);

  private final String dataInfoId;
  private final Map<String, T> clients;

  public ClientsGroup(String dataInfoId, int initialSize) {
    this.dataInfoId = dataInfoId;
    this.clients = new ConcurrentHashMap<>(initialSize);
  }

  public String dataInfoId() {
    return dataInfoId;
  }

  public int size() {
    return clients.size();
  }

  public Pair<Boolean, T> putIfAbsent(T storeData) {
    String registerId = storeData.getId();
    StoreData<?> exist = clients.putIfAbsent(registerId, storeData);
    if (exist == null) {
      return new Pair<>(true, null);
    }

    for (; ; ) {
      final T existing = clients.get(registerId);
      if (existing == null) {
        if (clients.putIfAbsent(registerId, storeData) == null) {
          System.out.println(">>>>>>>>>>" + System.currentTimeMillis() + " add: " + registerId);
          return new Pair<>(true, null);
        }
      } else {
        if (!existing.registerVersion().orderThan(storeData.registerVersion())) {
          LOGGER.warn(
              "[conflict]{},{},exist={}/{},input={}/{}",
              storeData.getDataInfoId(),
              registerId,
              existing.registerVersion(),
              existing.getRegisterTimestamp(),
              storeData.registerVersion(),
              storeData.getRegisterTimestamp());
          return new Pair<>(false, existing);
        }
        if (clients.replace(registerId, existing, storeData)) {
          System.out.println(">>>>>>>>>>" + System.currentTimeMillis() + " add: " + registerId);
          return new Pair<>(true, existing);
        }
      }
    }
  }

  public T get(String registerId) {
    return clients.get(registerId);
  }

  public T remove(String registerId) {
    return clients.remove(registerId);
  }

  public boolean remove(T t) {
    return clients.remove(t.getId(), t);
  }

  public Collection<T> all() {
    return clients.values();
  }

  public Collection<T> limit(int limit) {
    return clients.values().stream().limit(Math.max(limit, 0)).collect(Collectors.toList());
  }
}
