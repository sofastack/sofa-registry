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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

/**
 * batch callable with future
 *
 * @author xiaojian.xj
 * @version $Id: BatchCallableRunnable.java, v 0.1 2021年01月22日 22:00 xiaojian.xj Exp $
 */
public abstract class BatchCallableRunnable<T, E> {

  protected Integer sleep;

  protected TimeUnit timeUnit;

  // must have
  protected int batchSize;

  // no blocking
  private final Queue<TaskEvent> queue = new ConcurrentLinkedQueue();

  public InvokeFuture commit(TaskEvent task) {
    queue.offer(task);
    return task.future;
  }

  public abstract boolean batchProcess(List<TaskEvent> tasks);

  @PostConstruct
  public void init() {
    setBatchSize();
    setTimeUnit();
    setSleep();
    ConcurrentUtils.createDaemonThread(
            this.getClass().getSimpleName() + "WatchDog", new BatchCallableWatchDog())
        .start();
  }

  private class BatchCallableWatchDog extends LoopRunnable {

    private final Logger LOG = LoggerFactory.getLogger(BatchCallableWatchDog.class);

    @Override
    public void runUnthrowable() {
      List<TaskEvent> tasks = new ArrayList<>();

      for (int i = 0; i < batchSize && !queue.isEmpty(); i++) {
        TaskEvent task = queue.poll();
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
        LOG.error("batch run task error.", t);
        for (TaskEvent task : tasks) {
          task.future.error(t.getMessage());
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(sleep, timeUnit);
    }
  }

  public final class TaskEvent {

    private T data;

    private InvokeFuture<E> future;

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
     * Setter method for property <tt>data</tt>.
     *
     * @param data value to be assigned to property data
     */
    public void setData(T data) {
      this.data = data;
    }

    /**
     * Getter method for property <tt>future</tt>.
     *
     * @return property value of future
     */
    public InvokeFuture<E> getFuture() {
      return future;
    }

    /**
     * Setter method for property <tt>future</tt>.
     *
     * @param future value to be assigned to property future
     */
    public void setFuture(InvokeFuture<E> future) {
      this.future = future;
    }
  }

  public class InvokeFuture<E> {

    private boolean success;

    private String message;

    private E response;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public void finish() {
      this.countDownLatch.countDown();
    }

    public void fail() {
      this.success = false;
      this.countDownLatch.countDown();
    }

    public void error(String message) {
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

  /** Setter method for property <tt>sleep</tt>. */
  protected abstract void setSleep();

  /** Setter method for property <tt>timeUnit</tt>. */
  protected abstract void setTimeUnit();

  /** Setter method for property <tt>batchSize</tt>. */
  protected abstract void setBatchSize();
}
