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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import sun.net.util.IPAddressUtil;

/**
 * @author chen.zhu
 *     <p>Mar 24, 2021
 */
@Path("opsapi/v1")
public class RegistryCoreOpsResource {

  private final Logger LOGGER = LoggerFactory.getLogger(RegistryCoreOpsResource.class);

  /**
   * use /opsapi/v2 instead
   *
   * @param ip
   * @return
   */
  @PUT
  @Path("/server/group/quit/{ip}")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  @Deprecated
  public CommonResponse kickoffServer(@PathParam(value = "ip") String ip) {
    LOGGER.warn("[kickoffServer][begin] server [{}], use opsapi/v2 instead", ip);
    if (StringUtils.isBlank(ip) || !IPAddressUtil.isIPv4LiteralAddress(ip)) {
      LOGGER.error("[kickoffServer]invalid ip: {}", ip);
      return GenericResponse.buildFailedResponse("invalid ip address: " + ip);
    }
    return GenericResponse.buildSuccessResponse("use opsapi/v2 instead");
  }

  /**
   * use /opsapi/v2 instead
   *
   * @param ip
   * @return
   */
  @PUT
  @Path("/server/group/join/{ip}")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  @Deprecated
  public CommonResponse rejoinServerGroup(@PathParam(value = "ip") String ip) {
    LOGGER.warn("[rejoinServerGroup][begin] server [{}], use opsapi/v2 instead", ip);
    if (StringUtils.isBlank(ip) || !IPAddressUtil.isIPv4LiteralAddress(ip)) {
      LOGGER.error("[rejoinServerGroup]invalid ip: {}", ip);
      return GenericResponse.buildFailedResponse("invalid ip address: " + ip);
    }
    return GenericResponse.buildSuccessResponse("use opsapi/v2 instead");
  }
}
