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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Ordered interceptor manager. */
public class OrderedInterceptorManager implements InterceptorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderedInterceptorManager.class);

  private final List<Interceptor> interceptors;

  public OrderedInterceptorManager() {
    // 拦截器是系统初始化阶段生成的，这里就不用考虑并发的问题了
    this.interceptors = new ArrayList<>();
  }

  @Override
  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
    interceptors.sort(Comparator.comparingInt(Interceptor::order));
  }

  @Override
  public void executeInterceptors(RegisterInvokeData registerInvokeData)
      throws InterceptorExecutionException {
    if (interceptors.size() == 0) {
      return;
    }

    for (Interceptor interceptor : interceptors) {
      try {
        boolean result = interceptor.process(registerInvokeData);
        if (!result) {
          LOGGER.warn("interceptor({}) return false, skip all subsequent interceptors");
          break;
        }
      } catch (InterceptorExecutionException e) {
        LOGGER.error(
            "interceptor({}) process data(dataId={}) exception",
            interceptor.getName(),
            registerInvokeData.getStoreData().getId(),
            e);
        throw e;
      } catch (Throwable cause) {
        LOGGER.error(
            "interceptor({}) process data(dataId={}) encountered an unexpected exception",
            interceptor.getName(),
            registerInvokeData.getStoreData().getId(),
            cause);
        throw new InterceptorExecutionException(
            "interceptor("
                + interceptor.getName()
                + ") process data(dataId="
                + registerInvokeData.getStoreData().getId()
                + ") encountered an unexpected exception",
            cause);
      }
    }
  }
}
