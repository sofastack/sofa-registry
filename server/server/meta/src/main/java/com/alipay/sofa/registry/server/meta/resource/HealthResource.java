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
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerBootstrap;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: PushSwitchResource.java, v 0.1 2018-10-29 16:51 shangyu.wh Exp $
 */
@Path("health")
public class HealthResource {

  @Autowired private MetaServerBootstrap metaServerBootstrap;

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private CurrentDcMetaServer currentDcMetaServer;

  @PostConstruct
  public void init() {
    MetricRegistry metrics = new MetricRegistry();
    metrics.register("healthCheck", (Gauge<CommonResponse>) () -> getHealthCheckResult());
    ReporterUtils.startSlf4jReporter(60, metrics);
  }

  @GET
  @Path("check")
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkHealth() {
    ResponseBuilder builder = Response.status(Response.Status.OK);
    CommonResponse response = getHealthCheckResult();
    builder.entity(response);
    if (!response.isSuccess()) {
      builder.status(Status.INTERNAL_SERVER_ERROR);
    }
    return builder.build();
  }

  private CommonResponse getHealthCheckResult() {
    CommonResponse response;

    StringBuilder sb = new StringBuilder("MetaServerBoot ");

    boolean start = metaServerBootstrap.isRpcServerForSessionStarted();
    boolean ret = start;
    sb.append("sessionRegisterServer:").append(start);

    start = metaServerBootstrap.isRpcServerForDataStarted();
    ret = ret && start;
    sb.append(", dataRegisterServerStart:").append(start);

    start = metaServerBootstrap.isRpcServerForMetaStarted();
    ret = ret && start;
    sb.append(", otherMetaRegisterServerStart:").append(start);

    start = metaServerBootstrap.isHttpServerStarted();
    ret = ret && start;
    sb.append(", httpServerStart:").append(start);

    start = metaServerBootstrap.isRpcServerForRemoteMetaStarted();
    ret = ret && start;
    sb.append(", remoteMetaRegisterServerStart:").append(start);

    boolean leaderNotEmpty = StringUtils.isNotBlank(metaLeaderService.getLeader());
    ret = ret && leaderNotEmpty;

    sb.append(", role:").append(metaLeaderService.amILeader() ? "leader" : "follower");
    sb.append(", warmuped:").append(metaLeaderService.isWarmuped());
    sb.append(", leader:").append(metaLeaderService.getLeader());
    sb.append(", meta-servers:").append(currentDcMetaServer.getClusterMembers());

    if (ret) {
      response = CommonResponse.buildSuccessResponse(sb.toString());
    } else {
      response = CommonResponse.buildFailedResponse(sb.toString());
    }
    return response;
  }

  @VisibleForTesting
  protected HealthResource setMetaServerBootstrap(MetaServerBootstrap metaServerBootstrap) {
    this.metaServerBootstrap = metaServerBootstrap;
    return this;
  }

  @VisibleForTesting
  protected HealthResource setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
    return this;
  }

  @VisibleForTesting
  protected HealthResource setCurrentDcMetaServer(CurrentDcMetaServer currentDcMetaServer) {
    this.currentDcMetaServer = currentDcMetaServer;
    return this;
  }
}
