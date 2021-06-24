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

import com.alipay.sofa.registry.lifecycle.Suspendable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 16:51 yuzhi.lyz Exp $
 */
public abstract class LoopRunnable implements Runnable, Suspendable {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoopRunnable.class);

  private volatile boolean closed = false;

  private volatile boolean suspend = false;

  public abstract void runUnthrowable();

  public abstract void waitingUnthrowable();

  public void close() {
    closed = true;
  }

  @Override
  public void suspend() {
    this.suspend = true;
  }

  @Override
  public void resume() {
    this.suspend = false;
  }

  @Override
  public boolean isSuspended() {
    return suspend;
  }

  public boolean isClosed() {
    return closed;
  }

  public void run() {
    LOGGER.info("loop-run started {}", this.getClass().getSimpleName());
    for (; ; ) {
      try {
        if (closed) {
          LOGGER.warn("[closed] quit");
          break;
        }
        if (suspend) {
          LOGGER.warn("[suspend] break");
        } else {
          try {
            runUnthrowable();
          } catch (Throwable unexpect) {
            LOGGER.error("run unexpect error", unexpect);
          }
        }
        try {
          waitingUnthrowable();
        } catch (Throwable unexpect) {
          LOGGER.error("waiting unexpect error", unexpect);
        }
      } catch (Throwable e) {
        LOGGER.safeError("loop unexpect error", e);
      }
    }
    LOGGER.info("loop-run exit {}, closed={}", this.getClass().getSimpleName(), closed);
  }
}
