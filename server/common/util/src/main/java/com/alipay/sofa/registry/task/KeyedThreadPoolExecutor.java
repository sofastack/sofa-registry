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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * thread unsafe, could not use concurrently
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-15 13:38 yuzhi.lyz Exp $
 */
public class KeyedThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyedThreadPoolExecutor.class);

    private final Worker[]      workers;
    private final String        executorName;
    private final int           coreBufferSize;

    public KeyedThreadPoolExecutor(String executorName, int coreSize, int coreBufferSize) {
        this.executorName = executorName;
        this.coreBufferSize = coreBufferSize;
        workers = new Worker[coreSize];
        for (int i = 0; i < coreSize; i++) {
            Worker w = new Worker(i, new ArrayBlockingQueue<>(coreBufferSize));
            workers[i] = w;
            ConcurrentUtils.createDaemonThread(executorName + "_" + i, w).start();
        }
    }

    private final class Worker implements Runnable {
        final BlockingQueue<KeyedTask> queue;
        final int                      idx;

        Worker(int idx, BlockingQueue<KeyedTask> queue) {
            this.idx = idx;
            this.queue = queue;
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    final KeyedTask task = queue.poll(180, TimeUnit.SECONDS);
                    if (task == null) {
                        LOGGER.info("{}_{} idle", executorName, idx);
                        continue;
                    }
                    task.run();
                } catch (Throwable e) {
                    LOGGER.error("{}_{} run task error", executorName, idx, e);
                }
            }
        }
    }

    public <T extends Runnable> KeyedTask<T> execute(Object key, T runnable) {
        KeyedTask task = new KeyedTask(key, runnable);
        Worker w = workerOf(key);
        if (!w.queue.offer(task)) {
            throw new RejectedExecutionException(String.format("%s_%d full, max=%d, now=%d",
                executorName, w.idx, coreBufferSize, w.queue.size()));
        }
        return task;
    }

    private Worker workerOf(Object key) {
        int n = (key.hashCode() & 0x7fffffff) % workers.length;
        return workers[n];
    }

    public static final class KeyedTask<T extends Runnable> implements Runnable {
        final long       createTime = System.currentTimeMillis();
        final Object     key;
        final T          runnable;

        volatile long    startTime;
        volatile long    endTime;
        volatile boolean success;
        volatile boolean canceled;

        private KeyedTask(Object key, T runnable) {
            this.key = key;
            this.runnable = runnable;
        }

        public void cancel() {
            this.canceled = true;
        }

        @Override
        public void run() {
            try {
                if (!canceled) {
                    runnable.run();
                }
                this.success = true;
            } catch (Throwable e) {
                LOGGER.error("failed to run task {}, {}", key, runnable, e);
            } finally {
                this.endTime = System.currentTimeMillis();
            }
        }

        public boolean isFinished() {
            return this.endTime > 0;
        }

        public boolean isSuccess() {
            return isFinished() && success;
        }

        public boolean isFailed() {
            return isFinished() && !success;
        }

        public long getCreateTime() {
            return createTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public Object key() {
            return key;
        }

        public T getRunnable() {
            return runnable;
        }

        public boolean isOverAfter(int intervalMs) {
            if (!isFinished()) {
                return false;
            }
            return canceled || System.currentTimeMillis() - endTime >= intervalMs;
        }

        @Override
        public String toString() {
            return "KeyedTask{" + "createTime=" + createTime + ", key=" + key + ", runnable="
                   + runnable + ", startTime=" + startTime + ", endTime=" + endTime + ", success="
                   + success + ", canceled=" + canceled + '}';
        }
    }
}
