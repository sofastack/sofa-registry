package com.alipay.sofa.registry.server.meta.monitor;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author chen.zhu
 * <p>
 * Mar 03, 2021
 */
public class PrometheusMetrics {

    public static final class SlotArrange {

        private static final Logger LOGGER = LoggerFactory.getLogger(SlotArrange.class);

        private static long start = -1L;

        private static final Histogram SLOT_ARRANGE_HISTOGRAM    = Histogram
                .build()
                .namespace("meta")
                .subsystem("slot_arrange")
                .name(
                        "leader_migrating_secs")
                .help(
                        "migrating in seconds.")
                .buckets(3, 5, 10, 20, 30,
                        60, 120, 180, 240)
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

        private static final Gauge DATA_HEART_BEAT_GUAGE = Gauge.build().namespace("meta")
                .subsystem("heartbeat")
                .name("data_heartbeat")
                .help("data heartbeat times")
                .labelNames("data_server")
                .register();

        private static final Gauge SESSION_HEART_BEAT_GUAGE = Gauge.build().namespace("meta")
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

        private static final Gauge     HIGH_LEADER_MIGRATE        = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_balancer")
                .name(
                        "high_leader_migrate")
                .help(
                        "high leader migrate gauge")
                .labelNames("from", "to", "slot")
                .register();

        private static final Gauge     HIGH_LEADER_UPGRADE        = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_balancer")
                .name(
                        "high_leader_upgrade")
                .help(
                        "high leader upgrade gauge")
                .labelNames("from", "to", "slot")
                .register();

        private static final Gauge     HIGH_FOLLOWER_BALANCE       = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_balancer")
                .name(
                        "high_follower_balance")
                .help(
                        "data to swap slot follower out")
                .labelNames("from", "to", "slot")
                .register();

        private static final Gauge     LOW_FOLLOWER_BALANCE        = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_balancer")
                .name(
                        "low_follower_balance")
                .help(
                        "data to swap slot follower in")
                .labelNames("from", "to", "slot")
                .register();

        private static final Gauge     LOW_LEADER_BALANCE       = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_balancer")
                .name(
                        "low_leader_balance")
                .help(
                        "data to swap slot leader out")
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

        private static final Gauge     LEADER_ASSIGN_GAUGE           = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_assigner")
                .name(
                        "assign_leader")
                .help("leader assign")
                .labelNames("leader", "slot")
                .register();

        private static final Gauge     FOLLOWER_ASSIGN_GAUGE         = Gauge
                .build()
                .namespace("meta")
                .subsystem("slot_assigner")
                .name(
                        "assign_follower")
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

        private static final Gauge     LEADER_ASSIGN_GAUGE           = Gauge
                .build()
                .namespace("meta")
                .subsystem("data")
                .name(
                        "leader_slot_number")
                .help("leader slot number")
                .labelNames("data_server")
                .register();

        private static final Gauge     FOLLOWER_ASSIGN_GAUGE         = Gauge
                .build()
                .namespace("meta")
                .subsystem("data")
                .name(
                        "follower_slot_number")
                .help("follower slot number")
                .labelNames("data_server")
                .register();

        public static void setLeaderNumbers(String dataServer, int leaderNum) {
            try {
                LEADER_ASSIGN_GAUGE.labels(dataServer).set(leaderNum);
            } catch (Throwable throwable) {
                LOGGER.error("[setLeaderNumbers]", throwable);
            }
        }

        public static void setFollowerNumbers(String dataServer, int followerNum) {
            try {
                FOLLOWER_ASSIGN_GAUGE.labels(dataServer).set(followerNum);
            } catch (Throwable throwable) {
                LOGGER.error("[setFollowerNumbers]", throwable);
            }
        }
    }

}
