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
package com.alipay.sofa.registry.task;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedPreemptThreadPoolExecutor extends KeyedThreadPoolExecutor {
  private final Comparator<? extends Runnable> comparator;

  public KeyedPreemptThreadPoolExecutor(
      String executorName,
      int coreSize,
      int coreBufferSize,
      Comparator<? extends Runnable> comparator) {
    super(executorName, coreSize, coreBufferSize);
    this.comparator = comparator;
  }

  @Override
  protected AbstractWorker[] createWorkers(int coreSize, int coreBufferSize) {
    AbstractWorker[] workers = new AbstractWorker[coreSize];
    for (int i = 0; i < coreSize; i++) {
      AbstractWorker w = new WorkerImpl(i, coreBufferSize / coreSize);
      workers[i] = w;
    }
    return workers;
  }

  private final class WorkerImpl extends AbstractWorker {
    final Map<Object, KeyedTask> map = new HashMap<>(128);
    final LinkedList<Object> keysQueue;
    final Lock lock = new ReentrantLock();
    final Condition cond = lock.newCondition();
    final int queueSize;

    WorkerImpl(int idx, int queueSize) {
      super(idx);
      this.queueSize = queueSize;
      this.keysQueue = new LinkedList<>();
    }

    public int size() {
      lock.lock();
      try {
        return keysQueue.size();
      } finally {
        lock.unlock();
      }
    }

    public KeyedTask poll() throws InterruptedException {
      // get the key,
      lock.lock();
      try {
        if (keysQueue.isEmpty()) {
          cond.await(180, TimeUnit.SECONDS);
        }
        final Object key = keysQueue.poll();
        if (key == null) {
          return null;
        }
        KeyedTask task = map.remove(key);
        return task;
      } finally {
        lock.unlock();
      }
    }

    public boolean offer(KeyedTask task) {
      if (size() >= queueSize && getQueueSize() >= coreBufferSize) {
        // reach avg and total size, is full
        return false;
      }
      final Comparator<Runnable> c = (Comparator<Runnable>) comparator;
      lock.lock();
      try {
        final KeyedTask prev = map.get(task.key);
        if (prev != null) {
          int comp = c.compare(prev.runnable, task.runnable);
          if (comp >= 0) {
            // the prev is high priority than current, ignored
            return true;
          }
        } else {
          // try add to keys queue to poll
          if (!keysQueue.offer(task.key)) {
            // queue is full
            return false;
          }
        }
        // at last, overwrite the prev or insert the current
        // promise that: if map.task exist, the queue.key must be exist
        map.put(task.key, task);
        cond.signal();
        return true;
      } finally {
        lock.unlock();
      }
    }
  }
}
