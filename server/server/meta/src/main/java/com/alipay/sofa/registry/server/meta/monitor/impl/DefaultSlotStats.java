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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Jan 28, 2021
 */
public class DefaultSlotStats implements SlotStats {

  private final Slot slot;
  private final long maxSyncGap;

  private volatile BaseSlotStatus.LeaderStatus leaderStatus = BaseSlotStatus.LeaderStatus.INIT;

  /**
   * offsets: follower replicate offset against the leader. Current implementation is as simple as
   * no incremental data replication Under this circumstances, offset stands for the last successful
   * sync task execute time
   */
  private final Map<String, Long> followerLastSyncTimes = Maps.newConcurrentMap();

  /**
   * Constructor.
   *
   * @param slot the slot
   * @param maxSyncGap maxSyncGap
   */
  public DefaultSlotStats(Slot slot, long maxSyncGap) {
    this.slot = slot;
    this.maxSyncGap = maxSyncGap;
  }

  /**
   * Gets get slot.
   *
   * @return the get slot
   */
  @Override
  public Slot getSlot() {
    return slot;
  }

  /**
   * Is leader stable boolean.
   *
   * @return the boolean
   */
  @Override
  public boolean isLeaderStable() {
    return leaderStatus.isHealthy();
  }

  /**
   * Is followers stable boolean.
   *
   * @return the boolean
   */
  @Override
  public boolean isFollowersStable() {
    for (String dataServer : slot.getFollowers()) {
      if (!isFollowerStable(dataServer)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Is follower stable boolean.
   *
   * @param dataServer the data server
   *     <p>Calculate the replicate gap between leader and followers, check if it's legal Current
   *     implementation is as simple as no incremental data replication Under this circumstances,
   *     offset stands for the last successful sync task execute time
   * @return the boolean
   */
  @Override
  public boolean isFollowerStable(String dataServer) {
    if (StringUtils.isEmpty(dataServer)) {
      return false;
    }
    Long offset = followerLastSyncTimes.get(dataServer);
    return offset != null && System.currentTimeMillis() - offset < maxSyncGap;
  }

  /**
   * Update leader state.
   *
   * @param leaderSlotStatus the leader slot status
   */
  @Override
  public void updateLeaderState(LeaderSlotStatus leaderSlotStatus) {
    this.leaderStatus = leaderSlotStatus.getLeaderStatus();
  }

  /**
   * Update follower state.
   *
   * @param followerSlotStatus the follower slot status
   */
  @Override
  public void updateFollowerState(FollowerSlotStatus followerSlotStatus) {
    followerLastSyncTimes.put(
        followerSlotStatus.getServer(), followerSlotStatus.getLastLeaderSyncTime());
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "DefaultSlotStats{"
        + "slot="
        + slot
        + ", leaderStatus="
        + leaderStatus
        + ", followerOffsets="
        + followerLastSyncTimes
        + '}';
  }

  /**
   * Equals boolean.
   *
   * @param o the o
   * @return the boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultSlotStats that = (DefaultSlotStats) o;
    return Objects.equals(slot, that.slot)
        && leaderStatus == that.leaderStatus
        && Objects.equals(followerLastSyncTimes, that.followerLastSyncTimes);
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(slot, leaderStatus, followerLastSyncTimes);
  }
}
