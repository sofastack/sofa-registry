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
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch;
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch.CauseEnum;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
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

@Path("stopServer")
@LeaderAwareRestController
public class StopServerSwitchResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(StopServerSwitchResource.class, "[DBService]");

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(StopServerSwitchResource.class, "[Task]");

  @Autowired private ProvideDataService provideDataService;

  @Autowired private DefaultProvideDataNotifier provideDataNotifier;

  @POST
  @Path("update")
  @Produces(MediaType.APPLICATION_JSON)
  public Result stop(@FormParam("stop") String stop) {
    StopServerSwitch stopServerSwitch;
    if (StringUtils.equalsIgnoreCase(stop, "true")) {
      stopServerSwitch = new StopServerSwitch(true, CauseEnum.FORCE);
    } else {
      stopServerSwitch = new StopServerSwitch(false);
    }

    String value = JsonUtils.writeValueAsString(stopServerSwitch);
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.STOP_SERVER_SWITCH_DATA_ID, value);
    try {
      boolean ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("Success update stopServerSwitch:{} to DB result {}!", value, ret);
    } catch (Throwable e) {
      DB_LOGGER.error("Error update stopServerSwitch:{} to DB!", value, e);
      throw new RuntimeException("Update stopServerSwitch to error!", e);
    }

    fireDataChangeNotify(persistenceData.getVersion(), ValueConstants.STOP_SERVER_SWITCH_DATA_ID);

    Result result = new Result();
    result.setSuccess(true);
    return result;
  }

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> query() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.STOP_SERVER_SWITCH_DATA_ID);
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
  public StopServerSwitchResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }

  @VisibleForTesting
  public StopServerSwitchResource setProvideDataNotifier(
      DefaultProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }
}
