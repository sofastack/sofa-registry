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
package com.alipay.sofa.registry.server.data.multi.cluster.client.handler;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Set;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RemoteDataChangeNotifyHandler.java, v 0.1 2022年08月01日 15:51 xiaojian.xj Exp $
 */
public class RemoteDataChangeNotifyHandler extends AbstractClientHandler<DataChangeRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDataChangeNotifyHandler.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private MultiClusterSlotManager multiClusterSlotManager;

  @Autowired private MultiClusterExecutorManager multiClusterExecutorManager;

  /**
   * return processor request class name
   *
   * @return Class
   */
  @Override
  public Class interest() {
    return DataChangeRequest.class;
  }

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.DATA;
  }

  @Override
  public Executor getExecutor() {
    return multiClusterExecutorManager.getRemoteDataChangeRequestExecutor();
  }

  @Override
  public void checkParam(DataChangeRequest request) {
    ParaCheckUtil.checkNotBlank(request.getDataCenter(), "request.dataCenter");
    ParaCheckUtil.checkNotEmpty(request.getDataInfoIds(), "request.dataInfoIds");
  }

  /**
   * execute
   *
   * @param channel channel
   * @param request request
   * @return Object
   */
  @Override
  public Object doHandle(Channel channel, DataChangeRequest request) {

    if (dataServerConfig.isLocalDataCenter(request.getDataCenter())) {
      LOGGER.error(
          "[DataChangeRequest]local dataCenter data change should not notify localData, request:{}",
          request);
      return null;
    }
    Set<String> dataInfoIds = request.getDataInfoIds().keySet();
    LOGGER.info(
        "[DataChangeRequest]dataCenter:{} data change:{}",
        request.getDataCenter(),
        dataInfoIds.size());
    multiClusterSlotManager.dataChangeNotify(request.getDataCenter(), dataInfoIds);
    return null;
  }
}
