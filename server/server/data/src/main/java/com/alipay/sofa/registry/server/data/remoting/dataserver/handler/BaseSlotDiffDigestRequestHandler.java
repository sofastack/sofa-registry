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
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
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
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : BaseSlotDiffDigestRequestHandler.java, v 0.1 2022年05月14日 20:50 xiaojian.xj Exp $
 */
public abstract class BaseSlotDiffDigestRequestHandler
    extends AbstractServerHandler<DataSlotDiffDigestRequest> {

  private final Logger logger;

  @Resource private DatumStorageDelegate datumStorageDelegate;

  @Autowired protected SlotManager slotManager;

  @Autowired protected DataServerConfig dataServerConfig;

  public BaseSlotDiffDigestRequestHandler(Logger logger) {
    this.logger = logger;
  }

  protected abstract boolean preCheck(DataSlotDiffDigestRequest request);

  protected abstract boolean postCheck(DataSlotDiffDigestRequest request);

  @Override
  public Object doHandle(Channel channel, DataSlotDiffDigestRequest request) {
    try {
      final int slotId = request.getSlotId();
      if (!preCheck(request)) {
        return new GenericResponse().fillFailed("pre check fail of: " + slotId);
      }
      slotManager.triggerUpdateSlotTable(request.getSlotTableEpoch());

      // not use acceptorManager to filter in getPublishers() method,
      // because getPublishers() method only loop dataIndoId, will not loop publishers;
      Map<String, Map<String, Publisher>> existingPublishers =
          datumStorageDelegate.getPublishers(
              dataServerConfig.getLocalDataCenter(), request.getSlotId());

      // use acceptorManager in DataSlotDiffUtils.diffDigestResult,
      // as it will loop publishers once
      DataSlotDiffDigestResult result =
          calcDiffResult(
              request.getLocalDataCenter(),
              slotId,
              request.getDatumDigest(),
              existingPublishers,
              request.getAcceptorManager());
      result.setSlotTableEpoch(slotManager.getSlotTableEpoch());

      if (!postCheck(request)) {
        return new GenericResponse().fillFailed("post check fail of: " + slotId);
      }
      return new GenericResponse().fillSucceed(result);
    } catch (Throwable e) {
      String msg =
          StringFormatter.format(
              "DiffSyncDigest request from {} error for slot {},{}",
              request.getLocalDataCenter(),
              request.getLocalDataCenter(),
              request.getSlotId());
      logger.error(msg, e);
      return new GenericResponse().fillFailed(msg);
    }
  }

  private DataSlotDiffDigestResult calcDiffResult(
      String requestDataCenter,
      int targetSlot,
      Map<String, DatumDigest> targetDigestMap,
      Map<String, Map<String, Publisher>> existingPublishers,
      SyncSlotAcceptorManager acceptorManager) {
    DataSlotDiffDigestResult result =
        DataSlotDiffUtils.diffDigestResult(targetDigestMap, existingPublishers, acceptorManager);
    DataSlotDiffUtils.logDiffResult(requestDataCenter, result, targetSlot, logger);
    return result;
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.DATA;
  }

  @Override
  public Class interest() {
    return DataSlotDiffDigestRequest.class;
  }

  @Override
  public void checkParam(DataSlotDiffDigestRequest request) {
    ParaCheckUtil.checkNonNegative(request.getSlotId(), "request.slotId");
    ParaCheckUtil.checkNotNull(request.getDatumDigest(), "request.datumDigest");
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  /**
   * Setter method for property <tt>datumStorageDelegate</tt>.
   *
   * @param datumStorageDelegate value to be assigned to property datumStorageDelegate
   * @return BaseSlotDiffDigestRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffDigestRequestHandler setDatumStorageDelegate(
      DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
    return this;
  }

  /**
   * Setter method for property <tt>slotManager</tt>.
   *
   * @param slotManager value to be assigned to property slotManager
   * @return BaseSlotDiffDigestRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffDigestRequestHandler setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
    return this;
  }

  /**
   * Setter method for property <tt>dataServerConfig</tt>.
   *
   * @param dataServerConfig value to be assigned to property dataServerConfig
   * @return BaseSlotDiffDigestRequestHandler
   */
  @VisibleForTesting
  public BaseSlotDiffDigestRequestHandler setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
    return this;
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
   * Getter method for property <tt>slotManager</tt>.
   *
   * @return property value of slotManager
   */
  @VisibleForTesting
  public SlotManager getSlotManager() {
    return slotManager;
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
}
