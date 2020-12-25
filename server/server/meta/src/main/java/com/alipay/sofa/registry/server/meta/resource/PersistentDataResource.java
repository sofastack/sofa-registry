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
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author shangyu.wh
 * @version $Id: DecisionModeResource.java, v 0.1 2018-02-01 16:50 shangyu.wh Exp $
 */
@Path("persistentData")
public class PersistentDataResource {

    private static final Logger        DB_LOGGER  = LoggerFactory.getLogger(
                                                      PersistentDataResource.class, "[DBService]");

    private static final Logger        taskLogger = LoggerFactory.getLogger(
                                                      PersistentDataResource.class, "[Task]");

    @RaftReference
    private DBService                  persistenceDataDBService;

    @Autowired
    private DefaultProvideDataNotifier provideDataNotifier;

    @POST
    @Path("put")
    @Produces(MediaType.APPLICATION_JSON)
    public Result put(PersistenceData data) {

        checkObj(data, "PersistenceData");
        checkString(data.getData());
        checkObj(data.getVersion(), "version");

        String dataInfoId = DataInfo.toDataInfoId(data.getDataId(), data.getInstanceId(),
            data.getGroup());

        try {
            boolean ret = persistenceDataDBService.put(dataInfoId, data);
            DB_LOGGER.info("put Persistence Data {} to DB result {}!", data, ret);
        } catch (Throwable e) {
            DB_LOGGER.error("error put Persistence Data {} to DB!", data, e);
            throw new RuntimeException("Put Persistence Data " + data + " to DB error!", e);
        }

        fireDataChangeNotify(data.getVersion(), dataInfoId, DataOperator.ADD);

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    @POST
    @Path("remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Result remove(PersistenceData data) {

        checkObj(data, "PersistenceData");
        checkObj(data.getVersion(), "version");

        String dataInfoId = DataInfo.toDataInfoId(data.getDataId(), data.getInstanceId(),
            data.getGroup());

        try {
            boolean ret = persistenceDataDBService.remove(dataInfoId);
            DB_LOGGER.info("remove Persistence Data {} from DB result {}!", data, ret);
        } catch (Exception e) {
            DB_LOGGER.error("error remove Persistence Data {} from DB!", data);
            throw new RuntimeException("Remove Persistence Data " + data + " from DB error!");
        }

        fireDataChangeNotify(data.getVersion(), dataInfoId, DataOperator.REMOVE);

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    private void fireDataChangeNotify(Long version, String dataInfoId, DataOperator dataOperator) {

        ProvideDataChangeEvent provideDataChangeEvent = new ProvideDataChangeEvent(dataInfoId,
            version, dataOperator);

        if (taskLogger.isInfoEnabled()) {
            taskLogger.info("send PERSISTENCE_DATA_CHANGE_NOTIFY_TASK notifyProvideDataChange: {}",
                provideDataChangeEvent);
        }
        provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
    }

    private void checkString(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Error String data input:" + input);
        }
    }

    private void checkObj(Object input, String objName) {
        if (input == null) {
            throw new IllegalArgumentException("Error null Object " + objName + " data input!");
        }
    }
}
