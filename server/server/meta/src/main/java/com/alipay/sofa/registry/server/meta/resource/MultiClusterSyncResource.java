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
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Sets;
import java.util.Locale;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigResource.java, v 0.1 2021年09月25日 00:02 xiaojian.xj Exp $
 */
@Path("api/multi/cluster")
@Produces(MediaType.APPLICATION_JSON)
public class MultiClusterSyncResource {

  private static final Logger LOG =
      LoggerFactory.getLogger("MULTI-CLUSTER-CONFIG", "[MultiClusterSyncResource]");

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<MultiClusterSyncInfo> query(
      @QueryParam("remoteDataCenter") String remoteDataCenter) {
    GenericResponse<MultiClusterSyncInfo> response = new GenericResponse();
    if (StringUtils.isBlank(remoteDataCenter)) {
      LOG.error("[query]remoteDataCenter is not allow empty.");
      return response.fillFailed("remoteDataCenter is not allow empty.");
    }
    MultiClusterSyncInfo query = multiClusterSyncRepository.query(remoteDataCenter);
    response.fillSucceed(query);
    return response;
  }

  @POST
  @Path("/save")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse saveConfig(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("remoteMetaAddress") String remoteMetaAddress,
      @FormParam("token") String token) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "save multi cluster syncs config, remoteDataCenter={}, remoteMetaAddress={}, auth check={} fail!",
          remoteDataCenter,
          remoteMetaAddress,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }
    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(remoteMetaAddress)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, remoteMetaAddress is not allow empty.");
    }

    MultiClusterSyncInfo multiClusterSyncInfo =
        new MultiClusterSyncInfo(
            remoteDataCenter, remoteMetaAddress, PersistenceDataBuilder.nextVersion());
    multiClusterSyncInfo.setEnableSyncDatum(true);
    multiClusterSyncInfo.setEnablePush(false);
    boolean ret = multiClusterSyncRepository.insert(multiClusterSyncInfo);

    LOG.info(
        "[saveConfig]save multi cluster sync config, result:{}, remoteDataCenter:{}, remoteMetaAddress:{}",
        ret,
        remoteDataCenter,
        remoteMetaAddress);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/sync/enable")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse syncEnable(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("expectVersion") String expectVersion,
      @FormParam("token") String token) {
    return updateSyncSwitch(token, remoteDataCenter, expectVersion, true);
  }

  @POST
  @Path("/sync/disable")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse syncDisable(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("expectVersion") String expectVersion,
      @FormParam("token") String token) {
    return updateSyncSwitch(token, remoteDataCenter, expectVersion, false);
  }

  private CommonResponse updateSyncSwitch(
      String token, String remoteDataCenter, String expectVersion, boolean enable) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "update multi cluster sync switch, remoteDataCenter={}, auth check={} fail!",
          remoteDataCenter,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.setEnableSyncDatum(enable);
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[syncSwitch]result:{}, remoteDataCenter:{}, remoteMetaAddress:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/push/enable")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse pushEnable(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("expectVersion") String expectVersion,
      @FormParam("token") String token) {
    return updatePushSwitch(token, remoteDataCenter, expectVersion, true);
  }

  @POST
  @Path("/push/disable")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse pushDisable(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("expectVersion") String expectVersion,
      @FormParam("token") String token) {
    return updatePushSwitch(token, remoteDataCenter, expectVersion, false);
  }

  private CommonResponse updatePushSwitch(
      String token, String remoteDataCenter, String expectVersion, boolean pushEnable) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "update multi cluster push switch, remoteDataCenter={}, auth check={} fail!",
          remoteDataCenter,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    if (!exist.isEnableSyncDatum() && pushEnable) {
      LOG.error(
          "update multi cluster push switch, remoteDataCenter={}, exist={}, not allow set pushEnable=true when syncEnable is false",
          remoteDataCenter,
          exist);
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter={}, not allow set pushEnable=true when syncEnable is false",
              remoteDataCenter));
    }

    exist.setEnablePush(pushEnable);
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[pushSwitch]result:{}, remoteDataCenter:{}, remoteMetaAddress:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/updateMetaAddress")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse updateMetaAddress(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("remoteMetaAddress") String remoteMetaAddress,
      @FormParam("token") String token,
      @FormParam("expectVersion") String expectVersion) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "update multi cluster syncs config, remoteDataCenter={}, remoteMetaAddress={}, auth check={} fail!",
          remoteDataCenter,
          remoteMetaAddress,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter)
        || StringUtils.isBlank(remoteMetaAddress)
        || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, remoteMetaAddress, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.setRemoteMetaAddress(remoteMetaAddress);
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[updateMetaAddress]result:{}, remoteDataCenter:{}, remoteMetaAddress:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        remoteMetaAddress,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/sync/dataInfoIds/add")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse addSyncDataInfoIds(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("dataInfoIds") String dataInfoIds,
      @FormParam("token") String token,
      @FormParam("expectVersion") String expectVersion) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "add sync dataInfoIds, remoteDataCenter={}, dataInfoIds={}, auth check={} fail!",
          remoteDataCenter,
          dataInfoIds,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter)
        || StringUtils.isBlank(dataInfoIds)
        || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, dataInfoIds, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.getSyncDataInfoIds().addAll(Sets.newHashSet(dataInfoIds.split(",")));
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[addSyncDataInfoIds]result:{}, remoteDataCenter:{}, dataInfoIds:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        dataInfoIds,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/sync/dataInfoIds/remove")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse removeSyncDataInfoIds(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("dataInfoIds") String dataInfoIds,
      @FormParam("token") String token,
      @FormParam("expectVersion") String expectVersion) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "remove sync dataInfoIds, remoteDataCenter={}, dataInfoIds={}, auth check={} fail!",
          remoteDataCenter,
          dataInfoIds,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }
    if (StringUtils.isBlank(remoteDataCenter)
        || StringUtils.isBlank(dataInfoIds)
        || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, dataInfoIds, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.getSyncDataInfoIds().removeAll(Sets.newHashSet(dataInfoIds.split(",")));
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[removeSyncDataInfoIds]result:{}, remoteDataCenter:{}, dataInfoIds:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        dataInfoIds,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/sync/group/add")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse addSyncGroup(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("group") String group,
      @FormParam("token") String token,
      @FormParam("expectVersion") String expectVersion) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "add sync group, remoteDataCenter={}, group={}, auth check={} fail!",
          remoteDataCenter,
          group,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter)
        || StringUtils.isBlank(group)
        || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, group, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.getSynPublisherGroups().add(group.toUpperCase(Locale.ROOT));
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[addSyncGroup]result:{}, remoteDataCenter:{}, group:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        group,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/sync/group/remove")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse removeSyncGroup(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("group") String group,
      @FormParam("token") String token,
      @FormParam("expectVersion") String expectVersion) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "remove sync group, remoteDataCenter={}, group={}, auth check={} fail!",
          remoteDataCenter,
          group,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter)
        || StringUtils.isBlank(group)
        || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, group, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }

    exist.getSynPublisherGroups().remove(group.toUpperCase(Locale.ROOT));
    exist.setDataVersion(PersistenceDataBuilder.nextVersion());
    boolean ret = multiClusterSyncRepository.update(exist, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[removeSyncGroup]result:{}, remoteDataCenter:{}, group:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        group,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/remove")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse removeConfig(
      @FormParam("remoteDataCenter") String remoteDataCenter,
      @FormParam("expectVersion") String expectVersion,
      @FormParam("token") String token) {
    if (!AuthChecker.authCheck(token)) {
      LOG.error(
          "remove multi cluster syncs config, remoteDataCenter={}, auth check={} fail!",
          remoteDataCenter,
          token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (StringUtils.isBlank(remoteDataCenter) || StringUtils.isBlank(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          "remoteDataCenter, expectVersion is not allow empty.");
    }

    MultiClusterSyncInfo exist = multiClusterSyncRepository.query(remoteDataCenter);

    if (exist == null || exist.getDataVersion() != Long.parseLong(expectVersion)) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remoteDataCenter:{}, expectVersion:{} not exist.", remoteDataCenter, expectVersion));
    }
    if (exist.isEnableSyncDatum()) {
      return CommonResponse.buildFailedResponse(
          StringFormatter.format(
              "remove remoteDataCenter:{} sync config fail when enable sync is true.",
              remoteDataCenter));
    }

    int ret =
        multiClusterSyncRepository.remove(remoteDataCenter, NumberUtils.toLong(expectVersion));

    LOG.info(
        "[removeConfig]remove multi cluster sync config, result:{}, remoteDataCenter:{}, expectVersion:{}",
        ret,
        remoteDataCenter,
        expectVersion);

    CommonResponse response = new CommonResponse();
    response.setSuccess(ret > 0);
    return response;
  }
}
