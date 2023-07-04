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
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent.DatumType;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.multi.cluster.storage.MultiClusterDatumService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RemoteDatumClearEventHandler.java, v 0.1 2022年09月08日 17:23 xiaojian.xj Exp $
 */
public class RemoteDatumClearEventHandler extends AbstractClientHandler<RemoteDatumClearEvent> {

  @Autowired private ThreadPoolExecutor metaNodeExecutor;

  @Autowired private MultiClusterDatumService multiClusterDatumService;

  /**
   * return processor request class name
   *
   * @return Class
   */
  @Override
  public Class interest() {
    return RemoteDatumClearEvent.class;
  }

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.META;
  }

  @Override
  public void checkParam(RemoteDatumClearEvent request) {
    ParaCheckUtil.checkNotNull(request, "RemoteDatumClearEvent");
    ParaCheckUtil.checkNotBlank(request.getRemoteDataCenter(), "RemoteDataCenter");
    ParaCheckUtil.checkNotNull(request.getDatumType(), "DatumType");
    if (request.getDatumType() == DatumType.GROUP) {
      ParaCheckUtil.checkNotBlank(request.getGroup(), "Group");
    } else {
      ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "DataInfoId");
    }
  }

  /**
   * execute
   *
   * @param channel channel
   * @param request request
   * @return Object
   */
  @Override
  public Object doHandle(Channel channel, RemoteDatumClearEvent request) {
    multiClusterDatumService.clear(request);
    return CommonResponse.buildSuccessResponse();
  }

  @Override
  public CommonResponse buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }

  @Override
  public Executor getExecutor() {
    return metaNodeExecutor;
  }
}
