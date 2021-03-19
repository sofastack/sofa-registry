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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerInitializerConfiguration.java, v 0.1 2018-01-16 11:27 shangyu.wh Exp $
 */
public class MetaServerInitializerConfiguration implements SmartLifecycle {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MetaServerInitializerConfiguration.class);

  private AtomicBoolean running = new AtomicBoolean(false);

  @Autowired private MetaServerBootstrap metaServerBootstrap;

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void start() {

    try {
      metaServerBootstrap.start();
      LOGGER.info("Started MetaServer");

      MetaServerInitializerConfiguration.this.running.set(true);
    } catch (Exception ex) {
      MetaServerInitializerConfiguration.this.running.set(false);
      LOGGER.error("Could not initialize Meta server!", ex);
    }
  }

  @Override
  public void stop() {
    this.running.set(false);
    metaServerBootstrap.destroy();
  }

  @Override
  public boolean isRunning() {
    return this.running.get();
  }

  @Override
  public int getPhase() {
    return 0;
  }

  @Override
  public void stop(Runnable callback) {
    callback.run();
    this.running.set(false);
  }
}
