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

public class KeyedTask<T extends Runnable> implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyedTask.class);
  final long createTime = System.currentTimeMillis();
  final Object key;
  final T runnable;

  volatile long startTime;
  volatile long endTime;
  volatile boolean success;
  volatile boolean canceled;

  KeyedTask(Object key, T runnable) {
    this.key = key;
    this.runnable = runnable;
  }

  public void cancel() {
    this.canceled = true;
  }

  @Override
  public void run() {
    try {
      this.startTime = System.currentTimeMillis();
      if (!canceled) {
        runnable.run();
      }
      this.success = true;
    } catch (TaskErrorSilenceException ignored) {
      LOGGER.debug("failed to run task {}, {}", key, runnable);
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
    return "KeyedTask{"
        + "createTime="
        + createTime
        + ", key="
        + key
        + ", runnable="
        + runnable
        + ", startTime="
        + startTime
        + ", endTime="
        + endTime
        + ", success="
        + success
        + ", canceled="
        + canceled
        + '}';
  }
}
