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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author chen.zhu
 *     <p>Nov 23, 2020
 */
public final class ConcurrentUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentUtils.class);

  private ConcurrentUtils() {}

  public abstract static class SafeParaLoop<T> {

    private static final Logger logger = LoggerFactory.getLogger(SafeParaLoop.class);

    private final List<T> list;

    private final Executor executors;

    public SafeParaLoop(Executor executors, Collection<T> src) {
      this.executors = executors;
      this.list = src == null ? Collections.emptyList() : Lists.newLinkedList(src);
    }

    public void run() {
      for (T t : list) {
        executors.execute(
            () -> {
              try {
                doRun0(t);
              } catch (Throwable e) {
                logger.safeError("[SafeParaLoop][{}]", t, e);
              }
            });
      }
    }

    public boolean runAndWait(long timeoutMillis) {
      CountDownLatch latch = new CountDownLatch(list.size());
      for (T t : list) {
        executors.execute(
            () -> {
              try {
                doRun0(t);
              } catch (Throwable e) {
                logger.safeError("[SafeParaLoop][{}]", t, e);
              } finally {
                latch.countDown();
              }
            });
      }
      try {
        return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        return false;
      }
    }

    protected abstract void doRun0(T t) throws Exception;
  }

  public static Thread createDaemonThread(String name, Runnable r) {
    Thread t = new Thread(r, name);
    t.setDaemon(true);
    return t;
  }

  public static void objectWaitUninterruptibly(Object o, int timeoutMs) {
    try {
      o.wait(timeoutMs);
    } catch (InterruptedException ignored) {
      // no need to remark Thread.currentThread().interrupt();
      LOGGER.warn("Interrupted waiting", ignored);
    }
  }

  public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
    try {
      unit.sleep(sleepFor);
    } catch (InterruptedException ignored) {
      // no need to remark Thread.currentThread().interrupt();
      LOGGER.warn("Interrupted sleeping", ignored);
    }
  }

  public static <T> T pollUninterruptibly(BlockingQueue<T> queue, long wait, TimeUnit unit) {
    try {
      return queue.poll(wait, unit);
    } catch (InterruptedException ignored) {
      // no need to remark Thread.currentThread().interrupt();
      LOGGER.warn("Interrupted polling", ignored);
    }
    return null;
  }
}
