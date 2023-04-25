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
            .linearBuckets(0, 1000, 15)
            .namespace("session")
            .subsystem("push")
            .name("push_delay")
            .help("push delay")
            .labelNames("datacenter", "cause", "status")
            .register();
    private static final Histogram PUSH_DELAY_HISTOGRAM_SUB_OK =
        Histogram.build()
            .linearBuckets(0, 1000, 15)
            .namespace("session")
            .subsystem("push_sub_ok")
            .name("push_delay")
            .help("push delay")
            .labelNames("datacenter", "cause", "status")
            .register();

    private static final Histogram PUSH_DELAY_HISTOGRAM_SUB_FAIL =
        Histogram.build()
            .linearBuckets(0, 1000, 15)
            .namespace("session")
            .subsystem("push_sub_fail")
            .name("push_delay")
            .help("push delay")
            .labelNames("datacenter", "cause", "status")
            .register();

    private static final Histogram PUSH_DELAY_HISTOGRAM_REG_OK =
        Histogram.build()
            .linearBuckets(0, 1000, 15)
            .namespace("session")
            .subsystem("push_reg_ok")
            .name("push_delay")
            .help("push delay")
            .labelNames("datacenter", "cause", "status")
            .register();

    private static final Histogram PUSH_DELAY_HISTOGRAM_REG_FAIL =
        Histogram.build()
            .linearBuckets(0, 1000, 15)
            .namespace("session")
            .subsystem("push_reg_fail")
            .name("push_delay")
            .help("push delay")
            .labelNames("datacenter", "cause", "status")
            .register();

    static void observePushDelayHistogram(
        String dataCenter, PushType pushType, long millis, PushTrace.PushStatus status) {
      // quick path
      if (status == PushTrace.PushStatus.OK) {
        if (pushType == PushType.Sub) {
          PUSH_DELAY_HISTOGRAM_SUB_OK
              .labels(dataCenter, PushType.Sub.name(), PushTrace.PushStatus.OK.name())
              .observe(millis);
          PUSH_DELAY_HISTOGRAM
              .labels(dataCenter, PushType.Sub.name(), PushTrace.PushStatus.OK.name())
              .observe(millis);

          return;
        }
        if (pushType == PushType.Reg) {
          PUSH_DELAY_HISTOGRAM_REG_OK
              .labels(dataCenter, PushType.Reg.name(), PushTrace.PushStatus.OK.name())
              .observe(millis);
          PUSH_DELAY_HISTOGRAM
              .labels(dataCenter, PushType.Reg.name(), PushTrace.PushStatus.OK.name())
              .observe(millis);

          return;
        }
      } else {

        PUSH_DELAY_HISTOGRAM.labels(dataCenter, pushType.name(), status.name()).observe(millis);

        if (pushType == PushType.Sub) {
          PUSH_DELAY_HISTOGRAM_SUB_FAIL
              .labels(dataCenter, PushType.Sub.name(), PushTrace.PushStatus.Fail.name())
              .observe(millis);

          return;
        }
        if (pushType == PushType.Reg) {
          PUSH_DELAY_HISTOGRAM_REG_FAIL
              .labels(dataCenter, PushType.Reg.name(), PushTrace.PushStatus.Fail.name())
              .observe(millis);

          return;
        }
      }
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
