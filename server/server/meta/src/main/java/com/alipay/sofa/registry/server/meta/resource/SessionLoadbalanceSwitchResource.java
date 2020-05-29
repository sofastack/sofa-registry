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
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangxu
 * @version : SessionLoadbalanceSwitchResource.java, v 0.1 2020年06月02日 2:51 下午 xiangxu Exp $
 */

@Path("sessionLoadbalance")
public class SessionLoadbalanceSwitchResource {
    private static final Logger DB_LOGGER   = LoggerFactory.getLogger(
                                                SessionLoadbalanceSwitchResource.class,
                                                "[DBService]");

    private static final Logger TASK_LOGGER = LoggerFactory.getLogger(
                                                SessionLoadbalanceSwitchResource.class, "[Task]");

    @RaftReference
    private DBService           persistenceDataDBService;

    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> get() throws Exception {
        Map<String, String> resultMap = new HashMap<>(1);

        DBResponse enabledZones = persistenceDataDBService
            .get(ValueConstants.ENABLE_SESSION_LOAD_BALANCE);
        resultMap
            .put(
                "enabledZones",
                enabledZones != null && enabledZones.getEntity() != null ? ((PersistenceData) enabledZones
                    .getEntity()).getData() : "");
        return resultMap;
    }

    @POST
    @Path("enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result enable(@FormParam("zones") String zones) {
        setEnabledZones(zones);
        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    @POST
    @Path("disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result disable() {
        setEnabledZones("");
        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    private void setEnabledZones(String value) {
        String msg = "put ENABLE_DATA_DATUM_EXPIRE to DB";
        PersistenceData persistenceData = createData();
        persistenceData.setData(value);
        try {
            boolean ret = persistenceDataDBService.put(ValueConstants.ENABLE_SESSION_LOAD_BALANCE,
                persistenceData);
            DB_LOGGER.info("%s result %s", msg, ret);
        } catch (Exception e) {
            DB_LOGGER.info("Error %s: %s", msg, e.getMessage());
            throw new RuntimeException(String.format("Error %s: %s", msg, e.getMessage()), e);
        }
    }

    private PersistenceData createData() {
        DataInfo dataInfo = DataInfo.valueOf(ValueConstants.ENABLE_SESSION_LOAD_BALANCE);
        PersistenceData persistenceData = new PersistenceData();
        persistenceData.setData(dataInfo.getDataId());
        persistenceData.setGroup(dataInfo.getDataType());
        persistenceData.setInstanceId(dataInfo.getInstanceId());
        persistenceData.setVersion(System.currentTimeMillis());
        return persistenceData;
    }
}