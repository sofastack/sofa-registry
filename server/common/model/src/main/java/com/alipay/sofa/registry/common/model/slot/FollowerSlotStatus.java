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
public class FollowerSlotStatus extends BaseSlotStatus implements Serializable {

  private final long lastSyncTaskStartTime;

  private final long lastLeaderSyncTime;

  /**
   * Constructor.
   *
   * @param slotId the slot id
   * @param slotLeaderEpoch the slot leader epoch
   * @param server the server
   * @param lastSyncTaskStartTime the last sync task start time
   * @param lastLeaderSyncTime the last leader sync time
   */
  public FollowerSlotStatus(
      int slotId,
      long slotLeaderEpoch,
      String server,
      long lastSyncTaskStartTime,
      long lastLeaderSyncTime) {
    super(slotId, slotLeaderEpoch, Slot.Role.Follower, server);
    this.lastSyncTaskStartTime = lastSyncTaskStartTime;
    this.lastLeaderSyncTime = lastLeaderSyncTime;
  }

  /**
   * Gets get last sync task start time.
   *
   * @return the get last sync task start time
   */
  public long getLastSyncTaskStartTime() {
    return lastSyncTaskStartTime;
  }

  /**
   * Gets get last follower sync time.
   *
   * @return the get last follower sync time
   */
  public long getLastLeaderSyncTime() {
    return lastLeaderSyncTime;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "FollowerSlotStatus{"
        + "lastSyncTaskStartTime="
        + lastSyncTaskStartTime
        + ", lastLeaderSyncTime="
        + lastLeaderSyncTime
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
