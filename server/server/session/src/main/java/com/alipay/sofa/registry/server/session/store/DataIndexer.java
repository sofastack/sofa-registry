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

import com.alipay.sofa.registry.concurrent.UnThrowableCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public abstract class DataIndexer<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger("SRV-CONNECT");

  private volatile Map<K, Set<V>> index = new ConcurrentHashMap<>(1024);
  private volatile Map<K, Set<V>> tempIndex = new ConcurrentHashMap<>(1024);
  private volatile Term lastTerm = new Term();
  private volatile boolean doubleWrite = false;

  private final IndexerRefresher indexerRefresher = new IndexerRefresher();

  public DataIndexer(String name) {
    ConcurrentUtils.createDaemonThread(name + "-IndexerRefresher", indexerRefresher).start();
  }

  public <R> R add(K key, V val, UnThrowableCallable<R> dataStoreCaller) {
    Term term = lastTerm;
    term.start.incrementAndGet();
    try {
      if (doubleWrite) {
        insert(tempIndex, key, val);
      }
      insert(index, key, val);
      return dataStoreCaller.call();
    } finally {
      term.done.incrementAndGet();
    }
  }

  private void insert(Map<K, Set<V>> d, K key, V val) {
    d.computeIfAbsent(key, k -> Sets.newConcurrentHashSet()).add(val);
  }

  public Set<V> queryByKey(K key) {
    Set<V> s = index.get(key);
    if (s == null) {
      return Collections.emptySet();
    }
    return new HashSet<>(s);
  }

  public Set<K> getKeys() {
    return new HashSet<>(index.keySet());
  }

  private void refresh() {
    tempIndex = new ConcurrentHashMap<>(this.index.size());
    Term prevTerm = lastTerm;
    doubleWrite = true;
    try {
      lastTerm = new Term();
      long startTime = System.currentTimeMillis();
      boolean timeout = !prevTerm.waitAllDone();
      long waitTime = System.currentTimeMillis();
      if (timeout) {
        LOG.error("[IndexBuildTimeout]index refresh timeout span={}ms", waitTime - startTime);
      }
      dataStoreForEach(
          (key, val) -> {
            insert(tempIndex, key, val);
          });
      LOG.info(
          "index refresh finished waitSpan={}ms, buildSpan={}ms, indexSize={}",
          waitTime - startTime,
          System.currentTimeMillis() - waitTime,
          tempIndex.size());
      index = tempIndex;
    } finally {
      doubleWrite = false;
    }
  }

  @VisibleForTesting
  public void triggerRefresh() {
    indexerRefresher.wakeup();
  }

  protected abstract void dataStoreForEach(BiConsumer<K, V> consumer);

  private static final class Term {
    private final AtomicLong start = new AtomicLong(0);
    private final AtomicLong done = new AtomicLong(0);
    private static final long timeoutMillis = 10000;

    private boolean waitAllDone() {
      long startTs = System.currentTimeMillis();
      for (; ; ) {
        long doneCount = this.done.get();
        long startCount = this.start.get();
        if (startCount == doneCount) {
          return true;
        }
        if (System.currentTimeMillis() - startTs > timeoutMillis) {
          return false;
        }
        ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
    }
  }

  private final class IndexerRefresher extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      refresh();
    }

    @Override
    public int getWaitingMillis() {
      return (int) ((ThreadLocalRandom.current().nextDouble() * 30 + 30) * 1000);
    }
  }
}
