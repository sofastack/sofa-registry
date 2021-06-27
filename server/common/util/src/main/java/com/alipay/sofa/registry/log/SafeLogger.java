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
package com.alipay.sofa.registry.log;

import com.alipay.sofa.registry.metrics.CounterFunc;

public class SafeLogger {

  final CounterFunc COUNTER =
      CounterFunc.build()
          .namespace("safelogger")
          .name("critical")
          .labelNames("type")
          .help("sofa logger critical error")
          .create()
          .register();

  long oom_count = 0;
  long unknown_count = 0;
  private static final SafeLogger instance = new SafeLogger();

  private SafeLogger() {
    COUNTER.labels("oom").func(() -> this.oom_count);
    COUNTER.labels("unknown").func(() -> this.unknown_count);
  }

  public static SafeLogger getInstance() {
    return instance;
  }

  public void handleExp(Throwable e) {
    try {
      if (e instanceof OutOfMemoryError) {
        oom_count++;
        return;
      }
      unknown_count++;
    } catch (Throwable ignored) {
      // do nothing to avoid cause exception
    }
  }
}
