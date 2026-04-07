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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.limit.AdaptiveFlowOperationLimiter;
import com.alipay.sofa.registry.server.meta.resource.filter.AuthRestController;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderForwardRestController;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST resource for emergency control of adaptive flow operation throttling.
 *
 * <p>Provides simple, explicit endpoints to enable or disable throttling in emergencies, ensuring
 * safe and unambiguous operations during incidents.
 */
@Path("/flowoperation/throttling")
@LeaderForwardRestController
public class FlowOperationThrottlingResource {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FlowOperationThrottlingResource.class);

  @Autowired private AdaptiveFlowOperationLimiter adaptiveFlowOperationLimiter;

  /**
   * Enable emergency throttling override. This forces the system to respect adaptive throttling
   * decisions (from DB or cluster).
   */
  @POST
  @Path("/emergency/enable")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public Result enableEmergencyThrottling() {
    try {
      adaptiveFlowOperationLimiter.setEmergencyThrottlingEnabled(true);
      LOGGER.info("[FlowOperationResource] Emergency throttling ENABLED");
      return Result.success();
    } catch (Throwable t) {
      LOGGER.error("[FlowOperationResource] Failed to enable emergency throttling", t);
      return Result.failed("Failed to enable emergency throttling");
    }
  }

  /**
   * Disable emergency throttling override. This forcibly turns OFF all adaptive throttling,
   * regardless of configuration or cluster status.
   */
  @POST
  @Path("/emergency/disable")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public Result disableEmergencyThrottling() {
    try {
      adaptiveFlowOperationLimiter.setEmergencyThrottlingEnabled(false);
      LOGGER.info("[FlowOperationResource] Emergency throttling DISABLED");
      return Result.success();
    } catch (Throwable t) {
      LOGGER.error("[FlowOperationResource] Failed to disable emergency throttling", t);
      return Result.failed("Failed to disable emergency throttling");
    }
  }

  /** Get current effective throttling status. */
  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public GenericResponse<FlowOperationThrottlingStatus> getThrottlingStatus() {
    try {
      FlowOperationThrottlingStatus status =
          adaptiveFlowOperationLimiter.getFlowOperationThrottlingStatus();
      return new GenericResponse<FlowOperationThrottlingStatus>().fillSucceed(status);
    } catch (Throwable t) {
      LOGGER.error("[FlowOperationResource] Failed to get throttling status", t);
      return new GenericResponse<FlowOperationThrottlingStatus>()
          .fillFailed("Failed to retrieve throttling status");
    }
  }

  /** Get current emergency override status. */
  @GET
  @Path("/emergency/status")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public GenericResponse<Map<String, Boolean>> getEmergencyStatus() {
    try {
      boolean enabled = adaptiveFlowOperationLimiter.isEmergencyThrottlingEnabled();
      Map<String, Boolean> response = new HashMap<>();
      response.put("emergencyEnabled", enabled);
      return new GenericResponse<Map<String, Boolean>>().fillSucceed(response);
    } catch (Throwable t) {
      LOGGER.error("[FlowOperationResource] Failed to get emergency status", t);
      return new GenericResponse<Map<String, Boolean>>()
          .fillFailed("Failed to retrieve emergency status");
    }
  }
}
