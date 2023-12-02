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
package com.alipay.sofa.registry.server.meta.resource.filter;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.ADMIN_API_TOKEN_DATA_ID;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author jiangcun.hlc
 *     <p>Nov 17, 2023
 */
@Provider
@AuthRestController
@Priority(Priorities.USER)
public class AuthRestFilter implements ContainerRequestFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthRestFilter.class);

  @Autowired private ProvideDataService provideDataService;

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    boolean authAllow;
    DBResponse<PersistenceData> queryResponse =
        provideDataService.queryProvideData(ADMIN_API_TOKEN_DATA_ID);
    if (queryResponse.getOperationStatus() == OperationStatus.NOTFOUND
        || StringUtils.isBlank(queryResponse.getEntity().getData())) {
      authAllow = true;
    } else {
      authAllow = false;
      Set<String> tokens =
          JsonUtils.read(queryResponse.getEntity().getData(), new TypeReference<Set<String>>() {});
      if (!CollectionUtils.isEmpty(tokens)
          && tokens.contains(getAuthToken(containerRequestContext))) {
        authAllow = true;
      }
    }
    if (!authAllow) {
      Response response =
          Response.status(Response.Status.UNAUTHORIZED)
              .header("reason", "auth check failed!")
              .build();
      LOGGER.error(
          "[filter] url: %s, auth check fail!", containerRequestContext.getUriInfo().getPath());
      containerRequestContext.abortWith(response);
    }
  }

  public String getAuthToken(ContainerRequestContext context) {
    String token = context.getHeaderString("x-apiauth-token");
    if (StringUtils.isNotBlank(token)) {
      return token;
    }
    return "unknown";
  }
}
