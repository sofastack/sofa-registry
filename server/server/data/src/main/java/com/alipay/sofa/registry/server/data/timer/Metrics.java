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
package com.alipay.sofa.registry.server.data.timer;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public final class Metrics {
  private Metrics() {}

  static final Gauge PUB_GAUGE =
      Gauge.build()
          .namespace("data")
          .subsystem("cache")
          .name("pub_total")
          .labelNames("dataCenter", "remote", "instanceId", "group")
          .help("publisher cache num")
          .register();

  static final Gauge PUB_DATA_ID_GAUGE =
      Gauge.build()
          .namespace("data")
          .subsystem("cache")
          .name("pub_dataID_total")
          .labelNames("dataCenter", "remote", "instanceId", "group")
          .help("publisher dataID cache num")
          .register();

  private static final Counter SYNC_COUNTER =
      Counter.build()
          .namespace("data")
          .subsystem("access")
          .name("sync_total")
          .labelNames("type")
          .help("sync data access num")
          .register();
  static final Counter.Child SYNC_ALL = SYNC_COUNTER.labels("ALL");
  static final Counter.Child SYNC_DELTA = SYNC_COUNTER.labels("DELTA");

  public static void syncAccess(SyncType syncType) {
    if (syncType == SyncType.SYNC_ALL) {
      SYNC_ALL.inc();
    } else if (syncType == SyncType.SYNC_DELTA) {
      SYNC_DELTA.inc();
    } else {
      throw new IllegalArgumentException("illegal sync type: " + syncType);
    }
  }

  public enum SyncType {
    SYNC_ALL,
    SYNC_DELTA,
    ;
  }
}
