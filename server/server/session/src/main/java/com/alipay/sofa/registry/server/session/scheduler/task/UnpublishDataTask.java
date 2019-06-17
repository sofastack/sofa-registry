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

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author kezhu.wukz
 * @version $Id: UnpublishDataTask.java, v 0.1 2019-06-14 12:15 kezhu.wukz Exp $
 */
public class UnpublishDataTask extends AbstractSessionTask {

    private final DataNodeService     dataNodeService;

    private final SessionServerConfig sessionServerConfig;

    private Publisher                 unpublisher;

    public UnpublishDataTask(SessionServerConfig sessionServerConfig,
                             DataNodeService dataNodeService) {
        this.sessionServerConfig = sessionServerConfig;
        this.dataNodeService = dataNodeService;
    }

    @Override
    public void execute() {
        dataNodeService.unregister(unpublisher);
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();
        if (obj instanceof Publisher) {
            this.unpublisher = (Publisher) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public String toString() {
        return "UNPUBLISH_DATA_TASK{" + "taskId='" + getTaskId() + '\'' + ", unpublisher="
               + unpublisher + ", retry='" + sessionServerConfig.getUnpublishDataTaskRetryTimes()
               + '\'' + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getUnpublishDataTaskRetryTimes());
    }
}