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

import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest;
import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.List;

/**
 * @author xiaojian.xj
 * @version $Id: MetaRevisionHeartbeatPbHandler.java, v 0.1 2021年02月04日 22:49 xiaojian.xj Exp $
 */
public class MetaRevisionHeartbeatPbHandler
    extends AbstractClientMetadataRequestHandler<MetaHeartbeatRequest> {

  @Override
  public void checkParam(MetaHeartbeatRequest request) {
    ParaCheckUtil.checkNotNull(request, "request");
    ParaCheckUtil.checkNotEmpty(request.getRevisionsList(), "request.revisions");
  }

  @Override
  public Object doHandle(Channel channel, MetaHeartbeatRequest request) {
    List<String> revisions = request.getRevisionsList();
    MetaHeartbeatResponse response = appRevisionHandlerStrategy.heartbeat(revisions);
    return response;
  }

  @Override
  public Class interest() {
    return MetaHeartbeatRequest.class;
  }
}
