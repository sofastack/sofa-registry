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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import org.apache.commons.lang.StringUtils;

/** @Author dzdx @Date 2022/2/28 4:02 下午 @Version 1.0 */
public class ClientIpWrapperInterceptor implements WrapperInterceptor<RegisterInvokeData, Boolean> {
  private static final Logger LOGGER = LoggerFactory.getLogger("SRV-CONNECT");

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation)
      throws Exception {
    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();
    String clientIp = storeData.getIp();
    Channel channel = registerInvokeData.getChannel();
    if (StringUtils.isNotBlank(clientIp) && !StringUtils.equals(channel.getClientIP(), clientIp)) {
      LOGGER.info("[ClientIP] add mapping {}->{}", channel.getRemoteAddress(), clientIp);
      channel.setClientIP(clientIp);
    }
    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
