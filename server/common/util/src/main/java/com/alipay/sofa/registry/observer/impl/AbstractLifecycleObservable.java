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
package com.alipay.sofa.registry.observer.impl;

import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.lifecycle.LifecycleController;
import com.alipay.sofa.registry.lifecycle.LifecycleState;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.UnblockingObserver;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chen.zhu
 *     <p>Nov 25, 2020
 */
public class AbstractLifecycleObservable extends AbstractLifecycle
    implements Observable, Lifecycle {

  final List<UnblockingObserver> observers = new CopyOnWriteArrayList<>();

  public AbstractLifecycleObservable() {}

  public AbstractLifecycleObservable(
      LifecycleState lifecycleState, LifecycleController lifecycleController) {
    super(lifecycleState, lifecycleController);
  }

  @Override
  public void addObserver(UnblockingObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(UnblockingObserver observer) {
    observers.remove(observer);
  }

  protected void notifyObservers(final Object message) {

    for (final Object observer : observers) {
      try {
        ((UnblockingObserver) observer).update(AbstractLifecycleObservable.this, message);
      } catch (Throwable e) {
        logger.error("[notifyObservers][{}]", observer, e);
      }
    }
  }
}
