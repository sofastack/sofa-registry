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
package com.alipay.sofa.registry.server.data.multi.cluster.slot;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotMetrics.java, v 0.1 2022年05月09日 16:30 xiaojian.xj Exp $
 */
public class MultiClusterSlotMetrics {
  private MultiClusterSlotMetrics() {}

  private static final Gauge REMOTE_LEADER_ASSIGN_GAUGE =
      Gauge.build()
          .namespace("data")
          .subsystem("slot")
          .name("remote_leader_assign_total")
          .help("leader assign")
          .labelNames("dataCenter")
          .register();

  private static final Histogram REMOTE_LEADER_SYNCING_HISTOGRAM =
      Histogram.build()
          .namespace("data")
          .subsystem("slot")
          .name("remote_leader_syncing_secs")
          .help("syncing in seconds.")
          .labelNames("dataCenter", "slot")
          .buckets(3, 5, 10, 20, 30, 60, 120, 180, 240)
          .register();

  private static final Gauge REMOTE_LEADER_SYNCING_GAUGE =
      Gauge.build()
          .namespace("data")
          .subsystem("slot")
          .name("remote_leader_syncing_total")
          .help("count remote leader is syncing")
          .labelNames("dataCenter", "slot")
          .register();

  public static final class RemoteSyncLeader {
    private static final Counter REMOTE_SYNC_LEADER_ID_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("remote_leader_id_total")
            .help("count remote sync leader dataInfoIds")
            .labelNames("dataCenter", "slot")
            .register();

    private static final Counter REMOTE_SYNC_LEADER_ID_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("remote_leader_id_num_total")
            .help("count remote sync leader dataInfoId's num")
            .labelNames("dataCenter", "slot")
            .register();

    private static final Counter REMOTE_SYNC_LEADER_PUB_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("remote_leader_pub_total")
            .help("count remote sync leader pubs")
            .labelNames("dataCenter", "slot")
            .register();

    private static final Counter REMOTE_SYNC_LEADER_PUB_NUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("sync")
            .name("remote_leader_pub_num_total")
            .help("count remote sync leader pub's num")
            .labelNames("dataCenter", "slot")
            .register();

    public static void observeSyncLeaderId(String dataCenter, int slotId, int idNum) {
      final String str = String.valueOf(slotId);
      REMOTE_SYNC_LEADER_ID_COUNTER.labels(dataCenter, str).inc();
      REMOTE_SYNC_LEADER_ID_NUM_COUNTER.labels(dataCenter, str).inc(idNum);
    }

    public static void observeSyncLeaderPub(String dataCenter, int slotId, int pubNum) {
      final String str = String.valueOf(slotId);
      REMOTE_SYNC_LEADER_PUB_COUNTER.labels(dataCenter, str).inc();
      REMOTE_SYNC_LEADER_PUB_NUM_COUNTER.labels(dataCenter, str).inc(pubNum);
    }
  }

  static void observeRemoteLeaderAssignGauge(String dataCenter, int num) {
    REMOTE_LEADER_ASSIGN_GAUGE.labels(dataCenter).set(num);
  }

  static void observeRemoteLeaderSyncingStart(String dataCenter, int slotId) {
    REMOTE_LEADER_SYNCING_GAUGE.labels(dataCenter, String.valueOf(slotId)).set(1);
  }

  static void observeRemoteLeaderSyncingFinish(String dataCenter, int slotId) {
    REMOTE_LEADER_SYNCING_GAUGE.labels(dataCenter, String.valueOf(slotId)).set(0);
  }

  static void observeRemoteLeaderSyncingHistogram(String dataCenter, int slotId, long millis) {
    REMOTE_LEADER_SYNCING_HISTOGRAM
        .labels(dataCenter, String.valueOf(slotId))
        .observe(millis / 1000d);
  }

  private static final Counter SYNC_COUNTER =
      Counter.build()
          .namespace("data")
          .subsystem("access")
          .name("sync_total")
          .labelNames("remote", "type")
          .help("sync data access num")
          .register();

  public static void syncAccess(String remote, SyncType syncType) {
    if (syncType == SyncType.SYNC_ALL) {
      SYNC_COUNTER.labels(remote, "ALL").inc();
    } else if (syncType == SyncType.SYNC_DELTA) {
      SYNC_COUNTER.labels(remote, "DELTA").inc();
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
