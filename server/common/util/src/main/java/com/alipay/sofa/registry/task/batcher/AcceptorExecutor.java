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
package com.alipay.sofa.registry.task.batcher;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An active object with an internal thread accepting tasks from clients, and dispatching them to
 * workers in a pull based manner. Workers explicitly request an item or a batch of items whenever they are
 * available. This guarantees that data to be processed are always up to date, and no stale data processing is done.
 *
 * <h3>Task identification</h3>
 * Each task passed for processing has a corresponding task id. This id is used to remove duplicates (replace
 * older copies with newer ones).
 *
 * <h3>Re-processing</h3>
 * If data processing by a worker failed, and the failure is transient in nature, the worker will put back the
 * task(s) back to the {@link AcceptorExecutor}. This data will be merged with current workload, possibly discarded if
 * a newer version has been already received.
 *
 * @author Tomasz Bak

 * @author shangyu.wh modify
 * @version $Id: AcceptorExecutor.java, v 0.1 2017-11-14 15:35 shangyu.wh Exp $
 */
public class AcceptorExecutor<ID, T> {

    private static final Logger                          LOGGER            = LoggerFactory
                                                                               .getLogger(AcceptorExecutor.class);

    /**
     * Get the maximum number of task that can be allowed to back up in the task pool
     * Default 10000
     */
    private final int                                    maxBufferSize;

    private final String                                 name;

    private final AtomicBoolean                          isShutdown        = new AtomicBoolean(
                                                                               false);

    private final BlockingQueue<TaskHolder<ID, T>>       acceptorQueue     = new LinkedBlockingQueue<>();
    private final BlockingDeque<TaskHolder<ID, T>>       reprocessQueue    = new LinkedBlockingDeque<>();
    private final Thread                                 acceptorThread;

    private final Map<ID, TaskHolder<ID, T>>             pendingTasks      = new HashMap<>();
    private final Deque<ID>                              processingOrder   = new LinkedList<>();

    private final Semaphore                              workSemaphore     = new Semaphore(0);
    private final BlockingQueue<TaskHolder<ID, T>>       itemWorkQueue     = new LinkedBlockingQueue<>();

    private final Semaphore                              batchWorkRequests = new Semaphore(0);
    private final BlockingQueue<List<TaskHolder<ID, T>>> batchWorkQueue    = new LinkedBlockingQueue<>();

    private final TrafficShaper                          trafficShaper;

    private AtomicLong                                   acceptedTasks     = new AtomicLong();

    private AtomicLong                                   replayedTasks     = new AtomicLong();

    private AtomicLong                                   expiredTasks      = new AtomicLong();

    private AtomicLong                                   overriddenTasks   = new AtomicLong();

    private AtomicLong                                   queueOverflows    = new AtomicLong();

    AcceptorExecutor(String id, int maxBufferSize, long congestionRetryDelayMs,
                     long networkFailureRetryMs) {
        this.name = "TaskAcceptor-" + id;
        this.maxBufferSize = maxBufferSize;
        this.trafficShaper = new TrafficShaper(congestionRetryDelayMs, networkFailureRetryMs);

        ThreadGroup threadGroup = new ThreadGroup("serverTaskExecutors");
        this.acceptorThread = new Thread(threadGroup, new AcceptorRunner(), "TaskAcceptor-" + id);
        this.acceptorThread.setDaemon(true);
        this.acceptorThread.start();

    }

    void process(ID id, T task, long expiryTime) {
        acceptorQueue.add(new TaskHolder<>(id, task, expiryTime));
        acceptedTasks.incrementAndGet();
    }

    void reprocess(TaskHolder<ID, T> taskHolder, ProcessingResult processingResult) {
        reprocessQueue.add(taskHolder);
        replayedTasks.incrementAndGet();
        trafficShaper.registerFailure(processingResult);
    }

    BlockingQueue<TaskHolder<ID, T>> requestWorkItem() {
        workSemaphore.release();
        return itemWorkQueue;
    }

    BlockingQueue<List<TaskHolder<ID, T>>> requestWorkItems() {
        batchWorkRequests.release();
        return batchWorkQueue;
    }

