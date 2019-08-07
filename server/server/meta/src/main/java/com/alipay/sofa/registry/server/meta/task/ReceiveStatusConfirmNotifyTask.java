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
package com.alipay.sofa.registry.server.meta.task;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.StatusConfirmRequest;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.node.DataNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author shangyu.wh
 * @version $Id: ReceiveStatusConfirmNotifyTask.java, v 0.1 2018-03-24 17:08 shangyu.wh Exp $
 */
public class ReceiveStatusConfirmNotifyTask extends AbstractMetaServerTask {

    private final DataNodeService  dataNodeService;
    final private MetaServerConfig metaServerConfig;
    private DataNode               dataNode;

    public ReceiveStatusConfirmNotifyTask(DataNodeService dataNodeService,
                                          MetaServerConfig metaServerConfig) {
        this.dataNodeService = dataNodeService;
        this.metaServerConfig = metaServerConfig;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        Object obj = taskEvent.getEventObj();

        if (obj instanceof DataNode) {
            this.dataNode = (DataNode) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public void execute() {
        StatusConfirmRequest statusConfirmRequest = new StatusConfirmRequest(dataNode,
            dataNode.getNodeStatus());
        dataNodeService.notifyStatusConfirm(statusConfirmRequest);
    }

    @Override
    public String toString() {
        return "RECEIVE_STATUS_CONFIRM_NOTIFY_TASK{" + "taskId='" + taskId + '\'' + ", dataNode="
               + dataNode + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(metaServerConfig.getReceiveStatusConfirmNotifyTaskRetryTimes());
    }
}