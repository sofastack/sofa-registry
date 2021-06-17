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
package com.alipay.sofa.registry.server.data.bootstrap;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

/**
 * @author qian.lqlq
 * @version $Id: DataServerInitializer.java, v 0.1 2018年01月04日 11:08 qian.lqlq Exp $
 */
public class DataServerInitializer implements SmartLifecycle {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataServerInitializer.class);

  @Autowired private DataServerBootstrap dataServerBootstrap;

  private volatile boolean isRunning;

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    runnable.run();
    this.isRunning = false;
  }

  @Override
  public void start() {
    try {
      dataServerBootstrap.start();
      this.isRunning = true;
    } catch (Throwable ex) {
      this.isRunning = false;
      LOGGER.error("Could not initalized Data server", ex);
      Runtime.getRuntime().halt(-1);
    }
  }

  @Override
  public void stop() {
    this.isRunning = false;
  }

  @Override
  public boolean isRunning() {
    return this.isRunning;
  }

  @Override
  public int getPhase() {
    return 0;
  }
}
