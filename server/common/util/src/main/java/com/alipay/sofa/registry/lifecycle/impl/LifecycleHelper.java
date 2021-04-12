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
import com.alipay.sofa.registry.lifecycle.Lifecycle;

/**
 * @author chen.zhu
 *     <p>Nov 20, 2020
 */
public final class LifecycleHelper {
  private LifecycleHelper() {}

  public static void initializeIfPossible(Object obj) throws InitializeException {

    if (obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canInitialize()) {
      ((Lifecycle) obj).initialize();
    }
  }

  public static void startIfPossible(Object obj) throws StartException {

    if (obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canStart()) {
      ((Lifecycle) obj).start();
    }
  }

  public static void stopIfPossible(Object obj) throws StopException {

    if (obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canStop()) {
      ((Lifecycle) obj).stop();
    }
  }

  public static void disposeIfPossible(Object obj) throws DisposeException {
    if (obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canDispose()) {
      ((Lifecycle) obj).dispose();
    }
  }
}
