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
package com.alipay.sofa.registry.server.meta.monitor;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author chen.zhu
 *     <p>Mar 03, 2021
 */
public class Metrics {

  public static final class SlotArrange {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotArrange.class);

    private static long start = -1L;

    private static final Histogram SLOT_ARRANGE_HISTOGRAM =
        Histogram.build()
            .namespace("meta")
            .subsystem("slot_arrange")
            .name("leader_migrating_secs")
            .help("migrating in seconds.")
            .buckets(3, 5, 10, 20, 30, 60, 120, 180, 240)
            .register();

    public static void begin() {
      start = System.currentTimeMillis();
    }

    public static void end() {
      try {
        SLOT_ARRANGE_HISTOGRAM.observe(System.currentTimeMillis() - start);
      } catch (Throwable throwable) {
        LOGGER.error("[end]", throwable);
      }
    }
  }

  public static final class Heartbeat {

    private static final Logger LOGGER = LoggerFactory.getLogger(Heartbeat.class);

    private static final Gauge DATA_HEART_BEAT_GUAGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("heartbeat")
            .name("data_heartbeat")
            .help("data heartbeat times")
            .labelNames("data_server")
            .register();

    private static final Gauge SESSION_HEART_BEAT_GUAGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("heartbeat")
            .name("session_heartbeat")
            .help("session heartbeat times")
            .labelNames("session_server")
            .register();

    public static void onDataHeartbeat(String dataServer) {
      try {
        DATA_HEART_BEAT_GUAGE.labels(dataServer).set(1);
      } catch (Throwable th) {
        LOGGER.error("[onDataHeartbeat]", th);
      }
    }

    public static void onDataEvict(String dataServer) {
      try {
        DATA_HEART_BEAT_GUAGE.labels(dataServer).set(0);
      } catch (Throwable th) {
        LOGGER.error("[onDataEvict]", th);
      }
    }

    public static void onSessionHeartbeat(String session) {
      try {
        SESSION_HEART_BEAT_GUAGE.labels(session).set(1);
      } catch (Throwable th) {
        LOGGER.error("[onSessionHeartbeat]", th);
      }
    }

    public static void onSessionEvict(String session) {
      try {
        SESSION_HEART_BEAT_GUAGE.labels(session).set(0);
      } catch (Throwable th) {
        LOGGER.error("[onSessionEvict]", th);
      }
    }
  }

  public static final class SlotBalance {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotBalance.class);

    private static final Gauge HIGH_LEADER_MIGRATE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_balancer")
            .name("high_leader_migrate")
            .help("high leader migrate gauge")
            .labelNames("from", "to", "slot")
            .register();

    private static final Gauge HIGH_LEADER_UPGRADE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_balancer")
            .name("high_leader_upgrade")
            .help("high leader upgrade gauge")
            .labelNames("from", "to", "slot")
            .register();

    private static final Gauge HIGH_FOLLOWER_BALANCE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_balancer")
            .name("high_follower_balance")
            .help("data to swap slot follower out")
            .labelNames("from", "to", "slot")
            .register();

    private static final Gauge LOW_FOLLOWER_BALANCE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_balancer")
            .name("low_follower_balance")
            .help("data to swap slot follower in")
            .labelNames("from", "to", "slot")
            .register();

    private static final Gauge LOW_LEADER_BALANCE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_balancer")
            .name("low_leader_balance")
            .help("data to swap slot leader out")
            .labelNames("from", "to", "slot")
            .register();

    public static void onLeaderUpgrade(String from, String to, int slotId) {
      try {
        HIGH_LEADER_MIGRATE.labels(from, to, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onLeaderUpgrade]", throwable);
      }
    }

    public static void onLeaderMigrate(String from, String to, int slotId) {
      try {
        HIGH_LEADER_UPGRADE.labels(from, to, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onLeaderMigrate]", throwable);
      }
    }

    public static void onHighFollowerMigrate(String from, String to, int slotId) {
      try {
        HIGH_FOLLOWER_BALANCE.labels(from, to, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onHighFollowerMigrate]", throwable);
      }
    }

    public static void onLowFollowerMigrate(String from, String to, int slotId) {
      try {
        LOW_FOLLOWER_BALANCE.labels(from, to, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onLowFollowerMigrate]", throwable);
      }
    }

    public static void onLowLeaderReplace(String from, String to, int slotId) {
      try {
        LOW_LEADER_BALANCE.labels(from, to, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onLowLeaderReplace]", throwable);
      }
    }
  }

  public static final class SlotAssign {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotAssign.class);

    private static final Gauge LEADER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_assigner")
            .name("assign_leader")
            .help("leader assign")
            .labelNames("leader", "slot")
            .register();

    private static final Gauge FOLLOWER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("slot_assigner")
            .name("assign_follower")
            .help("follower assign")
            .labelNames("follower", "slot")
            .register();

    public static void onSlotLeaderAssign(String dataServer, int slotId) {
      try {
        LEADER_ASSIGN_GAUGE.labels(dataServer, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onSlotLeaderAssign]", throwable);
      }
    }

    public static void onSlotFollowerAssign(String dataServer, int slotId) {
      try {
        FOLLOWER_ASSIGN_GAUGE.labels(dataServer, String.valueOf(slotId)).inc();
      } catch (Throwable throwable) {
        LOGGER.error("[onSlotFollowerAssign]", throwable);
      }
    }
  }

  public static final class DataSlot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSlot.class);

    private static final Gauge LEADER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("data")
            .name("leader_slot_number")
            .help("leader slot number")
            .labelNames("data_server")
            .register();

    private static final Gauge FOLLOWER_ASSIGN_GAUGE =
        Gauge.build()
            .namespace("meta")
            .subsystem("data")
            .name("follower_slot_number")
            .help("follower slot number")
            .labelNames("data_server")
            .register();

    private static final Gauge DATA_SLOT_TABLE_LAG_TIMES =
        Gauge.build()
            .namespace("meta")
            .subsystem("data")
            .name("data_server_slot_lag")
            .help("data server slot is behind meta")
            .labelNames("data_server")
            .register();

    private static final Gauge DATA_SLOT_GREATER_THAN_META =
        Gauge.build()
            .namespace("meta")
            .subsystem("data")
            .name("data_slot_greater_than_meta")
            .help("data server slot epoch greater than meta")
            .labelNames("data_server", "slotId")
            .register();

    private static final Gauge DATA_SLOT_NOT_EQUALS_META =
        Gauge.build()
            .namespace("meta")
            .subsystem("data")
            .name("data_slot_not_equals_meta")
            .help("data server slot is not as expected as meta")
            .labelNames("data_server")
            .register();

    public static void setLeaderNumbers(String dataServer, int leaderNum) {
      try {
        LEADER_ASSIGN_GAUGE.labels(dataServer).set(leaderNum);
      } catch (Throwable throwable) {
        LOGGER.error("[setLeaderNumbers]", throwable);
      }
    }

    public static void clearLeaderNumbers() {
      LEADER_ASSIGN_GAUGE.clear();
    }

    public static void setFollowerNumbers(String dataServer, int followerNum) {
      try {
        FOLLOWER_ASSIGN_GAUGE.labels(dataServer).set(followerNum);
      } catch (Throwable throwable) {
        LOGGER.error("[setFollowerNumbers]", throwable);
      }
    }

    public static void clearFollowerNumbers() {
      FOLLOWER_ASSIGN_GAUGE.clear();
    }

    public static void setDataServerSlotLagTimes(String dataServer, int times) {
      try {
        DATA_SLOT_TABLE_LAG_TIMES.labels(dataServer).set(times);
      } catch (Throwable throwable) {
        LOGGER.error("[setDataServerSlotLagTimes]", throwable);
      }
    }

    public static long getDataServerSlotLagTimes(String dataServer) {
      try {
        return (long) DATA_SLOT_TABLE_LAG_TIMES.labels(dataServer).get();
      } catch (Throwable throwable) {
        LOGGER.error("[setDataServerSlotLagTimes]", throwable);
      }
      return 0;
    }

    public static void setDataSlotGreaterThanMeta(String dataServer, int slotId) {
      try {
        DATA_SLOT_GREATER_THAN_META.labels(dataServer, String.valueOf(slotId)).set(1);
      } catch (Throwable throwable) {
        LOGGER.error("[setDataServerSlotLagTimes]", throwable);
      }
    }

    public static void setDataReportNotStable(String dataServer, int slotId) {
      try {
        DATA_SLOT_NOT_EQUALS_META.labels(dataServer, String.valueOf(slotId)).set(1);
      } catch (Throwable throwable) {
        LOGGER.error("[setDataServerSlotLagTimes]", throwable);
      }
    }
  }
}
