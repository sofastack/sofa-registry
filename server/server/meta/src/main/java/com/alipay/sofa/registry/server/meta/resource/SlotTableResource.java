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

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.tasks.init.InitReshardingTask;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author chen.zhu
 * <p>
 * Jan 08, 2021
 */
@Path("openapi/slot/table")
public class SlotTableResource {

    @Autowired
    private DefaultSlotManager       defaultSlotManager;

    @Autowired
    private LocalSlotManager         slotManager;

    @Autowired
    private DefaultDataServerManager dataServerManager;

    @PUT
    @Path("force/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public SlotTable forceRefreshSlotTable() {
        InitReshardingTask task = new InitReshardingTask(slotManager,
            defaultSlotManager.getRaftSlotManager(), dataServerManager);
        task.run();
        return slotManager.getSlotTable();
    }

    public SlotTableResource() {
    }

    public SlotTableResource(DefaultSlotManager defaultSlotManager, LocalSlotManager slotManager,
                             DefaultDataServerManager dataServerManager) {
        this.defaultSlotManager = defaultSlotManager;
        this.slotManager = slotManager;
        this.dataServerManager = dataServerManager;
    }
}
