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
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryForbiddenServerRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryForbiddenServerManager;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.google.common.annotations.VisibleForTesting;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import sun.net.util.IPAddressUtil;

/**
 * @author chen.zhu
 *     <p>Mar 24, 2021
 */
@Path("opsapi/v2")
public class RegistryCoreOpsV2Resource {

  private final Logger LOGGER = LoggerFactory.getLogger(RegistryCoreOpsV2Resource.class);

  @Autowired private RegistryForbiddenServerManager registryForbiddenServerManager;

  @PUT
  @Path("/server/group/quit/{ip}")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public CommonResponse kickoffServer(
      @FormParam(value = "cell") String cell,
      @FormParam(value = "nodeType") String nodeType,
      @PathParam(value = "ip") String ip) {
    LOGGER.info("[kickoffServerV2][begin] server [{}][{}][{}]", cell, nodeType, ip);

    if (StringUtils.isBlank(cell) || StringUtils.isBlank(nodeType)) {
      return GenericResponse.buildFailedResponse("invalid param.");
    }
    NodeType nodeTypeEnum = NodeType.codeOf(nodeType);
    if (nodeTypeEnum == null) {
      return GenericResponse.buildFailedResponse("invalid nodeType: " + nodeType);
    }

    if (StringUtils.isBlank(ip) || !IPAddressUtil.isIPv4LiteralAddress(ip)) {
      LOGGER.error("[kickoffServerV2]invalid ip: {}", ip);
      return GenericResponse.buildFailedResponse("invalid ip address: " + ip);
    }
    try {
      boolean success =
          registryForbiddenServerManager.addToBlacklist(
              new RegistryForbiddenServerRequest(DataOperation.ADD, nodeTypeEnum, ip, cell));

      if (!success) {
        LOGGER.error("[kickoffServerV2] add ip: {} to blacklist fail.", ip);
      }
      return success
          ? GenericResponse.buildSuccessResponse()
          : GenericResponse.buildFailedResponse("kickoffServer: " + ip + " fail.");
    } catch (Throwable th) {
      LOGGER.error("[kickoffServerV2]", th);
      return GenericResponse.buildFailedResponse(th.getMessage());
    } finally {
      LOGGER.info("[kickoffServerV2][end] server [{}]", ip);
    }
  }

  @PUT
  @Path("/server/group/join/{ip}")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public CommonResponse rejoinServerGroup(
      @FormParam(value = "cell") String cell,
      @FormParam(value = "nodeType") String nodeType,
      @PathParam(value = "ip") String ip) {
    LOGGER.info("[rejoinServerGroupV2][begin] server [{}][{}][{}]", cell, nodeType, ip);

    if (StringUtils.isBlank(cell) || StringUtils.isBlank(nodeType)) {
      return GenericResponse.buildFailedResponse("invalid param.");
    }
    NodeType nodeTypeEnum = NodeType.codeOf(nodeType);
    if (nodeType == null) {
      return GenericResponse.buildFailedResponse("invalid nodeType: " + nodeType);
    }

    if (StringUtils.isBlank(ip) || !IPAddressUtil.isIPv4LiteralAddress(ip)) {
      LOGGER.error("[rejoinServerGroupV2]invalid ip: {}", ip);
      return GenericResponse.buildFailedResponse("invalid ip address: " + ip);
    }
    try {
      boolean success =
          registryForbiddenServerManager.removeFromBlacklist(
              new RegistryForbiddenServerRequest(DataOperation.REMOVE, nodeTypeEnum, ip, cell));
      if (!success) {
        LOGGER.error("[rejoinServerGroupV2] remove ip: {} to blacklist fail.", ip);
      }
      return success
          ? GenericResponse.buildSuccessResponse()
          : GenericResponse.buildFailedResponse("rejoinServerGroup: " + ip + " fail.");
    } catch (Throwable th) {
      LOGGER.error("[rejoinServerGroupV2]", th);
      return GenericResponse.buildFailedResponse(th.getMessage());
    } finally {
      LOGGER.info("[rejoinServerGroupV2][end] server [{}]", ip);
    }
  }

  @VisibleForTesting
  protected RegistryCoreOpsV2Resource setRegistryForbiddenServerManager(
      RegistryForbiddenServerManager registryForbiddenServerManager) {
    this.registryForbiddenServerManager = registryForbiddenServerManager;
    return this;
  }
}
