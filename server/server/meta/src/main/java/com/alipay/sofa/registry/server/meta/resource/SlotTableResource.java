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
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.tasks.BalanceTask;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author chen.zhu
 * <p>
 * Jan 08, 2021
 */
@Path("openapi/v1/slot/table")
public class SlotTableResource {

    private final Logger             logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DefaultSlotManager       defaultSlotManager;

    @Autowired
    private LocalSlotManager         slotManager;

    @Autowired
    private DefaultDataServerManager dataServerManager;

    @Autowired
    private ScheduledSlotArranger    slotArranger;

    @PUT
    @Path("force/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public GenericResponse<SlotTable> forceRefreshSlotTable() {
        logger.info("[forceRefreshSlotTable] begin");
        if (ServiceStateMachine.getInstance().isLeader()) {
            if (slotArranger.tryLock()) {
                try {
                    BalanceTask task = new BalanceTask(slotManager,
                        defaultSlotManager.getRaftSlotManager(), dataServerManager);
                    task.run();
                    logger.info("[forceRefreshSlotTable] end with succeed");
                    return new GenericResponse<SlotTable>().fillSucceed(slotManager.getSlotTable());
                } finally {
                    slotArranger.unlock();
                }
            } else {
                logger.info("[forceRefreshSlotTable] end, fail to get the lock");
                return new GenericResponse<SlotTable>()
                    .fillFailed("scheduled slot arrangement is running");
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
            slotArranger.stop();
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
            slotArranger.start();
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
    public CommonResponse getReconcileStatus() {
        logger.info("[getReconcileStatus] begin");
        try {
            boolean result = slotArranger.getLifecycleState().isStarted();
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

    public SlotTableResource() {
    }

    public SlotTableResource(DefaultSlotManager defaultSlotManager, LocalSlotManager slotManager,
                             DefaultDataServerManager dataServerManager,
                             ScheduledSlotArranger slotArranger) {
        this.defaultSlotManager = defaultSlotManager;
        this.slotManager = slotManager;
        this.dataServerManager = dataServerManager;
        this.slotArranger = slotArranger;
    }
}
