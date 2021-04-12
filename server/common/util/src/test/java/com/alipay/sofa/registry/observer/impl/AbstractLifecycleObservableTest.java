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

import com.alipay.sofa.registry.lifecycle.LifecycleController;
import com.alipay.sofa.registry.lifecycle.LifecycleState;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.UnblockingObserver;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractLifecycleObservableTest {
  @Test
  public void test() {
    LifecycleState lifecycleState = Mockito.mock(LifecycleState.class);
    LifecycleController lifecycleController = Mockito.mock(LifecycleController.class);
    AbstractLifecycleObservable observable =
        new AbstractLifecycleObservable(lifecycleState, lifecycleController);
    final AtomicBoolean called = new AtomicBoolean();
    UnblockingObserver o =
        new UnblockingObserver() {
          @Override
          public void update(Observable source, Object message) {
            called.set(true);
            throw new RuntimeException();
          }
        };
    observable.addObserver(o);
    Assert.assertEquals(1, observable.observers.size());
    observable.removeObserver(o);
    Assert.assertEquals(0, observable.observers.size());
    observable.removeObserver(o);
    observable.addObserver(o);
    Assert.assertFalse(called.get());
    observable.notifyObservers("test");
    Assert.assertTrue(called.get());
  }
}
