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
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.remoting.data.DefaultDataServerService;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : MultiDatumResource.java, v 0.1 2022年09月08日 15:49 xiaojian.xj Exp $
 */
@Path("api/multi/datum")
@Produces(MediaType.APPLICATION_JSON)
public class MultiDatumResource {

  private static final Logger LOG =
      LoggerFactory.getLogger(MultiDatumResource.class, "[MultiDatumResource]");

  @Autowired private DefaultDataServerService defaultDataServerService;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @POST
  @Path("/dataInfoId/removePubs")
  public CommonResponse clearDatumPublish(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("dataInfoId") String dataInfoId,
      @FormParam("token") String token) {

    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "clear datum, remoteDataCenter={}, dataInfoId={}, auth check={} fail!",
          remoteDataCenter,
          dataInfoId,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(dataInfoId)) {
      return CommonResponse.buildFailedResponse("remoteDataCenter, dataInfoId is not allow empty.");
    }

    MultiClusterSyncInfo query = multiClusterSyncRepository.query(remoteDataCenter);
    if (query != null && query.isEnableSyncDatum()) {
      LOG.error("clear datum forbidden when sync enable, remoteDataCenter:{}", remoteDataCenter);
      return CommonResponse.buildFailedResponse("clear datum forbidden when sync enable.");
    }

    defaultDataServerService.notifyRemoteDatumClear(
        RemoteDatumClearEvent.dataInfoIdEvent(remoteDataCenter, dataInfoId));
    return CommonResponse.buildSuccessResponse();
  }

  @POST
  @Path("/group/removePubs")
  public CommonResponse clearGroupDatumPublish(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("group") String group,
      @FormParam("token") String token) {

    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "clear datum, remoteDataCenter={}, group={}, auth check={} fail!",
          remoteDataCenter,
          group,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(group)) {
      return CommonResponse.buildFailedResponse("remoteDataCenter, group is not allow empty.");
    }

    MultiClusterSyncInfo query = multiClusterSyncRepository.query(remoteDataCenter);
    if (query != null && query.isEnableSyncDatum()) {
      LOG.error("clear datum forbidden when sync enable, remoteDataCenter:{}", remoteDataCenter);
      return CommonResponse.buildFailedResponse("clear datum forbidden when sync enable.");
    }

    defaultDataServerService.notifyRemoteDatumClear(
        RemoteDatumClearEvent.groupEvent(remoteDataCenter, group));
    return CommonResponse.buildSuccessResponse();
  }
}
