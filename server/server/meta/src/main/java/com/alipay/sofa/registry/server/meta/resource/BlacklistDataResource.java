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
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.NotifyProvideDataChange;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author shangyu.wh
 * @version $Id: StopPushDataResource.java, v 0.1 2018-07-25 11:40 shangyu.wh Exp $
 */
@Path("stopPushDataSwitch")
public class BlacklistDataResource {

    private static final Logger DB_LOGGER   = LoggerFactory.getLogger(StopPushDataResource.class,
                                                "[DBService]");

    private static final Logger TASK_LOGGER = LoggerFactory.getLogger(StopPushDataResource.class,
                                                "[Task]");

    @RaftReference
    private DBService           persistenceDataDBService;

    @Autowired
    private TaskListenerManager taskListenerManager;

    /**
     * open push
     */
    @POST
    @Path("update")
    @Produces(MediaType.APPLICATION_JSON)
    public Result blacklistPush(String config) {
        PersistenceData persistenceData = createDataInfo();
        persistenceData.setData(config);
        try {
            boolean ret = persistenceDataDBService.update(ValueConstants.BLACK_LIST_DATA_ID,
                persistenceData);
            DB_LOGGER.info("Success update blacklist to DB result {}!", ret);
        } catch (Exception e) {
            DB_LOGGER.error("Error update blacklist to DB!", e);
            throw new RuntimeException("Update blacklist to error!");
        }

        fireDataChangeNotify(persistenceData.getVersion(), ValueConstants.BLACK_LIST_DATA_ID,
            DataOperator.UPDATE);

        Result result = new Result();
        result.setSuccess(true);
        return result;
    }

    private PersistenceData createDataInfo() {
        DataInfo dataInfo = DataInfo.valueOf(ValueConstants.BLACK_LIST_DATA_ID);
        PersistenceData persistenceData = new PersistenceData();
        persistenceData.setDataId(dataInfo.getDataId());
        persistenceData.setGroup(dataInfo.getDataType());
        persistenceData.setInstanceId(dataInfo.getInstanceId());
        persistenceData.setVersion(System.currentTimeMillis());
        return persistenceData;
    }

    private void fireDataChangeNotify(Long version, String dataInfoId, DataOperator dataOperator) {

        NotifyProvideDataChange notifyProvideDataChange = new NotifyProvideDataChange(dataInfoId,
            version, dataOperator);

        TaskEvent taskEvent = new TaskEvent(notifyProvideDataChange,
            TaskType.PERSISTENCE_DATA_CHANGE_NOTIFY_TASK);
        TASK_LOGGER.info("send PERSISTENCE_DATA_CHANGE_NOTIFY_TASK notifyProvideDataChange:"
                         + notifyProvideDataChange);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

}