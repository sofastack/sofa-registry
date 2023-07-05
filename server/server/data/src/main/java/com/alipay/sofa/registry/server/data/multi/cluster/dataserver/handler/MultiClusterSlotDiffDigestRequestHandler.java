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
package com.alipay.sofa.registry.server.data.multi.cluster.dataserver.handler;

import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.loggers.Loggers;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.BaseSlotDiffDigestRequestHandler;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotDiffDigestRequestHandler.java, v 0.1 2022年05月16日 20:57 xiaojian.xj Exp
 *     $
 */
public class MultiClusterSlotDiffDigestRequestHandler extends BaseSlotDiffDigestRequestHandler {

  private static final Logger LOGGER = Loggers.MULTI_CLUSTER_SRV_LOGGER;

  @Autowired private MultiClusterExecutorManager multiClusterExecutorManager;

  public MultiClusterSlotDiffDigestRequestHandler() {
    super(LOGGER);
  }

  /**
   * specify executor for processor handler
   *
   * @return Executor
   */
  @Override
  public Executor getExecutor() {
    return multiClusterExecutorManager.getRemoteSlotSyncProcessorExecutor();
  }

  @Override
  protected boolean preCheck(DataSlotDiffDigestRequest request) {
    return doCheck(request);
  }

  private boolean doCheck(DataSlotDiffDigestRequest request) {
    if (!slotManager.isLeader(dataServerConfig.getLocalDataCenter(), request.getSlotId())) {
      LOGGER.warn(
          "sync slot request from {}, not leader of {}",
          request.getLocalDataCenter(),
          request.getSlotId());
      return false;
    }

    SlotAccess slotAccess =
        slotManager.checkSlotAccess(
            dataServerConfig.getLocalDataCenter(),
            request.getSlotId(),
            request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());

    if (!slotAccess.isAccept()) {
      LOGGER.warn(
          "sync slot request from {}, slotId={}, slotAccess={}, request={}",
          request.getLocalDataCenter(),
          request.getSlotId(),
          slotAccess,
          request);
    }

    return slotAccess.isAccept();
  }

  @Override
  protected boolean postCheck(DataSlotDiffDigestRequest request) {
    return doCheck(request);
  }
}