    void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            acceptorThread.interrupt();
        }
    }

    class AcceptorRunner implements Runnable {
        @Override
        public void run() {
            long scheduleTime = 0;
            while (!isShutdown.get()) {
                try {
                    drainInputQueues();

                    int totalItems = processingOrder.size();

                    long now = System.currentTimeMillis();
                    if (scheduleTime < now) {
                        scheduleTime = now + trafficShaper.transmissionDelay();
                    }
                    if (scheduleTime <= now) {
                        assignItemWork();
                    }

                    if (totalItems == processingOrder.size()) {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException ex) {
                    // Ignore
                } catch (Throwable e) {
                    LOGGER.warn("AcceptorThread error", e);
                }
            }
        }

        private boolean isFull() {
            return pendingTasks.size() >= maxBufferSize;
        }

        private void drainInputQueues() throws InterruptedException {
            do {
                drainReprocessQueue();
                drainAcceptorQueue();

                if (!isShutdown.get()) {
                    // If all queues are empty, block for a while on the acceptor queue
                    if (reprocessQueue.isEmpty() && acceptorQueue.isEmpty()
                        && pendingTasks.isEmpty()) {
                        TaskHolder<ID, T> taskHolder = acceptorQueue
                            .poll(10, TimeUnit.MILLISECONDS);
                        if (taskHolder != null) {
                            appendTaskHolder(taskHolder);
                        }
                    }
                }
            } while (!reprocessQueue.isEmpty() || !acceptorQueue.isEmpty()
                     || pendingTasks.isEmpty());
        }

        private void drainAcceptorQueue() {
            while (!acceptorQueue.isEmpty()) {
                appendTaskHolder(acceptorQueue.poll());
            }
        }

        private void drainReprocessQueue() {
            long now = System.currentTimeMillis();
            while (!reprocessQueue.isEmpty() && !isFull()) {
                TaskHolder<ID, T> taskHolder = reprocessQueue.pollLast();
                ID id = taskHolder.getId();
                //expiryTime < 0 means task no expired
                if (taskHolder.getExpiryTime() > 0 && taskHolder.getExpiryTime() <= now) {
                    expiredTasks.incrementAndGet();
                } else if (pendingTasks.containsKey(id)) {
                    overriddenTasks.incrementAndGet();
                } else {
                    pendingTasks.put(id, taskHolder);
                    processingOrder.addFirst(id);
                }
            }
            if (isFull()) {
                LOGGER
                    .error(
                        "Now pending task full,it will clear reprocessQueue in to add new task,reprocessQueue size={},queueOverflows={},name={}",
                        reprocessQueue.size(), queueOverflows, name);
                queueOverflows.addAndGet(reprocessQueue.size());
                reprocessQueue.clear();
            }
        }

        private void appendTaskHolder(TaskHolder<ID, T> taskHolder) {
            if (isFull()) {
                LOGGER
                    .error(
                        "Now pending task full,it will remove first one to add task={},queueOverflows={},name={}",
                        taskHolder.getId(), queueOverflows, name);
                pendingTasks.remove(processingOrder.poll());
                queueOverflows.incrementAndGet();
            }
            TaskHolder<ID, T> previousTask = pendingTasks.put(taskHolder.getId(), taskHolder);
            if (previousTask == null) {
                processingOrder.add(taskHolder.getId());
            } else {
                overriddenTasks.incrementAndGet();
            }
        }

        void assignItemWork() {
            if (!processingOrder.isEmpty()) {
                if (workSemaphore.tryAcquire(1)) {
                    long now = System.currentTimeMillis();
                    while (!processingOrder.isEmpty()) {
                        ID id = processingOrder.poll();
                        TaskHolder<ID, T> holder = pendingTasks.remove(id);
                        //expiryTime < 0 means task no expired
                        if (holder.getExpiryTime() < 0 || holder.getExpiryTime() > now) {
                            itemWorkQueue.add(holder);
                            return;
                        }
                        expiredTasks.incrementAndGet();
                    }
                    workSemaphore.release();
                }
            }
        }

    }

    /**
     * Getter method for property <tt>maxBufferSize</tt>.
     *
     * @return property value of maxBufferSize
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Getter method for property <tt>acceptedTasks</tt>.
     *
     * @return property value of acceptedTasks
     */
    public AtomicLong getAcceptedTasks() {
        return acceptedTasks;
    }

    /**
     * Getter method for property <tt>replayedTasks</tt>.
     *
     * @return property value of replayedTasks
     */
    public AtomicLong getReplayedTasks() {
        return replayedTasks;
    }

    /**
     * Getter method for property <tt>expiredTasks</tt>.
     *
     * @return property value of expiredTasks
     */
    public AtomicLong getExpiredTasks() {
        return expiredTasks;
    }

    /**
     * Getter method for property <tt>overriddenTasks</tt>.
     *
     * @return property value of overriddenTasks
     */
    public AtomicLong getOverriddenTasks() {
        return overriddenTasks;
    }

    /**
     * Getter method for property <tt>queueOverflows</tt>.
     *
     * @return property value of queueOverflows
     */
    public AtomicLong getQueueOverflows() {
        return queueOverflows;
    }

    /**
     * Get pending task size
     * @return
     */
    public int getPendingTaskSize() {
        return pendingTasks.size();
    }
}