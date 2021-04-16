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

import com.alipay.sofa.registry.common.model.Node.NodeType;
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
import com.google.common.collect.Sets;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: StopPushDataResource.java, v 0.1 2018-07-25 11:40 shangyu.wh Exp $
 */
@Path("stopPushDataSwitch")
@LeaderAwareRestController
public class StopPushDataResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(StopPushDataResource.class, "[DBService]");

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(StopPushDataResource.class, "[Task]");

  @Autowired private ProvideDataService provideDataService;

  @Autowired private DefaultProvideDataNotifier provideDataNotifier;

  /** close push */
  @GET
  @Path("open")
  @Produces(MediaType.APPLICATION_JSON)
  public Result closePush() {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, "true");

    boolean ret;
    try {
      ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("open stop push data switch to DB result {}!", ret);
    } catch (Throwable e) {
      DB_LOGGER.error("error open stop push data switch to DB!", e);
      throw new RuntimeException("open stop push data switch to DB error!", e);
    }

    if (ret) {
      fireDataChangeNotify(
          persistenceData.getVersion(), ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    }

    Result result = new Result();
    result.setSuccess(ret);
    return result;
  }

  /** open push */
  @GET
  @Path("close")
  @Produces(MediaType.APPLICATION_JSON)
  public Result openPush() {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, "false");
    persistenceData.setData("false");

    boolean ret;
    try {
      ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("close stop push data switch to DB result {}!", ret);
    } catch (Exception e) {
      DB_LOGGER.error("error close stop push data switch from DB!");
      throw new RuntimeException("Close stop push data switch from DB error!");
    }

    if (ret) {
      fireDataChangeNotify(
          persistenceData.getVersion(), ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    }

    Result result = new Result();
    result.setSuccess(ret);
    return result;
  }

  private void fireDataChangeNotify(Long version, String dataInfoId) {

    ProvideDataChangeEvent provideDataChangeEvent =
        new ProvideDataChangeEvent(dataInfoId, version, getNodeTypes());
    if (TASK_LOGGER.isInfoEnabled()) {
      TASK_LOGGER.info(
          "send PERSISTENCE_DATA_CHANGE_NOTIFY_TASK notifyProvideDataChange: {}",
          provideDataChangeEvent);
    }
    provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
  }

  protected Set<NodeType> getNodeTypes() {
    return Sets.newHashSet(NodeType.SESSION);
  }

  @VisibleForTesting
  protected StopPushDataResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }

  @VisibleForTesting
  protected StopPushDataResource setProvideDataNotifier(
      DefaultProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }
}
