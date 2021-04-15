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
package com.alipay.sofa.registry.server.shared.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author chen.zhu
 *     <p>Jan 12, 2021
 */
@Path("openapi/v1/slot/table")
public class SlotGenericResource implements SlotTableRecorder {

  private volatile SlotTable slotTableRef = SlotTable.INIT;

  @GET
  @Path("/content")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<SlotTable> slotTable() {
    return new GenericResponse<SlotTable>().fillSucceed(slotTableRef);
  }

  @GET
  @Path("/epoch")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Long> epoch() {
    long epoch = slotTableRef.getEpoch();
    return new GenericResponse<Long>().fillSucceed(epoch);
  }

  @Override
  public void record(SlotTable slotTable) {
    slotTableRef = slotTable;
  }
}
