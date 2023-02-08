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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version $Id: ServiceAppMappingPbHandler.java, v 0.1 2021年02月04日 20:18 xiaojian.xj Exp $
 */
public class ServiceAppMappingPbHandler
    extends AbstractClientMetadataRequestHandler<ServiceAppMappingRequest> {

  @Override
  public void checkParam(ServiceAppMappingRequest request) {
    ParaCheckUtil.checkNotNull(request, "request");
    ParaCheckUtil.checkNotEmpty(request.getServiceIdsList(), "request.serviceIds");
  }

  @Override
  public Object doHandle(Channel channel, ServiceAppMappingRequest request) {
    List<String> services = request.getServiceIdsList();
    String remoteIp;
    if (channel != null
        && channel.getRemoteAddress() != null
        && channel.getRemoteAddress().getAddress() != null) {
      remoteIp = channel.getRemoteAddress().getAddress().getHostAddress();
    } else {
      remoteIp = StringUtils.EMPTY;
    }
    return appRevisionHandlerStrategy.queryApps(services, remoteIp);
  }

  @Override
  protected void logRequest(Channel channel, ServiceAppMappingRequest request) {
    if (exchangeLog.isInfoEnabled()) {
      String sb =
          "["
              + this.getClass().getSimpleName()
              + "] "
              + "Remote:"
              + RemotingHelper.getChannelRemoteAddress(channel)
              + " ServiceCount: "
              + request.getServiceIdsList().size();
      exchangeLog.info(sb);
    }
  }

  @Override
  public Class interest() {
    return ServiceAppMappingRequest.class;
  }
}
