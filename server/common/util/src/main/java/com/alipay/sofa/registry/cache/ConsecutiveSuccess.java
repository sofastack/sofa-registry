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
package com.alipay.sofa.registry.cache;

import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

public class ConsecutiveSuccess {

  private final ArrayDeque<record> records;
  private final int size;
  private final long expiredIntervalMs;
  private final ReentrantLock lock = new ReentrantLock();

  public ConsecutiveSuccess(int size, long expiredIntervalMs) {
    this.size = size;
    this.expiredIntervalMs = expiredIntervalMs;
    records = new ArrayDeque<>(size);
  }

  public void success() {
    add(new record(System.currentTimeMillis(), true));
  }

  public void fail() {
    add(new record(System.currentTimeMillis(), false));
  }

  private void add(record record) {
    lock.lock();
    try {
      if (records.size() >= this.size) {
        records.pollFirst();
      }
      records.addLast(record);
    } finally {
      lock.unlock();
    }
  }

  public boolean check() {
    long currentTs = System.currentTimeMillis();
    lock.lock();
    try {
      if (records.size() < size) {
        return false;
      }
      for (record r : records) {
        if (!r.success || r.timestamp < currentTs - expiredIntervalMs) {
          return false;
        }
      }
      return true;
    } finally {
      lock.unlock();
    }
  }

  public void clear() {
    lock.lock();
    try {
      records.clear();
    } finally {
      lock.unlock();
    }
  }

  private static final class record {
    private final long timestamp;
    private final boolean success;

    private record(long timestamp, boolean success) {
      this.timestamp = timestamp;
      this.success = success;
    }
  }
}
