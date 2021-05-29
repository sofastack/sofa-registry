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
package com.alipay.sofa.registry.jdbc.informer;

import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseInformer<T extends DbEntry, C extends DbEntryContainer<T>> {

  private volatile long lastLoadId;

  protected volatile C container = containerFactory();

  private final Lock listLock = new ReentrantLock();

  private final WatchLoop watchLoop = new WatchLoop();
  private final ListLoop listLoop = new ListLoop();
  private volatile boolean enabled;
  private boolean started;
  private volatile long syncStartVersion;
  private volatile long syncEndVersion;

  protected int watchLoopIntervalMs = 1000;
  protected int listLoopIntervalMs = 1000 * 60 * 30;
  protected int checkLoopIntervalMs = 100 * 60 * 5;

  public synchronized void start() {
    if (started) {
      return;
    }
    ConcurrentUtils.createDaemonThread(getClass().getSimpleName() + "-WatchLoop", watchLoop)
        .start();
    ConcurrentUtils.createDaemonThread(getClass().getSimpleName() + "-ListLoop", listLoop).start();
    started = true;
  }

  private void watch() {
    syncStart();
    try {
      long maxId = listOnePage(this.container, lastLoadId, 1);
      if (maxId == lastLoadId) {
        return;
      }
      maxId = listToTail(this.container, maxId, 100);
      lastLoadId = maxId;
    } finally {
      syncEnd();
    }
  }

  private void list() {
    syncStart();
    try {
      C newContainer = containerFactory();
      long maxId = listToTail(newContainer, 0, 1000);
      this.container = newContainer;
      lastLoadId = maxId;
    } finally {
      syncEnd();
    }
  }

  private long listToTail(C result, long start, int page) {
    long maxId;
    while (true) {
      maxId = listOnePage(result, start, page);
      if (maxId == start) {
        break;
      }
      start = maxId;
      ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
    }
    return maxId;
  }

  private long listOnePage(C result, long start, int page) {
    long maxId = start;
    List<T> entries = listFromStorage(start, page);
    for (T entry : entries) {
      result.onEntry(entry);
      start = Math.max(start, entry.getId());
      maxId = start;
    }
    return maxId;
  }

  public C getContainer() {
    return container;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      listLoop.wakeup();
    }
  }

  private void syncStart() {
    syncStartVersion = System.currentTimeMillis();
  }

  private void syncEnd() {
    syncEndVersion = System.currentTimeMillis();
  }

  public void waitSynced() {
    long version = System.currentTimeMillis();
    while (true) {
      if (syncStartVersion > version && syncEndVersion > syncStartVersion) {
        return;
      }
      ConcurrentUtils.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
  }

  protected abstract C containerFactory();

  protected abstract List<T> listFromStorage(long start, int limit);

  private final class WatchLoop extends WakeUpLoopRunnable {

    @Override
    public int getWaitingMillis() {
      return watchLoopIntervalMs;
    }

    @Override
    public void runUnthrowable() {
      if (!enabled) {
        return;
      }
      listLock.lock();
      try {
        watch();
      } finally {
        listLock.unlock();
      }
    }
  }

  private final class ListLoop extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      int base = listLoopIntervalMs / 2;
      return (int) (base + Math.random() * base);
    }

    @Override
    public void runUnthrowable() {
      if (!enabled) {
        return;
      }
      listLock.lock();
      try {
        list();
      } finally {
        listLock.unlock();
      }
    }
  }
}
