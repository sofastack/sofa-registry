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

import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public abstract class AbstractLifecycle implements Lifecycle, LifecycleStateAware {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final LifecycleState lifecycleState;
  private final LifecycleController lifecycleController;

  public AbstractLifecycle() {
    this.lifecycleController = new DefaultLifecycleController();
    this.lifecycleState = new DefaultLifecycleState(this, lifecycleController, logger);
  }

  public AbstractLifecycle(LifecycleState lifecycleState, LifecycleController lifecycleController) {
    this.lifecycleState = lifecycleState;
    this.lifecycleController = lifecycleController;
  }

  @Override
  public void initialize() throws InitializeException {
    LifecycleState.LifecyclePhase phaseName = lifecycleState.getPhase();
    if (!lifecycleController.canInitialize(phaseName)) {
      logger.error("[initialize][can not init]{}, {}", phaseName, this);
      throw new IllegalStateException("can not initialize:" + phaseName + "," + this);
    }

    try {
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.INITIALIZING);
      doInitialize();
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.INITIALIZED);
    } catch (Exception e) {
      lifecycleState.rollback(e);
      throw new InitializeException(e);
    }
  }

  protected void doInitialize() throws InitializeException {}

  @Override
  public void start() throws StartException {
    LifecycleState.LifecyclePhase phaseName = lifecycleState.getPhase();
    if (!lifecycleController.canStart(phaseName)) {
      logger.error("[initialize][can not start]{},{}", phaseName, this);
      throw new IllegalStateException("can not start:" + phaseName + ", " + this);
    }

    try {
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.STARTING);
      doStart();
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.STARTED);
    } catch (Exception e) {
      lifecycleState.rollback(e);
      throw new StartException(e);
    }
  }

  protected void doStart() throws StartException {}

  @Override
  public void stop() throws StopException {
    LifecycleState.LifecyclePhase phaseName = lifecycleState.getPhase();
    if (!lifecycleController.canStop(phaseName)) {
      logger.error("[initialize][can not stop]{}, {}", phaseName, this);
      throw new IllegalStateException("can not stop:" + phaseName + "," + this);
    }

    try {
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.STOPPING);
      doStop();
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.STOPPED);
    } catch (Exception e) {
      lifecycleState.rollback(e);
      throw new StopException(e);
    }
  }

  protected void doStop() throws StopException {}

  @Override
  public void dispose() throws DisposeException {
    LifecycleState.LifecyclePhase phaseName = lifecycleState.getPhase();
    if (!lifecycleController.canDispose(phaseName)) {
      logger.error("[initialize][can not stop]{}, {}", phaseName, this);
      throw new IllegalStateException("can not dispose:" + phaseName + "," + this);
    }
    try {
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.DISPOSING);
      doDispose();
      lifecycleState.setPhase(LifecycleState.LifecyclePhase.DISPOSED);
    } catch (Exception e) {
      lifecycleState.rollback(e);
      throw new DisposeException(e);
    }
  }

  protected void doDispose() throws DisposeException {}

  @Override
  public LifecycleState getLifecycleState() {
    return this.lifecycleState;
  }
}
