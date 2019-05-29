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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;

/**
 *
 * @author shangyu.wh
 * @version $Id: PushSwitchResource.java, v 0.1 2018-10-29 16:51 shangyu.wh Exp $
 */
@Path("health")
public class HealthResource {

    @Autowired
    private RaftClientManager      raftClientManager;

    @Autowired
    private SessionServerBootstrap sessionServerBootstrap;

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealth() {
        ResponseBuilder builder = Response.status(Response.Status.OK);

        CommonResponse response;

        StringBuilder sb = new StringBuilder("SessionServerBoot ");

        boolean start = raftClientManager.getClientStart().get();
        boolean ret = start;
        sb.append("RaftClientManager:").append(start);

        start = sessionServerBootstrap.getMetaStart().get();
        ret = ret && start;
        sb.append(", MetaServerStart:").append(start);

        start = sessionServerBootstrap.getSchedulerStart().get();
        ret = ret && start;
        sb.append(", SchedulerStart:").append(start);

        start = sessionServerBootstrap.getHttpStart().get();
        ret = ret && start;
        sb.append(", HttpServerStart:").append(start);

        start = sessionServerBootstrap.getServerStart().get();
        ret = ret && start;
        sb.append(", SessionServerStart:").append(start);

        start = sessionServerBootstrap.getDataStart().get();
        ret = ret && start;
        sb.append(", ConnectDataServer:").append(start);

        if (ret) {
            response = CommonResponse.buildSuccessResponse(sb.toString());
            builder.entity(response);
        } else {
            response = CommonResponse.buildFailedResponse(sb.toString());
            builder.entity(response);
            builder.status(Status.INTERNAL_SERVER_ERROR);
        }

        return builder.build();
    }
}