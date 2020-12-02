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
package com.alipay.sofa.registry.server.data.remoting.dataserver.task;

import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LogMetricsTask extends AbstractTask {
    private TaskMetrics         taskMetrics = TaskMetrics.getInstance();

    @Autowired
    private DataServerConfig    dataServerConfig;

    private static final Logger EXE_LOGGER  = LoggerFactory.getLogger("DATA-PROFILE-DIGEST",
                                                "[ExecutorMetrics]");

    public void printExecutorMetrics() {
        EXE_LOGGER.info(taskMetrics.metricsString());
    }

    @Override
    public void handle() {
        printExecutorMetrics();
    }

    @Override
    public int getDelay() {
        return dataServerConfig.getLogMetricsFixedDelay();
    }

    @Override
    public int getInitialDelay() {
        return dataServerConfig.getLogMetricsFixedDelay();
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public StartTaskTypeEnum getStartTaskTypeEnum() {
        return StartTaskTypeEnum.LOG_METRICS;
    }
}
