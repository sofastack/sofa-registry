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
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.client.manager.ClientManagerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * The type Clients open resource.
 *
 * @author xiaojian.xj
 * @version $Id : ClientManagerResource.java, v 0.1 2018-11-22 19:04 xiaojian.xj Exp $$
 */
@Path("api/clientManager")
@Produces(MediaType.APPLICATION_JSON)
public class ClientManagerResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(ClientManagerResource.class, "[DBService]");

  public static final TypeReference<Set<AddressVersion>> FORMAT =
      new TypeReference<Set<AddressVersion>>() {};

  @Resource private ClientManagerService clientManagerService;

  /**
   * Client off
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/clientOff")
  public CommonResponse clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.clientOff(ipSet);

    DB_LOGGER.info("client off result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret.isSuccess());
    return response;
  }

  /**
   * Client off
   *
   * @param ips
   * @return CommonResponse
   */
  @POST
  @Path("/clientOffWithSub")
  public CommonResponse clientOffWithSub(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }

    Set<AddressVersion> read = JsonUtils.read(ips, FORMAT);
    if (!validate(read)) {
      DB_LOGGER.error("client off new error, ips:{}", read);
      return CommonResponse.buildFailedResponse("ips is invalidate");
    }

    ClientManagerResult ret = clientManagerService.clientOffWithSub(read);

    DB_LOGGER.info("client off result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret.isSuccess());
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
   * @param ips
   * @return CommonResponse
   */
  @POST
  @Path("/clientOpen")
  public CommonResponse clientOpen(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.clientOpen(ipSet);

    DB_LOGGER.info("client open result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret.isSuccess());
    return response;
  }

  /**
   * Client Open
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/reduce")
  public CommonResponse reduce(@FormParam("ips") String ips) {
    if (StringUtils.isBlank(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    Set<String> ipSet = CollectionSdks.toIpSet(ips);

    ClientManagerResult ret = clientManagerService.reduce(ipSet);

    DB_LOGGER.info("reduce result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret.isSuccess());
    return response;
  }

  /**
   * Client Open
   *
   * @return GenericResponse
   */
  @GET
  @Path("/query")
  public GenericResponse<ClientManagerAddress> query() {
    DBResponse<ClientManagerAddress> ret = clientManagerService.queryClientOffAddress();
    DB_LOGGER.info("client off result:{}", ret.getOperationStatus(), ret.getEntity());

    if (ret.getOperationStatus() != OperationStatus.SUCCESS) {
      return new GenericResponse<ClientManagerAddress>().fillFailed("data not found");
    }
    return new GenericResponse<ClientManagerAddress>().fillSucceed(ret.getEntity());
  }

  /**
   * Setter method for property <tt>clientManagerService</tt>.
   *
   * @param clientManagerService value to be assigned to property clientManagerService
   * @return ClientManagerResource
   */
  @VisibleForTesting
  public ClientManagerResource setClientManagerService(ClientManagerService clientManagerService) {
    this.clientManagerService = clientManagerService;
    return this;
  }
}
