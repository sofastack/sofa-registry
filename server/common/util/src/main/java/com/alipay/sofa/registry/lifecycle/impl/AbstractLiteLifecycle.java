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
package com.alipay.sofa.registry.lifecycle.impl;

import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.LiteLifecycle;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chen.zhu
 *     <p>Nov 20, 2020
 */
public abstract class AbstractLiteLifecycle implements LiteLifecycle {

  private AtomicBoolean isStarted = new AtomicBoolean(false);

  protected Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void start() throws StartException {

    if (isStarted.compareAndSet(false, true)) {
      logger.info("[start]{}", this);
      doStart();
    } else {
      logger.warn("[start][already started]");
    }
  }

  protected abstract void doStart() throws StartException;

  @Override
  public void stop() throws StopException {
    if (isStarted.compareAndSet(true, false)) {
      logger.info("[stop]{}", this);
      doStop();
    } else {
      logger.warn("[stop][already stopped]");
    }
  }

  protected abstract void doStop() throws StopException;

  public boolean isStarted() {
    return isStarted.get();
  }
}
