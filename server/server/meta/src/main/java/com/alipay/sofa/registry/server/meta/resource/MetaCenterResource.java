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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.cleaner.AppRevisionCleaner;
import com.alipay.sofa.registry.server.meta.cleaner.InterfaceAppsIndexCleaner;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

@Path("metaCenter")
@LeaderAwareRestController
public class MetaCenterResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(MetaCenterResource.class, "[DBService]");

  @Autowired private InterfaceAppsIndexCleaner interfaceAppsIndexCleaner;

  @Autowired private AppRevisionCleaner appRevisionCleaner;

  @Autowired ProvideDataService provideDataService;

  @Autowired DefaultProvideDataNotifier provideDataNotifier;

  @PUT
  @Path("interfaceAppsIndex/renew")
  @Produces(MediaType.APPLICATION_JSON)
  public Result interfaceAppsIndexRenew() {
    Result result = new Result();
    interfaceAppsIndexCleaner.startRenew();
    result.setSuccess(true);
    return result;
  }

  @PUT
  @Path("appRevisionCleaner/switch")
  @Produces(MediaType.APPLICATION_JSON)
  public Result appRevisionCleanerEnable(@FormParam("enabled") boolean enabled) {
    Result result = new Result();
    try {
      appRevisionCleaner.setEnabled(enabled);
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }
    return result;
  }

  MetaCenterResource setInterfaceAppsIndexCleaner(InterfaceAppsIndexCleaner cleaner) {
    interfaceAppsIndexCleaner = cleaner;
    return this;
  }

  @PUT
  @Path("appRevision/writeSwitch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Result setAppRevisionWriteSwitch(AppRevisionDomainConvertor.EnableConfig enableConfig) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
            JsonUtils.writeValueAsString(enableConfig));
    Result result = new Result();
    boolean ret;
    try {
      ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("app revision write switch {} to DB result {}", enableConfig, ret);
    } catch (Throwable e) {
      DB_LOGGER.error("app revision write switch {} to DB result error", enableConfig, e);
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      return result;
    }
    if (ret) {
      ProvideDataChangeEvent provideDataChangeEvent =
          new ProvideDataChangeEvent(
              ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
              persistenceData.getVersion(),
              Sets.newHashSet(Node.NodeType.SESSION));
      provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
    }
    result.setSuccess(ret);
    return result;
  }

  @VisibleForTesting
  public MetaCenterResource setProvideDataNotifier(DefaultProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }

  @VisibleForTesting
  public MetaCenterResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }
}
