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

/** Manage all interceptor. */
public interface InterceptorManager {

  /**
   * Add a new interceptor according to the execution priority of the interceptor.
   *
   * @param interceptor target interceptor
   */
  void addInterceptor(Interceptor interceptor);

  /**
   * Execute all interceptors in order.
   *
   * @param registerInvokeData data
   * @return true if all interceptors executed successful
   * @throws InterceptorExecutionException throw when any interceptor encounters an exception, and
   *     stop executing subsequent interceptors
   */
  boolean executeInterceptors(RegisterInvokeData registerInvokeData)
      throws InterceptorExecutionException;
}
