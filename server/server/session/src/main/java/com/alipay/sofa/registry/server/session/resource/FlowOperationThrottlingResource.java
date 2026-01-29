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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.server.session.limit.FlowOperationThrottlingObserver;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin REST resource for flow operation throttling control on Session Follower nodes.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>Observability: query local and cluster throttling status
 *   <li>Emergency control: force enable/disable LOCAL or CLUSTER throttling
 * </ul>
 *
 * <p><b>Warning:</b>
 *
 * <ul>
 *   <li>Modifying cluster throttling status breaks consistency with Meta Leader.
 *   <li>Use /cluster/ APIs ONLY during emergencies (e.g., Meta unavailable).
 *   <li>After emergency, restart node or wait for next heartbeat to restore sync.
 * </ul>
 */
@Path("/flowoperation/throttling")
public class FlowOperationThrottlingResource {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FlowOperationThrottlingResource.class);

  @Autowired private FlowOperationThrottlingObserver flowOperationThrottlingObserver;

  /**
   * Get current local and cluster throttling statuses on this Session node.
   *
   * @return Result with JSON string in message field containing: { "localStatus": { "enabled":
   *     false, "throttlePercent": 0.0 }, "clusterStatus": { "enabled": true, "throttlePercent":
   *     75.0 } }
   */
  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Map<String, FlowOperationThrottlingStatus>> getStatus() {
    try {
      FlowOperationThrottlingStatus localStatus =
          flowOperationThrottlingObserver.getLocalThrottlingStatus();
      FlowOperationThrottlingStatus clusterStatus =
          flowOperationThrottlingObserver.getClusterThrottlingStatus();

      Map<String, FlowOperationThrottlingStatus> response = new HashMap<>();
      response.put("localStatus", localStatus);
      response.put("clusterStatus", clusterStatus);

      return new GenericResponse<Map<String, FlowOperationThrottlingStatus>>()
          .fillSucceed(response);
    } catch (Throwable t) {
      LOGGER.error("[FlowOperationThrottlingResource] Failed to get throttling status", t);
      return new GenericResponse<Map<String, FlowOperationThrottlingStatus>>()
          .fillFailed("Failed to retrieve throttling status");
    }
  }
}
