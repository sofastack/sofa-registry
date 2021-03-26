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
package com.alipay.sofa.registry.server.data.remoting.metaserver.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: DataChangeRequestHandler.java, v 0.1 2017-12-12 15:09 shangyu.wh Exp $
 */
public class NotifyProvideDataChangeHandler extends AbstractClientHandler<ProvideDataChangeEvent> {

  @Autowired private MetaServerService metaServerService;

  @Autowired private ProvideDataProcessor provideDataProcessorManager;

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.META;
  }

  @Override
  public Object doHandle(Channel channel, ProvideDataChangeEvent request) {
    String dataInfoId = request.getDataInfoId();
    ProvideData provideData = metaServerService.fetchData(dataInfoId);
    provideDataProcessorManager.changeDataProcess(provideData);
    return null;
  }

  @Override
  public Class interest() {
    return ProvideDataChangeEvent.class;
  }

  @Override
  public CommonResponse buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }
}
