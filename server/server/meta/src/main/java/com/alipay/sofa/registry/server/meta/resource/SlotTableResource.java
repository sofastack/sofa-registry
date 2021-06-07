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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.balance.BalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.balance.NaiveBalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.tasks.BalanceTask;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.util.MathUtils;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Jan 08, 2021
 */
@Path("openapi/v1/slot/table")
public class SlotTableResource {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private SlotManager slotManager;

  @Autowired private SlotTableMonitor slotTableMonitor;

  @Autowired private DataServerManager dataServerManager;

  @Autowired private ScheduledSlotArranger slotArranger;

  @Autowired private MetaLeaderService metaLeaderService;

  @PUT
  @Path("force/refresh")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public GenericResponse<SlotTable> forceRefreshSlotTable() {
    logger.info("[forceRefreshSlotTable] begin");
    if (metaLeaderService.amILeader()) {
      if (slotArranger.tryLock()) {
        try {
          BalanceTask task =
              new BalanceTask(
                  slotManager, dataServerManager.getDataServerMetaInfo().getClusterMembers());
          task.run();
          logger.info("[forceRefreshSlotTable] end with succeed");
          return new GenericResponse<SlotTable>().fillSucceed(slotManager.getSlotTable());
        } finally {
          slotArranger.unlock();
        }
      } else {
        logger.info("[forceRefreshSlotTable] end, fail to get the lock");
        return new GenericResponse<SlotTable>().fillFailed("scheduled slot arrangement is running");
      }
    } else {
      logger.info("[forceRefreshSlotTable] end, not meta-server leader");
      return new GenericResponse<SlotTable>().fillFailed("not the meta-server leader");
    }
  }

  @PUT
  @Path("/reconcile/stop")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse stopSlotTableReconcile() {
    logger.info("[stopSlotTableReconcile] begin");
    try {
      slotArranger.suspend();
      logger.info("[stopSlotTableReconcile] end with succeed");
      return GenericResponse.buildSuccessResponse("succeed");
    } catch (Throwable throwable) {
      logger.error("[stopSlotTableReconcile] end", throwable);
      return GenericResponse.buildFailedResponse(throwable.getMessage());
    }
  }

  @PUT
  @Path("/reconcile/start")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse startSlotTableReconcile() {
    logger.info("[startSlotTableReconcile] begin");
    try {
      slotArranger.resume();
      logger.info("[startSlotTableReconcile] end with succeed");
      return GenericResponse.buildSuccessResponse("succeed");
    } catch (Throwable throwable) {
      logger.error("[startSlotTableReconcile] end", throwable);
      return GenericResponse.buildFailedResponse(throwable.getMessage());
    }
  }

  @GET
  @Path("/reconcile/status")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public CommonResponse getReconcileStatus() {
    logger.info("[getReconcileStatus] begin");
    try {
      boolean result = slotArranger.getLifecycleState().isStarted() && !slotArranger.isSuspended();
      logger.info("[getReconcileStatus] end with succeed");
      if (result) {
        return GenericResponse.buildSuccessResponse("running");
      } else {
        return GenericResponse.buildSuccessResponse("stopped");
      }
    } catch (Throwable throwable) {
      logger.error("[getReconcileStatus] end", throwable);
      return GenericResponse.buildFailedResponse(throwable.getMessage());
    }
  }

  @GET
  @Path("/data/slot/status")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public GenericResponse<Object> getDataSlotStatuses() {
    logger.info("[getDataSlotStatuses] begin");
    try {
      return new GenericResponse<>().fillSucceed(dataServerManager.getDataServersStats());
    } catch (Throwable throwable) {
      return new GenericResponse<>().fillFailed(throwable.getMessage());
    } finally {
      logger.info("[getDataSlotStatuses] end");
    }
  }

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public GenericResponse<Object> getSlotTableStatus() {
    logger.info("[getSlotTableStatus] begin");
    try {
      boolean isSlotStable = slotTableMonitor.isStableTableStable();
      SlotTable slotTable = slotManager.getSlotTable();
      Map<String, Integer> leaderCounter = SlotTableUtils.getSlotTableLeaderCount(slotTable);
      Map<String, Integer> followerCounter = SlotTableUtils.getSlotTableSlotCount(slotTable);
      boolean isLeaderSlotBalanced =
          isSlotTableLeaderBalanced(
              leaderCounter, dataServerManager.getDataServerMetaInfo().getClusterMembers());
      boolean isFollowerSlotBalanced =
          isSlotTableFollowerBalanced(
              followerCounter, dataServerManager.getDataServerMetaInfo().getClusterMembers());
      return new GenericResponse<>()
          .fillSucceed(
              new SlotTableStatusResponse(
                  slotTable.getEpoch(),
                  isLeaderSlotBalanced,
                  isFollowerSlotBalanced,
                  isSlotStable,
                  slotArranger.isSlotTableProtectionMode(),
                  leaderCounter,
                  followerCounter));
    } catch (Throwable th) {
      logger.error("[getSlotTableStatus]", th);
      return new GenericResponse<>().fillFailed(th.getMessage());
    } finally {
      logger.info("[getSlotTableStatus] end");
    }
  }

