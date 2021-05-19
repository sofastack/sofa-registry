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
package com.alipay.sofa.registry.server.shared.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
@Path("opsapi/v1/")
public class RegistryOpsResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryOpsResource.class);

  @Autowired MetaServerService metaServerService;

  @PUT
  @Path("/server/group/quit")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse kickOffMyself() {
    LOGGER.info("[kickOffMyself] begin");
    CommonResponse response = null;
    try {
      metaServerService.addSelfToMetaBlacklist();
      response = GenericResponse.buildSuccessResponse();
    } catch (Throwable th) {
      LOGGER.error("[kickOffMyself]", th);
      response = GenericResponse.buildFailedResponse(th.getMessage());
    }
    LOGGER.info("[kickOffMyself] end, response: {}", response);
    return response;
  }

  @PUT
  @Path("/server/group/join")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse putMyselfBack() {
    LOGGER.info("[putMyselfBack] begin");
    CommonResponse response = null;
    try {
      metaServerService.removeSelfFromMetaBlacklist();
      response = GenericResponse.buildSuccessResponse();
    } catch (Throwable th) {
      LOGGER.error("[putMyselfBack]", th);
      response = GenericResponse.buildFailedResponse(th.getMessage());
    }
    LOGGER.info("[putMyselfBack] end, response: {}", response);
    return response;
  }
}
