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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.client.pb.AppList;
import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse;
import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatResponse;
import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.converter.pb.AppRevisionConvertor;
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultAppRevisionHandlerStrategy implements AppRevisionHandlerStrategy {

  private static final Logger LOG =
      LoggerFactory.getLogger(DefaultAppRevisionHandlerStrategy.class);

  private static final Logger REVISION_LOGGER =
      LoggerFactory.getLogger("REVISION-RECEIVE", "[register]");

  @Autowired private MetadataCacheRegistry metadataCacheRegistry;

  @Autowired private PushSwitchService pushSwitchService;

  protected void beforeRegister(AppRevision appRevision) {}

  @Override
  public void handleAppRevisionRegister(
      AppRevision appRevision, RegisterResponse response, String remoteAddress) {
    try {
      validate(appRevision);
      beforeRegister(appRevision);
      metadataCacheRegistry.register(appRevision);
      response.setSuccess(true);
      response.setMessage("app revision register success!");
    } catch (Throwable e) {
      response.setSuccess(false);
      String msg = StringFormatter.format("app revision register failed! {}", e.getMessage());
      response.setMessage(msg);
      LOG.error(msg, e);
    } finally {
      REVISION_LOGGER.info(
          "{},app={},revision={},size={},addr={}",
          response.isSuccess() ? "Y" : "N",
          appRevision.getAppName(),
          appRevision.getRevision(),
          appRevision.getSize(),
          remoteAddress);
    }
  }

  @Override
  public ServiceAppMappingResponse queryApps(List<String> services, String remoteIp) {
    ParaCheckUtil.checkNotEmpty(services, "services");
    ServiceAppMappingResponse.Builder builder = ServiceAppMappingResponse.newBuilder();

    if (!pushSwitchService.canIpPushLocal(remoteIp)) {
      builder.setStatusCode(ValueConstants.METADATA_STATUS_DATA_NOT_FOUND);
      return builder.build();
    }

    int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
    try {
      for (String service : services) {
        InterfaceMapping appNames = metadataCacheRegistry.getAppNames(service);
        AppList.Builder build = AppList.newBuilder().addAllApps(appNames.getApps());
        build.setVersion(appNames.getNanosVersion());
        builder.putServiceAppMapping(service, build.build());
      }
    } catch (Throwable e) {
      statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
      String msg =
          StringFormatter.format(
              "query apps by services error. service: {}, {}", services, e.getMessage());
      builder.setMessage(msg);
      LOG.error(msg, e);
    }
    builder.setStatusCode(statusCode);
    return builder.build();
  }

  @Override
  public GetRevisionsResponse queryRevision(List<String> revisions) {
    ParaCheckUtil.checkNotEmpty(revisions, "revisions");
    GetRevisionsResponse.Builder builder = GetRevisionsResponse.newBuilder();
    int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
    String queryRevision = null;
    try {
      for (String revision : revisions) {
        queryRevision = revision;
        AppRevision appRevision = null;
        try {
          appRevision = metadataCacheRegistry.getRevision(revision);
        } catch (Throwable e) {
          LOG.error("query revision {} error", queryRevision, e);
        }
        if (appRevision == null) {
          statusCode = ValueConstants.METADATA_STATUS_DATA_NOT_FOUND;
          String msg = StringFormatter.format("query revision not found, {}", revision);
          builder.setMessage(msg);
          LOG.error(msg);
        } else {
          builder.putRevisions(revision, AppRevisionConvertor.convert2Pb(appRevision));
        }
      }
    } catch (Throwable e) {
      statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
      String msg =
          StringFormatter.format("query revision {} error : {}", queryRevision, e.getMessage());
      builder.setMessage(msg);
      LOG.error(msg, e);
    }
    builder.setStatusCode(statusCode);
    return builder.build();
  }

  @Override
  public MetaHeartbeatResponse heartbeat(List<String> revisions) {
    ParaCheckUtil.checkNotEmpty(revisions, "revisions");
    MetaHeartbeatResponse.Builder builder = MetaHeartbeatResponse.newBuilder();
    int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
    for (String revision : revisions) {
      // avoid the error break the heartbeat of next revisions
      try {
        boolean success = metadataCacheRegistry.heartbeat(revision);
        if (!success) {
          statusCode = ValueConstants.METADATA_STATUS_DATA_NOT_FOUND;
          String msg = StringFormatter.format("heartbeat revision not found, {}", revision);
          builder.setMessage(msg);
          LOG.error(msg);
        }
      } catch (Throwable e) {
        statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
        String msg =
            StringFormatter.format("revisions {} heartbeat error: {}", revision, e.getMessage());
        builder.setMessage(msg);
        LOG.error(msg, e);
      }
    }
    builder.setStatusCode(statusCode);
    return builder.build();
  }

  private void validate(AppRevision appRevision) {
    ParaCheckUtil.checkNotBlank(appRevision.getAppName(), "appRevision.appName");
    ParaCheckUtil.checkNotBlank(appRevision.getRevision(), "appRevision.revision");
  }

  /**
   * Setter method for property <tt>metadataCacheRegistry</tt>.
   *
   * @param metadataCacheRegistry value to be assigned to property metadataCacheRegistry
   * @return DefaultAppRevisionHandlerStrategy
   */
  @VisibleForTesting
  public DefaultAppRevisionHandlerStrategy setMetadataCacheRegistry(
      MetadataCacheRegistry metadataCacheRegistry) {
    this.metadataCacheRegistry = metadataCacheRegistry;
    return this;
  }

  /**
   * Setter method for property <tt>pushSwitchService</tt>.
   *
   * @param pushSwitchService value to be assigned to property pushSwitchService
   * @return DefaultAppRevisionHandlerStrategy
   */
  @VisibleForTesting
  public DefaultAppRevisionHandlerStrategy setPushSwitchService(
      PushSwitchService pushSwitchService) {
    this.pushSwitchService = pushSwitchService;
    return this;
  }
}