  public boolean isSlotTableLeaderBalanced(
      Map<String, Integer> leaderCounter, List<DataNode> dataNodes) {
    BalancePolicy balancePolicy = new NaiveBalancePolicy();
    int expectedLeaderTotal = slotManager.getSlotNums();
    if (leaderCounter.values().stream().mapToInt(Integer::intValue).sum() < expectedLeaderTotal) {
      return false;
    }
    int leaderHighAverage = MathUtils.divideCeil(expectedLeaderTotal, dataNodes.size());
    int leaderLowAverage = Math.floorDiv(expectedLeaderTotal, dataNodes.size());
    int leaderHighWaterMark = balancePolicy.getHighWaterMarkSlotLeaderNums(leaderHighAverage);
    int leaderLowWaterMark = balancePolicy.getLowWaterMarkSlotLeaderNums(leaderLowAverage);

    for (DataNode dataNode : dataNodes) {
      String dataIp = dataNode.getIp();
      if (leaderCounter.get(dataIp) == null) {
        return false;
      }
      int leaderCount = leaderCounter.getOrDefault(dataIp, 0);
      if (leaderCount > leaderHighWaterMark || leaderCount < leaderLowWaterMark) {
        return false;
      }
    }
    return true;
  }

  public boolean isSlotTableFollowerBalanced(
      Map<String, Integer> followerCounter, List<DataNode> dataNodes) {
    BalancePolicy balancePolicy = new NaiveBalancePolicy();
    int expectedFollowerTotal = slotManager.getSlotNums() * (slotManager.getSlotReplicaNums() - 1);
    if (slotManager.getSlotReplicaNums() < dataNodes.size()) {
      if (followerCounter.values().stream().mapToInt(Integer::intValue).sum()
          < expectedFollowerTotal) {
        return false;
      }
    }
    int followerHighAverage = MathUtils.divideCeil(expectedFollowerTotal, dataNodes.size());
    int followerLowAverage = Math.floorDiv(expectedFollowerTotal, dataNodes.size());
    int followerHighWaterMark = balancePolicy.getHighWaterMarkSlotFollowerNums(followerHighAverage);
    int followerLowWaterMark = balancePolicy.getLowWaterMarkSlotFollowerNums(followerLowAverage);

    for (DataNode dataNode : dataNodes) {
      String dataIp = dataNode.getIp();
      int followerCount = followerCounter.getOrDefault(dataIp, 0);
      if (followerCount > followerHighWaterMark || followerCount < followerLowWaterMark) {
        return false;
      }
    }
    return true;
  }

  public static class SlotTableStatusResponse {

    private final long slotTableEpoch;

    private final boolean isSlotTableLeaderBalanced;
    private final boolean isSlotTableFollowerBalanced;

    private final boolean isSlotTableStable;
    private final boolean isProtectionMode;

    private final Map<String, Integer> leaderCount;

    private final Map<String, Integer> followerCount;

    public SlotTableStatusResponse(
        long slotTableEpoch,
        boolean slotTableLeaderBalanced,
        boolean slotTableFollowerBalanced,
        boolean slotTableStable,
        boolean protectMode,
        Map<String, Integer> leaderCount,
        Map<String, Integer> followerCount) {
      this.slotTableEpoch = slotTableEpoch;
      this.isSlotTableLeaderBalanced = slotTableLeaderBalanced;
      this.isSlotTableFollowerBalanced = slotTableFollowerBalanced;
      this.isProtectionMode = protectMode;
      this.isSlotTableStable = slotTableStable;
      this.leaderCount = leaderCount;
      this.followerCount = followerCount;
    }

    public boolean isSlotTableStable() {
      return isSlotTableStable;
    }

    public Map<String, Integer> getLeaderCount() {
      return leaderCount;
    }

    public Map<String, Integer> getFollowerCount() {
      return followerCount;
    }

    public boolean isSlotTableLeaderBalanced() {
      return isSlotTableLeaderBalanced;
    }

    public boolean isSlotTableFollowerBalanced() {
      return isSlotTableFollowerBalanced;
    }
  }

  public SlotTableResource() {}

  public SlotTableResource(
      SlotManager slotManager,
      SlotTableMonitor slotTableMonitor,
      DataServerManager dataServerManager,
      ScheduledSlotArranger slotArranger,
      MetaLeaderService metaLeaderService) {
    this.slotManager = slotManager;
    this.slotTableMonitor = slotTableMonitor;
    this.dataServerManager = dataServerManager;
    this.slotArranger = slotArranger;
    this.metaLeaderService = metaLeaderService;
  }
}
