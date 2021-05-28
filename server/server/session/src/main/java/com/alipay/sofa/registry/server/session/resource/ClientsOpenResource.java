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

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.server.session.registry.Registry;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * The type Clients open resource.
 *
 * @author zhuoyu.sjw
 * @version $Id : ClientsResource.java, v 0.1 2018-04-11 19:04 zhuoyu.sjw Exp $$
 */
@Path("api/clients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientsOpenResource {

  @Autowired private Registry sessionRegistry;

  /**
   * Client off common response.
   *
   * @param request the request
   * @return the common response
   */
  @POST
  @Path("/off")
  public CommonResponse clientOff(CancelAddressRequest request) {

    if (null == request) {
      return CommonResponse.buildFailedResponse("Request can not be null.");
    }

    if (CollectionUtils.isEmpty(request.getConnectIds())) {
      return CommonResponse.buildFailedResponse("ConnectIds can not be null.");
    }

    final List<ConnectId> connectIds = request.getConnectIds();
    sessionRegistry.clean(connectIds);
    return CommonResponse.buildSuccessResponse();
  }
}
