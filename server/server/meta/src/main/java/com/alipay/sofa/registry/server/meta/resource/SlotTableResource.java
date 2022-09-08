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
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.multi.cluster.DefaultMultiClusterSlotTableSyncer.RemoteClusterSlotState;
import com.alipay.sofa.registry.server.meta.multi.cluster.MultiClusterSlotTableSyncer;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.status.SlotTableStatusService;
import com.alipay.sofa.registry.server.meta.slot.tasks.BalanceTask;
import java.util.Map;
import javax.annotation.Resource;
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

  @Autowired private DataServerManager dataServerManager;

  @Autowired private ScheduledSlotArranger slotArranger;

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private SlotTableStatusService slotTableStatusService;

  @Resource private MultiClusterSlotTableSyncer multiClusterSlotTableSyncer;

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
      SlotTableStatusResponse slotTableStatus = slotTableStatusService.getSlotTableStatus();
      return new GenericResponse<>().fillSucceed(slotTableStatus);
    } catch (Throwable th) {
      logger.error("[getSlotTableStatus]", th);
      return new GenericResponse<>().fillFailed(th.getMessage());
    } finally {
      logger.info("[getSlotTableStatus] end");
    }
  }

  @GET
  @Path("/multi/slottable")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public GenericResponse<Object> getMultiSlottable() {
    logger.info("[getSyncSlottableStatus] begin");
    try {
      Map<String, RemoteClusterSlotState> multiClusterSlotTable =
          multiClusterSlotTableSyncer.getMultiClusterSlotTable();
      return new GenericResponse<>().fillSucceed(multiClusterSlotTable);
    } catch (Throwable th) {
      logger.error("[getSyncSlottableStatus]", th);
      return new GenericResponse<>().fillFailed(th.getMessage());
    } finally {
      logger.info("[getSyncSlottableStatus] end");
    }
  }

  public SlotTableResource() {}

  public SlotTableResource(
      SlotManager slotManager,
      DataServerManager dataServerManager,
      ScheduledSlotArranger slotArranger,
      MetaLeaderService metaLeaderService,
      SlotTableStatusService slotTableStatusService) {
    this.slotManager = slotManager;
    this.dataServerManager = dataServerManager;
    this.slotArranger = slotArranger;
    this.metaLeaderService = metaLeaderService;
    this.slotTableStatusService = slotTableStatusService;
  }
}
