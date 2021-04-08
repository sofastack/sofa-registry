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
package com.alipay.sofa.registry.server.meta.resource.filter;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.URI;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 17, 2021
 */
@Provider
@LeaderAwareRestController
@Priority(Priorities.USER)
public class LeaderAwareFilter implements ContainerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LeaderAwareFilter.class);

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private MetaServerConfig metaServerConfig;

  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    if (!metaLeaderService.amILeader()) {
      Response response;
      String leaderIp = metaLeaderService.getLeader();
      if (StringUtils.isEmpty(leaderIp)) {
        response =
            Response.status(Response.Status.BAD_REQUEST)
                .header("reason", "no leader found")
                .build();
      } else {
        try {
          LOGGER.warn(
              "[filter]request: [{}][{}] need to redirect to {}",
              getRemoteIP(context),
              context.getMethod(),
              leaderIp);
          response =
              Response.temporaryRedirect(
                      new URI(
                          "http://"
                              + leaderIp
                              + ":"
                              + metaServerConfig.getHttpServerPort()
                              + "/"
                              + context.getUriInfo().getPath()))
                  .build();
        } catch (Throwable e) {
          LOGGER.error("[filter]", e);
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      }
      context.abortWith(response);
    }
  }

  public String getRemoteIP(ContainerRequestContext context) {
    String remote = context.getHeaderString("x-forwarded-for");
    if (StringUtils.isNotBlank(remote)) {
      return remote;
    }
    return "unknown";
  }

  public LeaderAwareFilter() {}

  public LeaderAwareFilter(MetaLeaderService metaLeaderService, MetaServerConfig metaServerConfig) {
    this.metaLeaderService = metaLeaderService;
    this.metaServerConfig = metaServerConfig;
  }

  @VisibleForTesting
  protected LeaderAwareFilter setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
    return this;
  }
}
