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

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicSet<T> {
  private Set<T> data = Sets.newConcurrentHashSet();
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock rlock = lock.readLock();
  private final ReentrantReadWriteLock.WriteLock wlock = lock.writeLock();

  public void add(T t) {
    rlock.lock();
    try {
      data.add(t);
    } finally {
      rlock.unlock();
    }
  }

  public Set<T> getAndReset() {
    wlock.lock();
    try {
      Set<T> ret = data;
      data = Sets.newConcurrentHashSet();
      return ret;
    } finally {
      wlock.unlock();
    }
  }

  public Set<T> get() {
    return new HashSet<>(data);
  }

  public void addAll(Set<T> adds) {
    rlock.lock();
    try {
      data.addAll(adds);
    } finally {
      rlock.unlock();
    }
  }

  public int size() {
    rlock.lock();
    try {
      return data.size();
    } finally {
      rlock.unlock();
    }
  }
}
