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
import com.alipay.sofa.registry.common.model.metaserver.CompressDatumSwitch;
import com.alipay.sofa.registry.common.model.metaserver.CompressPushSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Path("compress")
@LeaderAwareRestController
public class CompressResource {
  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(CompressResource.class, "[DBService]");

  @Autowired ProvideDataService provideDataService;

  @Autowired DefaultProvideDataNotifier provideDataNotifier;

  @POST
  @Path("push/switch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Result setPushSwitch(CompressPushSwitch compressPushSwitch) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
            JsonUtils.writeValueAsString(compressPushSwitch));
    Result result = new Result();
    boolean ret;
    try {
      ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("push compressed {} to DB result {}", compressPushSwitch, ret);
    } catch (Throwable e) {
      DB_LOGGER.error("push compressed {} to DB result error", compressPushSwitch, e);
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      return result;
    }
    if (ret) {
      ProvideDataChangeEvent provideDataChangeEvent =
          new ProvideDataChangeEvent(
              ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
              persistenceData.getVersion(),
              Sets.newHashSet(Node.NodeType.SESSION));
      provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
    }
    result.setSuccess(ret);
    return result;
  }

  @GET
  @Path("push/state")
  @Produces(MediaType.APPLICATION_JSON)
  public CompressPushSwitch getPushSwitch() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID);
    if (response.getOperationStatus() == OperationStatus.NOTFOUND
        || StringUtils.isBlank(response.getEntity().getData())) {
      return CompressPushSwitch.defaultSwitch();
    }
    return JsonUtils.read(response.getEntity().getData(), CompressPushSwitch.class);
  }

  @POST
  @Path("datum/switch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Result setDatumSwitch(CompressDatumSwitch compressDatumSwitch) {
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.COMPRESS_DATUM_SWITCH_DATA_ID,
            JsonUtils.writeValueAsString(compressDatumSwitch));
    Result result = new Result();
    boolean ret;
    try {
      ret = provideDataService.saveProvideData(persistenceData);
      DB_LOGGER.info("datum compressed {} to DB result {}", compressDatumSwitch, ret);
    } catch (Throwable e) {
      DB_LOGGER.error("datum compressed {} to DB result error", compressDatumSwitch, e);
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      return result;
    }
    if (ret) {
      ProvideDataChangeEvent provideDataChangeEvent =
          new ProvideDataChangeEvent(
              ValueConstants.COMPRESS_DATUM_SWITCH_DATA_ID,
              persistenceData.getVersion(),
              Sets.newHashSet(Node.NodeType.DATA));
      provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
    }
    result.setSuccess(ret);
    return result;
  }

  @GET
  @Path("datum/state")
  @Produces(MediaType.APPLICATION_JSON)
  public CompressDatumSwitch getDatumSwitch() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.COMPRESS_DATUM_SWITCH_DATA_ID);
    if (response.getOperationStatus() == OperationStatus.NOTFOUND
        || StringUtils.isBlank(response.getEntity().getData())) {
      return CompressDatumSwitch.defaultSwitch();
    }
    return JsonUtils.read(response.getEntity().getData(), CompressDatumSwitch.class);
  }
}
