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
package com.alipay.sofa.registry.util;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

/**
 * batch callable with future
 *
 * @author xiaojian.xj
 * @version $Id: BatchCallableRunnable.java, v 0.1 2021年01月22日 22:00 xiaojian.xj Exp $
 */
public abstract class BatchCallableRunnable<T, E> {
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  protected final int sleep;

  protected final TimeUnit timeUnit;

  // must have
  protected final int batchSize;

  // no blocking
  private final Queue<TaskEvent> queue = new LinkedBlockingQueue<>(1024 * 100);
  private final BatchCallableWatchDog watchDog = new BatchCallableWatchDog();

  protected BatchCallableRunnable(int sleep, TimeUnit timeUnit, int batchSize) {
    this.sleep = sleep;
    this.timeUnit = timeUnit;
    this.batchSize = batchSize;
  }

  public InvokeFuture commit(TaskEvent task) {
    if (!queue.offer(task)) {
      throw new RejectedExecutionException("queue is full:" + queue.size());
    }
    return task.future;
  }

  protected abstract boolean batchProcess(List<TaskEvent> tasks);

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread(this.getClass().getSimpleName() + "WatchDog", watchDog)
        .start();
  }

  private class BatchCallableWatchDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      List<TaskEvent> tasks = Lists.newLinkedList();

      for (int i = 0; i < batchSize; i++) {
        TaskEvent task = queue.poll();
        if (task == null) {
          break;
        }
        tasks.add(task);
      }
      try {
        if (batchProcess(tasks)) {
          for (TaskEvent task : tasks) {
            task.future.finish();
          }
        } else {
          for (TaskEvent task : tasks) {
            task.future.fail();
          }
        }
      } catch (Throwable t) {
        // TODO failed the task
        LOGGER.error("batch run task error.", t);
        for (TaskEvent task : tasks) {
          task.future.error(t.getClass().getName() + ", msg=" + t.getMessage());
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(sleep, timeUnit);
    }
  }

  public final class TaskEvent {

    private final T data;

    private final InvokeFuture<E> future;

    public TaskEvent(T data) {
      this.data = data;
      this.future = new InvokeFuture();
    }

    /**
     * Getter method for property <tt>data</tt>.
     *
     * @return property value of data
     */
    public T getData() {
      return data;
    }

    /**
     * Getter method for property <tt>future</tt>.
     *
     * @return property value of future
     */
    public InvokeFuture<E> getFuture() {
      return future;
    }
  }

  public static class InvokeFuture<E> {

    private volatile boolean success;

    private volatile String message;

    private volatile E response;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    void finish() {
      this.countDownLatch.countDown();
    }

    void fail() {
      this.success = false;
      this.countDownLatch.countDown();
    }

    void error(String message) {
      this.success = false;
      this.message = message;
      this.countDownLatch.countDown();
    }

    public void putResponse(E response) {
      this.success = true;
      this.response = response;
    }

    public boolean isSuccess() throws InterruptedException {
      this.countDownLatch.await();
      return success;
    }

    public E getResponse() throws InterruptedException {
      this.countDownLatch.await();
      return response;
    }

    /**
     * Getter method for property <tt>message</tt>.
     *
     * @return property value of message
     */
    public String getMessage() {
      return message;
    }
  }
}
