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
package com.alipay.sofa.registry.server.meta.timertask;

import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ThreadPoolExecutor;

public class LogMetricsTask {
    private static final Logger EXE_LOGGER  = LoggerFactory.getLogger("META-PROFILE-DIGEST",
                                                "[ExecutorMetrics]");
    private final TaskMetrics   taskMetrics = TaskMetrics.getInstance();

    public LogMetricsTask() {
        ThreadPoolExecutor boltDefaultExecutor = (ThreadPoolExecutor) ProtocolManager
            .getProtocol(ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE)).getCommandHandler()
            .getDefaultExecutor();
        taskMetrics.registerThreadExecutor("Meta-BoltDefaultExecutor", boltDefaultExecutor);

    }

    @Scheduled(initialDelayString = "${meta.server.metricsExecutor.fixedDelay}", fixedDelayString = "${meta.server.metricsExecutor.fixedDelay}")
    public void printExecutorMetrics() {
        EXE_LOGGER.info(TaskMetrics.getInstance().metricsString());
    }
}
