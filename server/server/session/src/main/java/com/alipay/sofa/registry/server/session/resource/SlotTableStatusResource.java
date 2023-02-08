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
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 22, 2021
 */
@Path("openapi/v1/slot/table")
public class SlotTableStatusResource {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired MetaServerService metaServerService;

  @Autowired SlotTableCache slotTableCache;

  @Autowired DataCenterMetadataCache dataCenterMetadataCache;

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Object> getSlotTableStatus() {
    logger.info("[getSlotTableStatus] begin");
    try {
      SlotTableStatusResponse slotTableStatus = metaServerService.getSlotTableStatus();
      return new GenericResponse<>().fillSucceed(slotTableStatus);
    } catch (Throwable th) {
      logger.error("[getSlotTableStatus]", th);
      return new GenericResponse<>().fillFailed(th.getMessage());
    } finally {
      logger.info("[getSlotTableStatus] end");
    }
  }

  @GET
  @Path("/cache")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Object> getSlotTableCache(@QueryParam("dataCenter") String dataCenter) {
    logger.info("[getSlotTableCache] begin dataCenter={}", dataCenter);
    try {
      SlotTable slotTable = slotTableCache.getSlotTable(dataCenter);
      return new GenericResponse<>().fillSucceed(slotTable);
    } catch (Throwable th) {
      logger.error("[getSlotTableCache]", th);
      return new GenericResponse<>().fillFailed(th.getMessage());
    } finally {
      logger.info("[getSlotTableCache] end dataCenter={}", dataCenter);
    }
  }
}
