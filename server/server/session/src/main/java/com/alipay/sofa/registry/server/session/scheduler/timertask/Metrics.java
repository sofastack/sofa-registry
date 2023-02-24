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
package com.alipay.sofa.registry.server.session.scheduler.timertask;

import io.prometheus.client.Gauge;

public final class Metrics {
  private Metrics() {}

  static final Gauge PUB_SUM =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("pub_sum")
          .help("publisher cache num")
          .register();

  static final Gauge SUB_SUM =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("sub_sum")
          .help("subscriber cache num")
          .register();

  static final Gauge WAT_SUM =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("wat_sum")
          .help("watcher cache num")
          .register();

  static final Gauge CHANNEL_SUM =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("channel_sum")
          .help("channel cache num")
          .register();

  static final Gauge PUB_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("pub_total")
          .labelNames("instanceId", "group")
          .help("publisher cache num")
          .register();

  static final Gauge PUB_DATA_ID_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("pub_dataID_total")
          .labelNames("instanceId", "group")
          .help("publisher dataID cache num")
          .register();

  static final Gauge NOT_MULTI_SUB_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("not_multi_sub_total")
          .labelNames("instanceId", "group")
          .help("subscriber cache num")
          .register();

  static final Gauge MULTI_SUB_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("multi_sub_total")
          .labelNames("instanceId", "group")
          .help("subscriber cache num")
          .register();

  static final Gauge NOT_MULTI_SUB_DATA_ID_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("not_multi_sub_dataID_total")
          .labelNames("instanceId", "group")
          .help("subscriber dataID cache num")
          .register();

  static final Gauge MULTI_SUB_DATA_ID_GAUGE =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("multi_sub_dataID_total")
          .labelNames("instanceId", "group")
          .help("subscriber dataID cache num")
          .register();

  static final Gauge WAT_COUNTER =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("wat_total")
          .labelNames("instanceId", "group")
          .help("watcher cache num")
          .register();

  static final Gauge WAT_DATA_ID_COUNTER =
      Gauge.build()
          .namespace("session")
          .subsystem("cache")
          .name("wat_dataID_total")
          .labelNames("instanceId", "group")
          .help("watcher cache num")
          .register();
}
