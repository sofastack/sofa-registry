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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * {@link TaskExecutors} instance holds a number of worker threads that cooperate with {@link
 * AcceptorExecutor}. Each worker sends a job request to {@link AcceptorExecutor} whenever it is
 * available, and processes it once provided with a task(s).
 *
 * @author Tomasz Bak
 * @author shangyu.wh modify
 * @version $Id: TaskExecutors.java, v 0.1 2017-11-14 16:00 shangyu.wh Exp $
 */
public class TaskExecutors<ID, T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutors.class);

  private final AtomicBoolean isShutdown;
  private final List<Thread> workerThreads;

  /**
   * constructor
   *
   * @param workerRunnableFactory
   * @param workerCount
   * @param isShutdown
   */
  TaskExecutors(
      Function<Integer, WorkerRunnable<ID, T>> workerRunnableFactory,
      int workerCount,
      AtomicBoolean isShutdown) {
    this.isShutdown = isShutdown;
    this.workerThreads = new ArrayList<>();

    ThreadGroup threadGroup = new ThreadGroup("serverTaskExecutors");
    for (int i = 0; i < workerCount; i++) {
      WorkerRunnable<ID, T> runnable = workerRunnableFactory.apply(i);
      Thread workerThread = new Thread(threadGroup, runnable, runnable.getWorkerName());
      workerThreads.add(workerThread);
      workerThread.setDaemon(true);
      workerThread.start();
    }
  }

  /**
   * @param name
   * @param workerCount
   * @param processor
   * @param acceptorExecutor
   * @param <ID>
   * @param <T>
   * @return
   */
  static <ID, T> TaskExecutors<ID, T> createTaskExecutors(
      final String name,
      int workerCount,
      final TaskProcessor<T> processor,
      final AcceptorExecutor<ID, T> acceptorExecutor) {
    final AtomicBoolean isShutdown = new AtomicBoolean();
    return new TaskExecutors<>(
        idx -> new WorkerRunnable<>(name + '-' + idx, isShutdown, processor, acceptorExecutor),
        workerCount,
        isShutdown);
  }

  /** shutdown workerThread */
  void shutdown() {
    if (isShutdown.compareAndSet(false, true)) {
      for (Thread workerThread : workerThreads) {
        workerThread.interrupt();
      }
    }
  }

  /**
   * @param <ID>
   * @param <T>
   */
  static class WorkerRunnable<ID, T> implements Runnable {

    final String workerName;
    final AtomicBoolean isShutdown;
    final TaskProcessor<T> processor;
    final AcceptorExecutor<ID, T> acceptorExecutor;

    WorkerRunnable(
        String workerName,
        AtomicBoolean isShutdown,
        TaskProcessor<T> processor,
        AcceptorExecutor<ID, T> acceptorExecutor) {
      this.workerName = workerName;
      this.isShutdown = isShutdown;
      this.processor = processor;
      this.acceptorExecutor = acceptorExecutor;
    }

    String getWorkerName() {
      return workerName;
    }

    @Override
    public void run() {
      try {
        while (!isShutdown.get()) {
          try {
            BlockingQueue<TaskHolder<ID, T>> workQueue = acceptorExecutor.requestWorkItem();
            TaskHolder<ID, T> taskHolder;
            while ((taskHolder = workQueue.poll(1, TimeUnit.SECONDS)) == null) {
              if (isShutdown.get()) {
                return;
              }
            }
            ProcessingResult result = processor.process(taskHolder.getTask());
            switch (result) {
              case Success:
                break;
              case Congestion:
                acceptorExecutor.reprocess(taskHolder, result);
                break;
              case TransientError:
                acceptorExecutor.reprocess(taskHolder, result);
                break;
              case PermanentError:
                LOGGER.warn("Discarding a task of {} due to permanent error", workerName);
                break;
              default:
                break;
            }
          } catch (InterruptedException e) {
            // Ignore
          } catch (Throwable e) {
            LOGGER.error("Single WorkerThread process error", e);
          }
        }
      } catch (Throwable e) {
        LOGGER.error("WorkerThread error", e);
      }
    }
  }
}
