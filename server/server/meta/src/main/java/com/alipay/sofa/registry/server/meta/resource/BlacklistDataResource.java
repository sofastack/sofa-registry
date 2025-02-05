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
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.google.common.annotations.VisibleForTesting;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: StopPushDataResource.java, v 0.1 2018-07-25 11:40 shangyu.wh Exp $
 */
@Path("blacklist")
@LeaderAwareRestController
public class BlacklistDataResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(BlacklistDataResource.class, "[DBService]");

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(BlacklistDataResource.class, "[Task]");

  @Autowired private ProvideDataService provideDataService;

  @Autowired private DefaultProvideDataNotifier provideDataNotifier;

  /**
   * update blacklist e.g. curl -d
   * '{"FORBIDDEN_PUB":{"IP_FULL":["1.1.1.1","10.15.233.150"]},"FORBIDDEN_SUB_BY_PREFIX":{"IP_FULL":["1.1.1.1"]}}'
   * -H "Content-Type: application/json" -X POST http://localhost:9615/blacklist/update
   *
   * @param config config
   * @return Result
   */
  @POST
  @Path("update")
  @Produces(MediaType.APPLICATION_JSON)
  public Result blacklistPush(String config) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(ValueConstants.BLACK_LIST_DATA_ID, config);
    try {

      boolean ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("Success update blacklist to DB result {}!", ret);
    } catch (Throwable e) {
      DB_LOGGER.error("Error update blacklist to DB!", e);
      throw new RuntimeException("Update blacklist to error!", e);
    }

    fireDataChangeNotify(persistenceData.getVersion(), ValueConstants.BLACK_LIST_DATA_ID);

    Result result = new Result();
    result.setSuccess(true);
    return result;
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
  public BlacklistDataResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }

  @VisibleForTesting
  public BlacklistDataResource setProvideDataNotifier(
      DefaultProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }
}
