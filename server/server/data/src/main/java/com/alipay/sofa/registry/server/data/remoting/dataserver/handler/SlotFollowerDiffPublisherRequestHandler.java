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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class SlotFollowerDiffPublisherRequestHandler
    extends AbstractServerHandler<DataSlotDiffPublisherRequest> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SlotFollowerDiffPublisherRequestHandler.class);

  @Autowired private ThreadPoolExecutor slotSyncRequestProcessorExecutor;

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private DatumStorage localDatumStorage;

  @Autowired private SlotManager slotManager;

  @Override
  public void checkParam(DataSlotDiffPublisherRequest request) {
    ParaCheckUtil.checkNonNegative(request.getSlotId(), "request.slotId");
    ParaCheckUtil.checkNotNull(request.getDatumSummaries(), "request.datumSummaries");
  }

  @Override
  public Object doHandle(Channel channel, DataSlotDiffPublisherRequest request) {
    try {
      slotManager.triggerUpdateSlotTable(request.getSlotTableEpoch());
      final int slotId = request.getSlotId();
      if (!slotManager.isLeader(slotId)) {
        LOGGER.warn("not leader of {}", slotId);
        return new GenericResponse().fillFailed("not leader of " + slotId);
      }
      DataSlotDiffPublisherResult result =
          calcDiffResult(
              slotId,
              request.getDatumSummaries(),
              localDatumStorage.getPublishers(request.getSlotId()));
      result.setSlotTableEpoch(slotManager.getSlotTableEpoch());
      return new GenericResponse().fillSucceed(result);
    } catch (Throwable e) {
      String msg =
          StringFormatter.format(
              "DiffSyncPublisher request error for slot {}", request.getSlotId());
      LOGGER.error(msg, e);
      return new GenericResponse().fillFailed(msg);
    }
  }

  private DataSlotDiffPublisherResult calcDiffResult(
      int targetSlot,
      List<DatumSummary> datumSummaries,
      Map<String, Map<String, Publisher>> existingPublishers) {
    DataSlotDiffPublisherResult result =
        DataSlotDiffUtils.diffPublishersResult(
            datumSummaries, existingPublishers, dataServerConfig.getSlotSyncPublisherMaxNum());
    DataSlotDiffUtils.logDiffResult(result, targetSlot);
    return result;
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.DATA;
  }

  @Override
  public Class interest() {
    return DataSlotDiffPublisherRequest.class;
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  @Override
  public Executor getExecutor() {
    return slotSyncRequestProcessorExecutor;
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  @VisibleForTesting
  void setLocalDatumStorage(DatumStorage localDatumStorage) {
    this.localDatumStorage = localDatumStorage;
  }

  @VisibleForTesting
  SlotManager getSlotManager() {
    return slotManager;
  }

  @VisibleForTesting
  void setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
  }
}
