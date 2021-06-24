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
package com.alipay.sofa.registry.server.session.push;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

public final class PushMetrics {
  private PushMetrics() {}

  static final class Fetch {
    static final Counter CHANGE_TASK_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("change_task_total")
            .help("change task")
            .register();

    static final Counter CHANGE_TASK_EXEC_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("change_task_exec_total")
            .help("change task exec")
            .register();
    static final Counter REGISTER_TASK_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("register_task_total")
            .help("register task")
            .register();
    static final Counter WATCH_TASK_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("watch_task_total")
            .help("register task")
            .register();
    static final Counter PUSH_EMPTY_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("empty_task_total")
            .help("empty task")
            .register();
    static final Counter PUSH_TEMP_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("temp_task_total")
            .help("temp task")
            .register();

    private static final Counter CACHE_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("fetch")
            .name("cache_total")
            .help(" cache")
            .labelNames("hit")
            .register();

    static final Counter.Child CACHE_HIT_COUNTER = CACHE_COUNTER.labels("Y");
    static final Counter.Child CACHE_MISS_COUNTER = CACHE_COUNTER.labels("N");
  }

  static final class Push {
    private static final Counter BUFFER_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("buffer_total")
            .help("buffer count")
            .labelNames("type")
            .register();
    static final Counter.Child BUFFER_REPLACE_COUNTER = BUFFER_COUNTER.labels("replace");
    static final Counter.Child BUFFER_NEW_COUNTER = BUFFER_COUNTER.labels("new");
    static final Counter.Child BUFFER_SKIP_COUNTER = BUFFER_COUNTER.labels("skip");

    static final Counter COMMIT_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("fire_commit_total")
            .help("commit task")
            .register();

    static final Counter PUSH_CLIENT_ING_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("push_client_ing_total")
            .help("pushing client task")
            .register();

    static final Counter PUSH_RETRY_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("retry_total")
            .help("retry count")
            .labelNames("reason")
            .register();
    private static final Histogram PUSH_DELAY_HISTOGRAM =
        Histogram.build()
            .linearBuckets(0, 1000, 30)
            .namespace("session")
            .subsystem("push")
            .name("push_delay")
            .help("push delay")
            .labelNames("cause", "status")
            .register();

    private static final Histogram.Child SUB_OK =
        PUSH_DELAY_HISTOGRAM.labels(PushType.Sub.name(), PushTrace.PushStatus.OK.name());
    private static final Histogram.Child REG_OK =
        PUSH_DELAY_HISTOGRAM.labels(PushType.Reg.name(), PushTrace.PushStatus.OK.name());

    static void observePushDelayHistogram(
        PushType pushType, long millis, PushTrace.PushStatus status) {
      // quick path
      if (status == PushTrace.PushStatus.OK) {
        if (pushType == PushType.Sub) {
          SUB_OK.observe(millis);
          return;
        }
        if (pushType == PushType.Reg) {
          REG_OK.observe(millis);
          return;
        }
      }
      PUSH_DELAY_HISTOGRAM.labels(pushType.name(), status.name()).observe(millis);
    }

    static final Counter PUSH_EMPTY_SKIP_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("push_empty_skip")
            .help("push empty skip count")
            .register();

    static final Counter PUSH_REG_SKIP_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("push_reg_skip")
            .help("push reg skip count")
            .register();
    static final Counter PUSH_REG_COMMIT_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("push")
            .name("push_reg_commit")
            .help("push reg commit count")
            .register();
  }
}
