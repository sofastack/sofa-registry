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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class BlockingQueues<T> {
  private final BlockingQueue<T>[] queues;
  private final int avgQueueBufferSize;
  private final int queueBufferSize;
  private final int queueNum;

  public BlockingQueues(int queueNum, int queueBufferSize, boolean array) {
    this.queueNum = queueNum;
    this.queues = new BlockingQueue[queueNum];
    for (int i = 0; i < queueNum; i++) {
      // safeguard: set the queue.capacity as queueSize
      this.queues[i] =
          array
              ? new ArrayBlockingQueue<>(queueBufferSize)
              : new LinkedBlockingQueue<>(queueBufferSize);
    }
    // at most, cache double req
    this.avgQueueBufferSize = queueBufferSize / queueNum;
    this.queueBufferSize = queueBufferSize;
  }

  public boolean offer(int idx, T t) {
    BlockingQueue<T> q = queues[idx];
    if (q.size() < avgQueueBufferSize) {
      // not reach avg, add
      return q.offer(t);
    } else {
      // reach avg, check total size
      int totalSize = getTotalQueueSize();
      if (totalSize < queueBufferSize) {
        return q.offer(t);
      }
      return false;
    }
  }

  public void put(int idx, T t) {
    if (!offer(idx, t)) {
      throw new FastRejectedExecutionException(
          String.format(
              "BlockingQueues.put overflow, idx=%d, totalSize=%d, queueSize=%d",
              idx, getTotalQueueSize(), queues[idx].size()));
    }
  }

  public BlockingQueue<T> getQueue(int idx) {
    return queues[idx];
  }

  public int queueNum() {
    return queueNum;
  }

  public int getTotalQueueSize() {
    int count = 0;
    for (BlockingQueue<T> q : queues) {
      count += q.size();
    }
    return count;
  }
}
