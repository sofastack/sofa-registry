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

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CLIENT_OFF;

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.InterceptorExecutionException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.session.loggers.Loggers;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/** Client off interceptor impl. */
public class ClientOffInterceptor implements Interceptor {

  private static final Logger LOGGER = Loggers.CLIENT_OFF_LOG;

  private static final long REGISTER_PUSH_EMPTY_VERSION = 1;

  @Autowired private FirePushService firePushService;

  @Resource private FetchClientOffAddressService fetchClientOffAddressService;

  @Autowired private SessionRegistry sessionRegistry;

  @Override
  public boolean process(RegisterInvokeData registerInvokeData)
      throws InterceptorExecutionException {
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();
    URL url = storeData.getSourceAddress();
    ClientManagerAddress.AddressVersion address =
        fetchClientOffAddressService.getAddress(url.getIpAddress());
    if (address != null) {
      markChannel(registerInvokeData.getChannel());
      LOGGER.info(
          "dataInfoId:{} ,url:{} match clientOff ips.",
          storeData.getDataInfoId(),
          url.getIpAddress());
      if (StoreData.DataType.PUBLISHER == storeData.getDataType()) {
        // match client off pub, do unpub to data, make sure the publisher remove
        try {
          sessionRegistry.unRegister(storeData);
        } catch (Throwable e) {
          LOGGER.error(
              "failed to unRegister publisher {}, source={}", storeData.getDataInfoId(), url, e);
        }
        return false;
      }

      if (StoreData.DataType.SUBSCRIBER == storeData.getDataType()) {
        // in some case, need to push empty to new subscriber, and stop sub
        // else, filter not stop sub
        if (sessionRegistry.isPushEmpty((Subscriber) storeData) && address.isSub()) {
          firePushService.fireOnPushEmpty(
              (Subscriber) storeData,
              sessionRegistry.getDataCenterWhenPushEmpty(),
              REGISTER_PUSH_EMPTY_VERSION);
          LOGGER.info(
              "[clientOffSub],{},{}",
              storeData.getDataInfoId(),
              RemotingHelper.getAddressString(storeData.getSourceAddress()));
        }
      }
    }
    return true;
  }

  private void markChannel(Channel channel) {
    if (!(channel instanceof BoltChannel)) {
      return;
    }
    BoltChannel boltChannel = (BoltChannel) channel;
    Object value = boltChannel.getConnAttribute(CLIENT_OFF);
    if (!Boolean.TRUE.equals(value)) {
      boltChannel.setConnAttribute(CLIENT_OFF, Boolean.TRUE);
    }
  }

  @Override
  public int order() {
    return 300;
  }
}
