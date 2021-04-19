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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class DataSlotDiffDigestRequestHandler
    extends AbstractServerHandler<DataSlotDiffDigestRequest> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DataSlotDiffDigestRequestHandler.class);

  @Autowired ExecutorManager executorManager;

  @Autowired DataStore sessionDataStore;

  @Autowired SlotTableCache slotTableCache;

  @Override
  public void checkParam(DataSlotDiffDigestRequest request) {
    ParaCheckUtil.checkNonNegative(request.getSlotId(), "request.slotId");
    ParaCheckUtil.checkNotNull(request.getDatumDigest(), "request.datumDigest");
  }

  @Override
  public Object doHandle(Channel channel, DataSlotDiffDigestRequest request) {
    try {
      DataSlotDiffDigestResult result =
          calcDiffResult(
              request.getSlotId(),
              request.getDatumDigest(),
              sessionDataStore.getDataInfoIdPublishers(request.getSlotId()));
      result.setSlotTableEpoch(slotTableCache.getEpoch());
      result.setSessionProcessId(ServerEnv.PROCESS_ID);
      return new GenericResponse().fillSucceed(result);
    } catch (Throwable e) {
      String msg =
          StringFormatter.format("DiffSyncDigest request error for slot {}", request.getSlotId());
      LOGGER.error(msg, e);
      return new GenericResponse().fillFailed(msg);
    }
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  private DataSlotDiffDigestResult calcDiffResult(
      int targetSlot,
      Map<String, DatumDigest> digestMap,
      Map<String, Map<String, Publisher>> existingPublishers) {

    DataSlotDiffDigestResult result =
        DataSlotDiffUtils.diffDigestResult(digestMap, existingPublishers);
    DataSlotDiffUtils.logDiffResult(result, targetSlot);
    return result;
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.DATA;
  }

  @Override
  public Executor getExecutor() {
    return executorManager.getDataSlotSyncRequestExecutor();
  }

  @Override
  public Class interest() {
    return DataSlotDiffDigestRequest.class;
  }
}
