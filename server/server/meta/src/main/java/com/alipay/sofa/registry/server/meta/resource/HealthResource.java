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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerBootstrap;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;

/**
 *
 * @author shangyu.wh
 * @version $Id: PushSwitchResource.java, v 0.1 2018-10-29 16:51 shangyu.wh Exp $
 */
@Path("health")
public class HealthResource {

    @Autowired
    private MetaServerBootstrap metaServerBootstrap;

    @Autowired
    private RaftExchanger       raftExchanger;

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealth() {
        ResponseBuilder builder = Response.status(Response.Status.OK);

        CommonResponse response;

        StringBuilder sb = new StringBuilder("MetaServerBoot ");

        boolean start = metaServerBootstrap.getSessionStart().get();
        boolean ret = start;
        sb.append("sessionRegisterServer:").append(start);

        start = metaServerBootstrap.getDataStart().get();
        ret = ret && start;
        sb.append(", dataRegisterServerStart:").append(start);

        start = metaServerBootstrap.getMetaStart().get();
        ret = ret && start;
        sb.append(", otherMetaRegisterServerStart:").append(start);

        start = metaServerBootstrap.getHttpStart().get();
        ret = ret && start;
        sb.append(", httpServerStart:").append(start);

        start = raftExchanger.getServerStart().get();
        ret = ret && start;
        sb.append(", raftServerStart:").append(start);

        start = raftExchanger.getClientStart().get();
        ret = ret && start;
        sb.append(", raftClientStart:").append(start);

        start = raftExchanger.getClsStart().get();
        ret = ret && start;
        sb.append(", raftManagerStart:").append(start);

        start = ServiceStateMachine.getInstance().isLeader()
                || ServiceStateMachine.getInstance().isfollower();
        ret = ret && start;

        if (ServiceStateMachine.getInstance().isLeader()) {

            sb.append(", raftStatus:").append("Leader");
        } else if (ServiceStateMachine.getInstance().isfollower()) {
            sb.append(", raftStatus:").append("Follower");
        } else {
            sb.append(", raftStatus:").append(start);
        }

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