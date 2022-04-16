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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigResource.java, v 0.1 2021年09月25日 00:02 xiaojian.xj Exp $
 */
@Path("api/recover/config")
@Produces(MediaType.APPLICATION_JSON)
public class RecoverConfigResource {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(RecoverConfigResource.class, "[DBService]");

  @Autowired private RecoverConfigRepository recoverConfigRepository;

  @POST
  @Path("/save")
  public CommonResponse saveConfig(
      @FormParam("table") String table,
      @FormParam("key") String key,
      @FormParam("recoverClusterId") String recoverClusterId) {
    if (StringUtils.isBlank(table)
        || StringUtils.isBlank(key)
        || StringUtils.isBlank(recoverClusterId)) {
      return CommonResponse.buildFailedResponse("table, key, recoverClusterId is not allow empty.");
    }

    boolean ret = recoverConfigRepository.save(table, key, recoverClusterId);

    DB_LOGGER.info("save recover config result:{}, table:{}, key:{}", ret, table, key);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret);
    return response;
  }

  @POST
  @Path("/remove")
  public CommonResponse removeConfig(
      @FormParam("table") String table, @FormParam("key") String key) {
    if (StringUtils.isBlank(table) || StringUtils.isBlank(key)) {
      return CommonResponse.buildFailedResponse("table and key is not allow empty.");
    }

    boolean ret = recoverConfigRepository.remove(table, key);

    DB_LOGGER.info("remove recover config result:{}, table:{}, key:{}", ret, table, key);

    CommonResponse response = CommonResponse.buildSuccessResponse();
    response.setSuccess(ret);
    return response;
  }
}
