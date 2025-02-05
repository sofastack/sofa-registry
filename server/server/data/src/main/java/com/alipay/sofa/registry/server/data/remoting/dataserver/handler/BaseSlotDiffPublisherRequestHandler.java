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
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : BaseSlotDiffPublisherRequestHandler.java, v 0.1 2022年05月16日 21:16 xiaojian.xj Exp $
 */
public abstract class BaseSlotDiffPublisherRequestHandler
    extends AbstractServerHandler<DataSlotDiffPublisherRequest> {

  private final Logger logger;

  @Resource private DatumStorageDelegate datumStorageDelegate;

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private SlotManager slotManager;

  public BaseSlotDiffPublisherRequestHandler(Logger logger) {
    this.logger = logger;
  }

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
      if (!slotManager.isLeader(dataServerConfig.getLocalDataCenter(), slotId)) {
        logger.warn(
            "sync slot request from {}, not leader of {}", request.getLocalDataCenter(), slotId);
        return new GenericResponse().fillFailed("not leader of " + slotId);
      }

      Map<String, Map<String, Publisher>> existingPublishers =
          datumStorageDelegate.getPublishers(
              dataServerConfig.getLocalDataCenter(), request.getSlotId());

      DataSlotDiffPublisherResult result =
          calcDiffResult(
              request.getLocalDataCenter(),
              slotId,
              request.getDatumSummaries(),
              existingPublishers,
              request.getAcceptorManager());
      result.setSlotTableEpoch(slotManager.getSlotTableEpoch());
      return new GenericResponse().fillSucceed(result);
    } catch (Throwable e) {
      String msg =
          StringFormatter.format(
              "DiffSyncPublisher request from {} error for slot {}",
              request.getLocalDataCenter(),
              request.getSlotId());
      logger.error(msg, e);
      return new GenericResponse().fillFailed(msg);
    }
  }

  private DataSlotDiffPublisherResult calcDiffResult(
      String requestDataCenter,
      int targetSlot,
      List<DatumSummary> datumSummaries,
      Map<String, Map<String, Publisher>> existingPublishers,
      SyncSlotAcceptorManager acceptorManager) {
    DataSlotDiffPublisherResult result =
        DataSlotDiffUtils.diffPublishersResult(
            datumSummaries,
            existingPublishers,
            dataServerConfig.getSlotSyncPublisherMaxNum(),
            acceptorManager);
    DataSlotDiffUtils.logDiffResult(requestDataCenter, result, targetSlot, logger);
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

  /**
   * Getter method for property <tt>datumStorageDelegate</tt>.
   *
   * @return property value of datumStorageDelegate
   */
  @VisibleForTesting
  public DatumStorageDelegate getDatumStorageDelegate() {
    return datumStorageDelegate;
  }

  /**
   * Setter method for property <tt>datumStorageDelegate</tt>.
   *
   * @param datumStorageDelegate value to be assigned to property datumStorageDelegate
   * @return BaseSlotDiffPublisherRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffPublisherRequestHandler setDatumStorageDelegate(
      DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
    return this;
  }

  /**
   * Getter method for property <tt>dataServerConfig</tt>.
   *
   * @return property value of dataServerConfig
   */
  @VisibleForTesting
  public DataServerConfig getDataServerConfig() {
    return dataServerConfig;
  }

  /**
   * Setter method for property <tt>dataServerConfig</tt>.
   *
   * @param dataServerConfig value to be assigned to property dataServerConfig
   * @return BaseSlotDiffPublisherRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffPublisherRequestHandler setDataServerConfig(
      DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
    return this;
  }

  /**
   * Getter method for property <tt>slotManager</tt>.
   *
   * @return property value of slotManager
   */
  @VisibleForTesting
  public SlotManager getSlotManager() {
    return slotManager;
  }

  /**
   * Setter method for property <tt>slotManager</tt>.
   *
   * @param slotManager value to be assigned to property slotManager
   * @return BaseSlotDiffPublisherRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffPublisherRequestHandler setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
    return this;
  }
}
