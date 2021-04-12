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

import org.junit.Assert;
import org.junit.Test;

public class LifecycleStateTest {

  @Test
  public void testNext() {
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.INITIALIZING.next(),
        LifecycleState.LifecyclePhase.INITIALIZED);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.INITIALIZED.next(), LifecycleState.LifecyclePhase.STARTING);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.STARTING.next(), LifecycleState.LifecyclePhase.STARTED);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.STARTED.next(), LifecycleState.LifecyclePhase.STOPPING);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.STOPPING.next(), LifecycleState.LifecyclePhase.STOPPED);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.STOPPED.next(), LifecycleState.LifecyclePhase.DISPOSING);
    Assert.assertEquals(
        LifecycleState.LifecyclePhase.DISPOSING.next(), LifecycleState.LifecyclePhase.DISPOSED);
  }
}
