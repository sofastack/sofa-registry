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
import com.alipay.sofa.registry.util.ConcurrentUtils;
import io.prometheus.client.Counter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * thread unsafe, could not use concurrently
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-15 13:38 yuzhi.lyz Exp $
 */
public class KeyedThreadPoolExecutor {
    private static final Logger    LOGGER = LoggerFactory.getLogger(KeyedThreadPoolExecutor.class);
    private final AbstractWorker[] workers;
    private final String           executorName;
    private final int              coreBufferSize;

    private final Counter          taskCounter;

    public KeyedThreadPoolExecutor(String executorName, int coreSize, int coreBufferSize) {
        this.executorName = executorName;
        this.coreBufferSize = coreBufferSize;
        this.taskCounter = Counter.build().namespace("keyedExecutor")
            .help("metrics for keyed executor")
            .name(executorName.replace('-', '_') + "_task_total").labelNames("idx", "type")
            .register();
        workers = new AbstractWorker[coreSize];
        for (int i = 0; i < coreSize; i++) {
            AbstractWorker w = createWorker(i, coreBufferSize);
            workers[i] = w;
            ConcurrentUtils.createDaemonThread(executorName + "_" + i, w).start();
        }
    }

    protected AbstractWorker createWorker(int idx, int coreBufferSize) {
        return new WorkerImpl(idx, new LinkedBlockingQueue<>(coreBufferSize));
    }

    protected interface Worker extends Runnable {
        int size();

        KeyedTask poll() throws InterruptedException;

        boolean offer(KeyedTask task);

    }

    private final class WorkerImpl extends AbstractWorker {
        final BlockingQueue<KeyedTask> queue;

        WorkerImpl(int idx, BlockingQueue<KeyedTask> queue) {
            super(idx);
            this.queue = queue;
        }

        public int size() {
            return queue.size();
        }

        public KeyedTask poll() throws InterruptedException {
            return queue.poll(180, TimeUnit.SECONDS);
        }

        public boolean offer(KeyedTask task) {
            return queue.offer(task);
        }

    }

    protected abstract class AbstractWorker<T> implements Worker {
        final int           idx;
        final Counter.Child workerExecCounter;
        final Counter.Child workerCommitCounter;

        protected AbstractWorker(int idx) {
            this.idx = idx;
            this.workerExecCounter = taskCounter.labels(String.valueOf(idx), "exec");
            this.workerCommitCounter = taskCounter.labels(String.valueOf(idx), "commit");
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    final KeyedTask task = poll();
                    if (task == null) {
                        LOGGER.info("{}_{} idle", executorName, idx);
                        continue;
                    }
                    task.run();
                    workerExecCounter.inc();
                } catch (Throwable e) {
                    LOGGER.error("{}_{} run task error", executorName, idx, e);
                }
            }
        }
    }

    private int getQueueSize() {
        int size = 0;
        for (Worker w : workers) {
            size += w.size();
        }
        return size;
    }

    public <T extends Runnable> KeyedTask<T> execute(Object key, T runnable) {
        final int size = getQueueSize();
        if (size >= coreBufferSize) {
            throw new FastRejectedExecutionException(String.format("%s is full, max=%d, now=%d",
                executorName, coreBufferSize, size));
        }
        KeyedTask task = new KeyedTask(key, runnable);
        AbstractWorker w = workerOf(key);
        // should not happen,
        if (!w.offer(task)) {
            throw new FastRejectedExecutionException(String.format("%s_%d full, max=%d, now=%d",
                executorName, w.idx, coreBufferSize, w.size()));
        }
        w.workerCommitCounter.inc();
        return task;
    }

    private AbstractWorker workerOf(Object key) {
        int n = (key.hashCode() & 0x7fffffff) % workers.length;
        return workers[n];
    }

}
