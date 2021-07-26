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
package com.alipay.sofa.registry.concurrent;

import com.alipay.sofa.registry.cache.CacheCleaner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CachedExecutor<K, V> {
  private final Cache<K, V> cache;
  private long hitCount = 0;
  private long missingCount = 0;

  public CachedExecutor(long silentMs) {
    cache = CacheBuilder.newBuilder().expireAfterWrite(silentMs, TimeUnit.MILLISECONDS).build();
    CacheCleaner.autoClean(cache, silentMs);
  }

  public CachedExecutor(long silentMs, long maxWeight, Weigher<K, V> weigher) {
    cache =
        CacheBuilder.newBuilder()
            .maximumWeight(maxWeight)
            .weigher(weigher)
            .expireAfterWrite(silentMs, TimeUnit.MILLISECONDS)
            .build();
    CacheCleaner.autoClean(cache, silentMs);
  }

  public V execute(K key, Callable<V> callable) throws Exception {
    V v = cache.getIfPresent(key);
    if (v != null) {
      hitCount++;
      return v;
    }
    return cache.get(
        key,
        () -> {
          missingCount++;
          return callable.call();
        });
  }

  public void clean() {
    cache.invalidateAll();
  }

  public long getHitCount() {
    return hitCount;
  }

  public long getMissingCount() {
    return missingCount;
  }
}
