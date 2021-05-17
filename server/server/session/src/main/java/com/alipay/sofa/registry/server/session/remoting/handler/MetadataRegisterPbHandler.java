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

import com.alipay.sofa.registry.common.model.client.pb.MetaRegister;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.converter.pb.AppRevisionConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.RegisterResponseConvertor;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataRegisterPbHandler.java, v 0.1 2021年02月04日 17:13 xiaojian.xj Exp $
 */
public class MetadataRegisterPbHandler extends AbstractClientMetadataRequestHandler<MetaRegister> {
  @Override
  public Object doHandle(Channel channel, MetaRegister request) {
    RegisterResponse registerResponse = new RegisterResponse();
    AppRevision appRevision = AppRevisionConvertor.convert2Java(request);
    appRevisionHandlerStrategy.handleAppRevisionRegister(appRevision, registerResponse);
    return RegisterResponseConvertor.convert2Pb(registerResponse);
  }

  @Override
  public Class interest() {
    return MetaRegister.class;
  }

  @Override
  protected void logRequest(Channel channel, MetaRegister request) {
    if (exchangeLog.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder(256);
      sb.append("[").append(this.getClass().getSimpleName()).append("] ");
      sb.append("Remote:")
          .append(RemotingHelper.getChannelRemoteAddress(channel))
          .append(" Revision: ")
          .append(request.getRevision());
      exchangeLog.info(sb.toString());
    }
  }
}
