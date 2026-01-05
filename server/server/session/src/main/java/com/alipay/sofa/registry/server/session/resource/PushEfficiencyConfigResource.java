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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.server.session.push.ChangeDebouncingTime;
import com.alipay.sofa.registry.server.session.push.ChangeProcessor;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2025/9/8
 */
@Path("api/push/efficiency")
public class PushEfficiencyConfigResource {

  private Logger LOGGER = LoggerFactory.getLogger(PushEfficiencyConfigResource.class);

  @Autowired private ChangeProcessor changeProcessor;

  @GET
  @Path("/getChangeDebouncingMillis")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Map<String, ChangeDebouncingTime[]>> getChangeDebouncingMillis(
      @HeaderParam("token") String token) {
    try {
      if (!AuthChecker.authCheck(token)) {
        LOGGER.error(
            "[module=PushEfficiencyConfigResource][method=getChangeDebouncingMillis] auth check={} fail!",
            token);
        return new GenericResponse().fillFailed("auth check fail");
      }

      Map<String, ChangeDebouncingTime[]> changeDebouncingTimes =
          this.changeProcessor.getChangeDebouncingMillis();
      return new GenericResponse().fillSucceed(changeDebouncingTimes);
    } catch (Throwable throwable) {
      LOGGER.error(
          "[module=PushEfficiencyConfigResource][method=getChangeDebouncingMillis] getChangeDebouncingMillis exception",
          throwable);
      return new GenericResponse().fillFailed("getChangeDebouncingMillis exception");
    }
  }

  @GET
  @Path("/isUseLargeAdapterDelayChangeWorker")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Map<String, boolean[]>> isUseLargeAdapterDelayChangeWorker(
      @HeaderParam("token") String token) {
    try {
      if (!AuthChecker.authCheck(token)) {
        LOGGER.error(
            "[module=PushEfficiencyConfigResource][method=isUseLargeAdapterDelayChangeWorker] auth check={} fail!",
            token);
        return new GenericResponse().fillFailed("auth check fail");
      }
      Map<String, boolean[]> dcUseLargeAdapterDelayChangeWorker =
          this.changeProcessor.isUseLargeAdapterDelayChangeWorker();
      return new GenericResponse().fillSucceed(dcUseLargeAdapterDelayChangeWorker);
    } catch (Throwable throwable) {
      LOGGER.error(
          "[module=PushEfficiencyConfigResource][method=isUseLargeAdapterDelayChangeWorker] isUseLargeAdapterDelayChangeWorker exception",
          throwable);
      return new GenericResponse().fillFailed("isUseLargeAdapterDelayChangeWorker exception");
    }
  }
}
