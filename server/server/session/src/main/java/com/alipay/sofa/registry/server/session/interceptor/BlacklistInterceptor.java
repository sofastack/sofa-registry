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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.exception.InterceptorExecutionException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import com.alipay.sofa.registry.server.session.loggers.Loggers;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

/** Blacklist interceptor impl. */
public class BlacklistInterceptor implements Interceptor {

  private static final Logger LOGGER = Loggers.BLACK_LIST_LOG;

  @Autowired protected SessionRegistry sessionRegistry;

  @Autowired protected FirePushService firePushService;
  /** blacklist filter */
  @Autowired protected ProcessFilter<BaseInfo> processFilter;

  @Override
  public boolean process(RegisterInvokeData registerInvokeData)
      throws InterceptorExecutionException {
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();
    if (Strings.isNotBlank(storeData.attributeOf(ValueConstants.BLOCKED_REQUEST_KEY))
        || processFilter.match(storeData)) {
      if (StoreData.DataType.PUBLISHER == storeData.getDataType()) {
        // match blacklist stop pub.
        LOGGER.info(
            "[pub],{},{}",
            storeData.getDataInfoId(),
            RemotingHelper.getAddressString(storeData.getSourceAddress()));
        return false;
      }

      if (StoreData.DataType.SUBSCRIBER == storeData.getDataType()) {
        // in some case, need to push empty to new subscriber, and stop sub
        // else, filter not stop sub
        if (sessionRegistry.isPushEmpty((Subscriber) storeData)) {
          firePushService.fireOnPushEmpty(
              (Subscriber) storeData, sessionRegistry.getDataCenterWhenPushEmpty());
          LOGGER.info(
              "[sub],{},{}",
              storeData.getDataInfoId(),
              RemotingHelper.getAddressString(storeData.getSourceAddress()));
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int order() {
    return 200;
  }
}
