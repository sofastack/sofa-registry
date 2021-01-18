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
package com.alipay.sofa.registry.server.session.scheduler.task;

import com.alipay.sofa.registry.common.model.ClientOffPublishers;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author shangyu.wh
 * @version $Id: CancelDataTask.java, v 0.1 2017-12-27 12:15 shangyu.wh Exp $
 */
public class CancelDataTask extends AbstractSessionTask {
    /**
     * transfer data to DataNode
     */
    private final DataNodeService     dataNodeService;
    private final SessionServerConfig sessionServerConfig;
    private ClientOffPublishers       clientOffPublishers;

    public CancelDataTask(DataNodeService dataNodeService, SessionServerConfig sessionServerConfig) {
        this.dataNodeService = dataNodeService;
        this.sessionServerConfig = sessionServerConfig;
    }

    @Override
    public void execute() {
        dataNodeService.clientOff(clientOffPublishers);
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        this.clientOffPublishers = (ClientOffPublishers) taskEvent.getEventObj();

    }

    @Override
    public String toString() {
        return "CANCEL_DATA_TASK{" + "taskId='" + getTaskId() + '\'' + ", connectId="
               + clientOffPublishers.getConnectId() + ", retry='"
               + sessionServerConfig.getCancelDataTaskRetryTimes() + '\'' + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        //dataNodeService.clientOff will be retry all the failed
        return false;
    }
}