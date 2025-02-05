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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.client.manager.CheckClientManagerService;
import com.alipay.sofa.registry.server.shared.client.manager.ClientManagerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : PersistenceClientManagerResource.java, v 0.1 2022年01月20日 14:51 xiaojian.xj Exp $
 */
@Path("api/clientManager/persistence")
@Produces(MediaType.APPLICATION_JSON)
public class PersistenceClientManagerResource {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClientManagerResource.class, "[PersistenceClientManager]");

  public static final TypeReference<Set<AddressVersion>> FORMAT =
      new TypeReference<Set<AddressVersion>>() {};

  @Autowired private ClientManagerService clientManagerService;

  @Autowired private CheckClientManagerService checkClientManagerService;

  /**
   * Client off
   *
   * @param ips ips
   * @return GenericResponse
   */
  @POST
  @Path("/clientOff")
  public GenericResponse<Long> clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return new GenericResponse().fillFailed("clientOff ips is empty.");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.clientOff(ipSet);
    LOGGER.info("client off result:{}, ips:{}", ret, ips);

    GenericResponse<Long> response = new GenericResponse();
    response.setSuccess(ret.isSuccess());
    response.setData(ret.getVersion());
    return response;
  }

  /**
   * Client off
   *
   * @param ips ips
   * @return GenericResponse
   */
  @POST
  @Path("/clientOffWithSub")
  public GenericResponse<Long> clientOffWithSub(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return new GenericResponse().fillFailed("clientOffWithSub ips is empty.");
    }

    Set<AddressVersion> read = JsonUtils.read(ips, FORMAT);
    if (!validate(read)) {
      LOGGER.error("client off new error, ips:{}", read);
      return new GenericResponse().fillFailed("ips is invalidate.");
    }

    ClientManagerResult ret = clientManagerService.clientOffWithSub(read);

    LOGGER.info("clientOffWithSub result:{}, ips:{}", ret, ips);

    GenericResponse<Long> response = new GenericResponse();
    response.setSuccess(ret.isSuccess());
    response.setData(ret.getVersion());
    return response;
  }

  private boolean validate(Set<AddressVersion> list) {
    if (CollectionUtils.isEmpty(list)) {
      return false;
    }
    for (AddressVersion address : list) {
      if (!address.isPub() && !address.isSub()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Client Open
   *
   * @param ips ips
   * @return GenericResponse
   */
  @POST
  @Path("/clientOpen")
  public GenericResponse<Long> clientOpen(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return new GenericResponse().fillFailed("clientOpen ips is empty.");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.clientOpen(ipSet);

    LOGGER.info("client open result:{}, ips:{}", ret, ips);

    GenericResponse<Long> response = new GenericResponse();
    response.setSuccess(ret.isSuccess());
    response.setData(ret.getVersion());
    return response;
  }

  /**
   * Client Open
   *
   * @param ips ips
   * @return GenericResponse
   */
  @POST
  @Path("/reduce")
  public GenericResponse<Long> reduce(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return new GenericResponse().fillFailed("reduce ips is empty.");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.reduce(ipSet);

    LOGGER.info("reduce result:{}, ips:{}", ret, ips);

    GenericResponse<Long> response = new GenericResponse();
    response.setSuccess(ret.isSuccess());
    response.setData(ret.getVersion());
    return response;
  }

  @GET
  @Path("/checkVersion")
  public CommonResponse checkVersion(@QueryParam("version") String version) {
    try {
      long expectedVersion = Long.parseLong(version);
      boolean check = checkClientManagerService.check(expectedVersion);
      LOGGER.info("check version: {} ret: {}", version, check);
      CommonResponse response = CommonResponse.buildSuccessResponse();
      response.setSuccess(check);
      return response;
    } catch (Throwable t) {
      LOGGER.error("check version: {} fail.", version, t);
      CommonResponse response = CommonResponse.buildFailedResponse("check version fail.");
      return response;
    }
  }

  @GET
  @Path("/query")
  public GenericResponse<ClientManagerAddress> query() {
    DBResponse<ClientManagerAddress> ret = clientManagerService.queryClientOffAddress();
    LOGGER.info("client off result:{}", ret.getOperationStatus(), ret.getEntity());

    if (ret.getOperationStatus() != OperationStatus.SUCCESS) {
      return new GenericResponse<ClientManagerAddress>().fillFailed("data not found");
    }
    return new GenericResponse<ClientManagerAddress>().fillSucceed(ret.getEntity());
  }

  /**
   * Setter method for property <tt>clientManagerService</tt>.
   *
   * @param clientManagerService value to be assigned to property clientManagerService
   * @return PersistenceClientManagerResource
   */
  @VisibleForTesting
  public PersistenceClientManagerResource setClientManagerService(
      ClientManagerService clientManagerService) {
    this.clientManagerService = clientManagerService;
    return this;
  }
}
