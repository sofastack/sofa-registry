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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.data.multi.cluster.loggers.Loggers;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class SlotFollowerDiffDigestRequestHandler extends BaseSlotDiffDigestRequestHandler {
  private static final Logger LOGGER = Loggers.SYNC_SRV_LOGGER;

  @Autowired private ThreadPoolExecutor slotSyncRequestProcessorExecutor;

  public SlotFollowerDiffDigestRequestHandler() {
    super(LOGGER);
  }

  @Override
  public Executor getExecutor() {
    return slotSyncRequestProcessorExecutor;
  }

  @Override
  protected boolean preCheck(DataSlotDiffDigestRequest request) {
    if (!slotManager.isLeader(dataServerConfig.getLocalDataCenter(), request.getSlotId())) {
      LOGGER.warn(
          "sync slot request from {}, not leader of {}",
          request.getLocalDataCenter(),
          request.getSlotId());
      return false;
    }
    return true;
  }

  @Override
  protected boolean postCheck(DataSlotDiffDigestRequest request) {
    return true;
  }
}
