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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: RenewSwitchResource.java, v 0.1 2018-07-25 11:40 shangyu.wh Exp $
 */
@Path("slotSync")
@LeaderAwareRestController
public class SlotSyncResource {

  @Autowired private ProvideDataService provideDataService;

  @GET
  @Path("get")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getSlotSync() throws Exception {
    Map<String, Object> resultMap = new HashMap<>(2);
    DBResponse<PersistenceData> syncSessionIntervalSec =
        provideDataService.queryProvideData(ValueConstants.DATA_DATUM_SYNC_SESSION_INTERVAL_SEC);
    DBResponse<PersistenceData> dataDatumExpire =
        provideDataService.queryProvideData(ValueConstants.DATA_SESSION_LEASE_SEC);

    resultMap.put("syncSessionIntervalSec", getEntityData(syncSessionIntervalSec));
    resultMap.put("dataDatumExpire", getEntityData(dataDatumExpire));
    return resultMap;
  }

  private static String getEntityData(DBResponse<PersistenceData> resp) {
    if (resp != null && resp.getEntity() != null) {
      return resp.getEntity().getData();
    }
    return "null";
  }

  @VisibleForTesting
  protected SlotSyncResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }
}
