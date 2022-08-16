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
package com.alipay.sofa.registry.util;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicMap<K, V> {

  private Map<K, V> data = Maps.newConcurrentMap();

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
  private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

  public V put(K key, V value) {
    readLock.lock();
    try {
      return data.put(key, value);
    } finally {
      readLock.unlock();
    }
  }

  public Map<K, V> getAndReset() {
    writeLock.lock();
    try {
      Map<K, V> ret = data;
      data = Maps.newConcurrentMap();
      return ret;
    } finally {
      writeLock.unlock();
    }
  }
}
