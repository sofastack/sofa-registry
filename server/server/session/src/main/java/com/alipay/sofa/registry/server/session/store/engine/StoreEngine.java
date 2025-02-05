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
import java.util.Map;
import javafx.util.Pair;

/** Engine for storing StoreData. */
public interface StoreEngine<T extends StoreData<String>> {

  /**
   * If the specified key is
   *
   * <p>1. not already associated with a value
   *
   * <p>2. or is mapped to null
   *
   * <p>3. or the version of the associated value is less than the target version
   *
   * <p>, associates it with the given value and returns previous value.
   *
   * @param storeData new value
   * @return whether the put operation is successful and the previous value
   */
  Pair<Boolean, T> putIfAbsent(T storeData);

  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping
   * for the key.
   *
   * @param dataInfoId dataInfoId of StoreData
   * @param registerId register of StoreData
   * @return the value to which the specified key is mapped, or null if this map contains no mapping
   *     for the key
   */
  T get(String dataInfoId, String registerId);

  /**
   * Removes the mapping for a key from this map if it is present (optional operation).
   *
   * @param dataInfoId dataInfoId of StoreData
   * @param registerId register of StoreData
   * @return the previous value associated with key, or null if there was no mapping for key.
   */
  T delete(String dataInfoId, String registerId);

  /**
   * Delete target data.
   *
   * @param storeData target data
   * @return true if the target data was successfully deleted
   */
  boolean delete(T storeData);

  /**
   * Get all StoreData associated with target dataInfoId.
   *
   * @param dataInfoId dataInfoId of StoreData
   * @return all StoreData associated with target dataInfoId.
   */
  Collection<T> get(String dataInfoId);

  /**
   * Get all StoreData in this store.
   *
   * @return all StoreData in this store.
   */
  Collection<T> getAll();

  /**
   * Filter target data with limit.
   *
   * @param group DataInfo group
   * @param limit limit
   * @return data collections
   */
  Map<String, Collection<T>> filter(String group, int limit);

  /**
   * Get collection of dataInfoId which has non-empty StoreData collection.
   *
   * @return collection of dataInfoId which has non-empty StoreData collection.
   */
  Collection<String> getNonEmptyDataInfoId();

  /**
   * Get the stat of this store.
   *
   * @return the stat of this store.
   */
  StoreStat stat();

  class StoreStat {

    private final long nonEmptyDataIdSize;
    private final long size;

    public StoreStat(long nonEmptyDataIdSize, long size) {
      this.nonEmptyDataIdSize = nonEmptyDataIdSize;
      this.size = size;
    }

    public long nonEmptyDataIdSize() {
      return nonEmptyDataIdSize;
    }

    public long size() {
      return size;
    }
  }
}
