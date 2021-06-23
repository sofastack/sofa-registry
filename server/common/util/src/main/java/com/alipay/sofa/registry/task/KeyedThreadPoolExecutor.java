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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import io.prometheus.client.Counter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * thread unsafe, could not use concurrently
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-15 13:38 yuzhi.lyz Exp $
 */
public class KeyedThreadPoolExecutor {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyedThreadPoolExecutor.class);
  private final AbstractWorker[] workers;
  protected final String executorName;
  protected final int coreBufferSize;
  protected final int coreSize;

  private final Counter taskCounter;

  public KeyedThreadPoolExecutor(String executorName, int coreSize, int coreBufferSize) {
    this.executorName = executorName;
    this.coreBufferSize = coreBufferSize;
    this.coreSize = coreSize;
    this.taskCounter =
        Counter.build()
            .namespace("keyedExecutor")
            .help("metrics for keyed executor")
            .name(executorName.replace('-', '_') + "_task_total")
            .labelNames("idx", "type")
            .register();

    workers = createWorkers(coreSize, coreBufferSize);
    for (int i = 0; i < coreSize; i++) {
      ConcurrentUtils.createDaemonThread(executorName + "_" + i, workers[i]).start();
    }
    TaskMetrics.getInstance().registerKeyThreadExecutor("KeyedExecutor-" + executorName, this);
  }

  protected AbstractWorker[] createWorkers(int coreSize, int coreBufferSize) {
    BlockingQueues<KeyedTask> queues = new BlockingQueues<>(coreSize, coreBufferSize, false);
    AbstractWorker[] workers = new AbstractWorker[coreSize];
    for (int i = 0; i < coreSize; i++) {
      workers[i] = new WorkerImpl(i, queues);
    }
    return workers;
  }

  protected interface Worker extends Runnable {
    int size();

    KeyedTask poll() throws InterruptedException;

    boolean offer(KeyedTask task);
  }

  private final class WorkerImpl extends AbstractWorker {
    final BlockingQueues<KeyedTask> queues;
    final BlockingQueue<KeyedTask> queue;

    WorkerImpl(int idx, BlockingQueues<KeyedTask> queues) {
      super(idx);
      this.queues = queues;
      this.queue = queues.getQueue(idx);
    }

    public int size() {
      return queue.size();
    }

    public KeyedTask poll() throws InterruptedException {
      return queue.poll(180, TimeUnit.SECONDS);
    }

    public boolean offer(KeyedTask task) {
      return queues.offer(idx, task);
    }
  }

  protected abstract class AbstractWorker<T> implements Worker {
    final int idx;
    final Counter.Child workerExecCounter;
    final Counter.Child workerCommitCounter;
    volatile boolean running;

    protected AbstractWorker(int idx) {
      this.idx = idx;
      this.workerExecCounter = taskCounter.labels(String.valueOf(idx), "exec");
      this.workerCommitCounter = taskCounter.labels(String.valueOf(idx), "commit");
    }

    @Override
    public void run() {
      for (; ; ) {
        try {
          final KeyedTask task = poll();
          if (task == null) {
            LOGGER.info("{}_{} idle", executorName, idx);
            continue;
          }
          running = true;
          task.run();
          workerExecCounter.inc();
        } catch (Throwable e) {
          LOGGER.safeError("{}_{} run task error", executorName, idx, e);
        } finally {
          running = false;
        }
      }
    }

    protected boolean isActive() {
      return running;
    }
  }

  public int getQueueSize() {
    int size = 0;
    for (Worker w : workers) {
      size += w.size();
    }
    return size;
  }

  public int getActiveCount() {
    int count = 0;
    for (AbstractWorker w : workers) {
      if (w.isActive()) {
        count++;
      }
    }
    return count;
  }

  public long getTaskCount() {
    long count = 0;
    for (AbstractWorker w : workers) {
      count += w.workerCommitCounter.get();
    }
    return count;
  }

  public long getCompletedTaskCount() {
    long count = 0;
    for (AbstractWorker w : workers) {
      count += w.workerExecCounter.get();
    }
    return count;
  }

  public int getCoreSize() {
    return coreSize;
  }

  public <T extends Runnable> KeyedTask<T> execute(Object key, T runnable) {
    KeyedTask task = new KeyedTask(key, runnable);
    AbstractWorker w = workerOf(key);
    // should not happen,
    if (!w.offer(task)) {
      throw new FastRejectedExecutionException(
          String.format(
              "%s_%d full, max=%d, now=%d", executorName, w.idx, coreBufferSize, w.size()));
    }
    w.workerCommitCounter.inc();
    return task;
  }

  private AbstractWorker workerOf(Object key) {
    int n = (key.hashCode() & 0x7fffffff) % workers.length;
    return workers[n];
  }
}
