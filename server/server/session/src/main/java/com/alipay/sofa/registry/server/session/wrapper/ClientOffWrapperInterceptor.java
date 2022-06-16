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

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CLIENT_OFF;

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.common.model.store.StoreData.DataType;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
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

/**
 * @author xiaojian.xj
 * @version $Id: ClientOffWrapperInterceptor.java, v 0.1 2021年05月28日 21:18 xiaojian.xj Exp $
 */
public class ClientOffWrapperInterceptor
    implements WrapperInterceptor<RegisterInvokeData, Boolean> {

  private static final Logger LOGGER = Loggers.CLIENT_OFF_LOG;

  private static final long REGISTER_PUSH_EMPTY_VERSION = 1;

  @Autowired private FirePushService firePushService;

  @Resource private FetchClientOffAddressService fetchClientOffAddressService;

  @Autowired protected SessionRegistry sessionRegistry;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation)
      throws Exception {
    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();

    URL url = storeData.getSourceAddress();

    AddressVersion address = fetchClientOffAddressService.getAddress(url.getIpAddress());
    if (address != null) {
      markChannel(registerInvokeData.getChannel());
      LOGGER.info(
          "dataInfoId:{} ,url:{} match clientOff ips.",
          storeData.getDataInfoId(),
          url.getIpAddress());
      if (DataType.PUBLISHER == storeData.getDataType()) {
        // match client off pub, do unpub to data, make sure the publisher remove
        try {
          sessionRegistry.unRegister(storeData);
        } catch (Throwable e) {
          LOGGER.error(
              "failed to unRegister publisher {}, source={}", storeData.getDataInfoId(), url, e);
        }
        return true;
      }

      if (DataType.SUBSCRIBER == storeData.getDataType()) {
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
    return invocation.proceed();
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
  public int getOrder() {
    return 300;
  }
}
