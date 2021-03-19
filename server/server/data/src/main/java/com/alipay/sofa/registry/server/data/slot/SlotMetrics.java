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
package com.alipay.sofa.registry.server.data.slot;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public final class SlotMetrics {
  private SlotMetrics() {}

  static final class Manager {
    private static final Counter LEADER_UPDATE_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("slot")
            .name("leader_update_total")
            .help("count leader update")
            .register();

    private static final Gauge LEADER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("data")
            .subsystem("slot")
            .name("leader_assign_total")
            .help("leader assign")
            .register();

    private static final Gauge FOLLOWER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("data")
            .subsystem("slot")
            .name("follower_assign_total")
            .help("follower assign")
            .register();

    private static final Gauge LEADER_MIGRATING_GAUGE =
        Gauge.build()
            .namespace("data")
            .subsystem("slot")
            .name("leader_migrating_total")
            .help("count leader is migrating")
            .labelNames("slot")
            .register();

    private static final Counter LEADER_MIGRATING_FAIL_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("slot")
            .name("leader_migrating_fail_total")
            .help("count leader migrating fail")
            .labelNames("slot", "session")
            .register();

    private static final Histogram LEADER_MIGRATING_HISTOGRAM =
        Histogram.build()
            .namespace("data")
            .subsystem("slot")
            .name("leader_migrating_secs")
            .help("migrating in seconds.")
            .labelNames("slot")
            .buckets(3, 5, 10, 20, 30, 60, 120, 180, 240)
            .register();

    static void observeLeaderUpdateCounter() {
      LEADER_UPDATE_COUNTER.inc();
    }

    static void observeLeaderAssignGauge(int num) {
      LEADER_ASSIGN_GAUGE.set(num);
    }

    static void observeFollowerAssignGauge(int num) {
      FOLLOWER_ASSIGN_GAUGE.set(num);
    }

    static void observeLeaderMigratingStart(int slotId) {
      LEADER_MIGRATING_GAUGE.labels(String.valueOf(slotId)).set(1);
    }

    static void observeLeaderMigratingFinish(int slotId) {
      LEADER_MIGRATING_GAUGE.labels(String.valueOf(slotId)).set(0);
    }

    static void observeLeaderMigratingFail(int slotId, String sessionIp) {
      LEADER_MIGRATING_FAIL_COUNTER.labels(String.valueOf(slotId), sessionIp).inc();
    }

    static void observeLeaderMigratingHistogram(int slotId, long millis) {
      // seconds
      LEADER_MIGRATING_HISTOGRAM.labels(String.valueOf(slotId)).observe(millis / 1000d);
    }
  }

  static final class SyncSession {
    private static final Counter SYNC_SESSION_ID_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("session_id_total")
            .help("count sync session dataInfoIds")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_SESSION_ID_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("session_id_num_total")
            .help("count sync session dataInfoId's num")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_SESSION_PUB_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("session_pub_total")
            .help("count sync session pubs")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_SESSION_PUB_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("session_pub_num_total")
            .help("count sync session pub's num")
            .labelNames("slot")
            .register();

    static void observeSyncSessionId(int slotId, int idNum) {
      final String str = String.valueOf(slotId);
      SYNC_SESSION_ID_COUNTER.labels(str).inc();
      SYNC_SESSION_ID_NUM_COUNTER.labels(str).inc(idNum);
    }

    static void observeSyncSessionPub(int slotId, int pubNum) {
      final String str = String.valueOf(slotId);
      SYNC_SESSION_PUB_COUNTER.labels(str).inc();
      SYNC_SESSION_PUB_NUM_COUNTER.labels(str).inc(pubNum);
    }
  }

  static final class SyncLeader {
    private static final Counter SYNC_LEADER_ID_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("leader_id_total")
            .help("count sync leader dataInfoIds")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_LEADER_ID_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("leader_id_num_total")
            .help("count sync leader dataInfoId's num")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_LEADER_PUB_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("leader_pub_total")
            .help("count sync leader pubs")
            .labelNames("slot")
            .register();

    private static final Counter SYNC_LEADER_PUB_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("leader_pub_num_total")
            .help("count sync leader pub's num")
            .labelNames("slot")
            .register();

    static void observeSyncLeaderId(int slotId, int idNum) {
      final String str = String.valueOf(slotId);
      SYNC_LEADER_ID_COUNTER.labels(str).inc();
      SYNC_LEADER_ID_NUM_COUNTER.labels(str).inc(idNum);
    }

    static void observeSyncLeaderPub(int slotId, int pubNum) {
      final String str = String.valueOf(slotId);
      SYNC_LEADER_PUB_COUNTER.labels(str).inc();
      SYNC_LEADER_PUB_NUM_COUNTER.labels(str).inc(pubNum);
    }
  }
}
