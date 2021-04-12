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

import com.alipay.sofa.registry.lifecycle.*;
import com.alipay.sofa.registry.log.Logger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public class DefaultLifecycleState implements LifecycleState {

  private final Logger logger;

  private final AtomicReference<LifecyclePhase> phase = new AtomicReference<>();

  private final AtomicReference<LifecyclePhase> previoisPhase = new AtomicReference<>();

  private final Lifecycle lifecycle;

  private final LifecycleController lifecycleController;

  public DefaultLifecycleState(
      Lifecycle lifecycle, LifecycleController lifecycleController, Logger logger) {
    this.lifecycle = lifecycle;
    this.lifecycleController = lifecycleController;
    this.logger = logger;
  }

  @Override
  public boolean isEmpty() {
    return phase.get() == null;
  }

  @Override
  public boolean isInitializing() {

    LifecyclePhase currentPhase = getPhase();
    return currentPhase != null && currentPhase.equals(LifecyclePhase.INITIALIZING);
  }

  @Override
  public boolean isInitialized() {

    LifecyclePhase currentPhase = getPhase();
    return currentPhase != null
        && phaseNameIn(
            currentPhase,
            LifecyclePhase.INITIALIZED,
            LifecyclePhase.STARTING,
            LifecyclePhase.STARTED,
            LifecyclePhase.STOPPING,
            LifecyclePhase.STOPPED);
  }

  @Override
  public boolean isStarting() {

    LifecyclePhase currentPhase = getPhase();
    return currentPhase != null && currentPhase.equals(LifecyclePhase.STARTING);
  }

  @Override
  public boolean isStarted() {

    LifecyclePhase currentPhase = getPhase();
    return currentPhase == LifecyclePhase.STARTED;
  }

  @Override
  public boolean isStopping() {

    LifecyclePhase currentPhase = getPhase();
    return currentPhase == LifecyclePhase.STOPPING;
  }

  @Override
  public boolean isStopped() {

    LifecyclePhase phaseName = getPhase();
    return phaseName == null
        || (phaseNameIn(
            phaseName,
            LifecyclePhase.INITIALIZED,
            LifecyclePhase.STOPPED,
            LifecyclePhase.DISPOSING,
            LifecyclePhase.DISPOSED));
  }

  @Override
  public boolean isPositivelyStopped() {

    LifecyclePhase phaseName = getPhase();
    return phaseName != null
        && phaseNameIn(
            phaseName, LifecyclePhase.STOPPED, LifecyclePhase.DISPOSING, LifecyclePhase.DISPOSED);
  }

  private boolean phaseNameIn(LifecyclePhase phase, LifecyclePhase... ins) {

    for (LifecyclePhase in : ins) {
      if (phase == in) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDisposing() {

    LifecyclePhase phaseName = getPhase();
    return phaseName == LifecyclePhase.DISPOSING;
  }

  @Override
  public boolean isDisposed() {

    LifecyclePhase phaseName = getPhase();
    return phaseName == null || phaseName == LifecyclePhase.DISPOSED;
  }

  @Override
  public boolean isPositivelyDisposed() {

    LifecyclePhase phaseName = getPhase();
    return phaseName != null && phaseNameIn(getPhase(), LifecyclePhase.DISPOSED);
  }

  @Override
  public LifecyclePhase getPhase() {
    return phase.get();
  }

  @Override
  public void setPhase(LifecyclePhase currentPhase) {
    if (logger.isInfoEnabled()) {
      logger.info(
          "[setPhaseName]{}({}) --> {}",
          lifecycle,
          lifecycle.getClass().getSimpleName(),
          currentPhase);
    }
    previoisPhase.set(phase.get());
    phase.set(currentPhase);
  }

  @Override
  public String toString() {
    return String.format("%s, %s", lifecycle.toString(), phase.get());
  }

  /** only support rollback once */
  @Override
  public void rollback(Exception e) {
    if (logger.isInfoEnabled()) {
      logger.info(
          "[rollback]{},{} -> {}, reason:{}",
          this,
          phase.get(),
          previoisPhase.get(),
          e.getMessage());
    }
    phase.set(previoisPhase.get());
  }

  @Override
  public boolean canInitialize() {
    return lifecycleController.canInitialize(getPhase());
  }

  @Override
  public boolean canStart() {
    return lifecycleController.canStart(getPhase());
  }

  @Override
  public boolean canStop() {
    return lifecycleController.canStop(getPhase());
  }

  @Override
  public boolean canDispose() {
    return lifecycleController.canDispose(getPhase());
  }
}
