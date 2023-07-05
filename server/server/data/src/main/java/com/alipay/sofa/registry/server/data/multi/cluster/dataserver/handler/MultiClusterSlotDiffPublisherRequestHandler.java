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
package com.alipay.sofa.registry.server.data.multi.cluster.dataserver.handler;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.loggers.Loggers;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.BaseSlotDiffPublisherRequestHandler;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotDiffPublisherRequestHandler.java, v 0.1 2022年05月16日 21:51 xiaojian.xj
 *     Exp $
 */
public class MultiClusterSlotDiffPublisherRequestHandler
    extends BaseSlotDiffPublisherRequestHandler {

  private static final Logger LOGGER = Loggers.MULTI_CLUSTER_SRV_LOGGER;

  @Autowired private MultiClusterExecutorManager multiClusterExecutorManager;

  public MultiClusterSlotDiffPublisherRequestHandler() {
    super(LOGGER);
  }

  /**
   * specify executor for processor handler
   *
   * @return Executor
   */
  @Override
  public Executor getExecutor() {
    return multiClusterExecutorManager.getRemoteSlotSyncProcessorExecutor();
  }
}
