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
package com.alipay.sofa.registry.server.session.bootstrap;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

/**
 * SmartLifecycle for SessionServerBootstrap
 *
 * @author shangyu.wh
 * @version $Id: SessionServerInitializerConfiguration.java, v 0.1 2017-11-14 11:41 synex Exp $
 */
public class SessionServerInitializer implements SmartLifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionServerInitializer.class);

  @Autowired private SessionServerBootstrap sessionServerBootstrap;

  private AtomicBoolean running = new AtomicBoolean(false);

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void start() {
    try {
      sessionServerBootstrap.start();
      LOGGER.info("Started SessionServer");

      SessionServerInitializer.this.running.set(true);
    } catch (Throwable ex) {
      SessionServerInitializer.this.running.set(false);
      LOGGER.error("Could not initialize Session server!", ex);
      Runtime.getRuntime().halt(-1);
    }
  }

  @Override
  public void stop() {
    this.running.set(false);
    sessionServerBootstrap.destroy();
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
