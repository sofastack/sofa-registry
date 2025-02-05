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
package com.alipay.sofa.registry.lifecycle;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public interface LifecycleState {

  boolean isEmpty();

  boolean isInitializing();

  boolean isInitialized();

  boolean isStarting();

  boolean isStarted();

  boolean isStopping();

  boolean isStopped();

  boolean isPositivelyStopped();

  boolean isDisposing();

  boolean isDisposed();

  boolean isPositivelyDisposed();

  LifecyclePhase getPhase();

  void setPhase(LifecyclePhase phase);

  /**
   * rollback to previous state
   *
   * @param e Exception
   */
  void rollback(Exception e);

  boolean canInitialize();

  boolean canStart();

  boolean canStop();

  boolean canDispose();

  enum LifecyclePhase {
    INITIALIZING {
      @Override
      LifecyclePhase next() {
        return INITIALIZED;
      }
    },
    INITIALIZED {
      @Override
      LifecyclePhase next() {
        return STARTING;
      }
    },
    STARTING {
      @Override
      LifecyclePhase next() {
        return STARTED;
      }
    },
    STARTED {
      @Override
      LifecyclePhase next() {
        return STOPPING;
      }
    },
    STOPPING {
      @Override
      LifecyclePhase next() {
        return STOPPED;
      }
    },
    STOPPED {
      @Override
      LifecyclePhase next() {
        return DISPOSING;
      }
    },
    DISPOSING {
      @Override
      LifecyclePhase next() {
        return DISPOSED;
      }
    },
    DISPOSED {
      @Override
      LifecyclePhase next() {
        return INITIALIZING;
      }
    };

    abstract LifecyclePhase next();
  }
}
