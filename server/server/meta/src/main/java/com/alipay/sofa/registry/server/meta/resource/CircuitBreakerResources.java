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

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.CircuitBreakerData;
import com.alipay.sofa.registry.common.model.console.CircuitBreakerData.CircuitBreakOption;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : CircuitBreakerResources.java, v 0.1 2022年01月06日 19:54 xiaojian.xj Exp $
 */
@Path("circuit/breaker")
@LeaderAwareRestController
public class CircuitBreakerResources {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CircuitBreakerResources.class, "[CircuitBreaker]");

  @Resource private ProvideDataService provideDataService;

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<CircuitBreakerData> query() {
    Tuple<CircuitBreakerData, Long> tuple = queryData();
    return new GenericResponse().fillSucceed(tuple.o1);
  }

  @POST
  @Path("switch/close")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse switchClose() {

    boolean put = switchSave(false);

    LOGGER.info("circuit breaker switch close ret: {}", put);
    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(put);
    return response;
  }

  @POST
  @Path("switch/open")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse switchOpen() {

    boolean put = switchSave(true);

    LOGGER.info("circuit breaker switch close ret: {}", put);
    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(put);
    return response;
  }

  @POST
  @Path("add")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse add(@FormParam("option") String option, @FormParam("ips") String ips) {
    CircuitBreakOption optionEnum = CircuitBreakOption.getByOption(option);
    if (optionEnum == null || StringUtils.isBlank(ips)) {
      LOGGER.error("circuit breaker option or ips is blank.");
      return CommonResponse.buildFailedResponse("option or ips is blank.");
    }

    Tuple<CircuitBreakerData, Long> tuple = queryData();
    tuple.o1.add(optionEnum, CollectionSdks.toIpSet(ips));

    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID, JsonUtils.writeValueAsString(tuple.o1));
    boolean put = provideDataService.saveProvideData(persistenceData, tuple.o2);

    LOGGER.info("add circuit breaker ips: {}, ret: {}", ips, put);
    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(put);
    return response;
  }

  @POST
  @Path("remove")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse remove(@FormParam("option") String option, @FormParam("ips") String ips) {
    CircuitBreakOption optionEnum = CircuitBreakOption.getByOption(option);

    if (optionEnum == null || StringUtils.isBlank(ips)) {
      LOGGER.error("circuit breaker option or ips is blank.");
      return CommonResponse.buildFailedResponse("option or ips is blank.");
    }
    Tuple<CircuitBreakerData, Long> tuple = queryData();
    tuple.o1.remove(optionEnum, CollectionSdks.toIpSet(ips));

    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID, JsonUtils.writeValueAsString(tuple.o1));
    boolean put = provideDataService.saveProvideData(persistenceData, tuple.o2);

    LOGGER.info("remove circuit breaker ips: {}, ret: {}", ips, put);
    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(put);
    return response;
  }

  private boolean switchSave(boolean b) {
    Tuple<CircuitBreakerData, Long> tuple = queryData();
    tuple.o1.setAddressSwitch(b);

    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID, JsonUtils.writeValueAsString(tuple.o1));
    return provideDataService.saveProvideData(persistenceData, tuple.o2);
  }

  private Tuple<CircuitBreakerData, Long> queryData() {
    DBResponse<PersistenceData> queryResponse =
        provideDataService.queryProvideData(ValueConstants.CIRCUIT_BREAKER_DATA_ID);
    long expectVersion = 0;
    PersistenceData persistenceData = queryResponse.getEntity();
    CircuitBreakerData read = null;
    if (queryResponse.getOperationStatus() == OperationStatus.SUCCESS && persistenceData != null) {
      read = JsonUtils.read(persistenceData.getData(), CircuitBreakerData.class);
      expectVersion = persistenceData.getVersion();
    }
    if (read == null) {
      read = new CircuitBreakerData();
    }
    return new Tuple<>(read, expectVersion);
  }
}
