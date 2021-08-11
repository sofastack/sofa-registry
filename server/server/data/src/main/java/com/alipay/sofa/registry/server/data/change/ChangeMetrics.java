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
package com.alipay.sofa.registry.server.data.change;

import io.prometheus.client.Counter;

public final class ChangeMetrics {
  private ChangeMetrics() {}

  private static final Counter CHANGE_COUNTER_ =
      Counter.build()
          .namespace("data")
          .subsystem("change")
          .name("notify_total")
          .help("notify session")
          .labelNames("type")
          .register();

  static final Counter.Child CHANGE_COMMIT_COUNTER = CHANGE_COUNTER_.labels("commit");
  // retry change
  static final Counter.Child CHANGE_RETRY_COUNTER = CHANGE_COUNTER_.labels("retry");
  // skip change
  static final Counter.Child CHANGE_SKIP_COUNTER = CHANGE_COUNTER_.labels("skip");
  // skip same pub value
  public static final Counter.Child SKIP_SAME_VALUE_COUNTER =
      CHANGE_COUNTER_.labels("skipSameValue");

  static final Counter.Child CHANGE_FAIL_COUNTER = CHANGE_COUNTER_.labels("fail");
  static final Counter.Child CHANGE_SUCCESS_COUNTER = CHANGE_COUNTER_.labels("success");

  // should not use
  private static final Counter CHANGE_TEMP_COUNTER_ =
      Counter.build()
          .namespace("data")
          .subsystem("change")
          .name("notify_temp_total")
          .help("notify temp session")
          .labelNames("type")
          .register();

  static final Counter.Child CHANGETEMP_SKIP_COUNTER = CHANGE_TEMP_COUNTER_.labels("skip");
  static final Counter.Child CHANGETEMP_COMMIT_COUNTER = CHANGE_TEMP_COUNTER_.labels("commit");
  static final Counter.Child CHANGETEMP_SUCCESS_COUNTER = CHANGE_TEMP_COUNTER_.labels("success");
  static final Counter.Child CHANGETEMP_FAIL_COUNTER = CHANGE_TEMP_COUNTER_.labels("fail");
}
