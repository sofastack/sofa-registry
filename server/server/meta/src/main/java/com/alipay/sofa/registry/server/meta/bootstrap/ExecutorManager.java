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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author xiaojian.xj
 * @version $Id: ExecutorManager.java, v 0.1 2021年04月20日 14:15 xiaojian.xj Exp $
 */
public class ExecutorManager {

  private final ScheduledThreadPoolExecutor scheduler;

  public ExecutorManager(MetaServerConfig metaServerConfig) {
    scheduler =
        new ScheduledThreadPoolExecutor(
            metaServerConfig.getMetaSchedulerPoolSize(), new NamedThreadFactory("MetaScheduler"));
  }

  public void startScheduler() {}

  public void stopScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }
}
