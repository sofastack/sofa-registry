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
package com.alipay.sofa.registry.server.session.interceptor;

import com.alipay.sofa.registry.exception.InterceptorExecutionException;

/** Interceptor in the subscriber processing flow. */
public interface Interceptor {

  /**
   * Execute the target runnable impl.
   *
   * @param registerInvokeData data
   * @return false means that the current interceptor is not passed
   * @throws InterceptorExecutionException thrown when interceptor execute exception
   */
  boolean process(RegisterInvokeData registerInvokeData) throws InterceptorExecutionException;

  /**
   * Returns the execution order factor of the interceptor, and the interceptor with a smaller value
   * is executed first.
   *
   * @return the execution order factor of the interceptor
   */
  default int order() {
    return 0;
  }

  /**
   * Interceptor name.
   *
   * @return interceptor name
   */
  default String getName() {
    return getClass().getSimpleName();
  }
}
