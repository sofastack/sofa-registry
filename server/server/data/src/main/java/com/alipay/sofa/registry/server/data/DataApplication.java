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
package com.alipay.sofa.registry.server.data;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.EnableDataServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author zhuoyu.sjw
 * @version $Id: DataApplication.java, v 0.1 2017-11-13 19:04 zhuoyu.sjw Exp $$
 */
@EnableDataServer
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DataApplication {

  private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL-ERROR");

  public static void main(String[] args) {
    // setup DefaultUncaughtExceptionHandler
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) -> CRITICAL_LOGGER.safeError("UncaughtException in Thread {}", t.getName(), e));
    SpringApplication.run(DataApplication.class, args);
  }
}
