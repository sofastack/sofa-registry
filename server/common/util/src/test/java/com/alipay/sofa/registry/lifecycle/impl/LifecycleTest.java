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

import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.lifecycle.LifecycleController;
import com.alipay.sofa.registry.lifecycle.LifecycleState;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class LifecycleTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleTest.class);

  @Test
  public void testDefaultLifecycleState() {
    Lifecycle lifecycle = Mockito.mock(Lifecycle.class);
    LifecycleController controller = Mockito.mock(LifecycleController.class);
    DefaultLifecycleState state = new DefaultLifecycleState(lifecycle, controller, LOGGER);
    Assert.assertTrue(state.isEmpty());

    Assert.assertFalse(state.isInitializing());
    Assert.assertFalse(state.isInitialized());

    Assert.assertFalse(state.isStarting());
    Assert.assertFalse(state.isStarted());

    Assert.assertFalse(state.isStopping());
    // not init, state is stopped and disposed
    Assert.assertTrue(state.isStopped());

    Assert.assertFalse(state.isDisposing());
    Assert.assertTrue(state.isDisposed());

    Assert.assertFalse(state.isPositivelyDisposed());
    Assert.assertFalse(state.isPositivelyStopped());

    state.setPhase(LifecycleState.LifecyclePhase.INITIALIZING);
    Assert.assertTrue(state.isInitializing());
    Assert.assertFalse(state.isInitialized());

    state.setPhase(LifecycleState.LifecyclePhase.STARTING);
    Assert.assertTrue(state.isStarting());
    Assert.assertFalse(state.isStarted());

    state.setPhase(LifecycleState.LifecyclePhase.STOPPING);
    Assert.assertTrue(state.isStopping());
    Assert.assertFalse(state.isStopped());

    state.setPhase(LifecycleState.LifecyclePhase.DISPOSING);
    Assert.assertTrue(state.isDisposing());
    Assert.assertFalse(state.isDisposed());

    state.setPhase(LifecycleState.LifecyclePhase.DISPOSED);
    Assert.assertTrue(state.isPositivelyDisposed());
    Assert.assertTrue(state.isPositivelyStopped());

    Assert.assertTrue(state.toString(), state.toString().contains("DISPOSED"));
    // rollback to DISPOSING
    state.rollback(new RuntimeException());
    Assert.assertTrue(state.toString(), state.toString().contains("DISPOSING"));
  }
}
