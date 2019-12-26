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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.NotifyProvideDataChange;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.google.common.collect.Sets;

/**
 *
 * @author shangyu.wh
 * @version $Id: StopPushDataResource.java, v 0.1 2018-07-25 11:40 shangyu.wh Exp $
 */
@Path("renewSwitch")
public class RenewSwitchResource {

    private static final Logger DB_LOGGER   = LoggerFactory.getLogger(RenewSwitchResource.class,
                                                "[DBService]");

    private static final Logger TASK_LOGGER = LoggerFactory.getLogger(RenewSwitchResource.class,
                                                "[Task]");

    @RaftReference
    private DBService           persistenceDataDBService;

    @Autowired
    private TaskListenerManager taskListenerManager;

    /**
     * get
     */
    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPushSwitch() throws Exception {
        Map<String, Object> resultMap = new HashMap<>(1);
        DBResponse enableDataRenewSnapshot = persistenceDataDBService
            .get(ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT);
        DBResponse enableDataDatumExpire = persistenceDataDBService
            .get(ValueConstants.ENABLE_DATA_DATUM_EXPIRE);

        resultMap
            .put(
                "enableDataRenewSnapshot",
                enableDataRenewSnapshot != null && enableDataRenewSnapshot.getEntity() != null ? ((PersistenceData) enableDataRenewSnapshot
                    .getEntity()).getData() : "null");
        resultMap
            .put(
                "enableDataDatumExpire",
                enableDataDatumExpire != null && enableDataDatumExpire.getEntity() != null ? ((PersistenceData) enableDataDatumExpire
                    .getEntity()).getData() : "null");

        return resultMap;
    }

    /**
     * enable both
     */
    @GET
    @Path("enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result enableRenew() {
        invokeSession("true");
        invokeData("true");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    /**
     * disable both
     */
    @GET
    @Path("disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result disableRenew() {
        invokeSession("false");
        invokeData("false");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    /**
     * enable session
     */
    @GET
    @Path("session/enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result enableSessionRenew() {
        invokeSession("true");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    /**
     * disable session
     */
    @GET
    @Path("session/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result disableSessionRenew() {
        invokeSession("false");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    /**
     * enable data
     */
    @GET
    @Path("data/enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result enableDataRenew() {
        invokeData("true");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    /**
     * disable data
     */
    @GET
    @Path("data/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Result disableDataRenew() {
        invokeData("false");

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    private void invokeSession(String data) {
        String msg = "put ENABLE_DATA_RENEW_SNAPSHOT to DB";
        PersistenceData persistenceData = createPersistenceData(ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT);
        persistenceData.setData(data);
        try {
            boolean ret = persistenceDataDBService.put(ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT,
                persistenceData);
            DB_LOGGER.info(String.format("%s result %s", msg, ret));
        } catch (Exception e) {
            DB_LOGGER.error(String.format("Error %s: %s", msg, e.getMessage()));
            throw new RuntimeException(String.format("Error %s: %s", msg, e.getMessage()), e);
        }

        fireDataChangeNotify(NodeType.SESSION, persistenceData.getVersion(),
            ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT, DataOperator.UPDATE);
    }

    private void invokeData(String data) {
        String msg = "put ENABLE_DATA_DATUM_EXPIRE to DB";
        PersistenceData persistenceData = createPersistenceData(ValueConstants.ENABLE_DATA_DATUM_EXPIRE);
        persistenceData.setData(data);
        try {
            boolean ret = persistenceDataDBService.put(ValueConstants.ENABLE_DATA_DATUM_EXPIRE,
                persistenceData);
            DB_LOGGER.info(String.format("%s result %s", msg, ret));
        } catch (Exception e) {
            DB_LOGGER.error(String.format("Error %s: %s", msg, e.getMessage()));
            throw new RuntimeException(String.format("Error %s: %s", msg, e.getMessage()), e);
        }

        fireDataChangeNotify(NodeType.DATA, persistenceData.getVersion(),
            ValueConstants.ENABLE_DATA_DATUM_EXPIRE, DataOperator.UPDATE);
    }

    private PersistenceData createPersistenceData(String dataId) {
        DataInfo dataInfo = DataInfo.valueOf(dataId);
        PersistenceData persistenceData = new PersistenceData();
        persistenceData.setDataId(dataInfo.getDataId());
        persistenceData.setGroup(dataInfo.getDataType());
        persistenceData.setInstanceId(dataInfo.getInstanceId());
        persistenceData.setVersion(System.currentTimeMillis());
        return persistenceData;
    }

    private void fireDataChangeNotify(NodeType nodeType, Long version, String dataInfoId,
                                      DataOperator dataOperator) {
        NotifyProvideDataChange notifyProvideDataChange = new NotifyProvideDataChange(dataInfoId,
            version, dataOperator);
        notifyProvideDataChange.setNodeTypes(Sets.newHashSet(nodeType));

        TaskEvent taskEvent = new TaskEvent(notifyProvideDataChange,
            TaskType.PERSISTENCE_DATA_CHANGE_NOTIFY_TASK);
        TASK_LOGGER.info("send PERSISTENCE_DATA_CHANGE_NOTIFY_TASK notifyProvideDataChange:"
                         + notifyProvideDataChange);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

}