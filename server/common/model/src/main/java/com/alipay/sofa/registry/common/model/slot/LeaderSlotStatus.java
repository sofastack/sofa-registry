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
 *     <p>Mar 01, 2021
 */
public class LeaderSlotStatus extends BaseSlotStatus implements Serializable {

  private final LeaderStatus leaderStatus;

  /**
   * Constructor.
   *
   * @param slotId the slot id
   * @param slotLeaderEpoch the slot leader epoch
   * @param server the server
   * @param leaderStatus the leader status
   */
  public LeaderSlotStatus(
      int slotId, long slotLeaderEpoch, String server, LeaderStatus leaderStatus) {
    super(slotId, slotLeaderEpoch, Slot.Role.Leader, server);
    this.leaderStatus = leaderStatus;
  }

  /**
   * Gets get leader status.
   *
   * @return the get leader status
   */
  public LeaderStatus getLeaderStatus() {
    return leaderStatus;
  }

  @Override
  public String toString() {
    return "LeaderSlotStatus{"
        + "leaderStatus="
        + leaderStatus
        + ", slotId="
        + slotId
        + ", slotLeaderEpoch="
        + slotLeaderEpoch
        + ", role="
        + role
        + ", server='"
        + server
        + '\''
        + '}';
  }
}
