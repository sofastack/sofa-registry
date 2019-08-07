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

import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author kezhu.wukz
 * @version $Id: RenewDatumTask.java, v 0.1 2019-06-14 12:15 kezhu.wukz Exp $
 */
public class RenewDatumTask extends AbstractSessionTask {

    private static final Logger       RENEW_LOGGER = LoggerFactory.getLogger(
                                                       ValueConstants.LOGGER_NAME_RENEW,
                                                       "[RenewDatumTask]");

    private final DataNodeService     dataNodeService;

    private final SessionServerConfig sessionServerConfig;

    private final SessionRegistry     sessionRegistry;

    private RenewDatumRequest         renewDatumRequest;

    public RenewDatumTask(SessionServerConfig sessionServerConfig, DataNodeService dataNodeService,
                          SessionRegistry sessionRegistry) {
        this.sessionServerConfig = sessionServerConfig;
        this.dataNodeService = dataNodeService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void execute() {

        try {
            Boolean result = dataNodeService.renewDatum(renewDatumRequest);
            if (!result) {
                RENEW_LOGGER.info(
                    "Renew datum request to dataNode got digest different! renewDatumRequest={}",
                    renewDatumRequest);

                // send snapshot datum for the corresponding connId
                sessionRegistry.sendDatumSnapshot(renewDatumRequest.getConnectId(),
                    renewDatumRequest.getDataServerIP());

            }
        } catch (Exception e) {
            RENEW_LOGGER.error(String.format(
                "Renew datum request to dataNode error! renewDatumRequest=%s, errorMsg=%s",
                renewDatumRequest, e.getMessage()), e);
        }
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();
        if (obj instanceof RenewDatumRequest) {
            this.renewDatumRequest = (RenewDatumRequest) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public String toString() {
        return String.format("RENEW_DATUM_TASK{ taskId=%s, renewDatumRequest=%s }", getTaskId(),
            renewDatumRequest);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getRenewDatumTaskRetryTimes());
    }
}