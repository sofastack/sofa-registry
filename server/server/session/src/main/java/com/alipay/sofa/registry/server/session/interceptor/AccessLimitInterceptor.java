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

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.exception.InterceptorExecutionException;
import com.alipay.sofa.registry.server.session.limit.AccessLimitService;
import org.springframework.beans.factory.annotation.Autowired;

/** Access limit interceptor impl. */
public class AccessLimitInterceptor implements Interceptor {

  @Autowired private AccessLimitService accessLimitService;

  @Override
  public boolean process(RegisterInvokeData registerInvokeData)
      throws InterceptorExecutionException {
    BaseInfo baseInfo = (BaseInfo) registerInvokeData.getStoreData();
    if (!accessLimitService.tryAcquire()) {
      throw new InterceptorExecutionException(
          "Register access limit for session server!dataInfoId="
              + baseInfo.getDataInfoId()
              + ",connectId="
              + baseInfo.getSourceAddress());
    }

    return true;
  }

  @Override
  public int order() {
    return 0;
  }
}
