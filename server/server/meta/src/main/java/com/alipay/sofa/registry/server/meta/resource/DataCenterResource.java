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

import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2025/9/22
 */
@Path("datacenter")
@LeaderAwareRestController
public class DataCenterResource {

  private Logger LOGGER = LoggerFactory.getLogger(DataCenterResource.class);

  @Autowired private MetaServerConfig metaServerConfig;

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public Result queryBlackList() {
    try {
      String localDataCenter = this.metaServerConfig.getLocalDataCenter();
      Result result = Result.success();
      result.setMessage(localDataCenter);
      return result;
    } catch (Throwable throwable) {
      LOGGER.error("Query meta local datacenter exception", throwable);
      return Result.failed("Query meta local datacenter exception");
    }
  }

}
