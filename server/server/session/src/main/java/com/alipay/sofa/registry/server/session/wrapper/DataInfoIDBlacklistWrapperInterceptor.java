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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData.DataType;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.session.providedata.FetchDataInfoIDBlackListService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/16
 */
public class DataInfoIDBlacklistWrapperInterceptor
    implements WrapperInterceptor<RegisterInvokeData, Boolean> {

  @Autowired private FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation)
      throws Exception {
    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();

    // 这里我们只拦截 Publisher
    if (DataType.PUBLISHER == storeData.getDataType()) {
      Publisher publisher = (Publisher) storeData;
      String dataInfoId = publisher.getDataInfoId();
      if (this.fetchDataInfoIDBlackListService.isInBlackList(dataInfoId)) {
        // 命中规则，跳过 Pub
        return true;
      } else {
        // 没命中则继续处理
        return invocation.proceed();
      }
    }

    // 非 Publisher 也继续处理
    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return 400;
  }

  @VisibleForTesting
  public DataInfoIDBlacklistWrapperInterceptor setFetchDataInfoIDBlackListService(
      FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService) {
    this.fetchDataInfoIDBlackListService = fetchDataInfoIDBlackListService;
    return this;
  }
}
