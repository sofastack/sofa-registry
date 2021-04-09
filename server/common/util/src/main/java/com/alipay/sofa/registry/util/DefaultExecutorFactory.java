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

import java.util.concurrent.*;

/**
 * @author chen.zhu
 *     <p>Nov 23, 2020
 */
public class DefaultExecutorFactory implements ObjectFactory<ExecutorService> {

  private static final int DEFAULT_MAX_QUEUE_SIZE = 1 << 20;
  private static final RejectedExecutionHandler DEFAULT_HANDLER =
      new ThreadPoolExecutor.CallerRunsPolicy();
  private static final int DEFAULT_CORE_POOL_SIZE = OsUtils.getCpuCount();
  private static final int DEFAULT_MAX_POOL_SIZE = 4 * OsUtils.getCpuCount();
  private static final int DEFAULT_KEEPER_ALIVE_TIME_SECONDS = 60;
  private static final boolean DEFAULT_ALLOW_CORE_THREAD_TIMEOUT = true;
  private static final String DEFAULT_THREAD_PREFIX = "SofaRegistry";

  private final int corePoolSize;
  private final int maxPoolSize;
  private final long keepAliveTime;
  private final TimeUnit keepAliveTimeUnit;
  private final ThreadFactory threadFactory;
  private final RejectedExecutionHandler rejectedExecutionHandler;
  private final boolean allowCoreThreadTimeOut;
  private int queueSize = DEFAULT_MAX_QUEUE_SIZE;
  private ObjectFactory<BlockingQueue<Runnable>> blockingQueueObjectFactory;

  public static DefaultExecutorFactory createCachedThreadPoolFactory(
      String threadNamePrefix,
      int corePoolSize,
      int corePoolTimeAlive,
      TimeUnit corePoolTimeAliveUnit) {
    return new DefaultExecutorFactory(
        new NamedThreadFactory(threadNamePrefix),
        corePoolSize,
        true,
        Integer.MAX_VALUE,
        new ObjectFactory<BlockingQueue<Runnable>>() {
          @Override
          public BlockingQueue<Runnable> create() {
            return new SynchronousQueue<Runnable>();
          }
        },
        corePoolTimeAlive,
        corePoolTimeAliveUnit,
        DEFAULT_HANDLER);
  }

  public static Builder builder() {
    return new Builder();
  }

  private DefaultExecutorFactory(
      ThreadFactory threadFactory,
      int corePoolSize,
      boolean allowCoreThreadTimeOut,
      int maxPoolSize,
      int maxQueueSize,
      int keepAliveTime,
      TimeUnit keepAliveTimeUnit,
      RejectedExecutionHandler rejectedExecutionHandler) {
    this.threadFactory = threadFactory;
    this.corePoolSize = corePoolSize;
    this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    this.maxPoolSize = maxPoolSize;
    this.keepAliveTime = keepAliveTime;
    this.keepAliveTimeUnit = keepAliveTimeUnit;
    this.queueSize = maxQueueSize;
    this.rejectedExecutionHandler = rejectedExecutionHandler;
  }

  private DefaultExecutorFactory(
      ThreadFactory threadFactory,
      int corePoolSize,
      boolean allowCoreThreadTimeOut,
      int maxPoolSize,
      ObjectFactory<BlockingQueue<Runnable>> queueFactory,
      int keepAliveTime,
      TimeUnit keepAliveTimeUnit,
      RejectedExecutionHandler rejectedExecutionHandler) {
    this.threadFactory = threadFactory;
    this.corePoolSize = corePoolSize;
    this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    this.maxPoolSize = maxPoolSize;
    this.keepAliveTime = keepAliveTime;
    this.keepAliveTimeUnit = keepAliveTimeUnit;
    this.blockingQueueObjectFactory = queueFactory;
    this.rejectedExecutionHandler = rejectedExecutionHandler;
  }

  public static class Builder {

    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private int corePoolTimeAlive = DEFAULT_KEEPER_ALIVE_TIME_SECONDS;
    private TimeUnit corePoolTimeAliveUnit = TimeUnit.SECONDS;
    private int queueSize = DEFAULT_MAX_QUEUE_SIZE;
    private ObjectFactory<BlockingQueue<Runnable>> workQueueFactory;
    private ThreadFactory threadFactory;
    private String threadNamePrefix = DEFAULT_THREAD_PREFIX;
    private RejectedExecutionHandler rejectedExecutionHandler = DEFAULT_HANDLER;
    private boolean allowCoreThreadTimeOut = DEFAULT_ALLOW_CORE_THREAD_TIMEOUT;

    public Builder corePoolSize(int corePoolSize) {
      this.corePoolSize = corePoolSize;
      return this;
    }

    public Builder maxPoolSize(int maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
      return this;
    }

    public Builder corePoolTimeAlive(int corePoolTimeAlive) {
      this.corePoolTimeAlive = corePoolTimeAlive;
      return this;
    }

    public Builder corePoolTimeAliveUnit(TimeUnit corePoolTimeAliveUnit) {
      this.corePoolTimeAliveUnit = corePoolTimeAliveUnit;
      return this;
    }

    public Builder workQueueFactory(ObjectFactory<BlockingQueue<Runnable>> workQueueFactory) {
      this.workQueueFactory = workQueueFactory;
      return this;
    }

    public Builder threadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
      return this;
    }

    public Builder threadNamePrefix(String threadNamePrefix) {
      this.threadNamePrefix = threadNamePrefix;
      return this;
    }

    public Builder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
      this.rejectedExecutionHandler = rejectedExecutionHandler;
      return this;
    }

    public Builder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
      this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
      return this;
    }

    public Builder queueSize(int queueSize) {
      if (workQueueFactory != null) {
        throw new IllegalArgumentException(
            "work queue has been set, queueSize should not be called");
      }
      this.queueSize = queueSize;
      return this;
    }

    public DefaultExecutorFactory build() {
      ThreadFactory threadFactory =
          this.threadFactory != null
              ? this.threadFactory
              : new NamedThreadFactory(threadNamePrefix);
      if (workQueueFactory == null) {
        return new DefaultExecutorFactory(
            threadFactory,
            corePoolSize,
            allowCoreThreadTimeOut,
            maxPoolSize,
            queueSize,
            corePoolTimeAlive,
            corePoolTimeAliveUnit,
            rejectedExecutionHandler);
      } else {
        return new DefaultExecutorFactory(
            threadFactory,
            corePoolSize,
            allowCoreThreadTimeOut,
            maxPoolSize,
            workQueueFactory,
            corePoolTimeAlive,
            corePoolTimeAliveUnit,
            rejectedExecutionHandler);
      }
    }
  }

  @Override
  public ExecutorService create() {
    // core pool size must be less or equal to max size
    int useMaxPoolSize = Math.max(corePoolSize, maxPoolSize);
    BlockingQueue<Runnable> workQueue =
        blockingQueueObjectFactory == null
            ? new LinkedBlockingQueue<Runnable>(queueSize)
            : blockingQueueObjectFactory.create();
    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(
            corePoolSize,
            useMaxPoolSize,
            keepAliveTime,
            keepAliveTimeUnit,
            workQueue,
            threadFactory,
            rejectedExecutionHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    return executor;
  }
}
