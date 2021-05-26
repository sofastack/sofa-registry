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

import com.alipay.sofa.registry.trace.TraceID;

public final class MDC {
  private static final String KEY_TRACE_ID = "traceID";
  private static final String KEY_ADDRESS = "address";

  private MDC() {}

  public static void startTraceRequest(String address) {
    TraceID traceID = TraceID.newTraceID();
    put(KEY_TRACE_ID, traceID.toString());
    put(KEY_ADDRESS, address);
  }

  public static void finishTraceRequest() {
    org.slf4j.MDC.remove(KEY_TRACE_ID);
    org.slf4j.MDC.remove(KEY_ADDRESS);
  }

  public static void put(String key, String val) {
    org.slf4j.MDC.put(key, val);
  }

  public static void clear() {
    org.slf4j.MDC.clear();
  }
}
