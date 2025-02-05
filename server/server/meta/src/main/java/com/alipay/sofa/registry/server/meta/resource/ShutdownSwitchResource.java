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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch.CauseEnum;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.FetchStopPushService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Path("shutdown")
@LeaderAwareRestController
public class ShutdownSwitchResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(ShutdownSwitchResource.class, "[DBService]");

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(ShutdownSwitchResource.class, "[Task]");

  @Autowired private ProvideDataService provideDataService;

  @Autowired private DefaultProvideDataNotifier provideDataNotifier;

  @Autowired private FetchStopPushService fetchStopPushService;

  @POST
  @Path("update")
  @Produces(MediaType.APPLICATION_JSON)
  public Result shutdown(@FormParam("shutdown") String shutdown, @FormParam("token") String token) {

    if (!AuthChecker.authCheck(token)) {
      DB_LOGGER.error("update shutdownSwitch, shutdown={} auth check={} fail!", shutdown, token);
      return Result.failed("auth check fail");
    }

    if (!fetchStopPushService.isStopPush()) {
      DB_LOGGER.error("push switch is open");
      return Result.failed("push switch is open");
    }

    ShutdownSwitch shutdownSwitch;
    if (StringUtils.equalsIgnoreCase(shutdown, "true")) {
      shutdownSwitch = new ShutdownSwitch(true, CauseEnum.FORCE.getCause());
    } else {
      shutdownSwitch = new ShutdownSwitch(false);
    }

    String value = JsonUtils.writeValueAsString(shutdownSwitch);
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(ValueConstants.SHUTDOWN_SWITCH_DATA_ID, value);
    try {
      boolean ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("Success update shutdownSwitch:{} to DB result {}!", value, ret);
    } catch (Throwable e) {
      DB_LOGGER.error("Error update shutdownSwitch:{} to DB!", value, e);
      throw new RuntimeException("Update shutdownSwitch to error!", e);
    }

    fireDataChangeNotify(persistenceData.getVersion(), ValueConstants.SHUTDOWN_SWITCH_DATA_ID);

    Result result = new Result();
    result.setSuccess(true);
    return result;
  }

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> query() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.SHUTDOWN_SWITCH_DATA_ID);
    if (response.getOperationStatus() == OperationStatus.NOTFOUND) {
      return Collections.singletonMap("switch", "empty value.");
    }
    PersistenceData entity = response.getEntity();
    return Collections.singletonMap("switch", entity.getData());
  }

  private void fireDataChangeNotify(Long version, String dataInfoId) {

    ProvideDataChangeEvent provideDataChangeEvent = new ProvideDataChangeEvent(dataInfoId, version);
    if (TASK_LOGGER.isInfoEnabled()) {
      TASK_LOGGER.info(
          "send PERSISTENCE_DATA_CHANGE_NOTIFY_TASK notifyProvideDataChange: {}",
          provideDataChangeEvent);
    }
    provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
  }

  @VisibleForTesting
  public ShutdownSwitchResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }

  @VisibleForTesting
  public ShutdownSwitchResource setProvideDataNotifier(
      DefaultProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }

  /**
   * Setter method for property <tt>fetchStopPushService</tt>.
   *
   * @param fetchStopPushService value to be assigned to property fetchStopPushService
   * @return ShutdownSwitchResource
   */
  public ShutdownSwitchResource setFetchStopPushService(FetchStopPushService fetchStopPushService) {
    this.fetchStopPushService = fetchStopPushService;
    return this;
  }
}
