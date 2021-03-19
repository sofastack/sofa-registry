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
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.StoreData.DataType;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * blacklist filter
 *
 * @author shangyu.wh
 * @version 1.0: BlacklistWrapperInterceptor.java, v 0.1 2019-06-18 22:26 shangyu.wh Exp $
 */
public class BlacklistWrapperInterceptor implements WrapperInterceptor<StoreData, Boolean> {

  @Autowired private FirePushService firePushService;
  /** blacklist filter */
  @Autowired private ProcessFilter<BaseInfo> processFilter;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<StoreData, Boolean> invocation)
      throws Exception {

    BaseInfo storeData = (BaseInfo) invocation.getParameterSupplier().get();

    if (processFilter.match(storeData)) {
      if (DataType.PUBLISHER == storeData.getDataType()) {
        // match blacklist stop pub.
        return true;
      }

      if (DataType.SUBSCRIBER == storeData.getDataType()) {
        fireSubscriberPushEmptyTask((Subscriber) storeData);
        return true;
      }
    }
    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return 200;
  }

  private void fireSubscriberPushEmptyTask(Subscriber subscriber) {
    // trigger empty data push
    firePushService.fireOnPushEmpty(subscriber);
  }
}
