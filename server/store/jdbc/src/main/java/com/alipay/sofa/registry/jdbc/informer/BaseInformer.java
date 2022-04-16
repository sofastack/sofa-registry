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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.store.api.meta.DbEntry;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections.CollectionUtils;

public abstract class BaseInformer<T extends DbEntry, C extends DbEntryContainer<T>> {

  private volatile long lastLoadId;

  protected volatile C container = containerFactory();

  private final Lock listLock = new ReentrantLock();

  protected final WatchLoop watchLoop = new WatchLoop();
  protected final ListLoop listLoop = new ListLoop();
  private volatile boolean enabled;
  private boolean started;
  private volatile long syncStartVersion;
  private volatile long syncEndVersion;

  protected int watchLoopIntervalMs = 1000;
  protected int listLoopIntervalMs = 1000 * 60 * 30;
  private final String name;
  private final Logger logger;
  private static final int DB_INSERT_DELAY_MS = 1000;
  private volatile boolean allSynced = false;

  public BaseInformer(String name, Logger logger) {
    this.name = name;
    this.logger = logger;
  }

  /**
   * Getter method for property <tt>lastLoadId</tt>.
   *
   * @return property value of lastLoadId
   */
  public long getLastLoadId() {
    return lastLoadId;
  }

  public synchronized void start() {
    if (started) {
      return;
    }
    ConcurrentUtils.createDaemonThread(name + "-WatchLoop", watchLoop).start();
    ConcurrentUtils.createDaemonThread(name + "-ListLoop", listLoop).start();
    started = true;
    logger.info("{}-Informer started", name);
  }

  private void watch() {
    syncStart();
    try {
      long start = lastLoadId;
      if (listStableEntries(start, 1).size() == 0) {
        return;
      }
      logger.info("start watch from {}", start);
      long maxId =
          listToTail(
              (T entry) -> {
                container.onEntry(entry);
                logger.info("watch received entry: {}", entry);
              },
              start,
              100);
      logger.info("end watch to {}", maxId);
      lastLoadId = maxId;
    } finally {
      syncEnd();
    }
  }

  private void list() {
    syncStart();
    try {
      C newContainer = containerFactory();
      long maxId = listToTail(newContainer::onEntry, 0, 1000);
      logger.info("end list to {}", maxId);
      preList(newContainer);
      this.container = newContainer;
      lastLoadId = maxId;
    } finally {
      syncEnd();
    }
  }

  private long listToTail(EntryCallable<T> callable, final long start, final int page) {
    long curStart = start;
    while (true) {
      List<T> entries = listStableEntries(curStart, page);
      if (CollectionUtils.isEmpty(entries)) {
        break;
      }
      for (T entry : entries) {
        callable.onEntry(entry);
        curStart = Math.max(curStart, entry.getId());
      }
      ConcurrentUtils.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
    }
    return curStart;
  }

  private List<T> listStableEntries(long start, int limit) {
    Date now = getNow();
    List<T> entries = listFromStorage(start, limit);
    allSynced = false;
    if (CollectionUtils.isEmpty(entries)) {
      allSynced = true;
      return Collections.emptyList();
    }
    List<T> result = Lists.newArrayListWithExpectedSize(entries.size());
    for (T entry : entries) {
      if (entry.getGmtCreate().getTime() >= now.getTime() - DB_INSERT_DELAY_MS) {
        break;
      }
      result.add(entry);
    }
    return result;
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

  public void listWakeup() {
    listLoop.wakeup();
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
      if (syncStartVersion > version && syncEndVersion > syncStartVersion && allSynced) {
        long end = System.currentTimeMillis();
        logger.info("{}-waitSynced, start:{}, end:{}, cost:{}", name, version, end, end - version);
        return;
      }
      ConcurrentUtils.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
  }

  protected abstract C containerFactory();

  protected abstract List<T> listFromStorage(long start, int limit);

  protected abstract Date getNow();

  protected void preList(C newContainer) {}

  public void watchWakeup() {
    watchLoop.wakeup();
  }

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

  interface EntryCallable<T> {
    void onEntry(T entry);
  }
}
