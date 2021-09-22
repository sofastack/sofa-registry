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
import java.util.concurrent.atomic.LongAdder;

public class CachedExecutor<K, V> {
  private final Cache<K, V> cache;
  private final LongAdder hitCount = new LongAdder();
  private final LongAdder missingCount = new LongAdder();

  public CachedExecutor(long silentMs) {
    this(silentMs, false);
  }

  public CachedExecutor(long silentMs, boolean expireAfterAccess) {
    CacheBuilder builder = CacheBuilder.newBuilder();
    if (expireAfterAccess) {
      builder = builder.expireAfterAccess(silentMs, TimeUnit.MILLISECONDS);
    } else {
      builder = builder.expireAfterWrite(silentMs, TimeUnit.MILLISECONDS);
    }
    cache = builder.build();
    CacheCleaner.autoClean(cache, silentMs);
  }

  public CachedExecutor(
      long silentMs, long maxWeight, Weigher<K, V> weigher, boolean expireAfterAccess) {
    CacheBuilder builder = CacheBuilder.newBuilder().maximumWeight(maxWeight).weigher(weigher);
    if (expireAfterAccess) {
      builder = builder.expireAfterAccess(silentMs, TimeUnit.MILLISECONDS);
    } else {
      builder = builder.expireAfterWrite(silentMs, TimeUnit.MILLISECONDS);
    }
    cache = builder.build();
    CacheCleaner.autoClean(cache, silentMs);
  }

  public V execute(K key, Callable<V> callable) throws Exception {
    V v = cache.getIfPresent(key);
    if (v != null) {
      hitCount.increment();
      return v;
    }
    return cache.get(
        key,
        () -> {
          missingCount.increment();
          onMiss(key);
          return callable.call();
        });
  }

  protected void onMiss(K key) {}

  public void clean() {
    cache.invalidateAll();
  }

  public long getHitCount() {
    return hitCount.longValue();
  }

  public long getMissingCount() {
    return missingCount.longValue();
  }
}
