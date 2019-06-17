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

import com.alipay.sofa.registry.common.model.ReNewDatumRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author kezhu.wukz
 * @version $Id: ReNewDatumTask.java, v 0.1 2019-06-14 12:15 kezhu.wukz Exp $
 */
public class ReNewDatumTask extends AbstractSessionTask {

    private final static Logger       LOGGER = LoggerFactory.getLogger(ReNewDatumTask.class,
                                                 "[Task]");

    private final DataNodeService     dataNodeService;

    private final SessionServerConfig sessionServerConfig;

    private final SessionRegistry     sessionRegistry;

    private ReNewDatumRequest         reNewDatumRequest;

    public ReNewDatumTask(SessionServerConfig sessionServerConfig, DataNodeService dataNodeService,
                          SessionRegistry sessionRegistry) {
        this.sessionServerConfig = sessionServerConfig;
        this.dataNodeService = dataNodeService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void execute() {

        try {
            Boolean result = dataNodeService.reNewDatum(reNewDatumRequest);
            if (!result) {
                LOGGER
                    .info(
                        "ReNew datum request to dataNode got sub digest different! reNewDatumRequest={}",
                        reNewDatumRequest);

                // send snapshot datum for the corresponding connId
                sessionRegistry.sendDatumSnapshot(reNewDatumRequest.getConnectId());

            }
        } catch (Exception e) {
            LOGGER.error("ReNew datum request to dataNode error!  reNewDatumRequest={}",
                reNewDatumRequest, e);
        }
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();
        if (obj instanceof ReNewDatumRequest) {
            this.reNewDatumRequest = (ReNewDatumRequest) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public String toString() {
        return "RENEW_DATUM_TASK{" + "taskId='" + getTaskId() + '\'' + ", reNewDatumRequest="
               + reNewDatumRequest + ", retry='"
               + sessionServerConfig.getPublishDataTaskRetryTimes() + '\'' + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getReNewDatumTaskRetryTimes());
    }
}