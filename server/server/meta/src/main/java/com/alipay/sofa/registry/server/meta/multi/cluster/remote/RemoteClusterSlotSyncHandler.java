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
package com.alipay.sofa.registry.server.meta.multi.cluster.remote;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RemoteClusterSlotSyncHandler.java, v 0.1 2022年04月21日 21:54 xiaojian.xj Exp $
 */
public class RemoteClusterSlotSyncHandler
    extends AbstractServerHandler<RemoteClusterSlotSyncRequest> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-SRV", "[SlotTableHandler]");
  @Autowired private ExecutorManager executorManager;

  @Autowired private CurrentDcMetaServer currentDcMetaServer;

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Override
  public Class interest() {
    return RemoteClusterSlotSyncRequest.class;
  }

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.META;
  }

  /**
   * @param channel channel
   * @param request request
   * @return GenericResponse
   */
  @Override
  public GenericResponse<RemoteClusterSlotSyncResponse> doHandle(
      Channel channel, RemoteClusterSlotSyncRequest request) {

    String clusterId = defaultCommonConfig.getDefaultClusterId();
    LeaderInfo leaderInfo = metaLeaderService.getLeaderInfo();
    // wrong leader
    if (!metaLeaderService.amILeader()) {
      LOGGER.error("request: {} sync on follower, leader is:{}", request, leaderInfo.getLeader());
      return new GenericResponse<RemoteClusterSlotSyncResponse>()
          .fillFailData(
              RemoteClusterSlotSyncResponse.wrongLeader(
                  leaderInfo.getLeader(), leaderInfo.getEpoch()));
    }

    // leader not warmuped
    if (!metaLeaderService.amIStableAsLeader()) {
      LOGGER.error("request: {} sync on leader not warmuped.", request);
      return new GenericResponse<RemoteClusterSlotSyncResponse>()
          .fillFailData(
              RemoteClusterSlotSyncResponse.leaderNotWarmuped(
                  leaderInfo.getLeader(), leaderInfo.getEpoch()));
    }

    SlotTable slotTable = currentDcMetaServer.getSlotTable();
    if (!SlotTableUtils.isValidSlotTable(slotTable)) {
      return new GenericResponse<RemoteClusterSlotSyncResponse>()
          .fillFailed("slot-table not valid, check meta-server log for detail");
    }

    // check slot table epoch
    if (request.getSlotTableEpoch() > slotTable.getEpoch()) {
      // it should not happen, print error log and return false, restart meta leader;
      LOGGER.error(
          "[conflict]request: {} slotEpoch > local.slotEpoch: {}", request, slotTable.getEpoch());
      return new GenericResponse<RemoteClusterSlotSyncResponse>().fillFailed("slotEpoch conflict");
    } else if (request.getSlotTableEpoch() == slotTable.getEpoch()) {
      return new GenericResponse<RemoteClusterSlotSyncResponse>()
          .fillSucceed(
              RemoteClusterSlotSyncResponse.notUpgrade(
                  leaderInfo.getLeader(),
                  leaderInfo.getEpoch(),
                  new DataCenterMetadata(clusterId, metaServerConfig.getLocalDataCenterZones())));
    } else {
      SlotTable resp = slotTable.filterLeaderInfo();
      LOGGER.info(
          "sync request epoch:{} < newSlotTableEpoch:{}, leader:{}, leaderEpoch:{}, slotTable upgrade: {}",
          request,
          slotTable.getEpoch(),
          leaderInfo.getLeader(),
          leaderInfo.getEpoch(),
          resp);
      return new GenericResponse<RemoteClusterSlotSyncResponse>()
          .fillSucceed(
              RemoteClusterSlotSyncResponse.upgrade(
                  leaderInfo.getLeader(),
                  leaderInfo.getEpoch(),
                  resp,
                  new DataCenterMetadata(clusterId, metaServerConfig.getLocalDataCenterZones())));
    }
  }

  @Override
  public Executor getExecutor() {
    return executorManager.getRemoteClusterHandlerExecutor();
  }
}
