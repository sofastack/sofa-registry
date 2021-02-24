package com.alipay.sofa.registry.common.model.slot;

/**
 * @author chen.zhu
 * <p>
 * Feb 24, 2021
 */
public class SlotStatus {

    private final int slotId;

    private final long slotLeaderEpoch;

    private LeaderStatus leaderStatus = LeaderStatus.INIT;

    /**
     * Constructor.
     *
     * @param slotId          the slot id
     * @param slotLeaderEpoch the slot leader epoch
     */
    public SlotStatus(int slotId, long slotLeaderEpoch) {
        this.slotId = slotId;
        this.slotLeaderEpoch = slotLeaderEpoch;
    }


    /**
     * Constructor.
     *
     * @param slotId          the slot id
     * @param slotLeaderEpoch the slot leader epoch
     * @param leaderStatus    the leader status
     */
    public SlotStatus(int slotId, long slotLeaderEpoch, LeaderStatus leaderStatus) {
        this.slotId = slotId;
        this.slotLeaderEpoch = slotLeaderEpoch;
        this.leaderStatus = leaderStatus;
    }

    /**
     * Gets get slot id.
     *
     * @return the get slot id
     */
    public int getSlotId() {
        return slotId;
    }

    /**
     * Gets get slot leader epoch.
     *
     * @return the get slot leader epoch
     */
    public long getSlotLeaderEpoch() {
        return slotLeaderEpoch;
    }

    /**
     * Gets get leader status.
     *
     * @return the get leader status
     */
    public LeaderStatus getLeaderStatus() {
        return leaderStatus;
    }

    /**
     * From slot status.
     *
     * @param slotAccess the slot access
     * @return the slot status
     */
    public static SlotStatus from(SlotAccess slotAccess) {
        SlotStatus slotStatus = new SlotStatus(slotAccess.getSlotId(), slotAccess.getSlotLeaderEpoch());
        if (slotAccess.getStatus() != null) {
            switch (slotAccess.getStatus()) {
                case Accept:
                    slotStatus.leaderStatus = LeaderStatus.HEALTHY;
                default:
                    slotStatus.leaderStatus = LeaderStatus.UNHEALTHY;
            }
        }
        return slotStatus;
    }

    public static enum LeaderStatus {
        INIT, HEALTHY, UNHEALTHY;

        public boolean isHealthy() {
            return this == HEALTHY;
        }
    }

}
