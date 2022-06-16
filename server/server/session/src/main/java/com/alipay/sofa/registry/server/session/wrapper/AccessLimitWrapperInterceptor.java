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
package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.session.limit.AccessLimitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version 1.0: AccessLimitWrapperInterceptor.java, v 0.1 2019-08-26 20:29 shangyu.wh Exp $
 */
public class AccessLimitWrapperInterceptor
    implements WrapperInterceptor<RegisterInvokeData, Boolean> {

  @Autowired private AccessLimitService accessLimitService;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation)
      throws Exception {

    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo baseInfo = (BaseInfo) registerInvokeData.getStoreData();

    if (!accessLimitService.tryAcquire()) {
      throw new RuntimeException(
          String.format(
              "Register access limit for session server!dataInfoId=%s,connectId=%s",
              baseInfo.getDataInfoId(), baseInfo.getSourceAddress()));
    }

    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
