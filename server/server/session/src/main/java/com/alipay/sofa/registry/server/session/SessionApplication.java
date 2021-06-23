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
package com.alipay.sofa.registry.server.session;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zhuoyu.sjw
 * @version $Id: SessionApplication.java, v 0.1 2017-11-13 20:19 zhuoyu.sjw Exp $$
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
public class SessionApplication {

  private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL-ERROR");

  public static void main(String[] args) {
    // setup DefaultUncaughtExceptionHandler
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) ->
            CRITICAL_LOGGER.safeError(
                "UncaughtException in Thread({}): {}", t.getName(), e.getMessage(), e));

    SpringApplication.run(SessionApplication.class, args);
  }
}
