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
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import com.google.common.annotations.VisibleForTesting;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: MetaLeaderResource.java, v 0.1 2021年03月22日 10:19 xiaojian.xj Exp $
 */
@Path("meta/leader")
public class MetaLeaderResource {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private LeaderElector leaderElector;

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  @LeaderAwareRestController
  public GenericResponse<LeaderInfo> queryLeader() {
    logger.info("[queryLeader] begin");

    try {
      String leader = metaLeaderService.getLeader();
      long epoch = metaLeaderService.getLeaderEpoch();
      if (StringUtils.isBlank(leader)) {
        return new GenericResponse<LeaderInfo>().fillFailed("leader is null.");
      }

      LeaderInfo leaderInfo = new LeaderInfo(epoch, leader);
      return new GenericResponse<LeaderInfo>().fillSucceed(leaderInfo);
    } catch (Throwable throwable) {
      logger.error("[queryLeader] error.", throwable);
      return new GenericResponse<LeaderInfo>().fillFailed(throwable.getMessage());
    }
  }

  @PUT
  @Path("/quit/election")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse quitElection() {
    logger.info("[quitElection] begin");

    try {
      leaderElector.change2Observer();
      return GenericResponse.buildSuccessResponse();
    } catch (Throwable throwable) {
      logger.error("[quitElection] error.", throwable);
      return GenericResponse.buildFailedResponse(throwable.getMessage());
    }
  }

  @VisibleForTesting
  protected MetaLeaderResource setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
    return this;
  }

  @VisibleForTesting
  protected MetaLeaderResource setLeaderElector(LeaderElector leaderElector) {
    this.leaderElector = leaderElector;
    return this;
  }
}
