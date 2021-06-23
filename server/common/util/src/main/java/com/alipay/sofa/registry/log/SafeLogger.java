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

import io.prometheus.client.Counter;

public class SafeLogger {

  static final Counter COUNTER =
      Counter.build()
          .namespace("safelogger")
          .name("critical")
          .labelNames("type")
          .help("sofa logger critical error")
          .create()
          .register();
  static final Counter.Child OOM_COUNTER = COUNTER.labels("oom");
  static final Counter.Child UNKNOWN_COUNTER = COUNTER.labels("unknown");

  private static final SafeLogger instance = new SafeLogger();

  public static SafeLogger getInstance() {
    return instance;
  }

  public synchronized void oomErr() {
    OOM_COUNTER.inc();
  }

  public synchronized void unknownErr() {
    UNKNOWN_COUNTER.inc();
  }

  public void wrap(UnsafeLog l) {
    try {
      l.logging();
    } catch (OutOfMemoryError e) {
      oomErr();
    } catch (Throwable e) {
      unknownErr();
    }
  }

  interface UnsafeLog {
    void logging();
  }
}
