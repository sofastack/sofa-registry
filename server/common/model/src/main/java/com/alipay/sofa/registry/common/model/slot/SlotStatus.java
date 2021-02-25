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
package com.alipay.sofa.registry.common.model.slot;

import java.io.Serializable;

/**
 * @author chen.zhu
 * <p>
 * Feb 24, 2021
 */
public class SlotStatus implements Serializable {

    private final int    slotId;

    private final long   slotLeaderEpoch;

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
        SlotStatus slotStatus = new SlotStatus(slotAccess.getSlotId(),
            slotAccess.getSlotLeaderEpoch());
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
