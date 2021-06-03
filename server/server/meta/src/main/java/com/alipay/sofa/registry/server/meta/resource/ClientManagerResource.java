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

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.ClientManagerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Autowired private ClientManagerService clientManagerService;

  /** Client off */
  @POST
  @Path("/clientOff")
  public CommonResponse clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    String[] ipArray = StringUtils.split(ips.trim(), ';');
    HashSet<String> ipSet = Sets.newHashSet(ipArray);

    boolean ret = clientManagerService.clientOff(ipSet);

    DB_LOGGER.info("client off result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret);
    return response;
  }

  /** Client Open */
  @POST
  @Path("/clientOpen")
  public CommonResponse clientOpen(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    String[] ipArray = StringUtils.split(ips.trim(), ';');
    HashSet<String> ipSet = Sets.newHashSet(ipArray);

    boolean ret = clientManagerService.clientOpen(ipSet);

    DB_LOGGER.info("client open result:{}, ips:{}", ret, ips);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret);
    return response;
  }

  /** Client Open */
  @GET
  @Path("/query")
  public Map<String, Object> query() {
    DBResponse<ProvideData> ret = clientManagerService.queryClientOffSet();
    DB_LOGGER.info("client open result:{}", ret);

    Map<String, Object> response = Maps.newHashMap();
    response.put("status", ret.getOperationStatus());
    if (ret.getOperationStatus() != OperationStatus.SUCCESS) {
      return response;
    }
    ProvideData entity = ret.getEntity();
    response.put("version", entity.getVersion());
    response.put("ips", entity.getProvideData().getObject());

    return response;
  }

  /**
   * Setter method for property <tt>clientManagerService</tt>.
   *
   * @param clientManagerService value to be assigned to property clientManagerService
   */
  @VisibleForTesting
  protected ClientManagerResource setClientManagerService(
      ClientManagerService clientManagerService) {
    this.clientManagerService = clientManagerService;
    return this;
  }
}
